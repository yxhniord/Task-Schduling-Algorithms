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

	protected int iter;

  public DatacenterBroker_mGWO_WOA(String name, int iter) throws Exception {
    super(name);

		this.iter = iter;
  }

  @Override
  protected void submitCloudlets() {
		List<Cloudlet> clList = getCloudletList();
		List<Vm> vm_list = getVmsCreatedList();

		mGWO_WOA_Implement mgwo_woa = new mGWO_WOA_Implement();
		Map<Integer,Integer> allocatedTasks = mgwo_woa.allocateTasks(clList,vm_list,iter);

		for (int i=0;i<clList.size();i++) {
			Cloudlet cloudlet = clList.get(i);
			Vm vm = vm_list.get(allocatedTasks.get(i));
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
