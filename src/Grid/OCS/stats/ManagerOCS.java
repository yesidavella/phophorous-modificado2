package Grid.OCS.stats;

import Grid.Entity;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.*;
import Grid.Nodes.Hybrid.Parallel.HybridSwitchImpl;
import Grid.OCS.OCSRoute;
import java.util.ArrayList;
import java.util.HashMap;
import simbase.SimulationInstance;

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
        }
    }

    public void addTraffic(GridMessage gridMessage,
            HybridSwitchImpl sourceHybridSwitchImpl,
            HybridSwitchImpl destinationHybridSwitchImpl,
            int initIniWavelengthID) {

//        System.out.println("addTraffic:  " +sourceHybridSwitchImpl  + " -> " + destinationHybridSwitchImpl + " Color: " + initIniWavelengthID);

        int countID = findCountOCSSameWavelengthID_OCSRequested(sourceHybridSwitchImpl, destinationHybridSwitchImpl, initIniWavelengthID);
        InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(
                sourceHybridSwitchImpl,
                destinationHybridSwitchImpl,
                initIniWavelengthID,
                countID);

        InstanceOCS instanceOCS = mapInstanceOCSRequested.get(infoLastLinkOCS);

        if (instanceOCS == null) {
            ((Grid.GridSimulator) (sourceHybridSwitchImpl.getSimulator())).getEstablishedCircuits();
            System.out.println("INFOe " + sourceHybridSwitchImpl + "  " + destinationHybridSwitchImpl 
                    +" IDmsg:"+gridMessage.getId()+ "  ID " + initIniWavelengthID + "  sub: " + countID + "  tiempo" + destinationHybridSwitchImpl.getCurrentTime());
            return;
        }

        instanceOCS.setTrafficInstanceOCS(instanceOCS.getTrafficInstanceOCS() + gridMessage.getSize());

        SourceDestination sourceDestination =
                new SourceDestination(sourceHybridSwitchImpl, destinationHybridSwitchImpl);
        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);

        sumaryOCS.setTraffic(sumaryOCS.getTraffic() + gridMessage.getSize());

        instanceOCS.setMessageSent(instanceOCS.getMessageSent() + 1);

        if (gridMessage instanceof JobMessage) {
            instanceOCS.setJobSent(instanceOCS.getJobSent() + 1);
            sumaryOCS.setJobSent(sumaryOCS.getJobSent() + 1);
            instanceOCS.setJobTraffic(instanceOCS.getJobTraffic() + gridMessage.getSize());
            sumaryOCS.setJobTraffic(sumaryOCS.getJobTraffic() + gridMessage.getSize());

        } else if (gridMessage instanceof JobRequestMessage) {
            instanceOCS.setRequestJobSent(instanceOCS.getRequestJobSent() + 1);
            sumaryOCS.setRequestJobSent(sumaryOCS.getRequestJobSent() + 1);
            instanceOCS.setRequestJobTraffic(instanceOCS.getRequestJobTraffic() + gridMessage.getSize());
            sumaryOCS.setRequestJobTraffic(sumaryOCS.getRequestJobTraffic() + gridMessage.getSize());
        } else if (gridMessage instanceof JobAckMessage) {
            instanceOCS.setAckRequestJobSent(instanceOCS.getAckRequestJobSent() + 1);
            sumaryOCS.setAckRequestJobSent(sumaryOCS.getAckRequestJobSent() + 1);
            instanceOCS.setAckRequestJobTraffic(instanceOCS.getAckRequestJobTraffic() + gridMessage.getSize());
            sumaryOCS.setAckRequestJobTraffic(sumaryOCS.getAckRequestJobTraffic() + gridMessage.getSize());
        } else if (gridMessage instanceof JobResultMessage) {
            instanceOCS.setResultJobSent(instanceOCS.getResultJobSent() + 1);
            sumaryOCS.setResultJobSent(sumaryOCS.getResultJobSent() + 1);
            instanceOCS.setResultJobTraffic(instanceOCS.getResultJobTraffic() + gridMessage.getSize());
            sumaryOCS.setResultJobTraffic(sumaryOCS.getResultJobTraffic() + gridMessage.getSize());
        } else {
            System.out.println("XXXXXXXXXXX INFOe XXXXXXXXXXX  - TRAFICO NO MEDIDO ");
        }




        if (notificableOCS != null) {


            notificableOCS.notifyTrafficCreatedOCS(
                    sourceHybridSwitchImpl,
                    destinationHybridSwitchImpl,
                    sumaryOCS.getTraffic(),
                    sumaryOCS.getJobSent(),
                    sumaryOCS.getJobTraffic(),
                    sumaryOCS.getRequestJobSent(),
                    sumaryOCS.getRequestJobTraffic(),
                    sumaryOCS.getAckRequestJobSent(),
                    sumaryOCS.getAckRequestJobTraffic(),
                    sumaryOCS.getResultJobSent(),
                    sumaryOCS.getResultJobTraffic());
        }


    }

    public void confirmTearDownOCS(OCSTeardownMessage OCS_TeardownMessage, double time, int initIniWavelengthID) {

        InfoLinkWavelenghtOCS infoLastLinkOCS =
                new InfoLinkWavelenghtOCS(
                OCS_TeardownMessage.getSource(),
                OCS_TeardownMessage.getDestination(),
                initIniWavelengthID,
                getNewCountOCSSameWavelengthID_OCSConfirmed(OCS_TeardownMessage, initIniWavelengthID) - 1);


//        System.out.println("tear down:  " + OCS_TeardownMessage.getSource() + " -> " + OCS_TeardownMessage.getDestination() + " Color: " + initIniWavelengthID);

        InstanceOCS instanceOCS = mapInstanceOCSConfirmed.get(infoLastLinkOCS);
        instanceOCS.setTearDownTimeInstanceOCS(time);
        instanceOCS.setDurationTimeInstanceOCS(time - instanceOCS.getSetupTimeInstanceOCS());
        instanceOCS.setToreDown(true);

        SourceDestination sourceDestination =
                new SourceDestination(OCS_TeardownMessage.getSource(),
                OCS_TeardownMessage.getDestination());
        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);

        sumaryOCS.setCountTearDownOCS(sumaryOCS.getCountTearDownOCS() + 1);

        if (notificableOCS != null) {

            notificableOCS.notifyDeletedCreatedOCS(
                    OCS_TeardownMessage.getSource(),
                    OCS_TeardownMessage.getDestination());
        }


    }

    public void confirmInstanceOCS(OCSRequestMessage ocsRequestMessage, double time, int initWavelengthID) {

        InstanceOCS instanceOCS = mapInstanceOCS.get(ocsRequestMessage);
        instanceOCS.setSetupTimeInstanceOCS(time);

//        System.out.println(" Confirm " + ocsRequestMessage.getSource() + " -> " + ocsRequestMessage.getDestination() + " Color: " + initWavelengthID);
        SourceDestination sourceDestination =
                new SourceDestination(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination());

        SumaryOCS sumaryOCS = mapSumaryOCS.get(sourceDestination);
        sumaryOCS.setCountCreateOCS(sumaryOCS.getCountCreateOCS() + 1);

        InfoLinkWavelenghtOCS infoLastLinkOCS =
                new InfoLinkWavelenghtOCS(
                ocsRequestMessage.getSource(),
                ocsRequestMessage.getDestination(),
                initWavelengthID,
                getNewCountOCSSameWavelengthID_OCSConfirmed(ocsRequestMessage, initWavelengthID));

        mapInstanceOCSConfirmed.put(infoLastLinkOCS, instanceOCS);



        int countID = getNewCountOCSSameWavelengthID_OCSRequested(
                (HybridSwitchImpl) ocsRequestMessage.getSource(),
                (HybridSwitchImpl) ocsRequestMessage.getDestination(),
                initWavelengthID);

        //System.out.println(" addWavelengthID :  " + ocsRequestMessage.getSource() + " -> " + ocsRequestMessage.getDestination() + " Color: " + wavelengthID);
        InfoLinkWavelenghtOCS infoLastLinkOCS2 =
                new InfoLinkWavelenghtOCS(
                ocsRequestMessage.getSource(),
                ocsRequestMessage.getDestination(),
                initWavelengthID,
                countID);


//        System.out.println(ocsRequestMessage.getSource() + "  " + ocsRequestMessage.getDestination() + "  ID " + ocsRequestMessage.getWavelengthID() + "  sub: " + countID);


        mapInstanceOCSRequested.put(infoLastLinkOCS2, instanceOCS);



        if (notificableOCS != null) {
            //System.out.println("Confimacion de OCS " + ocsRequestMessage.getSource() + " -> " + ocsRequestMessage.getDestination() + " id " + ocsRequestMessage.getWavelengthID());
            notificableOCS.notifyNewCreatedOCS(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination(), (int) sumaryOCS.getCountCreateOCS());
        }
    }

    private int getNewCountOCSSameWavelengthID_OCSConfirmed(GridMessage ocsRequestMessage, int initIniWavelengthID) {
        int i = 0;
        while (true) {
            InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(ocsRequestMessage.getSource(), ocsRequestMessage.getDestination(), initIniWavelengthID, i);
            if (!mapInstanceOCSConfirmed.containsKey(infoLastLinkOCS)) {
                return i;
            }
            i++;
        }
    }

    private int getNewCountOCSSameWavelengthID_OCSRequested(HybridSwitchImpl sourceHybridSwitchImpl, HybridSwitchImpl destinationHybridSwitchImpl, int initIniWavelengthID) {
        int i = 0;
        while (true) {
            InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(
                    sourceHybridSwitchImpl,
                    destinationHybridSwitchImpl,
                    initIniWavelengthID,
                    i);
            if (!mapInstanceOCSRequested.containsKey(infoLastLinkOCS)) {
                return i;
            }
            i++;
        }
    }

    private int findCountOCSSameWavelengthID_OCSRequested(
            HybridSwitchImpl sourceHybridSwitchImpl,
            HybridSwitchImpl destinationHybridSwitchImpl,
            int initIniWavelengthID) {


        int i = 0;
        while (true) {
            InfoLinkWavelenghtOCS infoLastLinkOCS = new InfoLinkWavelenghtOCS(
                    sourceHybridSwitchImpl,
                    destinationHybridSwitchImpl,
                    initIniWavelengthID,
                    i);
            if (!mapInstanceOCSRequested.containsKey(infoLastLinkOCS)) {
                return i - 1;
            }
            i++;
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

    public void addInstaceOCS(OCSRequestMessage ocsRequestMessage, double time) {
        if (!mapInstanceOCS.containsKey(ocsRequestMessage)) {
            InstanceOCS instanceOCS = new InstanceOCS();
//            instanceOCS.setSetupTimeInstanceOCS(ocsRequestMessage.getGenerationTime().getTime());
            instanceOCS.setRequestTimeInstanceOCS(time);
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
        private long jobSent;
        private long requestJobSent;
        private long ackRequestJobSent;
        private long resultJobSent;
        private long messageSent;
        private double jobTraffic;
        private double requestJobTraffic;
        private double ackRequestJobTraffic;
        private double resultJobTraffic;

        public long getAckRequestJobSent() {
            return ackRequestJobSent;
        }

        public void setAckRequestJobSent(long ackRequestJobSent) {
            this.ackRequestJobSent = ackRequestJobSent;
        }

        public double getAckRequestJobTraffic() {
            return ackRequestJobTraffic;
        }

        public void setAckRequestJobTraffic(double ackRequestJobTraffic) {
            this.ackRequestJobTraffic = ackRequestJobTraffic;
        }

        public double getJobTraffic() {
            return jobTraffic;
        }

        public void setJobTraffic(double jobTraffic) {
            this.jobTraffic = jobTraffic;
        }

        public long getRequestJobSent() {
            return requestJobSent;
        }

        public void setRequestJobSent(long requestJobSent) {
            this.requestJobSent = requestJobSent;
        }

        public double getRequestJobTraffic() {
            return requestJobTraffic;
        }

        public void setRequestJobTraffic(double requestJobTraffic) {
            this.requestJobTraffic = requestJobTraffic;
        }

        public long getResultJobSent() {
            return resultJobSent;
        }

        public void setResultJobSent(long resultJobSent) {
            this.resultJobSent = resultJobSent;
        }

        public double getResultJobTraffic() {
            return resultJobTraffic;
        }

        public void setResultJobTraffic(double resultJobSentTraffic) {
            this.resultJobTraffic = resultJobSentTraffic;
        }

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

        public long getJobSent() {
            return jobSent;
        }

        public void setJobSent(long jobSent) {
            this.jobSent = jobSent;
        }

        public long getMessageSent() {
            return messageSent;
        }

        public void setMessageSent(long messageSent) {
            this.messageSent = messageSent;
        }
    }

    public static class SumaryOCS {

        private double countRequestOCS;
        private double countCreateOCS;
        private double countFaultOCS;
        private double countTearDownOCS;
        private double countAverageDurationTimeOCS;
        private SourceDestination sourceDestination;
        private boolean direct;
        private double traffic;
        private long jobSent;
        private long requestJobSent;
        private long ackRequestJobSent;
        private long resultJobSent;
        private double jobTraffic;
        private double requestJobTraffic;
        private double ackRequestJobTraffic;
        private double resultJobTraffic;
        private ArrayList<InstanceOCS> instanceOCSs = new ArrayList<InstanceOCS>();

        public long getAckRequestJobSent() {
            return ackRequestJobSent;
        }

        public void setAckRequestJobSent(long ackRequestJobSent) {
            this.ackRequestJobSent = ackRequestJobSent;
        }

        public double getAckRequestJobTraffic() {
            return ackRequestJobTraffic;
        }

        public void setAckRequestJobTraffic(double ackRequestJobTraffic) {
            this.ackRequestJobTraffic = ackRequestJobTraffic;
        }

        public double getJobTraffic() {
            return jobTraffic;
        }

        public void setJobTraffic(double jobTraffic) {
            this.jobTraffic = jobTraffic;
        }

        public long getRequestJobSent() {
            return requestJobSent;
        }

        public void setRequestJobSent(long requestJobSent) {
            this.requestJobSent = requestJobSent;
        }

        public double getRequestJobTraffic() {
            return requestJobTraffic;
        }

        public void setRequestJobTraffic(double requestJobTraffic) {
            this.requestJobTraffic = requestJobTraffic;
        }

        public long getResultJobSent() {
            return resultJobSent;
        }

        public void setResultJobSent(long resultJobSent) {
            this.resultJobSent = resultJobSent;
        }

        public double getResultJobTraffic() {
            return resultJobTraffic;
        }

        public void setResultJobTraffic(double resultJobTraffic) {
            this.resultJobTraffic = resultJobTraffic;
        }

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

        public double getTraffic() {
            return traffic;
        }

        public void setTraffic(double traffic) {
            this.traffic = traffic;
        }

        public long getJobSent() {
            return jobSent;
        }

        public void setJobSent(long jobSent) {
            this.jobSent = jobSent;
        }

        public double getCountTearDownOCS() {
            return countTearDownOCS;
        }

        public void setCountTearDownOCS(double countTearDownOCS) {
            this.countTearDownOCS = countTearDownOCS;
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
        private int countWavelengthID;

        public InfoLinkWavelenghtOCS(Entity entitySource, Entity entityDestination, int wavelengthID, int countWavelengthID) {
            this.entitySource = entitySource;
            this.entityDestination = entityDestination;
            this.wavelengthID = wavelengthID;
            this.countWavelengthID = countWavelengthID;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.entitySource != null ? this.entitySource.hashCode() : 0);
            hash = 89 * hash + (this.entityDestination != null ? this.entityDestination.hashCode() : 0);
            hash = 89 * hash + this.wavelengthID;
            hash = 89 * hash + this.countWavelengthID;
            return hash;
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
            if (!this.entitySource.getId().equals(other.entitySource.getId())) {
                return false;
            }
            if (!this.entityDestination.getId().equals(other.entityDestination.getId())) {
                return false;
            }


            if (this.wavelengthID != other.wavelengthID) {
                return false;
            }
            if (this.countWavelengthID != other.countWavelengthID) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "{ Source=" + entitySource + ", Destination=" + entityDestination + ", wID=" + wavelengthID + ", countWID=" + countWavelengthID + '}';
        }

        public int getCountWavelengthID() {
            return countWavelengthID;
        }

        public void setCountWavelengthID(int countWavelengthID) {
            this.countWavelengthID = countWavelengthID;
        }
    }
}
