package main.java.bgu.spl.app;

public class DiscountSchedule extends Schedule{

	private int amount;

	public DiscountSchedule (String shoeType, int tick, int amount) {
		super(shoeType,tick);
		this.amount=amount;
	}

	public int getAmount(){
		return amount;
	}	
	
	public void setAmount(){
		amount--;
	}
}


