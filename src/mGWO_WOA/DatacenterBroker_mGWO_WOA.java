package mGWO_WOA;


import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class DatacenterBroker_mGWO_WOA extends DatacenterBroker {

	protected int maxIter;
	protected int popSize;

  public DatacenterBroker_mGWO_WOA(String name, int popSize, int maxIter) throws Exception {
    super(name);

		this.maxIter = maxIter;
		this.popSize = popSize;
  }

  @Override
  protected void submitCloudlets() {
		List<Cloudlet> clList = getCloudletList();
		List<Vm> vm_list = getVmsCreatedList();

		mGWO_WOA_Implement1 mgwo_woa = new mGWO_WOA_Implement1(popSize, maxIter);
		Map<Integer,Integer> allocatedTasks = mgwo_woa.allocateTasks(clList,vm_list);

		for (Map.Entry<Integer, Integer> entry : allocatedTasks.entrySet()) {
			Cloudlet cloudlet = clList.get(entry.getKey());
			Vm vm = vm_list.get(entry.getValue());
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
		}
		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}
}
