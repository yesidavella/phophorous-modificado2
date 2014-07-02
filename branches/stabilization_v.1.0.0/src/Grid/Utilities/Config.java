package Grid.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class Config extends Properties {

//    protected HtmlWriter writer;
    protected String fileName;
  
    File folder = new File("configFiles"); 
    File file = new File(folder, "ConfigInitAG2.cfg");

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
    public Config() {

        super();
        
       
        
        if (file.exists()) 
        {
            
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                load(fileInputStream);
            } catch (IOException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else {
            try {
                folder.mkdir();
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
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
                setProperty("simulationTime", "4000");
                setProperty("switchingSpeed", "100000");
                setProperty("findCommonWavelenght", "0.03");
                setProperty("OCS_SwitchingDelay", "0.01");
                setProperty("confirmOCSDelay", "0.0001");
                setProperty("ACKsize", "0.1");
                setProperty("OCSSetupHandleTime", "0.5");
                setProperty("linkSpeed", "100");
                setProperty("OBSHandleTime", "10"); 
                setProperty("defaultCpuCapacity", "100");
                save(fileOutputStream,"--Edit from SIM_AG2---" );
            } catch (IOException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }


    public void save() {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            save(fileOutputStream,"-- ---" );
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
}
