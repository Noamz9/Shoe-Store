package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Request;

public abstract class RequestImpl<Receipt> implements Request<Receipt> {
	protected String shoeType;

	public RequestImpl(String shoeType) {
		this.shoeType = shoeType;
	}
	
	public String getShoeType(){
		return shoeType;
	}

}
