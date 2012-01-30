/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Grid.Nodes.Queueing;

import Grid.Jobs.QueuedJob;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class TimeComparator implements Comparator, Serializable{

    public int compare(Object o1, Object o2) {
        QueuedJob job1 = (QueuedJob)o1;
        QueuedJob job2 = (QueuedJob)o2;
        return job1.getQueueTime().compareTo(job2.getQueueTime());
    }
    
    

}
