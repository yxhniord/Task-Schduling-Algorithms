package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Commons {
  public static List<Cloudlet> cloudletList;

  public static List<Vm> vmList;

  public static int num_user;
  public static Calendar calendar;
  public static boolean trace_flag;

  public static void set_cloudsim_parameters(){
    num_user = 1;
    calendar = Calendar.getInstance();
    trace_flag = false;
  }

  public static List<Vm> createVM(int userId, int vms) {

    //Creates a container to store VMs. This list is passed to the broker later
    LinkedList<Vm> list = new LinkedList<Vm>();

    Random r = new Random(Constants.RANDOM_SEED);
    int mips = Constants.VM_MIPS;
    Vm[] vm = new Vm[vms];

    for (int i = 0; i < vms; i++) {
      mips = 500 + r.nextInt(500);
      vm[i] = new Vm(i, userId, mips, Constants.VM_PES, Constants.VM_RAM,
          Constants.VM_BANDWIDTH, Constants.VM_IMAGE_SIZE,
          Constants.VMM_NAME, new CloudletSchedulerTimeShared());
      //for creating a VM with a space shared scheduling policy for cloudlets:
//      vm[i] = new Vm(i, userId, mips, Constants.VM_PES, Constants.VM_RAM,
//          Constants.VM_BANDWIDTH, Constants.VM_IMAGE_SIZE,
//          Constants.VMM_NAME, new CloudletSchedulerSpaceShared());
      list.add(vm[i]);
    }

    vmList = list;

    return list;
  }

  public static List<Cloudlet> createCloudlet(int userId, int cloudlets){
    // Creates a container to store Cloudlets
    LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

    UtilizationModel utilizationModel = new UtilizationModelFull();
    int length = Constants.LEGNTH;
    Random r1 = new Random(Constants.RANDOM_SEED);
    Cloudlet[] cloudlet = new Cloudlet[cloudlets];

    for(int i=0;i<cloudlets;i++){
      length = 100 + r1.nextInt(900);
      cloudlet[i] = new Cloudlet(i, length, Constants.TASK_PES, Constants.FILE_SIZE, Constants.OUTPUT_SIZE, utilizationModel, utilizationModel, utilizationModel);
      // setting the owner of these Cloudlets
      cloudlet[i].setUserId(userId);
      list.add(cloudlet[i]);
    }

    cloudletList = list;

    return list;
  }

  public static Datacenter createDatacenter(String name, int elements){

    // Here are the steps needed to create a PowerDatacenter:
    // 1. We need to create a list to store one or more
    //    Machines
    List<Host> hostList = new ArrayList<Host>();

    // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
    //    create a list to store these PEs before creating
    //    a Machine.
    List<Pe> peList1 = new ArrayList<Pe>();

    // 3. Create PEs and add these into the list.
    //for a quad-core machine, a list of 4 PEs is required:
    for (int i=0;i<elements;i++){
      peList1.add(new Pe(i, new PeProvisionerSimple(Constants.HOST_MIPS))); // need to store Pe id and MIPS Rating
    }

    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    hostList.add(
        new Host(
            Constants.HOST_ID,
            new RamProvisionerSimple(Constants.HOST_RAM),
            new BwProvisionerSimple(Constants.HOST_BANDWIDTH),
            Constants.STORAGE,
            peList1,
            new VmSchedulerSpaceShared(peList1)
        )
    );


    //To create a host with a space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerSpaceShared(peList1)
    //		)
    //	);

    //To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerOportunisticSpaceShared(peList1)
    //		)
    //	);


    // 5. Create a DatacenterCharacteristics object that stores the
    //    properties of a data center: architecture, OS, list of
    //    Machines, allocation policy: time- or space-shared, time zone
    //    and its price (G$/Pe time unit).
    LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
        Constants.ARCHITECTURE, Constants.OS, Constants.VMM_NAME, hostList, Constants.TIME_ZONE,
        Constants.COST_PROCESSING, Constants.COST_MEMORY, Constants.COST_STORAGE, Constants.COST_BANDWIDTH);


    // 6. Finally, we need to create a PowerDatacenter object.
    Datacenter datacenter = null;
    try {
      datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return datacenter;
  }

  /**
   * Prints the Cloudlet objects
   * @param list  list of Cloudlets
   */
  // private static void printCloudletList(List<Cloudlet> list) {
  // 	int size = list.size();
  // 	Cloudlet cloudlet;

  // 	String indent = "    ";
  // 	Log.printLine();
  // 	Log.printLine("========== OUTPUT ==========");
  // 	Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
  // 			"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

  // 	DecimalFormat dft = new DecimalFormat("###.##");
  // 	for (int i = 0; i < size; i++) {
  // 		cloudlet = list.get(i);
  // 		Log.print(indent + cloudlet.getCloudletId() + indent + indent);

  // 		if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
  // 			Log.print("SUCCESS");

  // 			Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
  // 					indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
  // 					indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
  // 		}
  // 	}

  // }
}
