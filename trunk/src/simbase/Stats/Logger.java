/* Changelog
 * ---------
 * 
 * Version 1.0
 * 	- Extracted the logging facility from SimBaseSimulator
 */
package simbase.Stats;

import Grid.Utilities.HtmlWriter;
import java.io.Serializable;
import java.text.NumberFormat;
import simbase.Time;

/**
 * A logging facility.
 * 
 * @author Jens Buysse
 * @version 1.0
 */
public class Logger implements Serializable{

    public final static int BLACK = 0;
    public final static int GREEN = 1;
    public final static int BLUE = 2;
    public final static int YELLOW = 3;
    public final static int ORANGE = 4;
    public final static int RED = 5;
    public final static int BROWN = 6;
    public final static int PURPLE = 7;
    /**
     * The stream to print to.
     */
    private HtmlWriter out;
    /**
     * The length of the timestamp in the log
     */
    private int timeLength = 12;
    /**
     * The timestamp formatter.
     */
    private NumberFormat formatter = NumberFormat.getInstance();

    /**
     * Constructor. Creates a log with given timelength and printstream.
     * 
     * @param timeLength
     *            the length of the timestamp in the log
     * @param out
     *            the printstream to write to.
     * @since 1.0
     */
     public void close(){
         out.close();
     }
    public Logger(int timeLength, HtmlWriter out) {
        this.timeLength = timeLength;
        this.out = out;
        formatter.setMinimumIntegerDigits(timeLength);
        formatter.setMaximumFractionDigits(3);
        formatter.setMinimumFractionDigits(3);
        formatter.setGroupingUsed(true);
      
    }

    /**
     * Log an event.
     * 
     * @param time
     *            timestamp
     * @param log
     *            log text
     * @since 1.0
     */
    public void putLog(Time time, String log) 
    {
      
        if (time != null) {
            out.println(formatter.format(time.getTime()) + ": " + log +
                    "<br>");
        } else {
            out.println(repeat(" ", timeLength + 2) + log);
        }

    }

    /**
     * Repeats a string for a given times
     * 
     * @param src
     *            the string to repeat
     * @param repeat
     *            the times it has to be repeated
     * @return the repeated string
     * @since 1.0
     */
    private String repeat(String src, int repeat) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < repeat; i++) {
            buf.append(src);
        }
        return buf.toString();
    }

    public void logToStdOutput(Time time, String log) {
        System.out.println(formatter.format(time.getTime()) + ": " + log);
    }
}
