/*
 * This is a framework for selecting a cpu from a cpu set, following a specified
 * algorithm.
 */

package Grid.Interfaces;

import java.util.List;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public interface CpuSelector {

    /**
     * Return a cpu for executing a job.
     * @return
     */
    public CPU getCPU(List<CPU> cpuSet);
}
