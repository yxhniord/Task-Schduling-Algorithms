package CO;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

public class DatacenterBroker_CO extends DatacenterBroker {

	protected int maxIter;
	protected int popSize;

	public DatacenterBroker_CO(String name, int pop, int iter) throws Exception {
    super(name);
    	this.popSize = pop;
		this.maxIter = iter;
  }

	@Override
	protected void submitCloudlets() {
		List<Cloudlet> clList = getCloudletList();
		List<Vm> vm_list = getVmsCreatedList();

		COImplement co = new COImplement();
		Map<Integer, Integer> allocatedTasks = co.allocateTasks(clList, vm_list, popSize, maxIter);
		
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
