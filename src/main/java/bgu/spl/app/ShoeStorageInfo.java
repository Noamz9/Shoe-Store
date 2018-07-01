package main.java.bgu.spl.app;

public class ShoeStorageInfo {

	private String shoeType;
	private int amountOnStorage;
	private int discountedAmount;

	public ShoeStorageInfo(String shoeType, int amountOnStorage, int discountedAmount) {
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = discountedAmount;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmount() {
		return amountOnStorage;
	}

	public int getDiscount() {
		return discountedAmount;
	}

	public boolean hasDiscount() {
		return discountedAmount>0;
	}

	public void setAmount(int amount) {
		this.amountOnStorage += amount;
	}

	public void setDiscount(int amount) {
		this.discountedAmount += amount;
		if(discountedAmount>amountOnStorage)
			discountedAmount=amountOnStorage;
	}
	
	public void sell(){
		amountOnStorage--;
		if(discountedAmount>0)
			discountedAmount--;
	}
	
	public void print(){
		System.out.println("Shoe type: "+shoeType);
		System.out.println("Amount on storage: "+amountOnStorage);
		System.out.println("Discounted amount: "+discountedAmount);
		System.out.println();
		System.out.println();
	}
}
