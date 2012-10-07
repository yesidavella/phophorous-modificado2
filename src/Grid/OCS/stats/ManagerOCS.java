package Grid.OCS.stats;

import Grid.Entity;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.Interfaces.Messages.OCSTeardownMessage;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author AG2
 */
public class ManagerOCS {

    private static ManagerOCS managerOCS;
    private HashMap<OCSRequestMessage, InstanceOCS> mapInstanceOCS;
    private HashMap<SourceDestination, SumaryOCS> mapSumaryOCS;
    private NotificableOCS notificableOCS;
    private HashMap<InfoLinkWavelenghtOCS, InstanceOCS> mapInstanceOCSConfirmed;
    private HashMap<InfoLinkWavelenghtOCS, InstanceOCS> mapInstanceOCSRequested;

    public static ManagerOCS getInstance() {

        if (managerOCS == null) {
            managerOCS = new ManagerOCS();
        }

        return managerOCS;
    }

    public static void clean() {
        managerOCS.getNotificableOCS().clean();

        NotificableOCS notificable = managerOCS.getNotificableOCS();

        managerOCS = new ManagerOCS();

        managerOCS.setNotificableOCS(notificable);
    }

    private ManagerOCS() {
        mapInstanceOCS = new HashMap<OCSRequestMessage, InstanceOCS>();
        mapSumaryOCS = new HashMap<SourceDestination, SumaryOCS>();
        mapInstanceOCSConfirmed = new HashMap<InfoLinkWavelenghtOCS, InstanceOCS>();
        mapInstanceOCSRequested = new HashMap<InfoLinkWavelenghtOCS, InstanceOCS>();
    }

    public ArrayList<SumaryOCS> getListSummaryOCS() {
        ArrayList<SumaryOCS> sumaryOCSs = new ArrayList<SumaryOCS>();
        for (SumaryOCS sumaryOCS : mapSumaryOCS.values()) {
            sumaryOCSs.add(sumaryOCS);
        }
        return sumaryOCSs;
    }

    public void addWavelengthID(OCSRequestMessage ocsRequestMessage, int wavelengthID, Entity owner) {
        if (mapInstanceOCS.containsKey(ocsRequestMessage)) {
            InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
            if (owner != ocsRequestMessage.getDestination()) {

                instanceOCS.getListWavelengthID().add(wavelengthID);
            }
            if (instanceOCS.getListWavelengthID().size() == 1) {
//                //System.out.println(" addWavelengthID :  " + ocsRequestMessage.getSource() + " -> " + ocsRequestMessage.getDestination() + " Color: " + wavelengthID);
                InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination(), wavelengthID);
                mapInstanceOCSRequested.put(infoLastLinkOCS, instanceOCS);
            }
        }
    }

    public void addTraffic(GridMessage gridMessage, HybridSwitchImpl sourceHybridSwitchImpl, HybridSwitchImpl destinationHybridSwitchImpl) {

//        //System.out.println("addTraffic:  " +sourceHybridSwitchImpl  + " -> " + destinationHybridSwitchImpl + " Color: " + gridMessage.getWavelengthID());

        InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(sourceHybridSwitchImpl, destinationHybridSwitchImpl, gridMessage.getWavelengthID());
        InstanceOCS instanceOCS = mapInstanceOCSRequested.get(infoLastLinkOCS);

        instanceOCS.setTrafficInstanceOCS(instanceOCS.getTrafficInstanceOCS() + gridMessage.getSize());
    }

    public void confirmTearDownOCS(OCSTeardownMessage OCS_TeardownMessage, double time) {
        InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(OCS_TeardownMessage.getSource(), OCS_TeardownMessage.getDestination(), OCS_TeardownMessage.getWavelengthID());
        InstanceOCS instanceOCS = mapInstanceOCSConfirmed.get(infoLastLinkOCS);

        instanceOCS.setTearDownTimeInstanceOCS(time);
        instanceOCS.setDurationTimeInstanceOCS(time - instanceOCS.getSetupTimeInstanceOCS());
        //System.out.println("Establer tiempo de fin OCS "
           //     + OCS_TeardownMessage.getSource() + " ->  " + OCS_TeardownMessage.getDestination() + " Color: " + OCS_TeardownMessage.getWavelengthID());
        instanceOCS.setToreDown(true);

    }

    public void confirmInstanceOCS(OCSRequestMessage ocsRequestMessage, double time) {

        InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
        instanceOCS.setSetupTimeInstanceOCS(time);

        SourceDestination sourceDestination =
                new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());

        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);
        sumaryOCS.setCountCreateOCS(sumaryOCS.getCountCreateOCS() + 1);

        InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination(), ocsRequestMessage.getWavelengthID());
        mapInstanceOCSConfirmed.put(infoLastLinkOCS, instanceOCS);

        if (notificableOCS != null) {
            notificableOCS.notifyNewCreatedOCS(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination(), (int) sumaryOCS.getCountCreateOCS());
        }
    }

    public void notifyError(OCSRequestMessage ocsRequestMessage, double time, Entity entity, String message) {

        InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
        instanceOCS.setNodeErrorInstanceOCS(entity);
        instanceOCS.setProblemInstanceOCS("Tiempo :" + time + " \nProblema" + message);
        SourceDestination sourceDestination =
                new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());

        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);
        sumaryOCS.setCountFaultOCS(sumaryOCS.getCountFaultOCS() + 1);

    }

    public void addInstaceOCS(OCSRequestMessage ocsRequestMessage) {
        if (!mapInstanceOCS.containsKey(ocsRequestMessage)) {
            InstanceOCS instanceOCS = new InstanceOCS();
            instanceOCS.setSetupTimeInstanceOCS(ocsRequestMessage.getGenerationTime().getTime());
            instanceOCS.setRoute(ocsRequestMessage.getOCSRoute());
            mapInstanceOCS.put(ocsRequestMessage, instanceOCS);


//            //System.out.println("New Instance OCS REG - Source "
//                    + ocsRequestMessage.getSource() + " Destination " + ocsRequestMessage.getDestination()
//                    + " Time " + ocsRequestMessage.getGenerationTime().getTime());

            SourceDestination sourceDestination =
                    new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());
            SumaryOCS sumaryOCS;
            if (!mapSumaryOCS.containsKey(sourceDestination)) {
                sumaryOCS = new SumaryOCS(sourceDestination);
                sumaryOCS.setCountRequestOCS(1);
                mapSumaryOCS.put(sourceDestination, sumaryOCS);
//                //System.out.println("New Sumary OCS REG - Source "
//                        + ocsRequestMessage.getSource() + " Destination " + ocsRequestMessage.getDestination());
            } else {
                sumaryOCS = mapSumaryOCS.get(sourceDestination);
                sumaryOCS.setCountRequestOCS(sumaryOCS.getCountRequestOCS() + 1);
//                //System.out.println("OLD Sumary OCS REG - Source "
//                        + ocsRequestMessage.getSource() + " Destination " + ocsRequestMessage.getDestination() + " Count OCS " + sumaryOCS.getCountRequestOCS());

            }
            sumaryOCS.getInstanceOCSs().add(instanceOCS);

        }

    }

    public NotificableOCS getNotificableOCS() {
        return notificableOCS;
    }

    public void setNotificableOCS(NotificableOCS notificableOCS) {
        this.notificableOCS = notificableOCS;
    }

    public static class InstanceOCS {

        private boolean direct;
        private boolean toreDown = false;
        private ArrayList<Integer> listWavelengthID = new ArrayList<Integer>();
        private OCSRoute route;
        protected double requestTimeInstanceOCS;
        protected double setupTimeInstanceOCS;
        protected double durationTimeInstanceOCS;
        protected double tearDownTimeInstanceOCS;
        protected double trafficInstanceOCS;
        protected String problemInstanceOCS = "Sin problemas";
        protected Entity nodeErrorInstanceOCS;

        public boolean isToreDown() {
            return toreDown;
        }

        public void setToreDown(boolean toreDown) {
            this.toreDown = toreDown;
        }

        public boolean isDirect() {
            return direct;
        }

        public void setDirect(boolean direct) {
            this.direct = direct;
        }

        public ArrayList<Integer> getListWavelengthID() {
            return listWavelengthID;
        }

        public OCSRoute getRoute() {
            return route;
        }

        public void setRoute(OCSRoute route) {
            this.route = route;
        }

        public double getDurationTimeInstanceOCS() {
            return durationTimeInstanceOCS;
        }

        public void setDurationTimeInstanceOCS(double durationTimeInstanceOCS) {
            this.durationTimeInstanceOCS = durationTimeInstanceOCS;
        }

        public Entity getNodeErrorInstanceOCS() {
            return nodeErrorInstanceOCS;
        }

        public void setNodeErrorInstanceOCS(Entity nodeErrorInstanceOCS) {
            this.nodeErrorInstanceOCS = nodeErrorInstanceOCS;
        }

        public String getProblemInstanceOCS() {
            return problemInstanceOCS;
        }

        public void setProblemInstanceOCS(String problemInstanceOCS) {
            this.problemInstanceOCS = problemInstanceOCS;
        }

        public double getRequestTimeInstanceOCS() {
            return requestTimeInstanceOCS;
        }

        public void setRequestTimeInstanceOCS(double requestTimeInstanceOCS) {
            this.requestTimeInstanceOCS = requestTimeInstanceOCS;
        }

        public double getSetupTimeInstanceOCS() {
            return setupTimeInstanceOCS;
        }

        public void setSetupTimeInstanceOCS(double setupTimeInstanceOCS) {
            this.setupTimeInstanceOCS = setupTimeInstanceOCS;
        }

        public double getTearDownTimeInstanceOCS() {
            return tearDownTimeInstanceOCS;
        }

        public void setTearDownTimeInstanceOCS(double tearDownTimeInstanceOCS) {
            this.tearDownTimeInstanceOCS = tearDownTimeInstanceOCS;
        }

        public double getTrafficInstanceOCS() {
            return trafficInstanceOCS;
        }

        public void setTrafficInstanceOCS(double trafficInstanceOCS) {
            this.trafficInstanceOCS = trafficInstanceOCS;
        }
    }

    public static class SumaryOCS {

        private double countRequestOCS;
        private double countCreateOCS;
        private double countFaultOCS;
        private double countAverageDurationTimeOCS;
        private SourceDestination sourceDestination;
        private boolean direct;
        private ArrayList<InstanceOCS> instanceOCSs = new ArrayList<InstanceOCS>();

        public SumaryOCS(SourceDestination sourceDestination) {
            this.sourceDestination = sourceDestination;
        }

        public SourceDestination getSourceDestination() {
            return sourceDestination;
        }

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

        public double getCountAverageDurationTimeOCS() {
            return countAverageDurationTimeOCS;
        }

        public void setCountAverageTimeOCS(double countAverageTimeOCS) {
            this.countAverageDurationTimeOCS = countAverageTimeOCS;
        }

        public double getCountFaultOCS() {
            return countFaultOCS;
        }

        public void setCountFaultOCS(double countFaultOCS) {
            this.countFaultOCS = countFaultOCS;
        }

        public double getCountCreateOCS() {
            return countCreateOCS;
        }

        public void setCountCreateOCS(double countCreateOCS) {
            this.countCreateOCS = countCreateOCS;
        }

        public ArrayList<InstanceOCS> getInstanceOCSs() {
            return instanceOCSs;
        }
    }

    public static class SourceDestination {

        private Entity entitySource;
        private Entity entityDestination;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SourceDestination) {
                SourceDestination sourceDestination = (SourceDestination) obj;
                if (sourceDestination.getEntitySource().getId().equals(entitySource.getId())
                        && sourceDestination.getEntityDestination().getId().equals(entityDestination.getId())) {
                    return true;
                }

            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.entitySource.getId() != null ? this.entitySource.getId().hashCode() : 0);
            hash = 71 * hash + (this.entityDestination.getId() != null ? this.entityDestination.getId().hashCode() : 0);
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

    public HashMap<OCSRequestMessage, InstanceOCS> getMapInstanceOCS() {
        return mapInstanceOCS;
    }

    public HashMap<SourceDestination, SumaryOCS> getMapSumaryOCS() {
        return mapSumaryOCS;
    }

    public static class InfoLinkWavelenghtOCS {

        private Entity entitySource;
        private Entity entityDestination;
        private int wavelengthID;

        public InfoLinkWavelenghtOCS(Entity entitySource, Entity entityDestination, int wavelengthID) {
            this.entitySource = entitySource;
            this.entityDestination = entityDestination;
            this.wavelengthID = wavelengthID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InfoLinkWavelenghtOCS other = (InfoLinkWavelenghtOCS) obj;
            if (this.entitySource != other.entitySource && (this.entitySource == null || !this.entitySource.equals(other.entitySource))) {
                return false;
            }
            if (this.entityDestination != other.entityDestination && (this.entityDestination == null || !this.entityDestination.equals(other.entityDestination))) {
                return false;
            }
            if (this.wavelengthID != other.wavelengthID) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.entitySource != null ? this.entitySource.hashCode() : 0);
            hash = 97 * hash + (this.entityDestination != null ? this.entityDestination.hashCode() : 0);
            hash = 97 * hash + this.wavelengthID;
            return hash;
        }
    }
}
