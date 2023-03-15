import FCFS.FCFS_Scheduler;
import GWO.GWO_Scheduler;
import mGWO_WOA.mGWO_WOA_Scheduler;

import java.util.concurrent.TimeUnit;

public class Scheduler_Comparison {

  public static void main(String[] args) throws InterruptedException{
    FCFS_Scheduler.main(args);
    System.out.println("===========================================");
    TimeUnit.SECONDS.sleep(1);
    mGWO_WOA_Scheduler.main(args);
  }

}