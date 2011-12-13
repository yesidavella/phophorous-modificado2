/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Routing;

import Grid.Entity;

/**
 *
 * @author Jens Buysse
 */
public class GridEdge {

    private GridVertex from;
    private GridVertex to;

    public GridEdge(GridVertex from, GridVertex to) {
        this.from = from;
        this.to = to;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(from);
        buffer.append("-");
        buffer.append(to);
        return buffer.toString();

    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString());
    }

    public int compareTo(Object o) {
        return this.toString().compareTo(o.toString());
    }

    public GridVertex getFrom() {
        return from;
    }

    public void setFrom(GridVertex from) {
        this.from = from;
    }

    public GridVertex getTo() {
        return to;
    }

    public void setTo(GridVertex to) {
        this.to = to;
    }
}
