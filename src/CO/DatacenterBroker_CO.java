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

	protected int iter;

	public DatacenterBroker_CO(String name, int iter) throws Exception {
    super(name);

		this.iter = iter;
  }

	@Override
	protected void submitCloudlets() {
		List<Cloudlet> clList = getCloudletList();
		List<Vm> vm_list = getVmsCreatedList();

		COImplement co = new COImplement();
		Map<Integer, Integer> allocatedTasks = co.allocateTasks(clList, vm_list, iter);
		System.out.println("CO Done");
		for (int i = 0; i < clList.size(); i++) {
			Cloudlet cloudlet = clList.get(i);
			Vm vm = vm_list.get(allocatedTasks.get(i));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet " + cloudlet.getCloudletId()
					+ " to VM #" + vm.getId());
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
