package de.lessvoid.nifty.renderer.java2d;

import java.awt.*;
import java.nio.FloatBuffer;

/**
 * Created by sythelux on 01.04.16.
 */
public class GradientQuad {
    private final GradientPaint gradientPaint;
    private final FloatBuffer vertices;

    public GradientQuad(GradientPaint gradientPaint, FloatBuffer vertices) {
        this.gradientPaint = gradientPaint;
        this.vertices = vertices;
    }

    public GradientPaint getGradientPaint() {
        return gradientPaint;
    }

    public FloatBuffer getVertices() {
        return vertices;
    }

    public void paint(Graphics2D g2d) {
        g2d.setPaint(gradientPaint);
        NiftyRenderDeviceJava2D.drawVertices(vertices, g2d);
    }
}
