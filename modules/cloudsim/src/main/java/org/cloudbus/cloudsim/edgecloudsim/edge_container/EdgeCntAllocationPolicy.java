package org.cloudbus.cloudsim.edgecloudsim.edge_container;


import edu.boun.edgecloudsim.edge_server.EdgeVM;
import org.cloudbus.cloudsim.container.core.Container;

import java.util.List;
import java.util.Map;

/**
 * EdgeCntAllocationPolicy is an abstract class that represents the provisioning policy of Edge Virtual Machine to edge containers in a EdgeHost. It allocates edgevM for placing EdgeContainers.
 * It supports two-stage commit of reservation of EdgeVMs: first, we
 * reserve the EdgeVM and, once committed by the user, it is effectively allocated to he/she.
 *
 * @author USNLab
 * @author USNlab
 * @since EdgeCloudSim Toolkit 1.0
 */

public abstract class EdgeCntAllocationPolicy {


    /** The EdgeVMs list. */
    private List<? extends EdgeVM> edgeVMList;

    /**
     * Creates a new EdgecntAllocationPolicy object.
     *
     * @param list Machines available in a {@link edu.boun.edgecloudsim.edge_server.EdgeHost}
     * @pre $none
     * @post $none
     */
    public EdgeCntAllocationPolicy(List<? extends EdgeVM> list) {
        setEdgeVMList(list);
    }

    /**
     * Allocates a host for a given VM.
     *
     * @param vm the CNT to allocate a EdgeVM to
     * @return $true if the EdgevM could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateEdgeVMForCnt(EdgeContainer vm);

    /**
     * Allocates a specified EdgeVM for a given CNT (container).
     *
     * @param cnt container which the edgeVM is reserved to
     * @param EVm edgeVM to allocate the given CNT (container)
     * @return $true if the EdgeVM could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateEdgeVMForCnt(EdgeContainer cnt, EdgeVM EVm);

    /**
     * Optimize allocation of the CNTs according to current utilization.
     *
     * @param cntList the Container list
     * @return the array list< hash map< string, object>>
     *
     * @todo It returns a list of maps, where each map key is a string
     * and stores an object. What in fact are the keys and values of this
     * Map? Neither this class or its subclasses implement the method
     * or have clear documentation. The only sublcass is the
     *
     */
    public abstract List<Map<String, Object>> optimizeCNTAllocation(List<? extends EdgeContainer> cntList);

    /**
     * Releases the host used by a VM.
     *
     * @param cnt the vm to get its host released
     * @pre $none
     * @post $none
     */
    public abstract void deallocateEdgeVMForCnt(EdgeContainer cnt);

    /**
     * Get the host that is executing the given VM.
     *
     * @param cnt the cnt
     * @return the EdgeVM with the given EdgeVMID; $null if not found
     *
     * @pre $none
     * @post $none
     */
    public abstract EdgeVM getEdgeVM(EdgeContainer cnt);

    /**
     * Get the EdgeVM that is executing the given container belonging to the given edgeserver (or user).
     *
     * @param cntId the vm id
     * @param edgeHostId the edgeHost id
     * @return the EdgeVM with the given EdgeVMID and EdgeHostID; $null if not found
     * @pre $none
     * @post $none
     */
    public abstract EdgeVM getEdgeVM(int cntId, int edgeHostId);

    /**
     * Sets the host list.
     *
     * @param edgeVMList the new EdgeVM list
     */
    protected void setEdgeVMList(List<? extends EdgeVM> edgeVMList) {
        this.edgeVMList = edgeVMList;
    }

    /**
     * Gets the host list.
     *
     * @return the host list
     */
    @SuppressWarnings("unchecked")
    public <T extends EdgeVM> List<T> getEdgeVMList() {
        return (List<T>) edgeVMList;
    }

}
