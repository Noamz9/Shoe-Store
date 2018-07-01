package main.java.bgu.spl.app;
import main.java.bgu.spl.mics.Request;

public class RestockRequest<Boolean> implements Request<Boolean> {
	
	private String shoeType;
	
	public RestockRequest(String shoeType) {
		this.shoeType=shoeType;
	}

	public String getShoeType() {
		return shoeType;
	}
	
}
