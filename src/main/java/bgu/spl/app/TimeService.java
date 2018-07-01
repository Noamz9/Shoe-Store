package main.java.bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

public class TimeService extends MicroService {
	private CountDownLatch m_latchObject;
	private volatile int time;
	private int speed;
	private int duration;
	private Timer scheduler;
	final Logger LOGGER = Logger.getLogger(ShoeStoreRunner.class.getName()); 

	public TimeService( CountDownLatch latchObject,int speed, int duration) {
		super("timer");
		this.m_latchObject=latchObject;
		this.speed = speed;
		this.duration = duration;
		this.time = 1;
		this.scheduler = new Timer();
	}
	/* The timer service awaits for all the other services to finish initialize
	 * each unit of time the timer will send a TickBroadcast using a Timer and a TimerTask 
	 * when the time will reach the duration it will send a terminateBroadcast 
	 * @see bgu.spl.mics.MicroService#initialize()
	 */
	@Override
	protected void initialize() {
		LOGGER.info(getName()+" Clocked-In");

		try {
			m_latchObject.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			LOGGER.warning(getName()+" interrupted");
		}
	
		scheduler.schedule(new TimerTask() {
				
				@Override
				public void run() {
					if(time <= duration){
					sendBroadcast(new TickBroadcast(time));
					time++;
					}
					else
					{
						sendBroadcast(new terminateBroadcast());
						terminate();
						scheduler.cancel();						
						LOGGER.info("Tick "+(time-1)+":" + " TimeService terminated");
					}
				}
			}, speed,speed);
		
		this.subscribeBroadcast(terminateBroadcast.class, brod -> {
			terminate();
		});
	}

}
