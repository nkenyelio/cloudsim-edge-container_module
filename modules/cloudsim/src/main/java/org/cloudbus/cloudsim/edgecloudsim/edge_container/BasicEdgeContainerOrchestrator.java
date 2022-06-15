package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.Task;
import org.cloudbus.cloudsim.edgecloudsim.edge_container.EdgeContainerVM;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeVM;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;
import org.cloudbus.cloudsim.container.core.ContainerDatacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.List;

public class BasicEdgeContainerOrchestrator extends EdgeOrchestrator {

    private int numberOfHost; //used by load balancer
    private int lastSelectedHostIndex; //used by load balancer
    private int[] lastSelectedVmIndexes; //used by each host individually

    public BasicEdgeContainerOrchestrator(String _policy, String _simScenario) {
        super(_policy, _simScenario);
    }

    @Override
    public void initialize() {
        numberOfHost= SimSettings.getInstance().getNumOfEdgeHosts();

        lastSelectedHostIndex = -1;
        lastSelectedVmIndexes = new int[numberOfHost];
        for(int i=0; i<numberOfHost; i++)
            lastSelectedVmIndexes[i] = -1;
    }

    @Override
    public int getDeviceToOffload(Task task) {
        int result = SimSettings.GENERIC_EDGE_DEVICE_ID;
        if(!simScenario.equals("SINGLE_TIER")){
            //decide to use cloud or Edge VM
            int CloudVmPicker = SimUtils.getRandomNumber(0, 100);

            if(CloudVmPicker <= SimSettings.getInstance().getTaskLookUpTable()[task.getTaskType()][1])
                result = SimSettings.CLOUD_DATACENTER_ID;
            else
                result = SimSettings.GENERIC_EDGE_DEVICE_ID;
        }

        return result;
    }

    @Override
    public EdgeContainerVM getVmToOffload(Task task, int deviceId) {
        EdgeContainerVM selectedVM = null;

        if(deviceId == SimSettings.CLOUD_DATACENTER_ID){
            //Select VM on cloud devices via Least Loaded algorithm!
            double selectedVmCapacity = 0; //start with min value
            List<ContainerDatacenter> list = SimManager.getInstance().getEdgeServerVMManager().getDatacenterList();
            for (int hostIndex=0; hostIndex < list.size(); hostIndex++) {
                List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(hostIndex);
                for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                    double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                    double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                    if(requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity){
                        selectedVM = vmArray.get(vmIndex);
                        selectedVmCapacity = targetVmCapacity;
                    }
                }
            }
        }
        else if(simScenario.equals("TWO_TIER_WITH_EO"))
            selectedVM = selectVmOnLoadBalancer(task);
        else
            selectedVM = selectVmOnHost(task);

        return selectedVM;
    }

    public EdgeContainerVM selectVmOnHost(Task task){
        EdgeContainerVM selectedVM = null;

        Location deviceLocation = SimManager.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(), CloudSim.clock());
        //in our scenario, serving wlan ID is equal to the host id
        //because there is only one host in one place
        int relatedHostId=deviceLocation.getServingWlanId();
        List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(relatedHostId);

        if(policy.equalsIgnoreCase("RANDOM_FIT")){
            int randomIndex = SimUtils.getRandomNumber(0, vmArray.size()-1);
            double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(randomIndex).getVmType());
            double targetVmCapacity = (double)100 - vmArray.get(randomIndex).getContainerScheduler().getPeCapacity();
            if(requiredCapacity <= targetVmCapacity)
                selectedVM = vmArray.get(randomIndex);
        }
        else if(policy.equalsIgnoreCase("WORST_FIT")){
            double selectedVmCapacity = 0; //start with min value
            for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                if(requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity){
                    selectedVM = vmArray.get(vmIndex);
                    selectedVmCapacity = targetVmCapacity;
                }
            }
        }
        else if(policy.equalsIgnoreCase("BEST_FIT")){
            double selectedVmCapacity = 101; //start with max value
            for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                if(requiredCapacity <= targetVmCapacity && targetVmCapacity < selectedVmCapacity){
                    selectedVM = vmArray.get(vmIndex);
                    selectedVmCapacity = targetVmCapacity;
                }
            }
        }
        else if(policy.equalsIgnoreCase("FIRST_FIT")){
            for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                if(requiredCapacity <= targetVmCapacity){
                    selectedVM = vmArray.get(vmIndex);
                    break;
                }
            }
        }
        else if(policy.equalsIgnoreCase("NEXT_FIT")){
            int tries = 0;
            while(tries < vmArray.size()){
                lastSelectedVmIndexes[relatedHostId] = (lastSelectedVmIndexes[relatedHostId]+1) % vmArray.size();
                double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(lastSelectedVmIndexes[relatedHostId]).getVmType());
                double targetVmCapacity = (double)100 - vmArray.get(lastSelectedVmIndexes[relatedHostId]).getContainerScheduler().getPeCapacity();
                if(requiredCapacity <= targetVmCapacity){
                    selectedVM = vmArray.get(lastSelectedVmIndexes[relatedHostId]);
                    break;
                }
                tries++;
            }
        }

        return selectedVM;
    }

    public EdgeContainerVM selectVmOnLoadBalancer(Task task){
        EdgeContainerVM selectedVM = null;

        if(policy.equalsIgnoreCase("RANDOM_FIT")){
            int randomHostIndex = SimUtils.getRandomNumber(0, numberOfHost-1);
            List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(randomHostIndex);
            int randomIndex = SimUtils.getRandomNumber(0, vmArray.size()-1);

            double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(randomIndex).getVmType());
            double targetVmCapacity = (double)100 - vmArray.get(randomIndex).getContainerScheduler().getPeCapacity();
            if(requiredCapacity <= targetVmCapacity)
                selectedVM = vmArray.get(randomIndex);
        }
        else if(policy.equalsIgnoreCase("WORST_FIT")){
            double selectedVmCapacity = 0; //start with min value
            for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
                List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(hostIndex);
                for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                    double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                    double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                    if(requiredCapacity <= targetVmCapacity && targetVmCapacity > selectedVmCapacity){
                        selectedVM = vmArray.get(vmIndex);
                        selectedVmCapacity = targetVmCapacity;
                    }
                }
            }
        }
        else if(policy.equalsIgnoreCase("BEST_FIT")){
            double selectedVmCapacity = 101; //start with max value
            for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
                List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(hostIndex);
                for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                    double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                    double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                    if(requiredCapacity <= targetVmCapacity && targetVmCapacity < selectedVmCapacity){
                        selectedVM = vmArray.get(vmIndex);
                        selectedVmCapacity = targetVmCapacity;
                    }
                }
            }
        }
        else if(policy.equalsIgnoreCase("FIRST_FIT")){
            for(int hostIndex=0; hostIndex<numberOfHost; hostIndex++){
                List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(hostIndex);
                for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
                    double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(vmIndex).getVmType());
                    double targetVmCapacity = (double)100 - vmArray.get(vmIndex).getContainerScheduler().getPeCapacity();
                    if(requiredCapacity <= targetVmCapacity){
                        selectedVM = vmArray.get(vmIndex);
                        break;
                    }
                }
            }
        }
        else if(policy.equalsIgnoreCase("NEXT_FIT")){
            int hostCheckCounter = 0;
            while(selectedVM == null && hostCheckCounter < numberOfHost){
                int tries = 0;
                lastSelectedHostIndex = (lastSelectedHostIndex+1) % numberOfHost;

                List<EdgeContainerVM> vmArray = SimManager.getInstance().getEdgeServerVMManager().getVmList(lastSelectedHostIndex);
                while(tries < vmArray.size()){
                    lastSelectedVmIndexes[lastSelectedHostIndex] = (lastSelectedVmIndexes[lastSelectedHostIndex]+1) % vmArray.size();
                    double requiredCapacity = ((CpuUtilizationModel_Custom)task.getUtilizationModelCpu()).predictUtilization(vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]).getVmType());
                    double targetVmCapacity = (double)100 - vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]).getContainerScheduler().getPeCapacity();
                    if(requiredCapacity <= targetVmCapacity){
                        selectedVM = vmArray.get(lastSelectedVmIndexes[lastSelectedHostIndex]);
                        break;
                    }
                    tries++;
                }

                hostCheckCounter++;
            }
        }

        return selectedVM;
    }

    @Override
    public void processEvent(SimEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void shutdownEntity() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startEntity() {
        // TODO Auto-generated method stub

    }
}
