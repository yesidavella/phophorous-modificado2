/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Sender.Hybrid.Parallel;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Sender.Sender;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public abstract class AbstractHybridSender extends Sender {

    /**
     * The OCS Sender
     */
    protected Sender obsSender;
    /**
     * The OBS Sender
     */
    protected Sender ocsSender;


    /**
     * Constructor
     * @param owner The owner of this sender.
     * @param simulator The simulator this sender belongs to.
     */
    public AbstractHybridSender(Entity owner, GridSimulator simulator) {
        super(owner, simulator);
    }

    public Sender getObsSender() {
        return obsSender;
    }

    public void setObsSender(Sender obsSender) {
        this.obsSender = obsSender;
    }

    public Sender getOcsSender() {
        return ocsSender;
    }

    public void setOcsSender(Sender ocsSender) {
        this.ocsSender = ocsSender;
    }


    
    
}
