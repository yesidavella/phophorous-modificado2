/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.OCS.stats;

import Grid.Entity;
import Grid.Interfaces.Messages.OCSRequestMessage;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Frank
 */
public class ManagerOCS {

    private static ManagerOCS managerOCS;
    private HashMap<OCSRequestMessage, InstanceOCS> mapInstanceOCS;
    private HashMap<SourceDestination, SumaryOCS> mapSumaryOCS;
    private NotificableOCS notificableOCS;

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
    }

    public ArrayList<SumaryOCS> getListSummaryOCS() {
        ArrayList<SumaryOCS> sumaryOCSs = new ArrayList<SumaryOCS>();
        for (SumaryOCS sumaryOCS : mapSumaryOCS.values()) {
            sumaryOCSs.add(sumaryOCS);
        }
        return sumaryOCSs;
    }

    public void addWavelengthID(OCSRequestMessage ocsRequestMessage, int wavelengthID, Entity owner) {
        if (mapInstanceOCS.containsKey(ocsRequestMessage)) 
        {
            if (owner != ocsRequestMessage.getDestination())             
            {
                InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
                instanceOCS.getListWavelengthID().add(wavelengthID);
            }


        }
    }

    public void confirmInstanceOCS(OCSRequestMessage ocsRequestMessage, double time) {

        InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
        instanceOCS.setSetupTimeInstanceOCS(time);

        SourceDestination sourceDestination =
                new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());

        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);
        sumaryOCS.setCountCreateOCS(sumaryOCS.getCountCreateOCS() + 1);

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


//            System.out.println("New Instance OCS REG - Source "
//                    + ocsRequestMessage.getSource() + " Destination " + ocsRequestMessage.getDestination()
//                    + " Time " + ocsRequestMessage.getGenerationTime().getTime());

            SourceDestination sourceDestination =
                    new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());
            SumaryOCS sumaryOCS;
            if (!mapSumaryOCS.containsKey(sourceDestination)) {
                sumaryOCS = new SumaryOCS(sourceDestination);
                sumaryOCS.setCountRequestOCS(1);
                mapSumaryOCS.put(sourceDestination, sumaryOCS);
//                System.out.println("New Sumary OCS REG - Source "
//                        + ocsRequestMessage.getSource() + " Destination " + ocsRequestMessage.getDestination());
            } else {
                sumaryOCS = mapSumaryOCS.get(sourceDestination);
                sumaryOCS.setCountRequestOCS(sumaryOCS.getCountRequestOCS() + 1);
//                System.out.println("OLD Sumary OCS REG - Source "
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
        private ArrayList<Integer> listWavelengthID = new ArrayList<Integer>();
        private OCSRoute route;
        protected double requestTimeInstanceOCS;
        protected double setupTimeInstanceOCS;
        protected double durationTimeInstanceOCS;
        protected double tearDownTimeInstanceOCS;
        protected double trafficInstanceOCS;
        protected String problemInstanceOCS = "Sin problemas";
        protected Entity nodeErrorInstanceOCS;

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
}
