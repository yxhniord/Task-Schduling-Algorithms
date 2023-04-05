package mGWO_mWOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;

import java.util.List;

public class mGWO_EBWOA_Scheduler {
    public static void main(String[] args) {
        double finishtime = 0.0;  // no usage for now
        Log.printLine("Starting mGWO Scheduler...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            Commons.set_cloudsim_parameters();

            //createDatacenter createVM createCloudlet
            int num = 20;

            for (int k = 1; k <= 10; k++) {
                CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

                // Second step: Create Data Centers.
                // Data Centers are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
                @SuppressWarnings("unused")
                Datacenter datacenter = Commons.createDatacenter("Datacenter_mGWO", num);

                // Third step: Create Broker
                DatacenterBroker_mGWO_EBWOA broker = createBroker("Broker_mGWO", 100);
                int brokerId = broker.getId();

                Commons.createVM(brokerId, num);
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
                for (int j=0;j<100*k;j++){
                    cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
                }
                System.out.println("********cost = " + cost);

                Log.printLine("mGWO Scheduler finished!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }

    private static DatacenterBroker_mGWO_EBWOA createBroker(String name, int iter) throws Exception {
        return new DatacenterBroker_mGWO_EBWOA(name, iter);
    }
}
