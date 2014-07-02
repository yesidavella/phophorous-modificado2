package Grid.Interfaces.Messages;

import Grid.Interfaces.ResourceNode;
import Grid.Interfaces.ServiceNode;
import simbase.Time;


/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class ResourceRegistrationMessage extends GridMessage {

    /**
     * The service node which will get this message.
     */
    private ServiceNode serviceNode;
    /**
     * The resourcenode wich send the request.
     */
    private ResourceNode resource;
    /**
     * Constructor
     * 
     * @param res
     *            the resource being registered
     * @param destination
     *            the service node receiving the registration message
     */
    public ResourceRegistrationMessage(ResourceNode res, ServiceNode destination) {
        super(res.getID() + "-reg-" + destination.getID(), null);
        this.resource = res;
        this.source=res;
        this.serviceNode = destination;
        wavelengthID = -1;
    }

    public ResourceRegistrationMessage(Time generationTime, ServiceNode serviceNode, ResourceNode resource) {
        super(resource.getID() + "-reg-" + serviceNode.getID(),generationTime);
        this.serviceNode = serviceNode;
        this.resource = resource;
        this.source = resource;
        wavelengthID = -1;
    }
     

    public String toString() {
        return "Resource registration message: " + resource.toString();
    }

    public ServiceNode getServiceNode() {
        return serviceNode;
    }

    public ResourceNode getResource() {
        return resource;
    }

    public void setResource(ResourceNode resource) {
        this.resource = resource;
    }

    public void setServiceNode(ServiceNode serviceNode) {
        this.serviceNode = serviceNode;
    }
    
}
