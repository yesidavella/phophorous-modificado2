/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Grid.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class Config extends Properties {

//    protected HtmlWriter writer;

    public enum ConfigEnum {
        //Simulation time
        simulationTime,
        //true if output to html file, false if not
        output,
        //Offset for checking if the simulation can stop.
        stopEventOffSetTime,
        switchingSpeed, defaultWavelengths,
        //The size of control messages. If control messages are 0, the are being send immediately
        ACKsize, OBSHandleTime,
        defaultCapacity, defaultCPUCount, defaultQueueSize,
        defaultFlopSize, defaultDataSize, defaultJobIAT, maxDelay, outputFileName,
        OCSSetupHandleTime,linkSpeed
    
    }

    /**
     * Constructor
     * @param defaults
     */
    public Config(Properties defaults) {
        super(defaults);
    }

    public Config(String fileName) {
        super();
        loadProperties(fileName);
//        try {
//           // writer = new HtmlWriter(getProperty(ConfigEnum.outputFileName.toString()));
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        }

    }

    protected void loadProperties(String fileName) {
        try {
            FileInputStream in = new FileInputStream(fileName);
            this.load(in);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public double getDoubleProperty(Config.ConfigEnum key) {
        String propertie = this.getProperty(key.toString());
        if(propertie == null)
            throw new IllegalArgumentException(key.toString()+ " is not in the config file");
        return Double.parseDouble(propertie);
    }

    public boolean getBooleanProperty(Config.ConfigEnum key) {
        String propertie = this.getProperty(key.toString());
        return Boolean.parseBoolean(propertie);
    }

    public long getLongProperty(Config.ConfigEnum key) {
        String propertie = this.getProperty(key.toString());
        return Long.parseLong(propertie);
    }

    public int getIntProperty(Config.ConfigEnum key) {
        String propertie = this.getProperty(key.toString());
        return Integer.parseInt(propertie);
    }

//    public HtmlWriter getWriter() {
//        return writer;
//    }
//
//    public void setWriter(HtmlWriter writer) {
//        this.writer = writer;
//    }
}
