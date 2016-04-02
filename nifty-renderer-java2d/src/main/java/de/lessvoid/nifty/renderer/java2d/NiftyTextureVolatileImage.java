package de.lessvoid.nifty.renderer.java2d;

import de.lessvoid.nifty.spi.NiftyRenderDevice;
import de.lessvoid.nifty.spi.NiftyTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by sythelux on 29.03.16.
 */
public class NiftyTextureVolatileImage implements NiftyTexture {
    private static Logger Log = LoggerFactory.getLogger(NiftyTextureVolatileImage.class.getName());
    private int width;
    private int height;
    private ByteBuffer data;
    private NiftyRenderDevice.FilterMode filterMode;
    private VolatileImage volatileImage;

    public NiftyTextureVolatileImage() {
    }

    public NiftyTextureVolatileImage(int width, int height, GraphicsConfiguration gc) {
        this(width, height, null, null, gc);
    }

    public NiftyTextureVolatileImage(int width, int height, NiftyRenderDevice.FilterMode filterMode, GraphicsConfiguration gc) {
        this(width, height, null, filterMode, gc);
    }

    public NiftyTextureVolatileImage(int width, int height, ByteBuffer data, NiftyRenderDevice.FilterMode filterMode, GraphicsConfiguration gc) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.filterMode = filterMode;
        this.volatileImage = gc.createCompatibleVolatileImage(width, height);
    }

    public static NiftyTextureVolatileImage fromBufferedImage(BufferedImage bufferedImage, GraphicsConfiguration gc) throws AWTException {
        VolatileImage volatileImage = gc.createCompatibleVolatileImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getCapabilities(gc), bufferedImage.getTransparency());
        NiftyTextureVolatileImage niftyTextureVolatileImage = new NiftyTextureVolatileImage(volatileImage.getWidth(), volatileImage.getHeight(), gc);
        niftyTextureVolatileImage.setVolatileImage(volatileImage);
        return niftyTextureVolatileImage;
    }

    @Override
    public int getAtlasId() {
        return 0;
    }

    @Override
    public double getU0() {
        return 0;
    }

    @Override
    public double getV0() {
        return 0;
    }

    @Override
    public double getV1() {
        return 1.0;
    }

    @Override
    public double getU1() {
        return 1.0;
    }

    @Override
    public void saveAsPng(String filename) {
        try {
            if(!filename.endsWith("png")){
                filename+=".png";
            }
            File file = new File(filename);
            Log.info("saved as: " + file.getAbsolutePath());
            ImageIO.write(volatileImage.getSnapshot(), "png", file);
        } catch (IOException e) {
            Log.warn("", e);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public void update(ByteBuffer buffer) {
        this.data = buffer;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public NiftyRenderDevice.FilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(NiftyRenderDevice.FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public VolatileImage getVolatileImage() {
        return volatileImage;
    }

    public void setVolatileImage(VolatileImage volatileImage) {
        this.volatileImage = volatileImage;
    }

    public void paint(Graphics g) {
        g.drawImage(volatileImage, 0, 0, volatileImage.getWidth(), volatileImage.getHeight(), null);
    }
}
