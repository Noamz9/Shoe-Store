package main.java.bgu.spl.app;

import com.google.gson.annotations.SerializedName;

public class initialData {
	@SerializedName("initialStorage")
		public inputStorageInfo [] initialStorage;
	
	public class inputStorageInfo{
		@SerializedName("shoeType")
		public String shoeType;
		
		@SerializedName("amount")
		public int amount;
	}
	
	@SerializedName("services")
		public inputServices services;
	
	public class inputServices{
		
		@SerializedName("time")
		public time initTimer;
		
		@SerializedName("manager")
		public Manager manager;
		
		@SerializedName("factories")
		public int factories;
		
		@SerializedName("sellers")
		public int sellers;
		
		@SerializedName("customers")
		public customers [] customersInfo;
	}
	
	public class time{
		public int speed;
		public int duration;
	}
	
	public class Manager{
		public DiscountSchedule [] discountSchedule;
	}
	
	public class customers{
		
		@SerializedName("name")
		public String name;
		
		@SerializedName("wishList")
		public String [] wishList; 
		
		@SerializedName("purchaseSchedule")
		public PurchaseSchedule [] purchaseSchedule;
	}

}
