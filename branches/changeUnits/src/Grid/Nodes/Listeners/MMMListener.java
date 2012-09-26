/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes.Listeners;

import Grid.Interfaces.ResourceNode;
import Grid.Utilities.SampleAverage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class MMMListener implements ChangeListener {

    /**
     * The average system population
     */
    private SampleAverage systemPopulation;
    /**
     * The list with the resources
     */
    private ResourceNode resource;

    public MMMListener(ResourceNode resource) {
        systemPopulation = new SampleAverage();
        this.resource = resource;
    }

    /**
     * StateChanged method.
     * @param e
     */
    public void stateChanged(ChangeEvent e) {

        systemPopulation.addSample(resource.getNrOfJobsInQueue()+
                (resource.getCpuCount() - resource.getNrOfFreeCpus()));

    }

    public ResourceNode getResoource() {
        return resource;
    }

    public SampleAverage getSystemPopulation() {
        return systemPopulation;
    }
}
