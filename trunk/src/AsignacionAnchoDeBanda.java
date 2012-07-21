

public class AsignacionAnchoDeBanda {
    
    private BandwidthCalculator bandwidthCalculator;
    
    public AsignacionAnchoDeBanda(){
        
        bandwidthCalculator = BandwidthCalculator.getInstance();
        
        double bandwidthToGrant = bandwidthCalculator.getBandwidthToGrant(100,1,0);
        System.out.println("Ancho de banda asignado:"+bandwidthToGrant);
    }

    public static void main(String[] args) {
        new AsignacionAnchoDeBanda();
    }
}
