package main.java.bgu.spl.app;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

public class WebsiteClientService extends MicroService {
	final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName());
	private CountDownLatch m_latchObject;
	private ConcurrentHashMap<Integer, LinkedBlockingQueue<String>> pSchedule;
	private Set<String> wishList;
	private int currTick;

	public WebsiteClientService(String name, CountDownLatch latchObject, List<PurchaseSchedule> purchaseSchedule,
			Set<String> wishList) {
		super(name);
		this.m_latchObject = latchObject;
		this.pSchedule = new ConcurrentHashMap<Integer, LinkedBlockingQueue<String>>();
		this.wishList = wishList;
		Iterator<PurchaseSchedule> it = purchaseSchedule.iterator();
		while (it.hasNext()) {
			PurchaseSchedule temp = it.next();
			pSchedule.putIfAbsent(new Integer(temp.getTick()), new LinkedBlockingQueue<String>());
			pSchedule.get(new Integer(temp.getTick())).add(temp.getShoe());
		}
	}

	@Override
	protected void initialize() {
		LOGGER.info(getName()+" Logged-In");
		
		/*subscribe to tick broadcast 
		 * check all the ticks and if there is a purchase schedule- send purchase request
		 * if the wishlist and the purchase schedule are empties- terminate 
		 */
		this.subscribeBroadcast(TickBroadcast.class, tick -> {
			currTick = tick.getTick();
			if (pSchedule.containsKey(currTick)) {
				while (!pSchedule.get(currTick).isEmpty()) {
					String shoeType = pSchedule.get(currTick).poll();
					PurchaseOrderRequest<Receipt> req = new PurchaseOrderRequest<Receipt>(shoeType, getName(), false,
							currTick);
					LOGGER.info("Tick " + currTick +":" + " Website client " + getName() + " wants to buy " + shoeType);
					sendRequest(req, ans -> {
						if (ans != null) {
							if (pSchedule.containsKey(currTick) && pSchedule.get(currTick).isEmpty())
								pSchedule.remove(currTick);
							if (pSchedule.size() == 0 && wishList.size() == 0) {
								terminate();
								LOGGER.info("Tick " + currTick +":" + " Website Client " + getName() + " is terminated");
							}
						}
					});
					pSchedule.get(currTick).remove(0);
				}
			}
		});
		
		/*subscribe to NewDiscountBroadcast
		 *if there is new discount and client has it in his wishlist- send purchaseRequest
		 */
		this.subscribeBroadcast(NewDiscountBroadcast.class, brod -> {
			if (wishList.contains(brod.getShoe())) {
				PurchaseOrderRequest<Receipt> req = new PurchaseOrderRequest<Receipt>(brod.getShoe(), getName(), true,
						currTick);
				LOGGER.info("Tick " + currTick +":" + " Website client  " + getName() + " has "
				+ brod.getShoe() + " in his wishlist ");
				
		sendRequest(req, ans -> {
					if (ans != null)
						wishList.remove(brod.getShoe());
					if (pSchedule.size() == 0 && wishList.size() == 0) {
						terminate();
						LOGGER.info("Tick " + currTick +":" + " Website Client " + getName() + " is terminated");
					}
				});
			}
		});
		
		//Receives the broadcast and terminate
		this.subscribeBroadcast(terminateBroadcast.class, res -> {
			LOGGER.info("Tick " + currTick +":" + " Website Client " + getName() + " is terminated");
			this.terminate();
		});

		m_latchObject.countDown();

	}

}
