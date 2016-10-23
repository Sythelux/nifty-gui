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
package de.lessvoid.nifty.examples.usecase;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyConfiguration;
import de.lessvoid.nifty.examples.dummy.DummyInputDevice;
import de.lessvoid.nifty.examples.dummy.DummyRenderDevice;
import de.lessvoid.nifty.input.lwjgl.NiftyInputDeviceLWJGL;
import de.lessvoid.nifty.renderer.java2d.NiftyInputDeviceJava;
import de.lessvoid.nifty.renderer.java2d.NiftyRenderDeviceJava2D;
import de.lessvoid.nifty.time.AccurateTimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

import java.awt.*;

import static de.lessvoid.nifty.Nifty.createNifty;

public class UseCaseRunnerAdapterJava2D implements UseCaseRunnerAdapter {
    private static Logger log = LoggerFactory.getLogger(UseCaseRunnerAdapterDummy.class.getName());

    private boolean run = true;

    @Override
    public void run(final Class<?> useCaseClass, final String[] args, final NiftyConfiguration niftyConfiguration) throws Exception {
        // create nifty instance
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : ge.getScreenDevices()) {
            log.warn(device + "");
            for (GraphicsConfiguration configuration : device.getConfigurations()) {
                log.warn("\t" + configuration);
            }
        }
        NiftyRenderDeviceJava2D renderDeviceJava2D = new NiftyRenderDeviceJava2D(ge.getDefaultScreenDevice().getDefaultConfiguration());
        //graphicsConfig
        JFrame jf = new JFrame();
        jf.setSize(800, 600);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.add(renderDeviceJava2D);
        final Nifty nifty = createNifty(
                renderDeviceJava2D,
                new NiftyInputDeviceJava(),
                new AccurateTimeProvider(),
                niftyConfiguration);

        useCaseClass.getConstructor(Nifty.class).newInstance(nifty);

        jf.setVisible(true);
        nifty.update();
        nifty.render();

        logScene(nifty);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                run = false;
            }
        });

        while (run) {
            nifty.update();
            nifty.render();
        }

    }

    private void logScene(final Nifty nifty) {
        log.info(nifty.getSceneInfoLog());
    }

}
