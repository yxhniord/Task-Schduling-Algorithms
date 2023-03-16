package HOA;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

import java.util.List;
import java.util.Map;

public class DatacenterBroker_HOA extends DatacenterBroker {
    private static final int num_horses = 10;
    protected int iter;

    public DatacenterBroker_HOA(String name, int iter) throws Exception {
        super(name);

        this.iter = iter;
    }

    @Override
    protected void submitCloudlets() {
        List<Cloudlet> clList = getCloudletList();
        List<Vm> vm_list = getVmsCreatedList();

        HOAImplement hoa = new HOAImplement(num_horses,iter,clList.size(), vm_list.size());
        Map<Integer, Integer> allocatedTasks = hoa.implement();
        System.out.println("allocatedTasks"+ allocatedTasks.get(1));
        System.out.println("HOA Done");
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
