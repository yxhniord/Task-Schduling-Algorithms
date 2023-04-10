package mGWO_mWOA;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;
import utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class mGWO_EBWOA_Scheduler {
    final static private String RECORD_PATH = "/Users/niuyiheng/Documents/Align/CS7980/project/Task-Schduling-Algorithms/records.csv";
    public static void main(String[] args) {
        double finishtime = 0.0;  // no usage for now
        Log.printLine("Starting mGWO Scheduler...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            Commons.set_cloudsim_parameters();

            //createDatacenter createVM createCloudlet
            int num = 20;
//            int[] NUM_ITERATIONS = new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 200, 300, 500};
            int[] NUM_ITERATIONS  = new int[]{100, 300, 500};
            int[] NUM_POPULATIONS = new int[]{40, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240, 260, 280, 300, 500, 1000, 2000};

            File file = new File(RECORD_PATH);
            FileWriter outputFile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputFile);

            for (int i = 0; i < NUM_ITERATIONS.length; i++) {
                for (int k = 10; k <= 10; k++) {
                    CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

                    // Second step: Create Data Centers.
                    // Data Centers are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
                    @SuppressWarnings("unused")
                    Datacenter datacenter = Commons.createDatacenter("Datacenter_mGWO_mWOA", Constants.NO_OF_VMS);

                    // Third step: Create Broker
//                    DatacenterBroker_mGWO_EBWOA broker = createBroker("Broker_mGWO_mWOA", Constants.POPULATION, Constants.MAX_ITER);
                    DatacenterBroker_mGWO_EBWOA broker = createBroker("Broker_mGWO_mWOA", Constants.POPULATION, NUM_ITERATIONS[i]);
                    int brokerId = broker.getId();

                    Commons.createVM(brokerId, Constants.NO_OF_VMS);
                    Commons.createCloudlet(brokerId, 100 * k);

                    broker.submitVmList(Commons.vmList);
                    broker.submitCloudletList(Commons.cloudletList);

                    // Fifth step: Starts the simulation
                    CloudSim.startSimulation();

                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

                    double max = 0;
                    double min = Double.MAX_VALUE;
                    for (int j = 0; j < 100 * k; j++) {
                        max = Math.max(max, newList.get(j).getFinishTime());
                        min = Math.min(min, newList.get(j).getExecStartTime());
                    }
                    double makespan = max - min;

                    System.out.println("********makespan = " + makespan);

                    double cost = 0;
                    for (int j = 0; j < 100 * k; j++) {
                        cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
                    }
                    System.out.println("********cost = " + cost);

                    Log.printLine("mGWO Scheduler finished!");
                    double fitness = makespan + 0.001 * cost;
                    String[] nextLine = new String[]{String.valueOf(fitness)};
                    writer.writeNext(nextLine);
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }

    private static DatacenterBroker_mGWO_EBWOA createBroker(String name, int population, int iter) throws Exception {
        return new DatacenterBroker_mGWO_EBWOA(name, population, iter);
    }
}
