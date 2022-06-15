package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.core.SimSettings;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeContainerVmAllocationPolicy_Custom extends ContainerVmAllocationPolicy {

    /**
     * The vm table.
     */
    private Map<String, ContainerHost> vmTable;

    /**
     * The used pes.
     */
    private Map<String, Integer> usedPes;

    /**
     * The free pes.
     */
    private List<Integer> freePes;

    /**
     * Creates the new VmAllocationPolicySimple object.
     *
     * @param list the list
     * @pre $none
     * @post $none
     */
    private static int createdVmNum;
    private int DataCenterIndex;

    public EdgeContainerVmAllocationPolicy_Custom(List<? extends ContainerHost> list, int _DataCenterIndex) {
        super(list);
        setFreePes(new ArrayList<Integer>());
        for (ContainerHost host : getContainerHostList()) {
            getFreePes().add(host.getNumberOfPes());

        }

        setVmTable(new HashMap<String, ContainerHost>());
        setUsedPes(new HashMap<String, Integer>());
        DataCenterIndex=_DataCenterIndex;
        createdVmNum = 0;
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm) {

        int requiredPes = containerVm.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<>();
        for (Integer freePes : getFreePes()) {
            freePesTmp.add(freePes);
        }

        if(!getVmTable().containsKey(containerVm.getUid()) && containerVm instanceof ContainerVm) {
            boolean vmFound = false;
            int vmCounter = 0;
            int hostIndex = 0;
            int dataCenterIndex = 0;

            //find proper datacenter id and host id for this VM
            Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
            NodeList datacenterList = doc.getElementsByTagName("datacenter");
            for (int i = 0; (!vmFound && i < datacenterList.getLength()); i++) {
                Node datacenterNode = datacenterList.item(i);
                Element datacenterElement = (Element) datacenterNode;
                NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
                for (int j = 0; (!vmFound  && j < hostNodeList.getLength()); j++) {
                    Node hostNode = hostNodeList.item(j);
                    Element hostElement = (Element) hostNode;
                    NodeList vmNodeList = hostElement.getElementsByTagName("VM");
                    for (int k = 0; (!vmFound && k < vmNodeList.getLength()); k++) {

                        if(vmCounter == containerVm.getId()){
                            dataCenterIndex = i;
                            hostIndex = j;
                            vmFound = true;
                        }

                        vmCounter++;
                    }
                }
            }

            if(vmFound && dataCenterIndex == DataCenterIndex && hostIndex < getContainerHostList().size()){
                ContainerHost host = getContainerHostList().get(hostIndex);
                result = host.containerVmCreate(containerVm);

                if (result) { // if vm were successfully created in the host
                    getVmTable().put(containerVm.getUid(), host);
                    createdVmNum++;
                    Log.formatLine("%.2f: Edge VM #" + containerVm.getId() + " has been allocated to the host #" + host.getId(),CloudSim.clock());
                    result = true;
                }
            }

        }
        return result;
    }

    @Override
    public boolean allocateHostForVm(ContainerVm containerVm, ContainerHost containerHost) {
        if (containerHost.containerVmCreate(containerVm)) { // if vm has been successfully created in the host
            getVmTable().put(containerVm.getUid(), containerHost);
            createdVmNum++;

            Log.formatLine("%.2f: Edge Container VM #" + containerVm.getId() + " has been allocated to the host #" + containerHost.getId(), CloudSim.clock());
            return true;
        }

        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends ContainerVm> list) {
        return null;
    }

    @Override
    public void deallocateHostForVm(ContainerVm containerVm) {
        ContainerHost host = getVmTable().remove(containerVm.getUid());
        if (host != null) {
            host.containerVmDestroy(containerVm);
        }

    }


    @Override
    public ContainerHost getHost(ContainerVm containerVm) {
        return getVmTable().get(containerVm.getUid());
    }


    @Override
    public ContainerHost getHost(int vmId, int userId) {
        return getVmTable().get(ContainerVm.getUid(userId, vmId));
    }

    @Override
    public void setDatacenter(ContainerDatacenter containerDatacenter) {

    }
    public void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }
    public void setVmTable(Map<String, ContainerHost> vmTable) {
        this.vmTable = vmTable;
    }
    public List<Integer> getFreePes() {
        return freePes;
    }
    public void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }
    public Map<String, ContainerHost> getVmTable() {
        return vmTable;
    }
}
