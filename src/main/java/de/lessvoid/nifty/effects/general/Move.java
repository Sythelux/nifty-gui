package de.lessvoid.nifty.effects.general;

import java.util.Properties;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.render.RenderEngine;

/**
 * Move - move stuff around.
 * @author void
 */
public class Move implements EffectImpl {

  /**
   * left.
   */
  private static final String LEFT = "left";

  /**
   * right.
   */
  private static final String RIGHT = "right";

  /**
   * top.
   */
  private static final String TOP = "top";

  /**
   * bottom.
   */
  private static final String BOTTOM = "bottom";

  /**
   * direction param.
   */
  private String direction;

  /**
   * offset helper.
   */
  private long offset = 0;

  /**
   * start offset helper.
   */
  private long startOffset = 0;

  /**
   * direction: in or out movement.
   */
  private int offsetDir = 0;

  private float offsetY;

  private float startOffsetY;

  private int startOffsetX;

  private float offsetX;
  
  private boolean withTarget = false;

  /**
   * Initialize.
   * @param nifty Nifty
   * @param element Element
   * @param parameter Properties
   */
  public final void initialize(final Nifty nifty, final Element element, final Properties parameter) {
    direction = parameter.getProperty("direction");
    if (LEFT.equals(direction)) {
      offset = element.getX() + element.getWidth();
    } else if (RIGHT.equals(direction)) {
      offset = element.getX() + element.getWidth();
    } else if (TOP.equals(direction)) {
      offset = element.getY() + element.getHeight();
    } else if (BOTTOM.equals(direction)) {
      offset = element.getY() + element.getHeight();
    } else {
      offset = 0;
    }

    String mode = parameter.getProperty("mode");
    if ("out".equals(mode)) {
      startOffset = 0;
      offsetDir = -1;
      withTarget = false;
    } else if ("in".equals(mode)) {
      startOffset = offset;
      offsetDir = 1;
      withTarget = false;
    } else if ("fromPosition".equals(mode)) {
      withTarget = true;
    } else if ("toPosition".equals(mode)) {
      withTarget = true;
    }

    String target = parameter.getProperty("targetElement");
    if (target != null) {
      Element targetElement = nifty.getCurrentScreen().findElementByName(target);

      if ("fromPosition".equals(mode)) {
        startOffsetX = targetElement.getX()- element.getX();
        startOffsetY = targetElement.getY()- element.getY();
        offsetX = -(targetElement.getX() - element.getX());
        offsetY = -(targetElement.getY() - element.getY());
      } else if ("toPosition".equals(mode)) {
        startOffsetX = 0;
        startOffsetY = 0;
        offsetX = (targetElement.getX() - element.getX());
        offsetY = (targetElement.getY() - element.getY());
      }
    }
  }

  /**
   * execute the effect.
   * @param element Element
   * @param normalizedTime TimeInterpolator
   * @param r RenderDevice
   */
  public void execute(final Element element, final float normalizedTime, final RenderEngine r) {
    if (withTarget) {
      float moveToX = startOffsetX + normalizedTime * offsetX;
      float moveToY = startOffsetY + normalizedTime * offsetY;
      r.moveTo(moveToX, moveToY);
    } else {
      if (LEFT.equals(direction)) {
        r.moveTo(-startOffset + offsetDir * normalizedTime * offset, 0);
      } else if (RIGHT.equals(direction)) {
        r.moveTo(startOffset - offsetDir * normalizedTime * offset, 0);
      } else if (TOP.equals(direction)) {
        r.moveTo(0, -startOffset + offsetDir * normalizedTime * offset);
      } else if (BOTTOM.equals(direction)) {
        r.moveTo(0, startOffset - offsetDir * normalizedTime * offset);
      }
    }
  }
}