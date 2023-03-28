package utils;

public class Constants {
  //Datacenter Parameters
  public static String ARCHITECTURE = "x86";
  public static String OS = "Linux";
  public static double TIME_ZONE = 10.0;
  public static double COST_PROCESSING = 3.0;
  public static double COST_MEMORY = 0.05;
  public static double COST_STORAGE = 0.001;
  public static double COST_BANDWIDTH = 0.1;

  //Host Parameters
  public static int STORAGE = 1000000;
  public static int HOST_RAM = 25600;
  public static int HOST_BANDWIDTH = 50000;
  public static int HOST_MIPS = 1000;
  public static int HOST_ID = 0;

  //VM Parameters
  public static int NO_OF_VMS = 20;
  public static long VM_IMAGE_SIZE = 10000;
  public static int VM_RAM = 512;
  public static int VM_MIPS = 500;
  public static long VM_BANDWIDTH = 1000;
  public static int VM_PES = 1;
  public static String VMM_NAME = "Xen";

  //Cloudlet Parameters
  //LEGTH 100-1000 random
  public static int LEGNTH = 500;
  public static long FILE_SIZE = 300;
  public static long OUTPUT_SIZE = 300;
  public static int TASK_PES = 1;

  //Task Scheduler Parameters
  public static int POPULATION = 30;
  public static int MAX_ITER = 100;
}
