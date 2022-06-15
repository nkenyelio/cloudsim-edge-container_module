package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

public class EdgeContainerAllocationPolicy_Custom extends EdgeCntAllocationPolicy {

    /** The vm table. */
    private Map<String, EdgeVM> vmTable;
    private Map<String, Host> hostVMList;
    private static int createdVmNum;
    private int DataCenterIndex;

    public EdgeContainerAllocationPolicy_Custom(List<? extends EdgeVM> list, int _DataCenterIndex) {
        super(list);
        setVmTable(new HashMap<String, EdgeVM>());
        DataCenterIndex = _DataCenterIndex;
        createdVmNum = 0;

    }


    @Override
    public boolean allocateEdgeVMForCnt(EdgeContainer vm) {
        boolean result = false;

        if (!getVmTable().containsKey(vm.getUid()) && vm instanceof EdgeContainer) { // if this edgecontainer was not created
            boolean vmFound = false;
            int vmCounter = 0;
            int hostIndex = 0;
            int vmIndex = 0;
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
                        /*
                        * get the container node inside the VM machines*/
                        
                        Node vmNode = vmNodeList.item(k);
                        Element vmElement = (Element) vmNode;
                        NodeList cntNodeList = vmElement.getElementsByTagName("CN");
                        for (int t = 0; (!vmFound && t < cntNodeList.getLength()); t++){
                            if (vmCounter ==vm.getId()) {
                                dataCenterIndex = i;
                                hostIndex = j;
                                vmIndex = k;
                                vmFound = true;
                            }
                            vmCounter++;
                        }
                    }
                }
            }

            if(vmFound && dataCenterIndex == DataCenterIndex && hostIndex < getHostList().size()){
                Host host = getHostList().get(hostIndex);
                result = host.vmCreate(vm);

                if (result) { // if vm were successfully created in the host
                    getVmTable().put(vm.getUid(), host);
                    createdVmNum++;
                    Log.formatLine("%.2f: Edge VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),CloudSim.clock());
                    result = true;
                }
            }
        }

        return result;
    }

    private Map<String,Host> getHostList() {
        return hostVMList;
    }

    @Override
    public boolean allocateEdgeVMForCnt(EdgeContainer cnt, EdgeVM EVm) {
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeCNTAllocation(List<? extends EdgeContainer> cntList) {
        return null;
    }

    @Override
    public void deallocateEdgeVMForCnt(EdgeContainer cnt) {

    }

    @Override
    public EdgeVM getEdgeVM(EdgeContainer cnt) {
        return null;
    }

    @Override
    public EdgeVM getEdgeVM(int cntId, int edgeHostId) {
        return null;
    }

    /**
     * Gets the vm table.
     *
     * @return the vm table
     */
    public Map<String, EdgeVM> getVmTable() {
        return vmTable;
    }

    /**
     * Sets the vm table.
     *
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, EdgeVM> vmTable) {
        this.vmTable = vmTable;
    }
}
