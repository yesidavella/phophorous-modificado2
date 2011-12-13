/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Grid.Utilities;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class SampleAverage {
    
    private double average = 0.0d;
    
    private double square = 0.0d;
    
    private double numberOfSamples = 0.0d;

    public SampleAverage() {
    }

    /**
     * Return the sum of the samples, without the correction of the number 
     * of samples = Sum(samples).
     * @return The sum of the average
     */
    public double getTotalAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getNrOfSamples() {
        return numberOfSamples;
    }

    public void setNrOfSamples(double checked) {
        this.numberOfSamples = checked;
    }

    public double getSquare() {
        return square;
    }

    public void setSquare(double square) {
        this.square = square;
    }

    /**
     * Adds a sample.
     * @param sample The sample to add.
     */
    public void addSample(double sample){
        numberOfSamples++;
        average += sample;
        square += sample*sample;
    }
    
    /**
     * Retuns the average of this sample (Sum of samples / nr of samples)
     * @return The average of this sample.
     */
    public double getAverage(){
        return average/numberOfSamples;
    }
    
    

}
