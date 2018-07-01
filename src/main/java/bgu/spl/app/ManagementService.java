package main.java.bgu.spl.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;


public class ManagementService extends MicroService {
	final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
	private CountDownLatch m_latchObject;
	int currTick;
	private ConcurrentHashMap<Integer, LinkedBlockingQueue<DiscountSchedule>> DiscountSchedule;
	private ConcurrentHashMap<String, ArrayList<ManufacturingOrderRequest<Receipt>>> orderedShoes;
	private ConcurrentHashMap<ManufacturingOrderRequest<Receipt>, LinkedBlockingQueue<RestockRequest<Boolean>>> awaitingFcatory;

	public ManagementService(CountDownLatch latchObject,List<DiscountSchedule> list) {
		super("manager");
		this.m_latchObject=latchObject;
		DiscountSchedule = new ConcurrentHashMap<Integer,LinkedBlockingQueue< DiscountSchedule>>();
		orderedShoes = new ConcurrentHashMap<String, ArrayList<ManufacturingOrderRequest<Receipt>>>();
		Iterator<DiscountSchedule> it = list.iterator();
		while (it.hasNext()) {
			DiscountSchedule temp = it.next();
			DiscountSchedule.putIfAbsent(temp.getTick(),new LinkedBlockingQueue<DiscountSchedule>());
			DiscountSchedule.get(new Integer(temp.getTick())).add(temp);
		}
		awaitingFcatory = new ConcurrentHashMap<ManufacturingOrderRequest<Receipt>, LinkedBlockingQueue<RestockRequest<Boolean>>>();
	}

	@Override
	protected void initialize() {
		LOGGER.info(getName()+" Clocked-In");
		/*subscribe to tick broadcast
		 * when received a tick broadcast the manager will broadcast the appropriate discount
		 */
		this.subscribeBroadcast(TickBroadcast.class, tick -> {
			currTick = tick.getTick();
			if(DiscountSchedule.containsKey(currTick)){
			while (DiscountSchedule.get(currTick).size()>0) {
				DiscountSchedule temp = DiscountSchedule.get(currTick).poll();
				Store.getInstance().addDiscount(temp.getShoe(), temp.getAmount());
				sendBroadcast(new NewDiscountBroadcast(temp.getShoe()));
				LOGGER.info("Tick "+currTick +":" + " The manager Declared a new discount on "+temp.getShoe());
			}
		}
		});
		
		/*subscribe to restock request
		 *send new restock request to the factory 
		 */
		this.subscribeRequest(RestockRequest.class, req -> {
			String shoe = req.getShoeType();
			if (orderedShoes.containsKey(shoe)) {
				ManufacturingOrderRequest<Receipt> order = orderedShoes.get(shoe).get(0);
				int qunatity = order.getAmount();
				int waiting = awaitingFcatory.get(order).size();
				if (qunatity > waiting)
					awaitingFcatory.get(order).add(req);
				else 
					makeOrder(shoe,req);
			}
			else 
				makeOrder(shoe,req);
			
		});
		
		 
		 //terminate when receives the broadcast
		this.subscribeBroadcast(terminateBroadcast.class, res->{
			this.terminate();
			LOGGER.info("Tick "+currTick +":" + " Manager terminated ");
		
		});

		m_latchObject.countDown();
	}
	
	public void makeOrder(String shoe,RestockRequest<Boolean> r){
		ManufacturingOrderRequest<Receipt> newOrder = new ManufacturingOrderRequest<Receipt>(shoe,
				(currTick % 5) + 1,currTick);
		orderedShoes.putIfAbsent(shoe, new ArrayList<ManufacturingOrderRequest<Receipt>>());
		orderedShoes.get(shoe).add(0, newOrder);;
		LOGGER.info("Tick "+currTick +":" + " The manager made an order for "+shoe);
		awaitingFcatory.put(newOrder, new LinkedBlockingQueue<RestockRequest<Boolean>>());
		awaitingFcatory.get(newOrder).add(r);
		sendRequest(newOrder, ans -> {
			LinkedBlockingQueue<RestockRequest<Boolean>> q = awaitingFcatory.get(newOrder);
			if (ans == null) {
				while (!q.isEmpty())
					complete(q.poll(), false);
			} 
			else {
				int await =q.size();
				int quantity=newOrder.getAmount();
				Store.getInstance().file(ans);
				Store.getInstance().add(shoe, quantity-await);
				LOGGER.info("Tick "+currTick +":" + " Manager recieved new stock of  "+(quantity) +" "+shoe);
				while (!q.isEmpty())
					complete(q.poll(), true);
			}
			orderedShoes.get(shoe).remove(newOrder);
			awaitingFcatory.remove(newOrder);
			if(orderedShoes.get(shoe).isEmpty())
				orderedShoes.remove(shoe);
		});	
	}
}
