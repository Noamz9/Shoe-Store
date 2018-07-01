package main.java.bgu.spl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService {
	final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName());
	private CountDownLatch m_latchObject;
	private int currTick;
	private LinkedBlockingQueue<ManufacturingOrderRequest<Receipt>> ordersAwaiting;
	private ConcurrentHashMap<ManufacturingOrderRequest<Receipt>, Integer> shoesToMake;

	public ShoeFactoryService(String name, CountDownLatch latchObject) {
		super(name);
		this.m_latchObject = latchObject;
		this.shoesToMake = new ConcurrentHashMap<ManufacturingOrderRequest<Receipt>, Integer>();
		this.ordersAwaiting = new LinkedBlockingQueue<ManufacturingOrderRequest<Receipt>>();
	}

	@Override
	protected void initialize() {
		LOGGER.info(getName()+" Is Open");
		/*subscribe to TickBroadcast
		 *if there is a shoe to make-make one shoe per tick 
		 */
		subscribeBroadcast(TickBroadcast.class, tick -> {
			currTick = tick.getTick();
			if (ordersAwaiting.size() > 0) {
				ManufacturingOrderRequest<Receipt> order = ordersAwaiting.peek();
				int count = shoesToMake.get(order);
				if (count == 0) {
					 LOGGER.info("Tick " + currTick+": "  + getName()+" finished making " + order.getAmount() + " "
							+ order.getShoeType());
					complete(order, new Receipt(getName(), "store", order.getShoeType(), false, currTick,
							order.getTick(), order.getAmount()));
					shoesToMake.remove(order);
					ordersAwaiting.remove(order);
					if (!ordersAwaiting.isEmpty()) {
						int current = shoesToMake.get(ordersAwaiting.peek()).intValue();
						shoesToMake.replace(ordersAwaiting.peek(), current - 1);
					}
				}
				else {
					shoesToMake.replace(order, count - 1);
				}
			}
		});
		
		/*subscribe to ManufacturingOrderRequest
		 *when receives an order adds it to his Manufacturing Schedule 
		 */
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			LOGGER.info("Tick " + currTick +":" +" " +getName()+" recieved an order for " + req.getAmount() + " "
					+ req.getShoeType());
			ordersAwaiting.add(req);
			shoesToMake.put(req, req.getAmount());
		});
		
		//Receives the broadcast and terminate
		this.subscribeBroadcast(terminateBroadcast.class, res -> {
			while (!ordersAwaiting.isEmpty()) {
				complete(ordersAwaiting.poll(), null);
			}
			this.terminate();
			LOGGER.info("Tick " + currTick +":" + " The "+ getName()+" is closed ");
		
		});

		m_latchObject.countDown();

	}

}
