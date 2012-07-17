/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Nodes;

import Grid.Interfaces.Messages.GridMessage;
import Grid.Port.GridOutPort;
import java.util.ArrayList;

/**
 *
 * @author Frank
 */
public class LambdaChannelGroup 
{
    
    private GridOutPort gridOutPort;
    private int wavelengthID;
    private ArrayList<Channel> channels;

    public LambdaChannelGroup(GridOutPort gridOutPort, int wavelengthID) 
    {
        this.gridOutPort = gridOutPort;
        this.wavelengthID = wavelengthID;
        channels =  new ArrayList<Channel>();
    }
    
    public boolean  isWavelengthFree(double bandwidthRequested,double time )
    {
        double bandwidthFree = gridOutPort.getLinkSpeed() ;
        for (Channel channel: channels)
        {
            if(channel.getFreeAgainTime()>time)
            {
                bandwidthFree-=channel.getChannelSpeed(); // Resta lo ocupado.
            }
        }
        if(bandwidthFree>=bandwidthRequested)
        {
            return true;
        }        
        return false;
    }
    public Channel reserve(double bandwidthRequested, double time, GridMessage message )
    {
        if(!isWavelengthFree(bandwidthRequested, time))
        {
            return null;
        }
        for (Channel channel: channels)
        {
            if(channel.getFreeAgainTime()<=time)                
            {
               configChannel(channel, bandwidthRequested, time, message);
               channels.remove(channel);
            }
        }
        
        Channel channel = new Channel(channels.size() );
        configChannel(channel, bandwidthRequested, time, message);
        channels.add(channel);
        return channel;
        
    }

    private void configChannel(Channel channel, double bandwidthRequested, double time, GridMessage message) {
        channel.setChannelSpeed(bandwidthRequested);                
        channel.setFreeAgainTime(time + (message.getSize()/bandwidthRequested));
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
    
    public static class Channel
    {
        int id;
        double channelSpeed=0;
        double freeAgainTime=0;
        
        public Channel(int id)
        {
            this.id= id;
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
