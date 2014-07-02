/**
 * Used to keep track of the route a message has followed.
 * 
 * @version 2.0
 */
package Grid;

import java.util.ArrayList;

/**
 *
 * @author Jens Buysse
 */
public class Route extends ArrayList<Entity> {

    /**
     * The source, or beginning of the path
     */
    protected Entity source = null;
    /**
     * The destination, or end of the path
     */
    protected Entity destination = null;
    /**
     * Is this route closed (i.e. is there a complete path from source to
     * destination)
     */
    protected boolean closed = false;
    /**
     * The total delay of the route, the time the message took to take this
     * route. TODO: make sure the totaldely is computer correct !
     */
    protected double totalDelay = 0;

    /**
     * Constructor.
     * 
     * @param source
     *            beginning of the path
     * @param destination
     *            end of the path
     */
    public Route(Entity source, Entity destination) {
        super();
        this.source = source;
        this.destination = destination;
        add(source);
    }

    /**
     * Adds a hop to the path, after the current hop; checks if this closes the
     * path
     * 
     * @param hop
     *            the next hop on the path
     */
    public void addHop(Entity hop) {
        add(hop);
        closed = (hop.equals(destination));
    }

    /**
     * Returns the previous hop on the path
     * 
     * @return the previous hop on the path
     */
    public Entity getLastHopAddedToPath() {
        if (!isEmpty()) {
            return (Entity) (get(size() - 1));
        }
        return null;
    }

    /**
     * Returns the total hop count of the path, inlucding source and destination.
     * If the path is empty, 
     * 
     * @return the total hop count of the path
     */
    public int getHopCount() {
        return size();
    }

    /**
     * Returns the total time length of the path.
     * 
     * @return the total length of the path
     */
    public double getTotalDelay() {
        return totalDelay;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Entity getDestination() {
        return destination;
    }

    public void setDestination(Entity destination) {
        this.destination = destination;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Entity source) {
        this.source = source;
    }
}
