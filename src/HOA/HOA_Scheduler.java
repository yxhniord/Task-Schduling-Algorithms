package HOA;

import com.opencsv.CSVWriter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;
import utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HOA_Scheduler {
    public static double main(String[] args) throws IOException {
        double finishTime = 0.0;
        Log.printLine("Starting HOA Scheduler...");

        File filePost = new File("./recordhoaOLD.csv");
        FileWriter outputfilePost = new FileWriter(filePost);
        CSVWriter writerPost = new CSVWriter(outputfilePost);
        writerPost.writeNext(new String[] {"Nums of iteration", "Cost"});
        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            Commons.set_cloudsim_parameters();

            int vm = 50;
            int pop = 24;
            int iter = 100;
            int task = 10000;

            for (int k = 1000; k <= 10000; k+=1000) {
                CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

                // Second step: Create Datacenters
                // Datacenters are the resource providers in CloudSim. We need at list one of
                // them to run a CloudSim simulations
                Commons.createDatacenter("Datacenter_HOA", vm);

                // Third step: Create Broker
                DatacenterBroker_HOA broker = createBroker("Broker_HOA", pop, iter);
                int brokerId = broker.getId();

                Commons.createVM(brokerId, vm);
                Commons.createCloudlet(brokerId, k);

                broker.submitVmList(Commons.vmList);
                broker.submitCloudletList(Commons.cloudletList);

                // Fifth step: Starts the simulation
                CloudSim.startSimulation();

                // Final step: Print results when simulation is over
                List<Cloudlet> newList = broker.getCloudletReceivedList();

                CloudSim.stopSimulation();

                double max = 0;
                double min = Double.MAX_VALUE;
                for (int j=0;j<k;j++){
                    max = Math.max(max, newList.get(j).getFinishTime());
                    min = Math.min(min, newList.get(j).getExecStartTime());
                }

                double makespan = max - min;
                System.out.println("********makespan = " + makespan);

                double cost = 0;
                for (int j=0;j<k;j++){
//                    cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
                    cost += newList.get(j).getCloudletTotalLength()*0.1;
                }
                System.out.println("********cost = " + cost);

                double fitness = makespan + 0.001*cost;
                writerPost.writeNext(new String[] {String.valueOf(k), String.valueOf(fitness)});
                Log.printLine("HOA Scheduler finished!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
        writerPost.close();
        return finishTime;
    }

    private static DatacenterBroker_HOA createBroker(String name, int num_horse, int iter) throws Exception {
        return new DatacenterBroker_HOA(name, num_horse, iter);
    }
}
