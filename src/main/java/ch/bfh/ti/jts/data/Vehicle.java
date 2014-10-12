package ch.bfh.ti.jts.data;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class Vehicle {
    
    /**
     * Minimal acceleration (inclusive) [m/s^2]
     */
    private double              minAcceleration;
    /**
     * Max acceleration (inclusive) [m/s^^]
     */
    private double              maxAcceleration;
    /**
     * Minimal velocity (inclusive) [m/s], 0 := agent can't reverse.
     */
    private double              minVelocity;
    /**
     * Max velocity (inclusive) [m/s]
     */
    private double              maxVelocity;
    private final static double SIZE  = 3.0;
    private final static Shape  SHAPE = new Ellipse2D.Double(-SIZE / 2, -SIZE / 2, SIZE, SIZE);
    
    public Vehicle() {
        this(-5, 5, 0, 33.3);
    }
    
    public Vehicle(final double minAcceleration, final double maxAcceleration, final double minVelocity, final double maxVelocity) {
        this.minAcceleration = minAcceleration;
        this.maxAcceleration = maxAcceleration;
        this.minVelocity = minVelocity;
        this.maxVelocity = maxVelocity;
    }
    
    public Shape getShape() {
        return SHAPE;
    }
    
    public double getMaxAcceleration() {
        return maxAcceleration;
    }
    
    public double getMinAcceleration() {
        return minAcceleration;
    }
    
    public double getMaxVelocity() {
        return maxVelocity;
    }
    
    public double getMinVelocity() {
        return minVelocity;
    }
}
