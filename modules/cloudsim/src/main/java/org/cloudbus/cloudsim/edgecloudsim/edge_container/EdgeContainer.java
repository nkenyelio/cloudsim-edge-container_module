package org.cloudbus.cloudsim.edgecloudsim.edge_container;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletScheduler;

public class EdgeContainer extends Container {

    public EdgeContainer(
            int id,
            int userId,
            double mips,
            int numberOfPes,
            int ram,
            long bw,
            long size,
            String containerManager,
            ContainerCloudletScheduler containerCloudletScheduler, double schedulingInterval) {
        super(id, userId, mips,numberOfPes, ram, bw, size, containerManager, containerCloudletScheduler, schedulingInterval );
    }
}
