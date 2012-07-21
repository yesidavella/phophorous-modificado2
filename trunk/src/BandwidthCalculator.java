

public class BandwidthCalculator {
    /**
     * La variable A nunca puede ser menor a 1;
     */
    private final int A=3; 
    private static BandwidthCalculator bandwidhtCalculator = null;
    
    public static BandwidthCalculator getInstance(){
        
        if(bandwidhtCalculator == null){
            bandwidhtCalculator = new BandwidthCalculator();
        }
        
        return bandwidhtCalculator;
    }
    
    
    /**
     * Sugiere el ancho de banda a asignar teniendo en cuenta los sigs parametros.
     * El numero mas grande q retorna es a lo sumo es availableBandwith o si no puede
     * hacer el calculo retorna -1 o lanza una exepcion de argumento ilegal si no
     * se envia la prioridad del trafico mayor igual q 1 o menor igual q 10. 
     * @param availableBandwith
     * @param trafficPriority
     * @param numberOfChannels
     * @return Ancho de banda sugerido.
     */
    public double getBandwidthToGrant(double availableBandwith, int trafficPriority,int numberOfChannels){
        
        double bandwithToGrant = -1;
        double pendant = 0;
        double constant = 0;
            
        if(trafficPriority<1 || trafficPriority>10){
            throw new IllegalArgumentException("La prioridad debe ser un numero NATURAL entre 1 y 10");
        }
        
        /**Enfoque particular
        
        if(numberOfChannels==0){
            numberOfChannels=1;
        }
        
        pendant = (availableBandwith*numberOfChannels)/(9*(numberOfChannels+1));
        constant = availableBandwith - (10*(availableBandwith*numberOfChannels)/(9*(numberOfChannels+1)));
        * 
        * 
        */
        
        /**Enfoque general*/
        pendant = (availableBandwith*(numberOfChannels+A-1))/(9*(numberOfChannels+A));
        constant = (availableBandwith*(10-numberOfChannels-A))/(9*(numberOfChannels+A));
        
        /*
         * Dejo la ecuacion de la forma y=mx+c donde m=pendiente=pendant
         */
        bandwithToGrant = (pendant*trafficPriority)+constant;
        
        return bandwithToGrant;
    }
    
}
