package main.java.bgu.spl.app;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * implemented as Singleton 
 * the store handles to stock of shoes wile containing for each shoe its shoe storage info
 *
 */
public class Store {

	private ConcurrentHashMap<String, ShoeStorageInfo> store;
	LinkedBlockingQueue<Receipt> receipts;

	private static class SingeltonHolder {
		private static Store instance = new Store();
	}

	public static Store getInstance() {
		return SingeltonHolder.instance;
	}

	public Store() {
		this.store = new ConcurrentHashMap<String, ShoeStorageInfo>();
		this.receipts = new LinkedBlockingQueue<Receipt>();
	}

	/*
	 * Receives the initial amount to stock 
	 * saves it in an Hash map for each shoe name her shoe storage info
	 */
	public void load(ShoeStorageInfo[] storage) {
		for (int i = 0; i < storage.length; i++) {
			store.putIfAbsent(storage[i].getShoeType(), storage[i]);
		}
	}
	
	/*
	 * for each purchase possibility the function will return the appropriate enum
	 */
	public BuyResult take(String shoeType, boolean onlyDiscount) {
		if (store.containsKey(shoeType) && store.get(shoeType).getAmount() > 0) {
			ShoeStorageInfo shoe = store.get(shoeType);
			boolean isDiscount = shoe.hasDiscount();
			if (isDiscount) {
				store.get(shoeType).sell();
				return BuyResult.DISCOUNTED_PRICE;
			} else {
				if (!onlyDiscount) {
					store.get(shoeType).sell();
					return BuyResult.REGULAR_PRICE;
				} else
					return BuyResult.NOT_ON_DISCOUNT;
			}
		}
		return BuyResult.NOT_IN_STOCK;
	}
	
	/*
	 * adds the given amount to the given shoe storage 
	 * if the shoe does not exist in the storage it will create it with the given amount
	 */
	public void add(String shoeType, int amount) {
		if (store.containsKey(shoeType))
			store.get(shoeType).setAmount(amount);
		else{
			store.put(shoeType ,new ShoeStorageInfo(shoeType,amount,0));
		}
	}
	
	/*
	 * adds the given discount to the given shoe storage 
	 * if the shoe does not exist in the storage it will create it with 0 amount
	 */
	public void addDiscount(String shoeType, int amount) {
		if (store.containsKey(shoeType)){
				store.get(shoeType).setDiscount(amount);
		}
		else
			store.put(shoeType ,new ShoeStorageInfo(shoeType,0,0));
	}

	public void file(Receipt receipt) {
		receipts.add(receipt);
	}

	/*
	 * prints the store info
	 */
	public void print() {
		Set<String> shoes = store.keySet();
		Iterator<String> it1 = shoes.iterator();
		while (it1.hasNext())
			store.get(it1.next()).print();
		System.out.println();
		System.out.println("Number of receipts : "+receipts.size());
		System.out.println();
		Iterator<Receipt> it2 = receipts.iterator();
		int i=1;
		while (it2.hasNext()){
			System.out.println("Receipt #"+i);
			it2.next().print();
			i++;
		}

	}
	
}