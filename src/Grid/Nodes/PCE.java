/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.GridSimulator;
import Grid.Interfaces.ClientNode;
import Grid.Interfaces.ResourceNode;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.Routing.Routing;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PCE extends HybridSwitchImpl
{
    
    private GridSimulator simulator; 
    private Routing routing;
    public PCE(String id, GridSimulator simulator)
    {
      
       super(id, simulator);
       this.simulator = simulator;
       routing = simulator.getRouting();
       
        
    }        
    
    public Map<ResourceNode ,Double>  getMarkovCostList(ClientNode clientNode,  List<ResourceNode> resourceNodes)
    {
        
        Map<ResourceNode ,Double> map = new HashMap<ResourceNode ,Double>(); 
      
        
        
        double Wtotal;      
        double Wb;
        double Wsign;
        double Wsw;        
        double Ccap =1; // Coeficciente de costo de ancho de banda por unidad de capacidad.
        double W; //Capacidad de cada lambda.
        double Hf; //Numero de saltos 
        double T; // Tiempo de duracion de la solicitud. 
        
//        double 
        
      
               
        for(ResourceNode resourceNode: resourceNodes)
        {
//            routing.findOCSRoute(resourceNode, resourceNode)
            
            
        }       

        
        
        return null; 
    }
     
}
