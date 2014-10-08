package ch.bfh.ti.jts.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ch.bfh.ti.jts.ai.Brain;
import ch.bfh.ti.jts.ai.Decision;
import ch.bfh.ti.jts.ai.Thinkable;
import ch.bfh.ti.jts.simulation.Simulatable;

public class Agent extends Element implements Thinkable, Simulatable {
    
    public final static int     AGENT_LAYER = Junction.JUNCTION_LAYER + 1;
    private final static double size        = 3.0;
    private final static Shape  shape       = new Ellipse2D.Double(-size / 2, -size / 2, size, size);
    private Brain               brain;
    private Lane                lane;
    /**
     * The relative position of the agent on the lane (>= 0.0 and < 1.0)
     */
    private double              position    = 0;
    /**
     * The velocity of an agent in m/s
     */
    private double              velocity    = 14;
    
    public Agent() {
    }
    
    public void setBrain(final Brain brain) {
        this.brain = brain;
    }
    
    public Brain getBrain() {
        return brain;
    }
    
    public void setLane(final Lane lane) {
        this.lane = lane;
        lane.getAgents().add(this);
    }
    
    public Lane getLane() {
        return lane;
    }
    
    public void setPosition(final Double position) {
        if (position < 0 || position > 1.0) {
            throw new IllegalArgumentException("position");
        }
        this.position = position;
    }
    
    public double getPosition() {
        return position;
    }
    
    private double getX() {
        final Junction start = getLane().getEdge().getStart();
        final Junction end = getLane().getEdge().getEnd();
        return start.getX() + getPosition() * (end.getX() - start.getX());
    }
    
    private double getY() {
        final Junction start = getLane().getEdge().getStart();
        final Junction end = getLane().getEdge().getEnd();
        return start.getY() + getPosition() * (end.getY() - start.getY());
    }
    
    public void setVelocity(final Double velocity) {
        this.velocity = velocity;
    }
    
    public double getVelocity() {
        return velocity;
    }
    
    @Override
    public int getLayer() {
        return AGENT_LAYER;
    }
    
    /**
     * Gets the color of the agent by his velocity.
     *
     * @return color
     */
    private Color getColor() {
        final double MAX_VELOCITY = 50.0;
        float hue = 0;
        if (getVelocity() <= MAX_VELOCITY) {
            hue = 0.33f - (float) (getVelocity() / MAX_VELOCITY * 0.33);
        }
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }
    
    @Override
    public void render(final Graphics2D g) {
        final double x = getX();
        final double y = getY();
        g.setStroke(new BasicStroke(1));
        g.setColor(getColor());
        g.translate(x, y);
        g.fill(shape);
        g.translate(-x, -y);
    }
    
    @Override
    public void think(final Decision decision) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void simulate(final Duration duration, final Decision decision) {
        final double distanceToDrive = getVelocity() * duration.getNano() * 10E-9;
        followLane(getLane(), distanceToDrive);
    }
    
    private void followLane(final Lane currentLane, final double distanceToDrive) {
        final double lengthLane = currentLane.getLength();
        final double distanceOnLaneLeft = lengthLane * (1 - getPosition());
        if (distanceToDrive <= distanceOnLaneLeft) {
            // stay on this lane
            setLane(currentLane);
            setPosition(getPosition() + distanceToDrive / lengthLane);
        } else {
            // pass junction and switch to an other lane
            final double distanceToDriveOnNewLane = distanceToDrive - distanceOnLaneLeft;
            final Edge currentEdge = currentLane.getEdge();
            final Junction currentJunction = currentEdge.getEnd();
            // TODO: decide on which lane to go. atm take any random
            final List<Edge> nextEdges = currentJunction.getEdges().stream().filter(x -> x.comesFrom(currentJunction)).collect(Collectors.toList());
            Collections.shuffle(nextEdges);
            final Edge nextEdge = nextEdges.get(0);
            if (nextEdge == null) {
                throw new RuntimeException("no edge to go to");
            }
            // get first lane
            final Lane nextLane = nextEdge.getLanes().stream().findFirst().get();
            if (nextLane == null) {
                throw new RuntimeException("edge has no lanes!");
            }
            setPosition(0.0);
            followLane(nextLane, distanceToDriveOnNewLane);
        }
    }
}
