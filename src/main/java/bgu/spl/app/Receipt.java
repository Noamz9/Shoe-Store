package main.java.bgu.spl.app;

public class Receipt {
	
	private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	public Receipt(String seller,String customer,String shoeType,
			boolean discount,int issuedTick,int requestTick,int amountSold){
		this.seller=seller;
		this.customer=customer;
		this.shoeType=shoeType;
		this.discount=discount;
		this.issuedTick=issuedTick;
		this.requestTick=requestTick;
		this.amountSold=amountSold;
	}
	
	public void print(){
		System.out.println("Seller name: "+seller);
		System.out.println("customer name: "+customer);
		System.out.println("ShoeType: "+shoeType);
		System.out.println("Discount: "+discount);
		System.out.println("Issued tick: "+issuedTick);
		System.out.println("Request tick: "+requestTick);
		System.out.println("Amount Sold: "+amountSold);
		System.out.println();
		System.out.println();
	}
	

}
