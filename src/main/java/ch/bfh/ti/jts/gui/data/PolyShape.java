package ch.bfh.ti.jts.gui.data;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class PolyShape {
    
    private static final String SHAPE_REGEX_STRING = "^[-]?[0-9]+([.][0-9]+)[,][-]?[0-9]+([.][0-9]+)([ ][-]?[0-9]+([.][0-9]+)[,][-]?[0-9]+([.][0-9]+))*$";
    private List<Point2D>       points;
    private Shape               shape;
    private double              length;
    private boolean             closedPath;
    private Point2D             position;
    private double              orientation;
    
    public PolyShape(List<Point2D> points, final boolean closedPath) {
        if (points == null) {
            throw new IllegalArgumentException("points is null");
        }
        if (points.size() == 0) {
            throw new IllegalArgumentException("points is empty");
        }
        this.closedPath = closedPath;
        this.points = points;
        this.shape = buildShape();
        this.length = buildLength();
    }
    
    public PolyShape(List<Point2D> points) {
        this(points, false);
    }
    
    public PolyShape(final String shapeString, final boolean closedPath) {
        this(buildPoints(shapeString), closedPath);
    }
    
    public PolyShape(final String shapeString) {
        this(buildPoints(shapeString), false);
    }
    
    public Shape getShape() {
        return shape;
    }
    
    public Point2D getStartPoint() {
        return points.get(0);
    }
    
    public Point2D getEndPoint() {
        return points.get(points.size() - 1);
    }
    
    public double getLength() {
        return length;
    }
    
    public Point2D getRelativePosition(double relative) {
        calculate(relative);
        return position;
    }
    
    public double getRelativeOrientation(double relative) {
        calculate(relative);
        return orientation;
    }
    
    private void calculate(final double relative) {
        if (relative < 0.0 || relative > 1.0) {
            throw new IllegalArgumentException("relative is out of bounds");
        }
        if (points.size() == 2) {
            double x = getStartPoint().getX() + relative * (getEndPoint().getX() - getStartPoint().getX());
            double y = getStartPoint().getY() + relative * (getEndPoint().getY() - getStartPoint().getY());
            position = new Point2D.Double(x, y);
            orientation = getAngleBetweenTwoPoints(getStartPoint(), getEndPoint());
        }
        double lengthOnPolyline = relative * length;
        followPolygon(lengthOnPolyline, 0);
    }
    
    private void followPolygon(final double distanceToFollow, final int segment) {
        double distanceOnSegment = getSegmentLength(segment);
        if (distanceToFollow <= distanceOnSegment) {
            double relativePositionOnSegment = distanceToFollow / distanceOnSegment;
            Point2D segmentStart = points.get(segment);
            Point2D segmentEnd = points.get(segment + 1);
            double x = segmentStart.getX() + relativePositionOnSegment * (segmentEnd.getX() - segmentStart.getX());
            double y = segmentStart.getY() + relativePositionOnSegment * (segmentEnd.getY() - segmentStart.getY());
            position = new Point2D.Double(x, y);
            orientation = getAngleBetweenTwoPoints(segmentStart, segmentEnd);
        } else {
            // pass junction and switch to an other lane
            double distanceToDriveOnNextSegment = distanceToFollow - distanceOnSegment;
            followPolygon(distanceToDriveOnNextSegment, segment + 1);
        }
    }
    
    private static List<Point2D> buildPoints(final String shapeString) {
        if (shapeString == null) {
            throw new IllegalArgumentException("shapeString is null");
        }
        if (!Pattern.matches(SHAPE_REGEX_STRING, shapeString)) {
            throw new IllegalArgumentException("shapeString has wrong format");
        }
        List<Point2D> pointlist = new LinkedList<Point2D>();
        final String[] points = shapeString.split(" ");
        if (points.length == 0) {
            throw new IllegalArgumentException("shapeString has wrong format");
        }
        for (int i = 0; i < points.length; i++) {
            final String point = points[i];
            final String[] coordinates = point.split(",");
            if (coordinates.length != 2) {
                throw new IllegalArgumentException("invalid coordinates");
            }
            Point2D newPoint = new Point2D.Double(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));
            pointlist.add(newPoint);
        }
        return pointlist;
    }
    
    private double getAngleBetweenTwoPoints(final Point2D p1, final Point2D p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        return Math.atan2(dy, dx);
    }
    
    private Shape buildShape() {
        final Path2D path = new Path2D.Double();
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            if (i == 0) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }
        if (closedPath) {
            path.closePath();
        }
        return path;
    }
    
    private double buildLength() {
        double length = 0;
        if (points.size() > 1) {
            Point2D last = points.get(0);
            for (int i = 1; i < points.size(); i++) {
                Point2D current = points.get(i);
                length += current.distance(last);
                last = current;
            }
        }
        return length;
    }
    
    private double getSegmentLength(final int index) {
        if (index < 0 || index > points.size() - 2) {
            throw new IndexOutOfBoundsException("index");
        }
        return points.get(index).distance(points.get(index + 1));
    }
}
