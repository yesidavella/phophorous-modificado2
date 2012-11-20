package Grid.Interfaces.Messages;

import Grid.Nodes.PCE;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import simbase.Time;

/**
 *
 * @author AG2 team. Specialized msg to perform multicost operations.
 *
 */
public class MultiCostMessage extends GridMessage {

    /**
     * This message have to be avaluated at its first arrived at a switch, not
     * in the latter switches
     */
    private boolean realMarkovCostEvaluated = false;
    /**
     * The estimated network cost.
     */
    private double estimatedNetworkCost = 0;
    /**
     * The estimated grid cost.
     */
    private double estimatedGridCost = 0;
    /**
     * The PCE of the domain that has estimated the network cost.
     */
    private PCE domainPCE;
    /**
     * Contains a list of ocss than have to be created in the network.
     */
    private ArrayList<OCSRoute> OCS_Instructions;
    /**
     * The real network cost.
     */
    private double realNetworkCost = 0;

    /**
     * Constructor.
     *
     * @param id The id of this message.
     */
    public MultiCostMessage(String id, Time generationTime) {
        super(id, generationTime);
    }

    {
        OCS_Instructions = new ArrayList();
    }

    public boolean isRealMarkovCostEvaluated() {
        return realMarkovCostEvaluated;
    }

    public void setRealMarkovCostEvaluated(boolean realMarkovCostEvaluated) {
        this.realMarkovCostEvaluated = realMarkovCostEvaluated;
    }

    /**
     * Get the estimated network cost.
     *
     * @return estimatedMarkovianCost
     */
    public double getEstimatedNetworkCost() {
        return estimatedNetworkCost;
    }

    /**
     * Set the estimated network cost.
     */
    public void setEstimatedNetworkCost(double estimatedNetworkCost) {
        this.estimatedNetworkCost = estimatedNetworkCost;
    }

    /**
     * Returns the Grid cost estimated by the AG2 resource selector.
     *
     * @return estimatedGridCost.
     */
    public double getEstimatedGridCost() {
        return estimatedGridCost;
    }

    /**
     * Set the Grid cost estimated by the AG2 resource selector.
     *
     * @return estimatedGridCost.
     */
    public void setEstimatedGridCost(double estimatedGridCost) {
        this.estimatedGridCost = estimatedGridCost;
    }

    /**
     * Get the PCE than has estimated the network cost.
     *
     * @return PCE
     */
    public PCE getDomainPCE() {
        return domainPCE;
    }

    /**
     * Set the PCE than has estimated the network cost.
     */
    public void setDomainPCE(PCE domainPCE) {
        this.domainPCE = domainPCE;
    }

    /**
     * Get a list of ocs that have to be created in the network.
     *
     * @return OCS_Instructions
     */
    public ArrayList<OCSRoute> getOCS_Instructions() {
        return OCS_Instructions;
    }

    /**
     * Get the real network cost evalueted.
     *
     * @return realNetworkCost.
     */
    public double getRealNetworkCost() {
        return realNetworkCost;
    }

    /**
     * Set the real network cost evalueted.
     *
     * @param realNetworkCost
     */
    public void setRealNetworkCost(double realNetworkCost) {
        this.realNetworkCost = realNetworkCost;
    }
}
