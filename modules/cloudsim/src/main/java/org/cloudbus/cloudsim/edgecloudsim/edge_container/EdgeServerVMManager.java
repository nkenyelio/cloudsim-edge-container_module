/*
 * Title:        EdgeCloudSim - EdgeServerManager
 *
 * Description:
 * EdgeServerManager is responsible for creating and terminating
 * the edge datacenters which operates the hosts and ContainersVMs (This are containers running inside the VMs).
 * It also provides the list of ContainersVMs running on the hosts and
 * the average utilization of all ContainersVMs.
 *
 * Please note that, EdgeCloudSim is built on top of CloudSim
 * Therefore, all the computational units are handled by CloudSim
 *
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */


package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;

import java.util.ArrayList;
import java.util.List;

public abstract class EdgeServerVMManager {

    protected List<ContainerDatacenter> localDatacenters;
    protected List<List<EdgeContainerVM>> cntvmList;

    public EdgeServerVMManager() {
        localDatacenters=new ArrayList<ContainerDatacenter>();
        cntvmList = new ArrayList<List<EdgeContainerVM>>();
    }

    public List<EdgeContainerVM> getVmList(int hostId){
        return cntvmList.get(hostId);
    }

    public List<ContainerDatacenter> getDatacenterList(){
        return localDatacenters;
    }

    /*
     * initialize edge server manager if needed
     */
    public abstract void initialize();

    /*
     * provides abstract container Allocation Policy for Edge Datacenters
     */
    public abstract ContainerAllocationPolicy getContainerAllocationPolicy();

    /*
     * provide abstract container selection policy for the VM. This is like a form of migration in case the host is overloded
     */

    public abstract PowerContainerVmSelectionPolicy getPowerContainerVmSelectionPolicy ();


    public abstract HostSelectionPolicy getHostSelectionPolicy();


    public abstract ContainerVmAllocationPolicy getContainerVmAllocationPolicy(List<? extends ContainerHost> containerHostList);

    public abstract ContainerVmAllocationPolicy getContainerVmAllocationPolicy(List<? extends ContainerHost> containerHostList, int _DataCenterIndex);

    /*
     * Starts Datacenters
     */
    public abstract void startDatacenters() throws Exception;

    /*
     * Terminates Datacenters
     */
    public abstract void terminateDatacenters();
    /*
     * Creates VM List
     */
    public abstract void createVmList(int brokerId);


    protected abstract ArrayList<ContainerVm> createVmList(int brokerId, int containerVmsNumber);

    /*
     * returns average utilization of all VMs
     */
    public abstract double getAvgUtilization();
}
