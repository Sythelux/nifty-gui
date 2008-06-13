package de.lessvoid.nifty.render;

import de.lessvoid.nifty.tools.Color;

/**
 * RenderImage interface.
 * @author void
 */
public interface RenderImage {

  /**
   * Get the width of the image.
   * @return width of image in pixel
   */
  int getWidth();

  /**
   * Get the height of the image.
   * @return height of image in pixel
   */
  int getHeight();

  /**
   * Render the image.
   * @param x x
   * @param y y
   * @param width w
   * @param height h
   * @param color color
   * @param imageScale image scale
   */
  void render(int x, int y, int width, int height, Color color, float imageScale);

  /**
   * Set the sub image mode for this image.
   * @param newSubImageMode the SubImageMode
   */
  void setSubImageMode(RenderImageSubImageMode newSubImageMode);

  /**
   * Set the sub image dimensions.
   * @param newSubImageX x
   * @param newSubImageY y
   * @param newSubImageW w
   * @param newSubImageH h
   */
  void setSubImage(int newSubImageX, int newSubImageY, int newSubImageW, int newSubImageH);

  /**
   * Resize hint.
   * @param resizeHint new String with resize hint information.
   */
  void setResizeHint(String resizeHint);
}
