/*
 * FCFS queue of a resource.
 */
package Grid.Nodes.Queueing;

import Grid.Jobs.QueuedJob;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeSet;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class ResourceQueue implements Queue {

    /**
     * TreeSet, containing the queued jobs.
     */
    private TreeSet queue;
    /**
     * The comparator which takes care of the ordening of the set.
     */
    private Comparator comparator;

    /**
     * Constructor : makes a Resource queue with given comparator object
     * @param comparator The comparator which defines the ordening of the queue.
     */
    public ResourceQueue(int maxQueueSize,Comparator comparator) {
        this.comparator = comparator;
        queue = new TreeSet(comparator);
    }

    /**
     * Ensures that this collection contains the specified element (optional operation).
     * Returns true if this collection changed as a result of the call. 
     * (Returns false if this collection does not permit duplicates 
     * and already contains the specified element.)
     * @param e element whose presence in this collection is to be ensured.
     * @return true if this collection changed as a result of the call
     */
    public boolean add(Object e) {
        return queue.add(e);
    }

    /**
     * Retrieves, but does not remove, the head of this queue.
     * @return the head of this queue.
     */
    public Object element() {
        return queue.first();
    }

    /**
     * Inserts the specified element into this queue, if possible.
     * When using queues that may impose insertion restrictions 
     * (for example capacity bounds), method offer is generally preferable 
     * to method Collection.add(E), which can fail to insert an element 
     * only by throwing an exception.
     * @param e the element to insert.
     * @return true if it was possible to add the element to this queue, else false
     */
    public boolean offer(Object e) {
        return queue.add(e);
    }

    /**
     * Retrieves, but does not remove, the head of this queue, 
     * returning null if this queue is empty.
     * @return The head of the queue
     */
    public Object peek() {
        return queue.first();
    }

    /**
     * Retrieves and removes the head of this queue, or null  
     * if this queue is empty.
     * @return the head of this queue, or null if this queue is empty.
     */
    public Object poll() {
        if (!queue.isEmpty()) {
            QueuedJob job = (QueuedJob) queue.first();
            queue.remove(job);
            return job;
        } else {
            return null;
        }
    }

    /**
     * Retrieves and removes the head of this queue. 
     * This method differs from the poll method in that 
     * it throws an exception if this queue is empty.
     * @return the head of this queue.
     */
    public Object remove() {
        return queue.remove(queue.first());
    }

    /**
     * Adds all of the elements in the specified collection to this collection 
     * (optional operation). The behavior of this operation is undefined if 
     * the specified collection is modified while the operation is in progress. 
     * (This implies that the behavior of this call is undefined if the 
     * specified collection is this collection, and this collection is nonempty.)
     * @param c elements to be inserted into this collection.
     * @return true if this collection changed as a result of the call
     */
    public boolean addAll(Collection c) {
        return queue.addAll(c);
    }

    /**
     * Removes all of the elements from this collection (optional operation). 
     * This collection will be empty after this method returns unless it 
     * throws an exception.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * Returns true if this collection contains the specified element. 
     * More formally, returns true if and only if this collection contains 
     * at least one element e such that (o==null ? e==null : o.equals(e)).
     * @param o element whose presence in this collection is to be tested.
     * @return true if this collection contains the specified element
     */
    public boolean contains(Object o) {
        return queue.contains(o);
    }

    /**
     * Returns true if this collection contains all of the elements in the specified collection.
     * @param c collection to be checked for containment in this collection.
     * @return true if this collection contains all of the elements in the specified collection
     */
    public boolean containsAll(Collection c) {
        return queue.containsAll(c);
    }

    /**
     * Returns true if this collection contains no elements.
     * @return true if this collection contains no elements
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Iterator iterator() {
        return queue.iterator();
    }

    public boolean remove(Object o) {
        return queue.remove(o);
    }

    public boolean removeAll(Collection c) {
        return queue.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return queue.removeAll(c);
    }

    /**
     * Returns the number of elements in this collection. 
     * If this collection contains more than Integer.MAX_VALUE elements, 
     * returns Integer.MAX_VALUE.
     * @return the number of elements in this collection
     */
    public int size() {
        return queue.size();
    }

    public Object[] toArray() {
        return queue.toArray();
    }

    public Object[] toArray(Object[] a) {
        return queue.toArray(a);
    }
}
