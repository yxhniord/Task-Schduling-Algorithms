package mGWO_mWOA;

import GWO.GWOImplement;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

import java.util.List;
import java.util.Map;

public class DatacenterBroker_mGWO_EBWOA extends DatacenterBroker {

    protected int iter;
    protected int population;

    public DatacenterBroker_mGWO_EBWOA(String name, int population, int iter) throws Exception {
        super(name);
        this.population = population;
        this.iter = iter;
    }

    @Override
    protected void submitCloudlets() {
        List<Cloudlet> clList = getCloudletList();
        List<Vm> vm_list = getVmsCreatedList();

        mGWOImplementation mGWO = new mGWOImplementation();
        EBWOAImplementation ebWOA = new EBWOAImplementation();
        BridgeResult bridgeResult = mGWO.allocateTasksHybrid(clList, vm_list, population, iter);
        Map<Integer, Integer> allocatedTasks = ebWOA.allocateTasksWithBest(clList, vm_list, population, iter, bridgeResult);

        for (int i = 0; i < clList.size(); i++) {
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
