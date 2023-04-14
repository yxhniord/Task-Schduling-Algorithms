package newHOA;

import HOA.DatacenterBroker_HOA;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Commons;

import java.util.List;

public class newHoa {
    private int itr;
    private int numJobs;
    private int numVms;
    private int population;

    public newHoa(int itr, int numJobs, int numVms, int population){
        this.itr = itr;
        this.numJobs = numJobs;
        this.numVms = numVms;
        this.population = population;
    }

    public double implement() throws Exception {
        double finishTime;

        // First step: Initialize the CloudSim package. It should be called
        // before creating any entities.
        Commons.set_cloudsim_parameters();

        CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

        // Second step: Create Datacenters
        // Datacenters are the resource providers in CloudSim. We need at list one of
        // them to run a CloudSim simulations
        Commons.createDatacenter("Datacenter_HOA", numVms);

        // Third step: Create Broker
        DatacenterBroker_newHOA broker = createBroker("Broker_HOA", population, itr);
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
        for (int j=0;j<numJobs;j++){
            max = Math.max(max, newList.get(j).getFinishTime());
            min = Math.min(min, newList.get(j).getExecStartTime());
        }

        double makespan = max - min;
        double cost = 0;
        for (int j=0;j<numJobs;j++){
//            cost += newList.get(j).getCostPerSec() * newList.get(j).getActualCPUTime();
            cost += newList.get(j).getCloudletTotalLength()*0.1;
        }
        finishTime = makespan + 0.001*cost;
        return finishTime;
    }

    private static DatacenterBroker_newHOA createBroker(String name, int num_horse, int iter) throws Exception {
        return new DatacenterBroker_newHOA(name, num_horse, iter);
    }

}

