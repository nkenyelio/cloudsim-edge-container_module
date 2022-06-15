package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.core.SimSettings;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisioner;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.schedulers.ContainerScheduler;

import java.util.ArrayList;
import java.util.List;

public class EdgeContainerVM extends Vm {
    private SimSettingsContainer.CNT_TYPES type;

    public EdgeContainerVM(int id, int userId, double mips, int numberOfPes, int ram,
                  long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
        super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);

        type = SimSettingsContainer.CNT_TYPES.CONTAINER_EDGEVM;
    }

    public SimSettingsContainer.CNT_TYPES getVmType(){
        return type;
    }

    /**
     *  dynamically reconfigures the mips value of a  VM in CloudSim
     *
     * @param mips new mips value for this VM.
     */
    public void reconfigureMips(double mips){
        super.setMips(mips);
        super.getHost().getVmScheduler().deallocatePesForVm(this);

        List<Double> mipsShareAllocated = new ArrayList<Double>();
        for(int i= 0; i<getNumberOfPes(); i++)
            mipsShareAllocated.add(mips);

        super.getHost().getVmScheduler().allocatePesForVm(this, mipsShareAllocated);
    }



}