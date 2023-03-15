package main;

import java.util.concurrent.TimeUnit;

import CO.CO_Scheduler;
import FCFS.FCFS_Scheduler;
import GWO.GWO_Scheduler;

public class Scheduler_Comparison {

	public static void main(String[] args) throws InterruptedException {
		GWO_Scheduler.main(args);
		System.out.println("===========================================");
		TimeUnit.SECONDS.sleep(1);
		FCFS_Scheduler.main(args);
		System.out.println("===========================================");
		TimeUnit.SECONDS.sleep(1);
		CO_Scheduler.main(args);
		System.out.println("===========================================");
	}

}