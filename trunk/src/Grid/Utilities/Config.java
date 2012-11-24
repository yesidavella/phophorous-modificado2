package Grid.Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class Config extends Properties {

//    protected HtmlWriter writer;
    protected String fileName;

    public enum ConfigEnum {
        //Simulation time

        simulationTime,
        //true if output to html file, false if not
        //  output,
        //Offset for checking if the simulation can stop.
        stopEventOffSetTime,
        switchingSpeed, defaultWavelengths,
        //The size of control messages. If control messages are 0, the are being send immediately
        ACKsize, OBSHandleTime,
        defaultCapacity, defaultCPUCount, defaultQueueSize,
        defaultFlopSize, defaultDataSize, defaultJobIAT, maxDelay, outputFileName,
        OCSSetupHandleTime,
        allocateWavelenght,
        findCommonWavelenght,
        linkSpeed,
        OCS_SwitchingDelay,
        confirmOCSDelay
    }

    /**
     * Constructor
     *
     * @param defaults
     */
    public Config(Properties defaults) {
        super(defaults);
    }

    public Config() {
        super();

        setProperty("stopEventOffSetTime", "100");
        setProperty("defaultWavelengths", "253");
        setProperty("maxDelay", "10");
        setProperty("clientTrafficPriority", "5");
        setProperty("defaultFlopSize", "500");
        setProperty("defaultResultSize", "200");
        setProperty("defaultDataSize", "10");
        setProperty("routedViaJUNG", "true");
        setProperty("defaultCPUCount", "3");
        setProperty("defaultJobIAT", "200");
        setProperty("output", "true");
        setProperty("allocateWavelenght", "0.01");
        setProperty("outputFileName", "resultado.html");
        setProperty("defaultQueueSize", "20");
        setProperty("simulationTime", "2000");
        setProperty("switchingSpeed", "100000");
        setProperty("findCommonWavelenght", "0.03");
        setProperty("OCS_SwitchingDelay", "0.01"); 
        setProperty("confirmOCSDelay", "0.0001");
        setProperty("ACKsize", "0.1"); 
        setProperty("OCSSetupHandleTime", "0.5");
        setProperty("linkSpeed", "100");
        setProperty("OBSHandleTime", "10");
        setProperty("defaultCpuCapacity", "100");
             //loadProperties(fileName);
                //        try {
                //           // writer = new HtmlWriter(getProperty(ConfigEnum.outputFileName.toString()));
                //        } catch (IOException e) {
                //            System.err.println(e.getMessage());
                //        }

    }

//    public void loadProperties() {
//        loadProperties(fileName);
//    }

//    public void loadProperties(String fileName) {
//        try {
//            FileInputStream in = new FileInputStream(fileName);
//            this.load(in);
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        }
//    }

    public double getDoubleProperty(Config.ConfigEnum key) {
        String propertie = this.getProperty(key.toString());
        if (propertie == null) {
            throw new IllegalArgumentException(key.toString() + " is not in the config file");
        }
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
