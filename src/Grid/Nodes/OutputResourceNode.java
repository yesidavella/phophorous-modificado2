package Grid.Nodes;

import Distributions.DiscreteDistribution;
import Grid.GridSimulator;
import Grid.Interfaces.Messages.JobCompletedMessage;
import Grid.Interfaces.Messages.JobMessage;
import Grid.Nodes.OBS.OBSResourceNodeImpl;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import simbase.Port.SimBaseInPort;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class OutputResourceNode extends OBSResourceNodeImpl {

    private PrintWriter in;
    private PrintWriter out;

    public OutputResourceNode(String id, GridSimulator gridSim, DiscreteDistribution resultSizeDistribution) {
        super(id, gridSim, resultSizeDistribution);
        try {
            in = new PrintWriter(new BufferedWriter(new FileWriter("in.txt")));
            out = new PrintWriter(new BufferedWriter(new FileWriter("out.txt")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void handleJobCompletedMessage(JobCompletedMessage msg) {
        super.handleJobCompletedMessage(msg);
        StringBuffer buffer = new StringBuffer();
        buffer.append(currentTime.toString());
        buffer.append("\t");
        buffer.append(1);
        buffer.append("\t");
        buffer.append(1);
        out.write(buffer.toString());
        out.write("\n");
    }

    @Override
    protected void handleJobMessage(SimBaseInPort inPort, JobMessage message) {
        super.handleJobMessage(inPort, message);
        StringBuffer buffer = new StringBuffer();
        buffer.append(currentTime.toString());
        buffer.append("\t");
        buffer.append(2);
        buffer.append("\t");
        buffer.append(1);
        in.write(buffer.toString());
        in.write("\n");
    }

    @Override
    protected void finalize() throws Throwable {
        in.close();
        out.close();
        super.finalize();
    }
    
}
