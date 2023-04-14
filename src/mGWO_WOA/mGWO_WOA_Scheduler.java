package mGWO_WOA;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;
import utils.Constants;

public class mGWO_WOA_Scheduler {
  public static double main(String[] args) throws IOException {
    double finishtime = 0.0;
    Log.printLine("Starting mGWO_WOA Scheduler...");

    File filePost = new File("./recordmGWOWOA.csv");
    FileWriter outputfilePost = new FileWriter(filePost);
    CSVWriter writerPost = new CSVWriter(outputfilePost);
    writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
    try {
      // First step: Initialize the CloudSim package. It should be called
      // before creating any entities.
      Commons.set_cloudsim_parameters();

      //createDatacenter createVM createCloudlet
      for (int k=5;k<=100;k+=5){
        CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

        // Second step: Create Datacenters
        // Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
        Commons.createDatacenter("Datacenter_mGWO_WOA", Constants.NO_OF_VMS);

        // Third step: Create Broker
        DatacenterBroker_mGWO_WOA broker = createBroker("Broker_mGWO_WOA", Constants.POPULATION, k);
        int brokerId = broker.getId();

        Commons.createVM(brokerId, Constants.NO_OF_VMS);
        Commons.createCloudlet(brokerId,1000);

        broker.submitVmList(Commons.vmList);
        broker.submitCloudletList(Commons.cloudletList);

        // Fifth step: Starts the simulation
        CloudSim.startSimulation();

        // Final step: Print results when simulation is over
        List<Cloudlet> newList = broker.getCloudletReceivedList();

        CloudSim.stopSimulation();

        double max = 0;
        double min = Double.MAX_VALUE;
        for (int j=0;j<1000;j++){
          max = Math.max(max, newList.get(j).getFinishTime());
          min = Math.min(min, newList.get(j).getExecStartTime());
        }

        double makespan = max - min;
        System.out.println("********makespan = " + makespan);

        double cost = 0;
        for (int j=0;j<1000;j++){
          cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
        }
        System.out.println("********cost = " + cost);
        double fitness = makespan + 0.001*cost;
        writerPost.writeNext(new String[] {String.valueOf(k), String.valueOf(fitness)});
        Log.printLine("mGWO_WOA Scheduler finished!");
      }
    } catch (Exception e) {
      e.printStackTrace();
      Log.printLine("The simulation has been terminated due to an unexpected error");
    }
    writerPost.close();
    return finishtime;
  }

  private static DatacenterBroker_mGWO_WOA createBroker(String name, int popSize, int maxIter) throws Exception {
    return new DatacenterBroker_mGWO_WOA(name, popSize, maxIter);
  }
}
