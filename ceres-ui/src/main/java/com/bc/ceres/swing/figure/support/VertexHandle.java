package com.bc.ceres.swing.figure.support;

import com.bc.ceres.swing.figure.AbstractHandle;
import com.bc.ceres.swing.figure.Figure;
import com.bc.ceres.swing.figure.FigureStyle;
import static com.bc.ceres.swing.figure.support.StyleDefaults.*;

import java.awt.Shape;
import java.awt.geom.Path2D;


public class VertexHandle extends AbstractHandle {
    private final int segmentIndex;

    public VertexHandle(Figure figure,
                        int vertexIndex,
                        FigureStyle style,
                        FigureStyle selectedStyle) {
        super(figure, style, selectedStyle);
        this.segmentIndex = vertexIndex;
        updateLocation();
        setShape(createHandleShape());
    }

    @Override
    public void updateLocation() {
        final double[] segment = getFigure().getSegment(segmentIndex);
        if (segment != null) {
            setLocation(segment[0], segment[1]);
        }
    }

    @Override
    public void move(double dx, double dy) {
        setLocation(getX() + dx, getY() + dy);
        final double[] segment = getFigure().getSegment(segmentIndex);
        if (segment != null) {
            segment[0] += dx;
            segment[1] += dy;
            getFigure().setSegment(segmentIndex, segment);
        }

    }

    private static Shape createHandleShape() {
        Path2D path = new Path2D.Double();
        path.moveTo(0.0, -0.5 * VERTEX_HANDLE_SIZE);
        path.lineTo(0.5 * VERTEX_HANDLE_SIZE, 0.0);
        path.lineTo(0.0, 0.5 * VERTEX_HANDLE_SIZE);
        path.lineTo(-0.5 * VERTEX_HANDLE_SIZE, 0.0);
        path.closePath();
        return path;
    }
}