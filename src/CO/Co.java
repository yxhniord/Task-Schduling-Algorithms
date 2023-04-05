package CO;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;

import java.util.List;

public class Co {
    private int itr;
    private int numJobs;
    private int numVms;
    private int population;

    public Co(int itr, int numJobs, int numVms, int population){
        this.itr = itr;
        this.numJobs = numJobs;
        this.numVms = numVms;
        this.population = population;
    }

    public double implement() throws Exception {
        double finishtime = 0.0;
        Log.printLine("Starting CO Scheduler...");

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        Commons.set_cloudsim_parameters();

        // createDatacenter createVM createCloudlet
        int num = 20;


        CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

        // Second step: Create Datacenters
        // Datacenters are the resource providers in CloudSim. We need at list one of
        // them to run a CloudSim simulations
        Commons.createDatacenter("Datacenter_CO", numVms);

        // Third step: Create Broker
        DatacenterBroker_CO broker = createBroker("Broker_CO", itr);
        int brokerId = broker.getId();

        Commons.createVM(brokerId, numVms);
        Commons.createCloudlet(brokerId, numJobs);

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
        finishtime = makespan + cost;
        return finishtime;
    }

    private static DatacenterBroker_CO createBroker(String name, int iter) throws Exception {
        return new DatacenterBroker_CO(name, iter);
    }
}
