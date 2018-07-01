package main.java.bgu.spl.mics.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import main.java.bgu.spl.mics.Broadcast;
import main.java.bgu.spl.mics.Message;
import main.java.bgu.spl.mics.MessageBus;
import main.java.bgu.spl.mics.Request;
import main.java.bgu.spl.mics.RequestCompleted;
import main.java.bgu.spl.mics.MicroService;

/**
 * this is an Implementation of the given MessageBus
 * implemented as singleton
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> queueByName;
	private ConcurrentHashMap<Class<? extends Request>, myList<MicroService>> queueByRequest;
	private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> queueByBroadcast;
	private ConcurrentHashMap<Request<?>, MicroService> requesters;

	// implements as Singleton
	private static class SingeltonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return SingeltonHolder.instance;
	}

	public MessageBusImpl() {
		this.queueByName = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();		             //saves MicroServices and their messages queues
		this.queueByRequest = new ConcurrentHashMap<Class<? extends Request>, myList<MicroService>>();          //saves a Type of request and the MicroServices waiting for this type
		this.queueByBroadcast = new ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>>();  //saves a Type of broadcast and the MicroServices waiting for this type
		this.requesters = new ConcurrentHashMap<Request<?>, MicroService>();											//saves for a specific request the MicroServices waiting for its result
	}
	
	/*
	 * Subscribes the MicroService to the specific request
	 * @see bgu.spl.mics.MessageBus#subscribeRequest(java.lang.Class, bgu.spl.mics.MicroService)
	 */
	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		queueByRequest.putIfAbsent(type, new myList<MicroService>());
		queueByRequest.get(type).add(m);
	}
	
	/*
	 * Subscribes the MicroService to the specific broadcast
	 * @see bgu.spl.mics.MessageBus#subscribeBroadcast(java.lang.Class, bgu.spl.mics.MicroService)
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		queueByBroadcast.putIfAbsent(type, new LinkedBlockingQueue<MicroService>());
		queueByBroadcast.get(type).add(m);
	}
	
	/*
	 * makes a request completed message and sends it to the waiting MicroService
	 * @see bgu.spl.mics.MessageBus#complete(bgu.spl.mics.Request, java.lang.Object)
	 */
	@Override
	public <T> void complete(Request<T> r, T result) {
		MicroService m = requesters.get(r);
		if (queueByName.containsKey(m)) {
			LinkedBlockingQueue<Message> q = queueByName.get(m);
			q.add(new RequestCompleted<T>(r, result));
		}
		requesters.remove(r);
	}
	
	/*
	 * Sends Broadcast b to all its Subscribers
	 * @see bgu.spl.mics.MessageBus#sendBroadcast(bgu.spl.mics.Broadcast)
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		if (queueByBroadcast.containsKey(b.getClass())){
			Iterator<MicroService> it = queueByBroadcast.get(b.getClass()).iterator();
			while (it.hasNext()) {
				MicroService temp = it.next();
				if (queueByName.containsKey(temp))
					queueByName.get(temp).add(b);
			}
		}
	}
	
	/*
	 * Sends the request to one of the awaiting MicroServices in a RoundRobin manner
	 * @see bgu.spl.mics.MessageBus#sendRequest(bgu.spl.mics.Request, bgu.spl.mics.MicroService)
	 */
	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		if (!(queueByRequest.containsKey(r.getClass())))
			return false;
		requesters.putIfAbsent(r, requester);										//here we use my list that supports 
		Iterator<MicroService> it = queueByRequest.get(r.getClass()).iterator();	//the round-robin manner for sending requests 
		MicroService mService = null;
		if (it.hasNext()) {
			mService = it.next();
			queueByName.get(mService).add(r);
			return true;
		}
		return false;
	}
	
	/*
	 * registers the MicroService to receive messages
	 * @see bgu.spl.mics.MessageBus#register(bgu.spl.mics.MicroService)
	 */
	@Override
	public void register(MicroService m) {
		queueByName.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}
	
	/*
	 * Removes the MicroService from all its appearances
	 * @see bgu.spl.mics.MessageBus#unregister(bgu.spl.mics.MicroService)
	 */
	@Override
	public synchronized void unregister(MicroService m) {
		queueByBroadcast.forEach((k, v) -> v.remove(m));
		queueByRequest.forEach((k, v) -> v.remove(m));
		if (queueByName.containsKey(m)) { 						
			queueByName.remove(m);
		}
	}
	
	/*
	 * Give the registered MicroService his next message (if available) 
	 * @see bgu.spl.mics.MessageBus#awaitMessage(bgu.spl.mics.MicroService)
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		try {
			return queueByName.get(m).take();
		} catch (IllegalStateException e) {
		}
		return null;
	}

}
