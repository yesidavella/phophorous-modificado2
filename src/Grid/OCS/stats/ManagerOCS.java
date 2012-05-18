/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.OCS.stats;

import Grid.Entity;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.OCS.OCSRoute;
import java.util.HashMap;

/**
 *
 * @author Frank
 */
public class ManagerOCS 
{
    private static  ManagerOCS managerOCS;
    private HashMap<OCSRequestMessage, InstanceOCS> mapInstanceOCS;
    private HashMap<SourceDestination, SumaryOCS> mapSumaryOCS;
    
    public static ManagerOCS getInstance()
    {
        
        if(managerOCS==null)
        {
           managerOCS  = new ManagerOCS();
        }
        
        return  managerOCS;
    }
    
    private ManagerOCS()
    {
        mapInstanceOCS = new HashMap<OCSRequestMessage, InstanceOCS>();
        mapSumaryOCS = new HashMap<SourceDestination, SumaryOCS>();
    }
    
    public void addInstaceOCS( OCSRequestMessage ocsRequestMessage)
    {
        if(!mapInstanceOCS.containsKey(ocsRequestMessage))
        {
            InstanceOCS instanceOCS = new InstanceOCS();
            instanceOCS.setWavelengthID(ocsRequestMessage.getWavelengthID());
            instanceOCS.setStartTime(ocsRequestMessage.getGenerationTime().getTime());            
            instanceOCS.setRoute( ocsRequestMessage.getOCSRoute());            
            mapInstanceOCS.put(ocsRequestMessage, instanceOCS);     
            
            System.out.println("New Instance OCS REG - Source "+
                        ocsRequestMessage.getSource() +" Destination "+ocsRequestMessage.getDestination()
                    +" Time "+ ocsRequestMessage.getGenerationTime().getTime());
            
            SourceDestination sourceDestination = 
                    new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());
            if(!mapSumaryOCS.containsKey(sourceDestination))
            {
                SumaryOCS sumaryOCS = new SumaryOCS();
                sumaryOCS.setCountRequestOCS(1);
                mapSumaryOCS.put(sourceDestination, sumaryOCS);     
                System.out.println("New Sumary OCS REG - Source "+
                        ocsRequestMessage.getSource() +" Destination "+ocsRequestMessage.getDestination());
            }
            else
            {
                SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);
                sumaryOCS.setCountRequestOCS(sumaryOCS.getCountRequestOCS()+1);             
                 System.out.println("OLD Sumary OCS REG - Source "+
                        ocsRequestMessage.getSource() +" Destination "+ocsRequestMessage.getDestination()+" Count OCS "+sumaryOCS.getCountRequestOCS() );
                
            }
                
        }
        
    }
    
    
    
    
    public static class  InstanceOCS
    {        
         private double startTime;
         private boolean direct;
         private int  WavelengthID;
         private OCSRoute route;

        public boolean isDirect() {
            return direct;
        }

        public void setDirect(boolean direct) {
            this.direct = direct;
        }

        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public int getWavelengthID() {
            return WavelengthID;
        }

        public void setWavelengthID(int WavelengthID) {
            this.WavelengthID = WavelengthID;
        }

        public OCSRoute getRoute() {
            return route;
        }

        public void setRoute(OCSRoute route) {
            this.route = route;
        }
        
        
        
        
         
    }
   
    
     public static class  SumaryOCS
     {                
         private double countRequestOCS;
         private boolean direct;

        public double getCountRequestOCS() {
            return countRequestOCS;
        }

        public void setCountRequestOCS(double countRequestOCS) {
            this.countRequestOCS = countRequestOCS;
        }

        public boolean isDirect() {
            return direct;
        }

        public void setDirect(boolean direct) {
            this.direct = direct;
        }
         
     }
     
      public static class SourceDestination
    {
         private Entity entitySource;
         private Entity entityDestination;

        @Override
        public boolean equals(Object obj) 
        {
            if(obj instanceof SourceDestination)
            {
                SourceDestination sourceDestination = (SourceDestination)obj;
                if(sourceDestination.getEntitySource().getId().equals(entitySource.getId())
                        &&  sourceDestination.getEntityDestination().getId().equals(entityDestination.getId()) )
                {
                    return true;
                }
                
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.entitySource.getId() != null ? this.entitySource.getId().hashCode() : 0);
            hash = 71 * hash + (this.entityDestination.getId()  != null ? this.entityDestination.getId().hashCode() : 0);
            return hash;
        }

       
         
         
         

        public SourceDestination(Entity entitySource, Entity entityDestination) {
            this.entitySource = entitySource;
            this.entityDestination = entityDestination;
        }

        public Entity getEntityDestination() {
            return entityDestination;
        }

        public Entity getEntitySource() {
            return entitySource;
        }       
        
    }
    
}
