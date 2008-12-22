package de.lessvoid.nifty.effects.impl;

import java.util.Properties;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectImpl;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.tools.SizeValue;

/**
 * Hint - show hint.
 * @author void
 */
public class Hint implements EffectImpl {

  /**
   * nifty.
   */
  private Nifty nifty;

  /**
   * target element.
   */
  private Element targetElement;

  /**
   * hint text.
   */
  private String hintText;

  /**
   * initialize.
   * @param niftyParam Nifty
   * @param element Element
   * @param parameter Parameter
   */
  public void activate(final Nifty niftyParam, final Element element, final Properties parameter) {
    this.nifty = niftyParam;

    String target = parameter.getProperty("targetElement");
    if (target != null) {
      targetElement = nifty.getCurrentScreen().findElementByName(target);
    }

    String text = parameter.getProperty("hintText");
    if (text != null) {
      hintText = text;
    }
  }

  /**
   * execute the effect.
   * @param element the Element
   * @param normalizedTime TimeInterpolator to use
   * @param normalizedFalloff falloff value
   * @param r RenderDevice to use
   */
  public void execute(
      final Element element,
      final float normalizedTime,
      final Falloff falloff,
      final NiftyRenderEngine r) {
    if (targetElement != null) {
      TextRenderer textRenderer = targetElement.getRenderer(TextRenderer.class);
      textRenderer.changeText(hintText);
      targetElement.setConstraintWidth(new SizeValue(textRenderer.getTextWidth() + "px"));
      nifty.getCurrentScreen().layoutLayers();
    }
  }

  /**
   * deactivate the effect.
   */
  public void deactivate() {
  }
}