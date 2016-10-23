/*
 * Copyright (c) 2015, Nifty GUI Community
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are 
 * met: 
 * 
 *  * Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer. 
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.lessvoid.nifty.renderer.java2d;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.lessvoid.nifty.types.NiftyColor;
import de.lessvoid.nifty.types.NiftyCompositeOperation;
import de.lessvoid.nifty.types.NiftyLineCapType;
import de.lessvoid.nifty.types.NiftyLineJoinType;
import de.lessvoid.niftyinternal.NiftyResourceLoader;
import de.lessvoid.nifty.spi.NiftyRenderDevice;
import de.lessvoid.nifty.spi.NiftyTexture;
import de.lessvoid.niftyinternal.render.batch.TextureBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

public class NiftyRenderDeviceJava2D extends Canvas implements NiftyRenderDevice {
    private static Logger Log = LoggerFactory.getLogger(NiftyRenderDeviceJava2D.class.getName());
    private NiftyResourceLoader niftyResourceLoader;
    private boolean clearScreenBeforeRender;
    private NiftyCompositeOperation compositeOperation;
    private VolatileImage offscreen;
/*    private List<NiftyTexture> textureBuffer = Collections.synchronizedList(new ArrayList<NiftyTexture>());
    private List<FloatBuffer> verticesBuffer = Collections.synchronizedList(new ArrayList<FloatBuffer>());
    private List<GradientQuad> gradientQuadBuffer = Collections.synchronizedList(new ArrayList<GradientQuad>());*/

    public NiftyRenderDeviceJava2D(GraphicsConfiguration config) {
        super(config);
        offscreen = config.createCompatibleVolatileImage((int) config.getBounds().getWidth(), (int) config.getBounds().getHeight());
    }

    @Override
    public void setResourceLoader(@Nonnull NiftyResourceLoader niftyResourceLoader) {
        this.niftyResourceLoader = niftyResourceLoader;
    }

    @Override
    public int getDisplayWidth() {
        return getWidth();
    }

    @Override
    public int getDisplayHeight() {
        return getHeight();
    }

    @Override
    public void clearScreenBeforeRender(boolean clearScreenBeforeRender) {
        this.clearScreenBeforeRender = clearScreenBeforeRender;
    }

    @Override
    public void beginRender() {
        Log.info("beginRender " + "");

        if (clearScreenBeforeRender) {
            Graphics2D graphics2D = offscreen.createGraphics();
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
        }

        changeCompositeOperation(NiftyCompositeOperation.SourceOver);
    }

    @Override
    public void endRender() {
        Log.info("endRender " + "");
    }

    @Override
    public NiftyTexture createTexture(int width, int height, FilterMode filterMode) {
        Log.info("createTexture " + "width = [" + width + "], height = [" + height + "], filterMode = [" + filterMode + "]");
        return new NiftyTextureVolatileImage(width, height, filterMode, super.getGraphicsConfiguration());
    }

    @Override
    public NiftyTexture createTexture(int width, int height, ByteBuffer data, FilterMode filterMode) {
        Log.info("createTexture " + "width = [" + width + "], height = [" + height + "], data = [" + data + "], filterMode = [" + filterMode + "]");
        return new NiftyTextureVolatileImage(width, height, data, filterMode, super.getGraphicsConfiguration());
    }

    @Override
    public NiftyTexture loadTexture(String filename, FilterMode filterMode, PreMultipliedAlphaMode preMultipliedAlphaMode) {
        Log.info("loadTexture " + "filename = [" + filename + "], filterMode = [" + filterMode + "], preMultipliedAlphaMode = [" + preMultipliedAlphaMode + "]");
        try {
            if (filename != null) {
                URL resource = niftyResourceLoader.getResource(filename);
                if (resource != null) {
                    NiftyTextureVolatileImage image = NiftyTextureVolatileImage.fromBufferedImage(ImageIO.read(resource), super.getGraphicsConfiguration());
                    image.setFilterMode(filterMode);
                }
            }
        } catch (IOException | AWTException e) {
            Log.warn("", e);
        }
        return null;
    }

    @Override
    public void renderTexturedQuads(NiftyTexture texture, FloatBuffer vertices) {
        Log.info("renderTexturedQuads " + "texture = [" + texture + "], vertices = [" + vertices + "]");
        Graphics2D graphics2D = offscreen.createGraphics();
        internal(texture).paint(graphics2D);
        drawVertices(vertices, graphics2D);
        revalidate();
        repaint();
    }

    @Override
    public void renderColorQuads(FloatBuffer vertices) {
        Log.info("renderColorQuads " + "vertices = [" + vertices + "]");
        drawVertices(vertices, offscreen.createGraphics());
        revalidate();
        repaint();
    }

    @Override
    public void renderLinearGradientQuads(double x0, double y0, double x1, double y1, List<ColorStop> colorStops, FloatBuffer vertices) {
        Log.info("renderLinearGradientQuads " + "x0 = [" + x0 + "], y0 = [" + y0 + "], x1 = [" + x1 + "], y1 = [" + y1 + "], colorStops = [" + colorStops + "], vertices = [" + vertices + "]");
        if (colorStops.size() >= 2) {//TODO: multicolor
            GradientPaint gradientPaint = new GradientPaint((float) x0, (float) y0, niftyColorToAWTColor(colorStops.get(0).getColor()), (float) x1, (float) y1, niftyColorToAWTColor(colorStops.get(1).getColor()));
            new GradientQuad(gradientPaint, vertices).paint(offscreen.createGraphics());
        }
        revalidate();
        repaint();
    }

    @Override
    public void beginRenderToTexture(NiftyTexture texture) {
        Log.info("beginRenderToTexture " + "texture = [" + texture + "]");
        internal(texture).paint(offscreen.createGraphics());
    }

    @Override
    public void endRenderToTexture(NiftyTexture texture) {
        Log.info("endRenderToTexture " + "texture = [" + texture + "]");
    }

    @Override
    public void maskBegin() {
        Log.info("maskBegin " + "");
    }

    @Override
    public void maskEnd() {
        Log.info("maskEnd " + "");
    }

    @Override
    public void maskClear() {
        Log.info("maskClear " + "");
    }

    @Override
    public void changeCompositeOperation(NiftyCompositeOperation compositeOperation) {
        this.compositeOperation = compositeOperation;
        Log.info("changeCompositeOperation " + "compositeOperation = [" + compositeOperation + "]");
    }

    @Override
    public String loadCustomShader(String filename) {
        Log.info("loadCustomShader " + "filename = [" + filename + "]");
        return null;
    }

    @Override
    public void activateCustomShader(String shaderId) {
        Log.info("activateCustomShader " + "shaderId = [" + shaderId + "]");
    }

    @Override
    public void maskRenderLines(FloatBuffer b, float lineWidth, NiftyLineCapType lineCapType, NiftyLineJoinType lineJoinType) {
        Log.info("maskRenderLines " + "b = [" + b + "], lineWidth = [" + lineWidth + "], lineCapType = [" + lineCapType + "], lineJoinType = [" + lineJoinType + "]");
    }

    @Override
    public void maskRenderFill(FloatBuffer vertices) {
        Log.info("maskRenderFill " + "vertices = [" + vertices + "]");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Log.info("paint " + "g = [" + g + "]");
        Graphics2D g2d = (Graphics2D) g;
        if (offscreen != null) {
            g2d.drawImage(offscreen, 0, 0, null);
        }
        /*if (clearScreenBeforeRender) {
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        for (NiftyTexture niftyTexture : textureBuffer) {
            niftyTexture.saveAsPng("niftyTexture");
            internal(niftyTexture).paint(g2d);
        }
        for (GradientQuad gradientQuad : gradientQuadBuffer) {
            gradientQuad.paint(g2d);
        }*/

        //textureBuffer.clear();
    }

    private NiftyTextureVolatileImage internal(final NiftyTexture texture) {
        return (NiftyTextureVolatileImage) texture;
    }

    public Color niftyColorToAWTColor(NiftyColor niftyColor) {
        return new Color((float) niftyColor.getRed(), (float) niftyColor.getGreen(), (float) niftyColor.getBlue(), (float) niftyColor.getAlpha());
    }

    public static void drawVertices(FloatBuffer vertices, Graphics2D g2d) {
        float[] buf = new float[vertices.position() / TextureBatch.PRIMITIVE_SIZE * 6];
        vertices.flip();
        GeneralPath path = new GeneralPath();
        vertices.get(buf);
        Log.info(Arrays.toString(buf));
        path.moveTo(buf[0], buf[1]);
        while (vertices.hasRemaining()) {
            vertices.get(buf);
            Log.info(Arrays.toString(buf));
            path.lineTo(buf[0], buf[1]);
        }
        path.closePath();
        g2d.draw(path);
    }
}
