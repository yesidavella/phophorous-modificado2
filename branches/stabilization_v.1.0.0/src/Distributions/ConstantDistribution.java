/*
 * A class which only returns a constant.Call the
 * {@link #sample()} method to generate samples.
 */
package Distributions;

/**
 *
 * @author Jens Buysse - Jens.Buysse@intec.ugent.be
 */
public class ConstantDistribution extends DiscreteDistribution {

    /**
     * The constant which should be given.
     */
    private double constant;

    /**
     * Creates a distribution which only returns a constant.
     * @param constant The constant which it should return. 
     */
    public ConstantDistribution(double constant) {
        this.constant = constant;
    }

    
    
    /**
     * Return the constant
     * @return The constant.
     */
    @Override
    public long sample() {
        return (long)constant;
    }

    /**
     * Return the constant
     * @return The constant.
     */
    @Override
    public double sampleDouble() {
        return constant;
    }

    /**
     * Return a String representation.
     * @return The string representation of this distribution.
     */
    @Override
    public String toString() {
        return "ConstantDistribution for the constant : "+ constant;
    }

    /**
     * Returns the constant.
     * @return The constant.
     */
    public double getConstant() {
        return constant;
    }

    /**
     * Sets the constant.
     * @param constant The constant which should be returned by this distribution.
     */
    public void setConstant(double constant) {
        this.constant = constant;
    }
    
    
}
