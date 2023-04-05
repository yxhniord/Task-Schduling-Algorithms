package CO;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import utils.Commons;

public class CO_Scheduler {
	public static double main(String[] args) {
		double finishtime = 0.0;
		Log.printLine("Starting CO Scheduler...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			Commons.set_cloudsim_parameters();

			// createDatacenter createVM createCloudlet
			int num = 20;

			for (int k = 1; k <= 10; k++) {
				CloudSim.init(Commons.num_user, Commons.calendar, Commons.trace_flag);

				// Second step: Create Datacenters
				// Datacenters are the resource providers in CloudSim. We need at list one of
				// them to run a CloudSim simulations
				Commons.createDatacenter("Datacenter_CO", num);

				// Third step: Create Broker
				DatacenterBroker_CO broker = createBroker("Broker_CO", 1000);
				int brokerId = broker.getId();

				Commons.createVM(brokerId, num);
				Commons.createCloudlet(brokerId, 1000);

				broker.submitVmList(Commons.vmList);
				broker.submitCloudletList(Commons.cloudletList);

				// Fifth step: Starts the simulation
				CloudSim.startSimulation();

				// Final step: Print results when simulation is over
				List<Cloudlet> newList = broker.getCloudletReceivedList();

				CloudSim.stopSimulation();

				double max = 0;
				double sum = 0;
				double dtaskNum = k * 100;
				for (int j = 0; j < 100 * k; j++) {
					sum += newList.get(j).getFinishTime();
					if (max < newList.get(j).getFinishTime())
						max = newList.get(j).getFinishTime();
				}
				double min = Double.MAX_VALUE;
				for (int j = 0; j < 100 * k; j++) {
					if (min > newList.get(j).getExecStartTime())
						min = newList.get(j).getExecStartTime();
				}

				double aver = sum / dtaskNum;
				double di = (max - min) / aver;

				System.out.println(" di = " + di);
				System.out.println("********max = " + max + "*******min = " + min);

				Log.printLine("CO Scheduler finished!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
		return finishtime;
	}

	private static DatacenterBroker_CO createBroker(String name, int iter) throws Exception {
		return new DatacenterBroker_CO(name, iter);
	}
}
