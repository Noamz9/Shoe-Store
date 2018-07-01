package main.java.bgu.spl.app;

public class ManufacturingOrderRequest<Receipt> extends RequestImpl<Receipt> {
	private int amount;
	private int tick;
	
	public ManufacturingOrderRequest(String shoeType,int amount,int tick) {
		super(shoeType);
		this.amount=amount;
		this.tick=tick;
	}
	
	public int getAmount(){
		return amount;
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}
}
