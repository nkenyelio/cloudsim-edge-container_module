package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.edge_server.EdgeVmAllocationPolicy_Custom;
import edu.boun.edgecloudsim.utils.Location;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.CotainerPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.*;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DefaultEdgeServerVMManager extends EdgeServerVMManager {

    private int hostIdCounter;

    public DefaultEdgeServerVMManager()
    {
        hostIdCounter = 0;
    }

    @Override
    public void initialize() {

    }

    @Override
    public ContainerAllocationPolicy getContainerAllocationPolicy() {
        return new EdgeContainerAllocationPolicy_Custom();
    }

    @Override
    public PowerContainerVmSelectionPolicy getPowerContainerVmSelectionPolicy() {
        return null;
    }

    @Override
    public HostSelectionPolicy getHostSelectionPolicy() {
        return null;
    }

    @Override
    public ContainerVmAllocationPolicy getContainerVmAllocationPolicy(List<? extends ContainerHost> containerHostList) {
        return null;
    }

    @Override
    public ContainerVmAllocationPolicy getContainerVmAllocationPolicy(List<? extends ContainerHost> containerHostList, int _DataCenterIndex) {
        return new EdgeContainerVmAllocationPolicy_Custom(containerHostList, _DataCenterIndex);
    }

    @Override
    public void startDatacenters() throws Exception {

        Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
        NodeList datacenterList = doc.getElementsByTagName("datacenter");
        for (int i = 0; i < datacenterList.getLength(); i++) {
            Node datacenterNode = datacenterList.item(i);
            Element datacenterElement = (Element) datacenterNode;
            localDatacenters.add(createDatacenter(i, datacenterElement));
        }

    }

    private ContainerDatacenter createDatacenter(int index, Element datacenterElement) throws Exception {

        String arch = datacenterElement.getAttribute("arch");
        String os = datacenterElement.getAttribute("os");
        String vmm = datacenterElement.getAttribute("vmm");
        double costPerBw = Double.parseDouble(datacenterElement.getElementsByTagName("costPerBw").item(0).getTextContent());
        double costPerSec = Double.parseDouble(datacenterElement.getElementsByTagName("costPerSec").item(0).getTextContent());
        double costPerMem = Double.parseDouble(datacenterElement.getElementsByTagName("costPerMem").item(0).getTextContent());
        double costPerStorage = Double.parseDouble(datacenterElement.getElementsByTagName("costPerStorage").item(0).getTextContent());

        List<EdgeContainerHost> hostList=createHosts(datacenterElement);

        String name = "Datacenter_" + Integer.toString(index);
        int vm_index = 1;
        double time_zone = 3.0;         // time zone this resource located
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        ContainerDatacenterCharacteristics characteristics = new ContainerDatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, costPerSec, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        ContainerDatacenter datacenter = null;

        ContainerVmAllocationPolicy vm_policy = getContainerVmAllocationPolicy(hostList,index);
        ContainerAllocationPolicy containerAllocationPolicy = getContainerAllocationPolicy();

        double schedulingInterval = 0;
        String experimentName = null;
        String logAddress = null;
        double VMStartupDelay = 0;
        double ContainerStartupDelay = 0;
        datacenter = new PowerContainerDatacenterCM(name, characteristics, vm_policy,
                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, null, null,
                VMStartupDelay, ContainerStartupDelay);


        return datacenter;
    }

    private VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> hostList, int dataCenterIndex) {
        return new EdgeVmAllocationPolicy_Custom(hostList,dataCenterIndex);
    }

    private List<EdgeContainerHost> createHosts(Element datacenterElement) {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store one or more Machines
        List<EdgeContainerHost> hostList = new ArrayList<EdgeContainerHost>();

        Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
        String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
        int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
        int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
        int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
        int placeTypeIndex = Integer.parseInt(attractiveness);

        NodeList hostNodeList = datacenterElement.getElementsByTagName("host");
        for (int j = 0; j < hostNodeList.getLength(); j++) {
            Node hostNode = hostNodeList.item(j);

            Element hostElement = (Element) hostNode;
            int numOfCores = Integer.parseInt(hostElement.getElementsByTagName("core").item(0).getTextContent());
            double mips = Double.parseDouble(hostElement.getElementsByTagName("mips").item(0).getTextContent());
            int ram = Integer.parseInt(hostElement.getElementsByTagName("ram").item(0).getTextContent());
            long storage = Long.parseLong(hostElement.getElementsByTagName("storage").item(0).getTextContent());
            long bandwidth = SimSettings.getInstance().getWlanBandwidth() / hostNodeList.getLength();

            // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
            //    create a list to store these PEs before creating
            //    a Machine.
            int hostType = j / (int) Math.ceil((double) hostNodeList.getLength() / 3.0D);
            ArrayList<ContainerVmPe> peList = new ArrayList<ContainerVmPe>();

            // 3. Create PEs and add these into the list.
            //for a quad-core machine, a list of 4 PEs is required:
            for(int i=0; i< ConstantsExamples.HOST_PES[hostType]; i++){
                peList.add(new ContainerVmPe(i, new ContainerVmPeProvisionerSimple((double) ConstantsExamples.HOST_MIPS[hostType]))); // need to store Pe id and MIPS Rating
            }

            //4. Create Hosts with its id and list of PEs and add them to the list of machines
            EdgeContainerHost host = new EdgeContainerHost(
                    hostIdCounter,
                    new ContainerVmRamProvisionerSimple(ram),
                    new ContainerVmBwProvisionerSimple(bandwidth), //kbps
                    storage,
                    peList,
                    new ContainerVmSchedulerTimeSharedOverSubscription(peList)
            );

            host.setPlace(new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
            hostList.add(host);
            hostIdCounter++;
        }

        return hostList;
    }

    @Override
    public void terminateDatacenters() {
        for (ContainerDatacenter datacenter : localDatacenters) {
            datacenter.shutdownEntity();
        }

    }

    @Override
    public void createVmList(int brokerId) {

    }

    @Override
    protected ArrayList<ContainerVm> createVmList(int brokerId, int containerVmsNumber) {
        ArrayList<ContainerVm> containerVms = new ArrayList<ContainerVm>();

        for (int i = 0; i < containerVmsNumber; ++i) {
            ArrayList<ContainerPe> peList = new ArrayList<ContainerPe>();
            int vmType = i / (int) Math.ceil((double) containerVmsNumber / 4.0D);
            for (int j = 0; j < ConstantsExamples.VM_PES[vmType]; ++j) {
                peList.add(new ContainerPe(j,
                        new CotainerPeProvisionerSimple((double) ConstantsExamples.VM_MIPS[vmType])));
            }
            containerVms.add(new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId,
                    (double) ConstantsExamples.VM_MIPS[vmType], (float) ConstantsExamples.VM_RAM[vmType],
                    ConstantsExamples.VM_BW, ConstantsExamples.VM_SIZE, "Xen",
                    new ContainerSchedulerTimeSharedOverSubscription(peList),
                    new ContainerRamProvisionerSimple(ConstantsExamples.VM_RAM[vmType]),
                    new ContainerBwProvisionerSimple(ConstantsExamples.VM_BW),
                    peList, ConstantsExamples.SCHEDULING_INTERVAL));


        }

        return containerVms;


    }



    public static List<Container> createContainerList(int brokerId, int containersNumber) {
        ArrayList<Container> containers = new ArrayList<Container>();

        for (int i = 0; i < containersNumber; ++i) {
            int containerType = i / (int) Math.ceil((double) containersNumber / 3.0D);

            containers.add(new PowerContainer(IDs.pollId(EdgeContainer.class), brokerId, (double) ConstantsExamples.CONTAINER_MIPS[containerType], ConstantsExamples.
                    CONTAINER_PES[containerType], ConstantsExamples.CONTAINER_RAM[containerType], ConstantsExamples.CONTAINER_BW, 0L, "Xen",
                    new ContainerCloudletSchedulerDynamicWorkload(ConstantsExamples.CONTAINER_MIPS[containerType], ConstantsExamples.CONTAINER_PES[containerType]), ConstantsExamples.SCHEDULING_INTERVAL));
        }

        return containers;
    }

    @Override
    public double getAvgUtilization() {
        double totalUtilization = 0;
        double vmCounter = 0;

        // for each datacenter...
        for(int i= 0; i<localDatacenters.size(); i++) {
            List<ContainerHost> list = localDatacenters.get(i).getHostList();
            // for each host...
            for (int j=0; j < list.size(); j++) {
                ContainerHost host = list.get(j);
                List<EdgeVM> vmArray = SimManager.getInstance().getEdgeServerManager().getVmList(host.getId());
                //for each vm...
                for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                    totalUtilization += vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
                    vmCounter++;
                }
            }
        }
        return totalUtilization / vmCounter;
    }
}
