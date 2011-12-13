/*
 * Implementation of a CPU selector: First Come First Served strategy is used.
 */
package Grid.Nodes.Selector;

import Grid.Interfaces.CPU;
import Grid.Interfaces.CpuSelector;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class FCFSCPUSelector implements CpuSelector {

    public CPU getCPU(List<CPU> cpuSet) {
        Iterator<CPU> it = cpuSet.iterator();
        while (it.hasNext()) {
            CPU cpu = it.next();
            if (!cpu.isBusy()) {
                return cpu;
            }
        }
        return null;
    }
}
