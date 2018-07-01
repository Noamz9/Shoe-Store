package main.java.bgu.spl.app;

import java.io.BufferedReader;
import java.util.logging.Logger;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;


public class ShoeStoreRunner {
	public static void main(String[] args) {
		
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(args[0]));
		} catch (FileNotFoundException e) {
			LOGGER.warning("There is no file in the given location");
			e.printStackTrace();
		}
		Gson gson = new Gson();
		initialData readJson = gson.fromJson(br, initialData.class);
		
		//initialing the shoe storage from json 
		ShoeStorageInfo[] storage = new ShoeStorageInfo[readJson.initialStorage.length];
		for (int i = 0; i < storage.length; i++) {
			ShoeStorageInfo shoe = new ShoeStorageInfo(
					readJson.initialStorage[i].shoeType,
					readJson.initialStorage[i].amount, 0);
			storage[i] = shoe;
		}
		Store store = Store.getInstance(); // building and initializing the store
		store.load(storage);
		LOGGER.info("The store is open now :)");
		
		// creating microServices
		int numofServices =2;
		int numofFactories = readJson.services.factories;	
		int numofSellers = readJson.services.sellers;
		int numofClients = readJson.services.customersInfo.length;
		numofServices+=numofFactories+numofSellers+numofClients;
		CountDownLatch latchObject = new CountDownLatch (numofServices-1);
		
		// timeService
		TimeService timer = new TimeService(latchObject,readJson.services.initTimer.speed,  
				readJson.services.initTimer.duration);
		
		// Manager
		DiscountSchedule[] temp = readJson.services.manager.discountSchedule;
		ArrayList<DiscountSchedule> list = new ArrayList<DiscountSchedule>();
		for(int i=0;i<temp.length;i++){
			list.add(temp[i]);
		}
		ManagementService manager = new ManagementService(latchObject,list);
		
		// Factories 
		ShoeFactoryService[] factories = new ShoeFactoryService[numofFactories];
		for (int i = 1; i <= numofFactories; i++) {
			factories[i - 1] = new ShoeFactoryService("factory " + i,latchObject);
		}
		
		// Sellers
		SellingService[] sellers = new SellingService[numofSellers];
		for (int i = 1; i <= numofSellers; i++) {
			sellers[i - 1] = new SellingService("seller " + i,latchObject);
		}
		
		// Clients
		WebsiteClientService[] clients = new WebsiteClientService[numofClients];
		
		//initializing for all clients there wishlist and purchasechdule from json
		for (int i = 0; i < clients.length; i++){
			
			
			Set<String> wishList2 = new HashSet<String>();
			String [] t =readJson.services.customersInfo[i].wishList;
			for(int j=0;j<t.length;j++)
				wishList2.add(t[j]);	
			
			ArrayList <PurchaseSchedule> purchaseSchedule= new ArrayList<PurchaseSchedule>();
			PurchaseSchedule [] t2=readJson.services.customersInfo[i].purchaseSchedule;
			for(int k=0;k<t2.length;k++ )
				purchaseSchedule.add(t2[k]);
			
			clients[i] = new WebsiteClientService(		//make WebsiteClientService for all the clients
					readJson.services.customersInfo[i].name,latchObject,purchaseSchedule,wishList2);
			}
			
		// getting started 
		ArrayList <Thread> threads=new ArrayList<Thread>();
		threads.add(new Thread(timer));
		threads.add(new Thread(manager));
		for(int i=0;i<numofFactories;i++){
			threads.add(new Thread(factories[i]));	
		}
		for(int i=0;i<numofSellers;i++){
			threads.add(new Thread(sellers[i]));
		}
		for(int i=0;i<clients.length;i++){
			threads.add(new Thread(clients[i]));
		}
		
		//run Threads
		Iterator<Thread> it =  threads.iterator();
		while(it.hasNext()){
			Thread t = it.next();
			t.start();
			}
		
		it =threads.iterator();
		while(it.hasNext()){
			Thread t = it.next();
			try {
				t.join();
			} catch (InterruptedException e) {
				LOGGER.warning("Faild to wait for everyone to finish");
			}
			}
		System.out.println();
		System.out.println();
		store.print();
	}
}
