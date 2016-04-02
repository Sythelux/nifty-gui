package de.lessvoid.nifty.renderer.java2d;

import de.lessvoid.nifty.input.NiftyInputConsumer;
import de.lessvoid.nifty.spi.NiftyInputDevice;
import de.lessvoid.niftyinternal.NiftyResourceLoader;

import javax.annotation.Nonnull;

/**
 * Created by sythelux on 29.03.16.
 */
public class NiftyInputDeviceJava implements NiftyInputDevice {
    @Override
    public void setResourceLoader(@Nonnull NiftyResourceLoader niftyResourceLoader) {

    }

    @Override
    public void forwardEvents(@Nonnull NiftyInputConsumer inputEventConsumer) {

    }

    @Override
    public void setMousePosition(int x, int y) {

    }
}
