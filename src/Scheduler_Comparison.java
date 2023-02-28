import FCFS.FCFS_Scheduler;
import GWO.GWO_Scheduler;

import java.util.concurrent.TimeUnit;

public class Scheduler_Comparison {

  public static void main(String[] args) throws InterruptedException{
    GWO_Scheduler.main(args);
    System.out.println("===========================================");
    TimeUnit.SECONDS.sleep(1);
    FCFS_Scheduler.main(args);
  }

}