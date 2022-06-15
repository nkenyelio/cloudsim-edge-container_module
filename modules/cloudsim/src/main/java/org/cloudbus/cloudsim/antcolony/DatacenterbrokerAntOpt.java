package org.cloudbus.cloudsim.antcolony;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerCloudlet;
import org.cloudbus.cloudsim.container.core.ContainerDatacenterCharacteristics;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.core.containerCloudSimTags;
import org.cloudbus.cloudsim.container.lists.ContainerList;
import org.cloudbus.cloudsim.container.lists.ContainerVmList;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

public class DatacenterbrokerAntOpt extends SimEntity {

	/** The vm list. */
	protected List<? extends ContainerVm> vmList;

	/** The vms created list. */
	protected List<? extends ContainerVm> vmsCreatedList;
	
	 /** The containers created list.
     */
    protected List<? extends Container> containersCreatedList;


	/** The cloudlet list. */
	protected List<? extends ContainerCloudlet> cloudletList;
	
	 /**
	    * The container list
	     */

	    protected List<? extends Container> containerList;

	/** The cloudlet submitted list. */
	protected List<? extends ContainerCloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends ContainerCloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;
	
	
	/**
     * The containers acks.
     */
    protected int containersAcks;
    /**
     * The number of created containers
     */

    protected int containersCreated;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;
	
	/**
     * The vms to datacenters map.
     */
    protected Map<Integer, Integer> containersToDatacentersMap;


	/** The datacenter characteristics list. */
	protected Map<Integer, ContainerDatacenterCharacteristics> datacenterCharacteristicsList;

	protected double Q;
	protected double alpha;
	protected double beta;
	protected double gamma;
	protected double rho;
	protected int m;
	
	/**
     * The datacenter characteristics list.
     */
    protected double overBookingfactor;

    protected int numberOfCreatedVMs;


	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterbrokerAntOpt(String name, double overBookingfactor, int m, double Q, double alpha, double beta, double gamma, double rho) throws Exception {
		super(name);

		setVmList(new ArrayList<ContainerVm>());
		setContainerList(new ArrayList<Container>());
		setVmsCreatedList(new ArrayList<ContainerVm>());
		setContainersCreatedList(new ArrayList<Container>());
		setCloudletList(new ArrayList<ContainerCloudlet>());
		setCloudletSubmittedList(new ArrayList<ContainerCloudlet>());
		setCloudletReceivedList(new ArrayList<ContainerCloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setContainersAcks(0);
        setContainersCreated(0);
		setVmsDestroyed(0);
		setOverBookingfactor(overBookingfactor);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setContainersToDatacentersMap(new HashMap<Integer, Integer>());
        setDatacenterCharacteristicsList(new HashMap<Integer, ContainerDatacenterCharacteristics>());
		//setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
        setNumberOfCreatedVMs(0);

		this.m = m;
		this.Q = Q;
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		this.rho = rho;
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends ContainerVm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends ContainerCloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}
	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
     *
     * @param cloudletId ID of the cloudlet being bount to a vm
     * @param containerId       the vm id
     * @pre cloudletId > 0
     * @pre id > 0
     * @post $none
     */
    public void bindCloudletToContainer(int cloudletId, int containerId) {
        CloudletList.getById(getCloudletList(), cloudletId).setContainerId(containerId);
    }

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
			try {
				processVmCreate(ev);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				break;
			// New VM Creation answer
            case containerCloudSimTags.VM_NEW_CREATE:
                processNewVmCreate(ev);
                break;
            // A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			case containerCloudSimTags.CONTAINER_CREATE_ACK:
			try {
				processContainerCreate(ev);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
                break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		ContainerDatacenterCharacteristics characteristics = (ContainerDatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, ContainerDatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}
	
	 public void processContainerCreate(SimEvent ev) throws Exception {
	        int[] data = (int[]) ev.getData();
	        int vmId = data[0];
	        int containerId = data[1];
	        int result = data[2];

	        if (result == CloudSimTags.TRUE) {
	            if(vmId ==-1){
	                Log.printConcatLine("Error : Where is the VM");}
	            else{
	            getContainersToVmsMap().put(containerId, vmId);
	            getContainersCreatedList().add(ContainerList.getById(getContainerList(), containerId));

//	            ContainerVm p= ContainerVmList.getById(getVmsCreatedList(), vmId);
	            int hostId = ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId();
	            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": The Container #", containerId,
	                     ", is created on Vm #",vmId
	                    , ", On Host#", hostId);
	            setContainersCreated(getContainersCreated()+1);}
	        } else {
	            //Container container = ContainerList.getById(getContainerList(), containerId);
	            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Failed Creation of Container #", containerId);
	        }

	        incrementContainersAcks();
	        if (getContainersAcks() == getContainerList().size()) {
	            //Log.print(getContainersCreatedList().size() + "vs asli"+getContainerList().size());
	            submitCloudlets();
	            getContainerList().clear();
	        }

	    }
	
	
	
	protected void processNewVmCreate(SimEvent ev) {
        Map<String, Object> map = (Map<String, Object>) ev.getData();
        int datacenterId = (int) map.get("datacenterID");
        int result = (int) map.get("result");
        ContainerVm containerVm = (ContainerVm) map.get("vm");
        int vmId = containerVm.getId();
        if (result == CloudSimTags.TRUE) {
            getVmList().add(containerVm);
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(containerVm);
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
                    " has been created in Datacenter #", datacenterId, ", Host #",
                    ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Creation of VM #", vmId,
                    " failed in Datacenter #", datacenterId);
        }}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @throws FileNotFoundException 
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) throws FileNotFoundException {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(ContainerVmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ ContainerVmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add((ContainerCloudlet) cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");

		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (ContainerVm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * @throws FileNotFoundException 
	 * 
	 * @pre $none
	 * @post $none
	 */
	// protected void submitCloudlets() {
	// 	int vmIndex = 0;
	// 	for (Cloudlet cloudlet : getCloudletList()) {
	// 		Vm vm;
	// 		// if user didn't bind this cloudlet and it has not been executed yet
	// 		if (cloudlet.getVmId() == -1) {
	// 			vm = getVmsCreatedList().get(vmIndex);
	// 		} else { // submit to the specific vm
	// 			vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
	// 			if (vm == null) { // vm was not created
	// 				Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
	// 						+ cloudlet.getCloudletId() + ": bount VM not available");
	// 				continue;
	// 			}
	// 		}

	// 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
	// 				+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
	// 		cloudlet.setVmId(vm.getId());
	// 		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
	// 		cloudletsSubmitted++;
	// 		vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
	// 		getCloudletSubmittedList().add(cloudlet);
	// 	}

	// 	// remove submitted cloudlets from waiting list
	// 	for (Cloudlet cloudlet : getCloudletSubmittedList()) {
	// 		getCloudletList().remove(cloudlet);
	// 	}
	// }

	protected void submitCloudlets() throws FileNotFoundException {
		// int vmIndex = 0;
		List<ContainerCloudlet> clList = getCloudletList();
		List<ContainerVm> vm_list = getVmsCreatedList();
		// Random r = new Random();
		// int m = (vm_list.size()/4 + r.nextInt(vm_list.size()/2+1))%vm_list.size();

		AntColonyOptimization lbaco1 = new AntColonyOptimization(m,Q,alpha,beta,gamma,rho);
		Map<Integer,Integer> allocated = lbaco1.implement(clList,vm_list,100);

		for (int i=0;i<clList.size();i++) {
			ContainerCloudlet cloudlet = clList.get(i);
			ContainerVm vm = vm_list.get(allocated.get(i));
			// if user didn't bind this cloudlet and it has not been executed yet
			// if (cloudlet.getVmId() == -1) {
			// 	vm = getVmsCreatedList().get(vmIndex);
			// } else { // submit to the specific vm
			// 	vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			// 	if (vm == null) { // vm was not created
			// 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
			// 				+ cloudlet.getCloudletId() + ": bount VM not available");
			// 		continue;
			// 	}
			// }

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			// vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (ContainerCloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (ContainerVm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

    /**
    *
    */
   protected void submitContainers(){
       List<Container> successfullySubmitted = new ArrayList<>();
       int i = 0;
       for(Container container:getContainerList()) {
           ContainerCloudlet cloudlet = getCloudletList().get(i);
               //Log.printLine("Containers Created" + getContainersCreated());

               if (cloudlet.getUtilizationModelCpu() instanceof UtilizationModelPlanetLabInMemory) {
                   UtilizationModelPlanetLabInMemory temp = (UtilizationModelPlanetLabInMemory) cloudlet.getUtilizationModelCpu();
                   double[] cloudletUsage = temp.getData();
                   Percentile percentile = new Percentile();
                   double percentileUsage = percentile.evaluate(cloudletUsage, getOverBookingfactor());
                   //Log.printLine("Container Index" + containerIndex);
                   double newmips = percentileUsage * container.getMips();
//                   double newmips = percentileUsage * container.getMips();
//                   double maxUsage = Doubles.max(cloudletUsage);
//                   double newmips = maxUsage * container.getMips();
                   container.setWorkloadMips(newmips);
//                   bindCloudletToContainer(cloudlet.getCloudletId(), container.getId());
                   cloudlet.setContainerId(container.getId());
                   if(cloudlet.getContainerId() != container.getId()){
//                       Log.printConcatLine("Binding Cloudlet: ", cloudlet.getCloudletId(), "To Container: ",container.getId() , "Now it is", cloudlet.getContainerId());
                   }

               }
           i++;

       }

       for(Container container:getContainerList()){
           successfullySubmitted.add(container);

       }
       sendNow(getDatacenterIdsList().get(0), containerCloudSimTags.CONTAINER_SUBMIT, successfullySubmitted);

//       List<Container> successfullySubmitted = new ArrayList<>();
//       for (Container container : getContainerList()) {
//
//           if (!Log.isDisabled()) {
//               Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Sending container ",
//                       container.getId(), " to Datacenter");
//           }
//           cloudletsSubmitted++;
//           vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
//           getCloudletSubmittedList().add(cloudlet);
//           successfullySubmitted.add(cloudlet);
//       }

       // remove submitted cloudlets from waiting list
   }
	
	
	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ContainerVm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param arrayList the new vm list
	 */
	protected <T extends ContainerVm> void setVmList(ArrayList<ContainerVm> arrayList) {
		this.vmList = arrayList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ContainerCloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends ContainerCloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ContainerCloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends ContainerCloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ContainerCloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends ContainerCloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends ContainerVm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends ContainerVm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, ContainerDatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	public void setContainerList(List<? extends Container> containerList) {
        this.containerList = containerList;
    }
	
	public void setContainersCreatedList(ArrayList<Container> arrayList) {
        this.containersCreatedList = arrayList;
    }
	public void setContainersAcks(int containersAcks) {
        this.containersAcks = containersAcks;
    }
	
	public void setContainersCreated(int containersCreated) {
        this.containersCreated = containersCreated;
    }
	
	public void setOverBookingfactor(double overBookingfactor) {
        this.overBookingfactor = overBookingfactor;
    }
	
	public void setContainersToDatacentersMap(Map<Integer, Integer> containersToDatacentersMap) {
        this.containersToDatacentersMap = containersToDatacentersMap;
    }
	
	/**
     * Sets the datacenter characteristics list.
     *
     * @param hashMap the datacenter characteristics list
     */
    protected void setDatacenterCharacteristicsList(
            HashMap<Integer, ContainerDatacenterCharacteristics> hashMap) {
        this.datacenterCharacteristicsList = hashMap;
    }
    
    public void setNumberOfCreatedVMs(int numberOfCreatedVMs) {
        this.numberOfCreatedVMs = numberOfCreatedVMs;
    }
    
    public Map<Integer, Integer> getContainersToVmsMap() {
        return containersToDatacentersMap;
    }
    
    public <T extends Container> List<T> getContainersCreatedList() {
        return (List<T>) containersCreatedList;
    }
    
    public <T extends Container> List<T> getContainerList() {

        return (List<T>) containerList;
    }
    
    public int getContainersCreated() {
        return containersCreated;
    }
    
    /**
     * Increment vms acks.
     */
    protected void incrementContainersAcks() {
        setContainersAcks(getContainersAcks()+1);
    }
    
    public int getContainersAcks() {
        return containersAcks;
    }
    
    public double getOverBookingfactor() {
        return overBookingfactor;
    }
    
    public void submitContainerList(List<? extends Container> list) {
        getContainerList().addAll(list);
    }



}