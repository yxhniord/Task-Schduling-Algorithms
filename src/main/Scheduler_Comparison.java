package main;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import CO.CO_Scheduler;
import FCFS.FCFS_Scheduler;
import GWO.GWO_Scheduler;
import HOA.HOA_Scheduler;
import mGWO_WOA.mGWO_WOA_Scheduler;
import newHOA.NewHOA_Scheduler;

public class Scheduler_Comparison {

	public static void main(String[] args) throws InterruptedException, IOException {
//		GWO_Scheduler.main(args);
//		System.out.println("===========================================");
//		TimeUnit.SECONDS.sleep(1);
//		FCFS_Scheduler.main(args);
//		System.out.println("===========================================");
//		TimeUnit.SECONDS.sleep(1);
//		CO_Scheduler.main(args);
//		System.out.println("===========================================");
//		TimeUnit.SECONDS.sleep(1);
		NewHOA_Scheduler.main(args);
//		HOA_Scheduler.main(args);
//		System.out.println("===========================================");
//		mGWO_WOA_Scheduler.main(args);
	}

}