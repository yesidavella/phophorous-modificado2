/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.Entity;
import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Frank
 */
public class LambdaChannelGroup implements Serializable{

    private GridOutPort gridOutPort;
    private int wavelengthID;
    private ArrayList<Channel> channels;

    public LambdaChannelGroup(GridOutPort gridOutPort, int wavelengthID) {
        this.gridOutPort = gridOutPort;
        this.wavelengthID = wavelengthID;
        channels = new ArrayList<Channel>();
    }

    public int getChannelsSize(double time) {
        deleteLazyChannels(time);
        return channels.size();
    }

    public double getFreeBandwidth(double time) {
        double bandwidthFree = gridOutPort.getLinkSpeed();

        deleteLazyChannels(time);

        for (Channel channel : channels) {
            bandwidthFree -= channel.getChannelSpeed(); // Resta lo ocupado.            
        }
        return bandwidthFree;
    }
    
     public double getFreeBandwidthNoDeleteLazy(double time) 
     {
        double bandwidthFree = gridOutPort.getLinkSpeed();    

        for (Channel channel : channels) {
            bandwidthFree -= channel.getChannelSpeed(); // Resta lo ocupado.            
        }
        return bandwidthFree;
    }

    public void deleteLazyChannels(double time) {
        ArrayList<Channel> channelsToRemove = new ArrayList<Channel>();
        for (Channel channel : channels) {
            if (channel.getFreeAgainTime() <= time) {

                channelsToRemove.add(channel);
            }
        }
        channels.removeAll(channelsToRemove);
    }

    public boolean isWavelengthFree(double bandwidthRequested, double time) {
        double bandwidthFree = gridOutPort.getLinkSpeed();
        for (Channel channel : channels) {
            if (channel.getFreeAgainTime() > time) {
                bandwidthFree -= channel.getChannelSpeed(); // Resta lo ocupado.
            }
        }
        if (bandwidthFree >= bandwidthRequested) {
            return true;
        }
        return false;
    }

    public Channel reserve(  Entity entitySource,  Entity entityDestination,  double bandwidthRequested, double time, GridMessage message) {
        
        if (!isWavelengthFree(bandwidthRequested, time)) {
            return null;
        }

        Channel channel = new Channel(channels.size(),   entitySource,   entityDestination);
        configChannel(channel, bandwidthRequested, time, message);
        channels.add(channel);
        
        return channel;
    }

    private void configChannel(Channel channel, double bandwidthRequested, double time, GridMessage message) {
        channel.setChannelSpeed(bandwidthRequested);
        channel.setFreeAgainTime(time + (message.getSize() / bandwidthRequested));
    }

    public GridOutPort getGridOutPort() {
        return gridOutPort;
    }

    public void setGridOutPort(GridOutPort gridOutPort) {
        this.gridOutPort = gridOutPort;
    }

    public int getWavelengthID() {
        return wavelengthID;
    }

    public void setWavelengthID(int wavelengthID) {
        this.wavelengthID = wavelengthID;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    
    public static class Channel implements Serializable{

        int id;
        double channelSpeed = 0;
        double freeAgainTime = 0;
        Entity entitySource; 
        Entity entityDestination; 
        
        

        public Channel(int id,  Entity entitySource,  Entity entityDestination )
        {
            this.id = id;
            
             this.entitySource = entitySource ; 
             this.entityDestination = entityDestination;
        }

        public Entity getEntityDestination() {
            return entityDestination;
        }

        public Entity getEntitySource() {
            return entitySource;
        }
        
        

        public void setFreeAgainTime(double freeAgainTime) {
            this.freeAgainTime = freeAgainTime;
        }

        public double getFreeAgainTime() {
            return freeAgainTime;
        }

        public int getId() {
            return id;
        }

        public double getChannelSpeed() {
            return channelSpeed;
        }

        public void setChannelSpeed(double channelSpeeds) {
            this.channelSpeed = channelSpeeds;
        }
    }
}
