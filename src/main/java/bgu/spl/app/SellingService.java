package main.java.bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

public class SellingService extends MicroService {
	final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName());
	private CountDownLatch m_latchObject;
	private int currentTick;
	private final int sAmount = 1;

	public SellingService(String name  ,CountDownLatch latchObject) {//constructor
		super(name);
		this.m_latchObject=latchObject;
	}

	@Override
	protected void initialize() {
		LOGGER.info(getName()+" Clocked-In");
		
		this.subscribeBroadcast(TickBroadcast.class, tick -> {
			currentTick = tick.getTick();
		});

		this.subscribeRequest(PurchaseOrderRequest.class, req -> {
			
			BuyResult result = Store.getInstance().take(req.getShoeType(), req.isOnlyDiscount());
			switch (result) {
				case NOT_IN_STOCK:
					if (req.isOnlyDiscount() == false) {		 //send restockRequest only if the costumer doesn't want discount
						RestockRequest<Boolean> sReq = new RestockRequest<Boolean>(req.shoeType);
						sendRequest(sReq, bool -> {
							if (bool.booleanValue() == false){
								complete(req, null);			//if the factory can't make the shoes returns "null"
								LOGGER.info("Tick "+currentTick +":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" but it's NOT IN STOCK and not available to restock");	
							}
							
							else {
								Receipt receipt = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(), false,
									currentTick, req.getTick(), sAmount);
								complete(req, receipt);			//if the factory can make the shoes returns receipt
								Store.getInstance().file(receipt);
								LOGGER.info("Tick "+currentTick +":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" and we were able to get Stock");			
							}
						});
					}
					else										//if the client want discount-the manager doesn't send restockRequest
						complete(req, null);
					LOGGER.info("Tick "+currentTick+":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" but it's NOT IN STOCK");			
					break;
					
				case NOT_ON_DISCOUNT:							//if the client wants to buy shoes on discount but the store doesn't has one -return "null"
					complete(req, null);
					LOGGER.info("Tick "+currentTick+":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" but it's NOT ON DISCOUNT");					
					break;
					
				case REGULAR_PRICE:								//if the client wants to buy shoes and the store has it on regular price- return receipt
					Receipt receipt1 = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(),false,currentTick,
							req.getTick(), sAmount);
					complete(req, receipt1);
					Store.getInstance().file(receipt1);
					LOGGER.info("Tick "+currentTick +":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" and he bought it in REGULAR PRICE");			
					break;
					
				case DISCOUNTED_PRICE:							///if the client wants to buy shoes on discount and the store has it on discount price-return receipt 
					Receipt receipt2 = new Receipt(this.getName(), req.getCustomer(), req.getShoeType(),true, currentTick,
							req.getTick(), sAmount);
					complete(req, receipt2);
					Store.getInstance().file(receipt2);
					LOGGER.info("Tick "+currentTick +":" + " The client " +req.getCustomer()+" wanted "+req.getShoeType()+" and he bought it on DISCOUNTED PRICE");
					break;
			}
		});
		
		this.subscribeBroadcast(terminateBroadcast.class, res->{	//terminate 
			LOGGER.info("Tick "+currentTick +":" + " Selling service "+getName()+" terminated");		
			this.terminate();
		});
		
		m_latchObject.countDown();
		
	}
}
