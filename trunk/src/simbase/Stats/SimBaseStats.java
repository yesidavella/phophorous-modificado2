package simbase.Stats;

/**
 * A list of possible statistics that can be gathered from simulation.
 * 
 * @author Joachim Vermeir
 * @version 1.0
 */
public interface SimBaseStats
{
	public enum Stat
	{
            CLIENT_REQ_SENT,
            CLIENT_REQ_ACK_RECEIVED,
            CLIENT_JOB_SENT,
            CLIENT_RESULTS_RECEIVED,
            CLIENT_SENDING_FAILED,
            CLIENT_RESOURCES_BUSY_MSG,
            
            SERVICENODE_REQ_RECIEVED,
            SERVICENODE_REQ_ACK_SENT,
            SERVICENODE_REGISTRATION_RECEIVED,
            SERVICENODE_SENDING_FAILED,
            SERVICENODE_NO_FREE_RESOURCE,
            
            RESOURCE_JOB_RECEIVED,
            RESOURCE_RESULTS_SENT,
            RESOURCE_BUSY_TIME,
            RESOURCE_FAIL_NO_FREE_PLACE,
            RESOURCE_SENDING_FAILED,
            RESOURCE_REGISTRATION_SENT,
            
            SWITCH_MESSAGE_DROPPED,            
            SWITCH_JOBMESSAGE_DROPPED,
            SWITCH_JOBRESULTMESSAGE_DROPPED,
            SWITCH_MESSAGE_SWITCHED,
            SWITCH_JOBMESSAGE_SWITCHED,
            SWITCH_JOBRESULTMESSAGE_SWITCHED,
            SWITCH_REQ_MESSAGE_SWITCHED,
            SWITCH_REQ_MESSAGE_DROPPED,
            
            
            OCS_CIRCUIT_SET_UP,
            OCS_PART_OF_CIRCUIT_SET_UP,
            OCS_CIRCUIT_CONFLICT,
            OCS_CIRCUIT_TEAR_DOWN,
            OCS_CIRCUIT_PART_OF_CONFLICT,
            OCS_CIRCUIT_SETUP_DID_NOT_WORK
            
	}
}
