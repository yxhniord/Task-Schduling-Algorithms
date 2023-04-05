package GWO;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import utils.Commons;
import utils.Constants;

public class GWO_Scheduler {
	public static double main(String[] args) {
		double finishtime = 0.0;
		Log.printLine("Starting GWO Scheduler...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			Commons.set_cloudsim_parameters();

			// createDatacenter createVM createCloudlet
			for (int k = 1; k <= 10; k++) {
				CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

				// Second step: Create Datacenters
				// Datacenters are the resource providers in CloudSim. We need at list one of
				// them to run a CloudSim simulation
				Commons.createDatacenter("Datacenter_GWO", Constants.NO_OF_VMS);

				// Third step: Create Broker
				DatacenterBroker_GWO broker = createBroker("Broker_GWO", Constants.POPULATION, Constants.MAX_ITER);
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
		        for (int j=0;j<100*k;j++){
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
		        
				Log.printLine("GWO Scheduler finished!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
		return finishtime;
	}

	private static DatacenterBroker_GWO createBroker(String name, int popSize, int iter) throws Exception {
		return new DatacenterBroker_GWO(name, popSize, iter);
	}
}
