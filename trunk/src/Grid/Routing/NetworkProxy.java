package Grid.Routing;



import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import trs.core.IDDataProvider;
import trs.core.Network;
import trs.events.TRSEvent;
import trs.events.TRSEventListener;
import y.base.Edge;
import y.base.Graph;
import y.base.Node;

public class NetworkProxy 
{
    protected Network HyrbidNetwork;

    public void trsObjectChanged(TRSEvent trse) {
        HyrbidNetwork.trsObjectChanged(trse);
    }

    public String toString() {
        return HyrbidNetwork.toString();
    }

    public void setIDDataProviders(Map map) {
        HyrbidNetwork.setIDDataProviders(map);
    }

    public void setIDDataProviders(Collection clctn) {
        HyrbidNetwork.setIDDataProviders(clctn);
    }

    public void setID(String string) {
        HyrbidNetwork.setID(string);
    }

    public void setChanged() {
        HyrbidNetwork.setChanged();
    }

    public boolean removeNodeID(String string) {
        return HyrbidNetwork.removeNodeID(string);
    }

    public void removeNode(Node node) {
        HyrbidNetwork.removeNode(node);
    }

    public boolean removeIDDataProvider(IDDataProvider iddp) {
        return HyrbidNetwork.removeIDDataProvider(iddp);
    }

    public IDDataProvider removeIDDataProvider(String string) {
        return HyrbidNetwork.removeIDDataProvider(string);
    }

    public boolean removeEdgeID(String string) {
        return HyrbidNetwork.removeEdgeID(string);
    }

    public void removeEdge(Edge edge) {
        HyrbidNetwork.removeEdge(edge);
    }

    public void notifyTRSEventListeners(TRSEvent trse) {
        HyrbidNetwork.notifyTRSEventListeners(trse);
    }

    public void notifyTRSEventListeners() {
        HyrbidNetwork.notifyTRSEventListeners();
    }

    public Iterator nodeIDIterator() {
        return HyrbidNetwork.nodeIDIterator();
    }

    public boolean isConnectedBetween(String string, String string1) {
        return HyrbidNetwork.isConnectedBetween(string, string1);
    }

    public boolean hasNoListeners() {
        return HyrbidNetwork.hasNoListeners();
    }

    public boolean hasListeners() {
        return HyrbidNetwork.hasListeners();
    }

    public boolean hasChanged() {
        return HyrbidNetwork.hasChanged();
    }

    public String getTargetID(String string) {
        return HyrbidNetwork.getTargetID(string);
    }

    public List getTRSEventListeners() {
        return HyrbidNetwork.getTRSEventListeners();
    }

    public String getSourceID(String string) {
        return HyrbidNetwork.getSourceID(string);
    }

    public List getReverseEdgeIDs(String string) {
        return HyrbidNetwork.getReverseEdgeIDs(string);
    }

    public String getReverseEdgeID(String string) {
        return HyrbidNetwork.getReverseEdgeID(string);
    }

    public List getOutGoingEdgeIDs(String string) {
        return HyrbidNetwork.getOutGoingEdgeIDs(string);
    }

    public List getNodeIDs() {
        return HyrbidNetwork.getNodeIDs();
    }

    public Iterator getNodeIDIterator() {
        return HyrbidNetwork.getNodeIDIterator();
    }

    public Node getNode(String string) {
        return HyrbidNetwork.getNode(string);
    }

    public Map getNamedIDDataProviders() {
        return HyrbidNetwork.getNamedIDDataProviders();
    }

    public List getInGoingEdgeIDs(String string) {
        return HyrbidNetwork.getInGoingEdgeIDs(string);
    }

    public List getInAndOutGoingEdgeIDs(String string) {
        return HyrbidNetwork.getInAndOutGoingEdgeIDs(string);
    }

    public Collection getIDDataProviders() {
        return HyrbidNetwork.getIDDataProviders();
    }

    public String[] getIDDataProviderNames() {
        return HyrbidNetwork.getIDDataProviderNames();
    }

    public String getIDDataProviderName(IDDataProvider iddp) {
        return HyrbidNetwork.getIDDataProviderName(iddp);
    }

    public IDDataProvider getIDDataProvider(String string) {
        return HyrbidNetwork.getIDDataProvider(string);
    }

    public String getID() {
        return HyrbidNetwork.getID();
    }

    public String getID(Edge edge) {
        return HyrbidNetwork.getID(edge);
    }

    public String getID(Node node) {
        return HyrbidNetwork.getID(node);
    }

    public List getEdgeIDsFromTo(String string, String string1) {
        return HyrbidNetwork.getEdgeIDsFromTo(string, string1);
    }

    public List getEdgeIDsBetween(String string, String string1) {
        return HyrbidNetwork.getEdgeIDsBetween(string, string1);
    }

    public List getEdgeIDs() {
        return HyrbidNetwork.getEdgeIDs();
    }

    public Iterator getEdgeIDIterator() {
        return HyrbidNetwork.getEdgeIDIterator();
    }

    public String getEdgeIDFromTo(String string, String string1) {
        return HyrbidNetwork.getEdgeIDFromTo(string, string1);
    }

    public String getEdgeIDBetween(String string, String string1) {
        return HyrbidNetwork.getEdgeIDBetween(string, string1);
    }

    public String getEdgeBetween(String string, String string1) {
        return HyrbidNetwork.getEdgeBetween(string, string1);
    }

    public Edge getEdge(String string) {
        return HyrbidNetwork.getEdge(string);
    }

    public void finalize() {
        HyrbidNetwork.finalize();
    }

    public boolean equals(Object o) {
        return HyrbidNetwork.equals(o);
    }

    public boolean equalTopology(Network ntwrk) {
        return HyrbidNetwork.equalTopology(ntwrk);
    }

    public Iterator edgeIDIterator() {
        return HyrbidNetwork.edgeIDIterator();
    }

    public void deleteTRSEventListeners() {
        HyrbidNetwork.deleteTRSEventListeners();
    }

    public void deleteTRSEventListener(TRSEventListener tl) {
        HyrbidNetwork.deleteTRSEventListener(tl);
    }

    public String createNodeID() {
        return HyrbidNetwork.createNodeID();
    }

    public String createNodeID(String string) {
        return HyrbidNetwork.createNodeID(string);
    }

    public Node createNode() {
        return HyrbidNetwork.createNode();
    }

    public String createEdgeID(String string, String string1) {
        return HyrbidNetwork.createEdgeID(string, string1);
    }

    public String createEdgeID(String string, String string1, String string2) {
        return HyrbidNetwork.createEdgeID(string, string1, string2);
    }

    public Edge createEdge(Node node, Node node1) {
        return HyrbidNetwork.createEdge(node, node1);
    }

    public Edge createEdge(Node node, Edge edge, Node node1, Edge edge1, int i, int i1) {
        return HyrbidNetwork.createEdge(node, edge, node1, edge1, i, i1);
    }

    public Graph createCopy() {
        return HyrbidNetwork.createCopy();
    }

    public boolean containsNodeID(String string) {
        return HyrbidNetwork.containsNodeID(string);
    }

    public boolean containsEdgeID(String string) {
        return HyrbidNetwork.containsEdgeID(string);
    }

    public void clearChanged() {
        HyrbidNetwork.clearChanged();
    }

    public void clear() {
        HyrbidNetwork.clear();
    }

    public void ajc$interFieldSet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsObservableChanged(boolean bln) {
        HyrbidNetwork.ajc$interFieldSet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsObservableChanged(bln);
    }

    public void ajc$interFieldSet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsEventListeners(List list) {
        HyrbidNetwork.ajc$interFieldSet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsEventListeners(list);
    }

    public void ajc$interFieldSet$additions_ID_SimpleIDModel$additions_ID_SimpleIDModelIDEnabledInterface$ID(String string) {
        HyrbidNetwork.ajc$interFieldSet$additions_ID_SimpleIDModel$additions_ID_SimpleIDModelIDEnabledInterface$ID(string);
    }

    public boolean ajc$interFieldGet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsObservableChanged() {
        return HyrbidNetwork.ajc$interFieldGet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsObservableChanged();
    }

    public List ajc$interFieldGet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsEventListeners() {
        return HyrbidNetwork.ajc$interFieldGet$additions_events_SimpleEventModel$additions_events_SimpleEventModelTRSObservableInterface$trsEventListeners();
    }

    public String ajc$interFieldGet$additions_ID_SimpleIDModel$additions_ID_SimpleIDModelIDEnabledInterface$ID() {
        return HyrbidNetwork.ajc$interFieldGet$additions_ID_SimpleIDModel$additions_ID_SimpleIDModelIDEnabledInterface$ID();
    }

    public void addTRSEventListener(TRSEventListener tl) {
        HyrbidNetwork.addTRSEventListener(tl);
    }

    public void addNodeID(String string) {
        HyrbidNetwork.addNodeID(string);
    }

    public IDDataProvider addIDDataProvider(String string, IDDataProvider iddp) {
        return HyrbidNetwork.addIDDataProvider(string, iddp);
    }

    public void addIDDataProvider(IDDataProvider iddp) {
        HyrbidNetwork.addIDDataProvider(iddp);
    }

    public void setHyrbidNetwork(Network HyrbidNetwork) {
        this.HyrbidNetwork = HyrbidNetwork;
    }
   
}
