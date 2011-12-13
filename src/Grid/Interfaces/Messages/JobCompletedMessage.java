/*
 * This is message which gets send when the resource had finished a job.
 */
package Grid.Interfaces.Messages;

import Grid.Interfaces.ResourceNode;
import Grid.Jobs.QueuedJob;
import simbase.Time;

/**
 *
 * @author Jens Buysse
 */
public class JobCompletedMessage extends GridMessage {

    /**
     * The time the job finished
     */
    private Time completionTime;
    /**
     * The job's description
     */
    private JobMessage job;
    /**
     * The queuedjob thqt corresponds with this jobmessage.
     */
    private QueuedJob queuedJob;

    /**
     * Returns the time when the job is completed
     * 
     * @return the time when the job is completed
     */
    public Time getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Time completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * Constructor
     * @param id
     *            message id
     * @param msg
     *            the job which is completed
     * @param completionTime
     *            the time when the job is completed
     */
    public JobCompletedMessage(ResourceNode resource, String id, JobMessage msg,
             Time generationTime) {
        super(id, generationTime);
        this.source = resource;
        this.destination = resource;
        job = msg;
    }


    /**
     * Returns the completed job
     * 
     * @return the completed job
     */
    public JobMessage getJob() {
        return job;
    }

    /**
     * Return the corresponding queuedjob.
     * @return The corresponding queuedjob.
     */
    public QueuedJob getQueuedJob() {
        return queuedJob;
    }

    /**
     * Sets the queuedjob corresponding with this jobmessage.
     * @param queuedJob 
     */
    public void setQueuedJob(QueuedJob queuedJob) {
        this.queuedJob = queuedJob;
    }
}
