package mGWO_WOA;

import HOA.DatacenterBroker_HOA;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;
import utils.Constants;

import java.util.List;

public class mGWO {
    private int itr;
    private int numJobs;
    private int numVms;
    private int population;

    public mGWO(int itr, int numJobs, int numVms, int population){
        this.itr = itr;
        this.numJobs = numJobs;
        this.numVms = numVms;
        this.population = population;
    }

    public double implement() throws Exception {
        double finishTime;
        Log.printLine("Starting mGWO_WOA Scheduler...");


        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        Commons.set_cloudsim_parameters();

        //createDatacenter createVM createCloudlet

        CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

        // Second step: Create Datacenters
        // Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
        Commons.createDatacenter("Datacenter_mGWO_WOA", numVms);

        // Third step: Create Broker
        DatacenterBroker_mGWO_WOA broker = createBroker("Broker_mGWO_WOA", population, itr);
        int brokerId = broker.getId();

        Commons.createVM(brokerId, numVms);
        Commons.createCloudlet(brokerId,100*numJobs);

        broker.submitVmList(Commons.vmList);
        broker.submitCloudletList(Commons.cloudletList);

        // Fifth step: Starts the simulation
        CloudSim.startSimulation();

        // Final step: Print results when simulation is over
        List<Cloudlet> newList = broker.getCloudletReceivedList();

        CloudSim.stopSimulation();

        double max = 0;
        double min = Double.MAX_VALUE;
        for (int j=0;j<100*numJobs;j++){
            max = Math.max(max, newList.get(j).getFinishTime());
            min = Math.min(min, newList.get(j).getExecStartTime());
        }

        double makespan = max - min;
        double cost = 0;
        for (int j=0;j<100*numJobs;j++){
            cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
        }
        finishTime = makespan + cost;
        return finishTime;
    }

    private static DatacenterBroker_mGWO_WOA createBroker(String name, int popSize, int maxIter) throws Exception {
        return new DatacenterBroker_mGWO_WOA(name, popSize, maxIter);
    }
}

