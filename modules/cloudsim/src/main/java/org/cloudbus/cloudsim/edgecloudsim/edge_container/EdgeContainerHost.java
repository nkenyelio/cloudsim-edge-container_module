package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.utils.Location;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisioner;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmScheduler;

import java.util.List;

public class EdgeContainerHost extends ContainerHost {

    private Location location ;

    public EdgeContainerHost(int id, ContainerVmRamProvisioner containerVmRamProvisioner, ContainerVmBwProvisioner containerVmBwProvisioner, long storage, List<? extends ContainerVmPe> peList, ContainerVmScheduler containerVmScheduler) {
        super(id, containerVmRamProvisioner, containerVmBwProvisioner, storage, peList, containerVmScheduler);
    }

    public void setPlace(Location _location){

    }

    public Location getLocation(){
        return location;
    }
}
