package main.java.bgu.spl.app;

public class PurchaseOrderRequest<Receipt> extends RequestImpl<Receipt> {
	private boolean onlyDiscount;
	private String customer;
	private int tick;
	
	public PurchaseOrderRequest(String shoeType,String customer,boolean onlyDiscount,int tick) {
		super(shoeType);
		this.onlyDiscount=onlyDiscount;
		this.customer=customer;
		this.tick=tick;
	}

	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}

	public void setOnlyDiscount(boolean onlyDiscount) {
		this.onlyDiscount = onlyDiscount;
	}
	
	public String getCustomer(){
		return customer;
	}
	
	public int getTick(){
		return tick;
	}
}
