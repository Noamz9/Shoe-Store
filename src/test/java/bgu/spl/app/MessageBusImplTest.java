package test.java.bgu.spl.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.java.bgu.spl.app.PurchaseOrderRequest;
import main.java.bgu.spl.app.RestockRequest;
import main.java.bgu.spl.app.terminateBroadcast;
import main.java.bgu.spl.mics.RequestCompleted;
import main.java.bgu.spl.mics.impl.MessageBusImpl;
import main.java.bgu.spl.mics.MicroService;

public class MessageBusImplTest {
	private static MessageBusImpl bus;
	private MicroService m1;
	private MicroService m2;
	private MicroService m3;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bus = MessageBusImpl.getInstance();
	}

	@Before
	public void setUp() throws Exception {
		m1 = new testMicroService("m1");
		m2 = new testMicroService("m2");
		m3 = new testMicroService("m3");
		bus.register(m1);
		bus.register(m2);
		bus.register(m3);
	}

	@After
	public void tearDown() throws Exception {
		bus.unregister(m1);
		bus.unregister(m2);
		bus.unregister(m3);
	}

	@Test
	public void testGetInstance() {
		assertTrue(MessageBusImpl.getInstance() != null);
	}

	@Test
	public void testComplete() throws InterruptedException {
		RestockRequest<Boolean> r = new RestockRequest<Boolean>("brown-shoes");
		bus.subscribeRequest(RestockRequest.class, m3);                       //also checks subscribeRequest
		bus.sendRequest(r,m1);												  //also checks sendRequest
		bus.complete(r,false);
		assertTrue(bus.awaitMessage(m1).getClass()==RequestCompleted.class);  //also checks awaitMessage
	}

	@Test
	public void testSendBroadcast() throws InterruptedException {
		bus.subscribeBroadcast(terminateBroadcast.class,m1);
		bus.sendBroadcast(new terminateBroadcast());
		assertTrue(bus.awaitMessage(m1).getClass()==terminateBroadcast.class);
		
	}

	@Test
	public void testSendRequest() {
		assertTrue(bus.sendRequest(new PurchaseOrderRequest("moshe","brown-shoes",true,2),m2)==false);
	}


}
