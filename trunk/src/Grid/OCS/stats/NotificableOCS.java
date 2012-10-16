/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.OCS.stats;

import Grid.Entity;

/**
 *
 * @author Frank
 */
public interface NotificableOCS 
{
    public void notifyNewCreatedOCS(Entity entitySource , Entity entityDestination,  int countInstanceOCS);
    public void notifyTrafficCreatedOCS(Entity entitySource , Entity entityDestination,  double traffic);
    public void notifyJobSentCreatedOCS(Entity entitySource , Entity entityDestination,  long jobsSent);
  
    
    public void clean();
    
}
