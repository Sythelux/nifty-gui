package de.lessvoid.nifty.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.NiftyInputControl;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.EffectManager;
import de.lessvoid.nifty.effects.general.Effect;
import de.lessvoid.nifty.effects.shared.Falloff;
import de.lessvoid.nifty.elements.render.ElementRenderer;
import de.lessvoid.nifty.layout.LayoutPart;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.layout.align.VerticalAlign;
import de.lessvoid.nifty.layout.manager.LayoutManager;
import de.lessvoid.nifty.render.RenderEngine;
import de.lessvoid.nifty.render.RenderState;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.tools.TimeProvider;

/**
 * The Element.
 * @author void
 */
public class Element {

  /**
   * Time before we start an automated click when mouse button is holded.
   */
  private static final long REPEATED_CLICK_START_TIME = 350;

  /**
   * The time between automatic clicks.
   */
  private static final long REPEATED_CLICK_TIME = 150;

  /**
   * the logger.
   */
  private static Logger log = Logger.getLogger(Element.class.getName());

  /**
   * our identification.
   */
  private String id;

  /**
   * the parent element.
   */
  private Element parent;

  /**
   * the child elements.
   */
  private ArrayList < Element > elements = new ArrayList < Element >();

  /**
   * The LayoutManager we should use for all child elements.
   */
  private LayoutManager layoutManager;

  /**
   * The LayoutPart for layout this element.
   */
  private LayoutPart layoutPart;

  /**
   * The ElementRenderer we should use to render this element.
   */
  private ElementRenderer[] elementRenderer;

  /**
   * the effect manager for this element.
   */
  private EffectManager effectManager;

  /**
   * the fall off for hover effects.
   */
  private Falloff falloff;

  /**
   * method that should be invoked when someone clicks on this element.
   */
  private MethodInvoker onClickMethod = new MethodInvoker();

  /**
   * method that should be invoked when someone clicks and moves the cursor on this element.
   */
  private MethodInvoker onClickMouseMoveMethod = new MethodInvoker();

  /**
   * Nifty instance this element is attached to.
   */
  private Nifty nifty;

  /**
   * The focus handler this element is attached to.
   */
  private MouseFocusHandler focusHandler;

  /**
   * enable element.
   */
  private boolean enabled;

  /**
   * visible element.
   */
  private boolean visible;

  /**
   * this is set to true, when there's no interaction with the element
   * possible. this happens when the onEndScreen effect starts.
   */
  private boolean done;

  /**
   * mouse down flag.
   */
  private boolean mouseDown;

  /**
   * on click alternate key.
   */
  private String onClickAlternateKey;

  /**
   * visible to mouse events.
   */
  private boolean visibleToMouseEvents;

  /**
   * Last position of mouse X.
   */
  private int lastMouseX;

  /**
   * Last position of mouse Y.
   */
  private int lastMouseY;

  /**
   * mouse first click down time.
   */
  private long mouseDownTime;

  /**
   * Time the last repeat has been activated.
   */
  private long lastRepeatStartTime;

  /**
   * repeat on click.
   */
  private boolean onClickRepeat;

  /**
   * clip children.
   */
  private boolean clipChildren;

  /**
   * attached control when this element is an control.
   */
  private NiftyInputControl attachedInputControl = null;

  /**
   * remember if we've calculated this constraint ourself.
   */
  private boolean isCalcWidthConstraint;

  /**
   * remember if we've calculated this constraint ourself.
   */
  private boolean isCalcHeightConstraint;

  /**
   * construct new instance of Element.
   * @param newId the id
   * @param newParent new parent
   * @param newFocusHandler the new focus handler
   * @param newVisibleToMouseEvents TODO
   * @param newElementRenderer the element renderer
   */
  public Element(
      final String newId,
      final Element newParent,
      final MouseFocusHandler newFocusHandler,
      final boolean newVisibleToMouseEvents,
      final ElementRenderer ... newElementRenderer) {
    initialize(newId, newParent, newElementRenderer, new LayoutPart(), newFocusHandler, newVisibleToMouseEvents);
  }

  /**
   * construct new instance of Element using the given layoutPart instance.
   * @param newId the id
   * @param newParent new parent
   * @param newLayoutPart the layout part
   * @param newFocusHandler the new focus handler
   * @param newVisibleToMouseEvents TODO
   * @param newElementRenderer the element renderer
   */
  public Element(
      final String newId,
      final Element newParent,
      final LayoutPart newLayoutPart,
      final MouseFocusHandler newFocusHandler,
      final boolean newVisibleToMouseEvents,
      final ElementRenderer ... newElementRenderer) {
    initialize(newId, newParent, newElementRenderer, newLayoutPart, newFocusHandler, newVisibleToMouseEvents);
  }

  /**
   * initialize this instance helper.
   * @param newId the id
   * @param newParent TODO
   * @param newElementRenderer the element renderer to use
   * @param newLayoutPart the layoutPart to use
   * @param newFocusHandler the focus handler that this element is attached to
   * @param newVisibleToMouseEvents TODO
   */
  private void initialize(
      final String newId,
      final Element newParent,
      final ElementRenderer[] newElementRenderer,
      final LayoutPart newLayoutPart,
      final MouseFocusHandler newFocusHandler,
      final boolean newVisibleToMouseEvents) {
    this.id = newId;
    this.parent = newParent;
    this.elementRenderer = newElementRenderer;
    this.effectManager = new EffectManager();
    this.layoutPart = newLayoutPart;
    this.enabled = true;
    this.visible = true;
    this.done = false;
    this.onClickAlternateKey = null;
    this.focusHandler = newFocusHandler;
    this.visibleToMouseEvents = newVisibleToMouseEvents;
    this.setMouseDown(false, 0);
  }

  /**
   * get the id of this element.
   * @return the id
   */
  public final String getId() {
    return id;
  }

  /**
   * get parent.
   * @return parent
   */
  public final Element getParent() {
    return parent;
  }

  /**
   * get element state as string.
   * @return the element state as string.
   */
  public final String getElementStateString() {
    String pos =
      "[" + getX() + "," + getY() + ") (" + getWidth() + "," + getHeight() + "] "
      + "[" + outputSizeValue(layoutPart.getBoxConstraints().getX())
      + "," + outputSizeValue(layoutPart.getBoxConstraints().getY()) + ") "
      + "(" + outputSizeValue(layoutPart.getBoxConstraints().getWidth()) + ","
      + outputSizeValue(layoutPart.getBoxConstraints().getHeight()) + "]";
    if (isEffectActive(EffectEventId.onStartScreen)) {
      return pos + "(starting)";
    }

    if (isEffectActive(EffectEventId.onEndScreen)) {
      return pos + "(ending)";
    }

    if (!visible) {
      return pos + "(hidden)";
    }

    return pos + "normal";
  }

  /**
   * Output SizeValue.
   * @param value SizeValue
   * @return value string
   */
  private String outputSizeValue(final SizeValue value) {
    if (value == null) {
      return "null";
    } else {
      return value.toString();
    }
  }
  /**
   * get x.
   * @return x position of this element.
   */
  public final int getX() {
    return layoutPart.getBox().getX();
  }

  /**
   * get y.
   * @return the y position of this element.
   */
  public final int getY() {
    return layoutPart.getBox().getY();
  }

  /**
   * get height.
   * @return the height of this element.
   */
  public final int getHeight() {
    return layoutPart.getBox().getHeight();
  }

  /**
   * get width.
   * @return the width of this element.
   */
  public final int getWidth() {
    return layoutPart.getBox().getWidth();
  }

  /**
   * get all child elements of this element.
   * @return the list of child elements
   */
  public final List < Element > getElements() {
    return elements;
  }

  /**
   * add a child element.
   * @param widget the child to add
   */
  public final void add(final Element widget) {
    elements.add(widget);
  }

  /**
   * render this element.
   * @param r the RenderDevice to use
   */
  public final void render(final RenderEngine r) {
    // render element only when it is visible
    if (visible) {
      r.saveState(RenderState.allStates());

      // begin rendering / pre
      effectManager.begin(r);
      effectManager.renderPre(r);

      // render element
      if (elementRenderer != null) {
        for (ElementRenderer renderer : elementRenderer) {
          renderer.render(this, r);
        }
      }

      // finish rendering / post
      effectManager.renderPost(r);
      effectManager.end(r);

      if (clipChildren) {
        r.enableClip(getX(), getY(), getX() + getWidth(), getY() + getHeight());
      }

      // now render child elements
      for (int i = 0; i < elements.size(); i++) {
        Element p = elements.get(i);
        p.render(r);
      }

      if (clipChildren) {
        r.disableClip();
      }

      r.restoreState();
    }
  }

  /**
   * Set a new LayoutManager.
   * @param newLayout the new LayoutManager to use.
   */
  public final void setLayoutManager(final LayoutManager newLayout) {
    this.layoutManager = newLayout;
  }

  /**
   * layout this element and all it's children.
   */
  public final void layoutElements() {
    preProcessConstraintWidth();
    preProcessConstraintHeight();

    if (layoutManager != null) {
      // we need a list of LayoutPart and not of Element, so we'll build one on the fly here
      List < LayoutPart > layoutPartChild = new ArrayList < LayoutPart >();
      for (Element w : elements) {
        layoutPartChild.add(w.layoutPart);
      }

      // use out layoutManager to layout our children
      layoutManager.layoutElements(layoutPart, layoutPartChild);

      // repeat this step for all child elements
      for (Element w : elements) {
        w.layoutElements();
      }
    }
  }

  /**
   * pre-process constraint width.
   */
  private void preProcessConstraintWidth() {
    for (Element e : elements) {
      e.preProcessConstraintWidth();
    }

    // try the original width value first
    SizeValue myWidth = getConstraintWidth();

    // is it empty and we have an layoutManager there's still hope for a width constraint
    if (layoutManager != null && (myWidth == null || isCalcWidthConstraint)) {

      // collect all child layoutPart that have a fixed pixel size in a list
      List < LayoutPart > layoutPartChild = new ArrayList < LayoutPart >();
      for (Element e : elements) {
        SizeValue childWidth = e.getConstraintWidth();
        if (childWidth != null && childWidth.isPixel()) {
          layoutPartChild.add(e.layoutPart);
        }
      }

      // if all (!) child elements have a pixel fixed width we can calculate a new width constraint for this element!
      if (elements.size() == layoutPartChild.size()) {
        SizeValue newWidth = layoutManager.calculateConstraintWidth(layoutPartChild);
        if (newWidth != null) {
          log.info("pre processed new width for element: " + getId() + ": " + newWidth.getValueAsInt(0));
          setConstraintWidth(newWidth);
          isCalcWidthConstraint = true;
        }
      }
    }
  }

  /**
   * pre process constraint height.
   */
  private void preProcessConstraintHeight() {
    for (Element e : elements) {
      e.preProcessConstraintHeight();
    }

    // try the original height value first
    SizeValue myHeight = getConstraintHeight();

    // is it empty and we have an layoutManager there's still hope for a height constraint
    if (layoutManager != null && (myHeight == null || isCalcHeightConstraint)) {

      // collect all child layoutPart that have a fixed px size in a list
      List < LayoutPart > layoutPartChild = new ArrayList < LayoutPart >();
      for (Element e : elements) {
        SizeValue childHeight = e.getConstraintHeight();
        if (childHeight != null && childHeight.isPixel()) {
          layoutPartChild.add(e.layoutPart);
        }
      }

      // if all (!) child elements have a px fixed height we can calculate a new height constraint for this element!
      if (elements.size() == layoutPartChild.size()) {
        SizeValue newHeight = layoutManager.calculateConstraintHeight(layoutPartChild);
        if (newHeight != null) {
          log.info("pre processed new height for element: " + getId() + ": " + newHeight.getValueAsInt(0));
          setConstraintHeight(newHeight);
          isCalcHeightConstraint = true;
        }
      }
    }
  }

  /**
   * reset all effects.
   */
  public final void resetEffects() {
    effectManager.reset();

    for (Element w : elements) {
      w.resetEffects();
    }
  }

  /**
   * set new x position constraint.
   * @param newX new x constaint.
   */
  public final void setConstraintX(final SizeValue newX) {
    layoutPart.getBoxConstraints().setX(newX);
  }

  /**
   * set new y position constraint.
   * @param newY new y constaint.
   */
  public final void setConstraintY(final SizeValue newY) {
    layoutPart.getBoxConstraints().setY(newY);
  }

  /**
   * set new width constraint.
   * @param newWidth new width constraint.
   */
  public final void setConstraintWidth(final SizeValue newWidth) {
    layoutPart.getBoxConstraints().setWidth(newWidth);
  }

  /**
   * set new height constraint.
   * @param newHeight new height constraint.
   */
  public final void setConstraintHeight(final SizeValue newHeight) {
    layoutPart.getBoxConstraints().setHeight(newHeight);
  }

  /**
   * get current width constraint.
   * @return current width constraint
   */
  public final SizeValue getConstraintWidth() {
    return layoutPart.getBoxConstraints().getWidth();
  }

  /**
   * get current height constraint.
   * @return current height constraint.
   */
  public final SizeValue getConstraintHeight() {
    return layoutPart.getBoxConstraints().getHeight();
  }

  /**
   * set new horizontal align.
   * @param newHorizontalAlign new horizontal align.
   */
  public final void setConstraintHorizontalAlign(final HorizontalAlign newHorizontalAlign) {
    layoutPart.getBoxConstraints().setHorizontalAlign(newHorizontalAlign);
  }

  /**
   * set new vertical align.
   * @param newVerticalAlign new vertical align.
   */
  public final void setConstraintVerticalAlign(final VerticalAlign newVerticalAlign) {
    layoutPart.getBoxConstraints().setVerticalAlign(newVerticalAlign);
  }

  /**
   * get current horizontal align.
   * @return current horizontal align.
   */
  public final HorizontalAlign getConstraintHorizontalAlign() {
    return layoutPart.getBoxConstraints().getHorizontalAlign();
  }

  /**
   * get current vertical align.
   * @return current vertical align.
   */
  public final VerticalAlign getConstraintVerticalAlign() {
    return layoutPart.getBoxConstraints().getVerticalAlign();
  }

  /**
   * register an effect for this element.
   * @param theId the effect id
   * @param e the effect
   */
  public final void registerEffect(
      final EffectEventId theId,
      final Effect e) {
    log.info("register: " + theId.toString() + "(" + e.getStateString() + ") for Element: " + this.getId());
    effectManager.registerEffect(theId, e);
  }

  /**
   * on start screen event.
   * @param effectEventId the effect event id to start
   * @param time current time
   * @param effectEndNotiy the EffectEndNotify event we should activate
   */
  public final void startEffect(
      final EffectEventId effectEventId,
      final TimeProvider time,
      final EndNotify effectEndNotiy) {

    if (effectEventId == EffectEventId.onStartScreen) {
      done = false;
    }

    if (effectEventId == EffectEventId.onEndScreen) {
      done = true;
    }

    // whenever the effect ends we forward to this event
    // that checks first, if all child elements are finished
    // and when yes forwards to the actual effectEndNotify event.
    //
    // this way we ensure that all child finished the effects
    // before forwarding this to the real event handler.
    //
    // little bit tricky though :/
    LocalEndNotify forwardToSelf = new LocalEndNotify(effectEventId, effectEndNotiy);

    // start the effect for ourself
    effectManager.startEffect(effectEventId, this, time, forwardToSelf);

    // notify all child elements of the start effect
    for (Element w : getElements()) {
      w.startEffect(effectEventId, time, forwardToSelf);
    }

    if (effectEventId == EffectEventId.onFocus) {
      if (attachedInputControl != null) {
        attachedInputControl.onFocus(true);
      }
    }
  }

  /**
   * stop the given effect.
   * @param effectEventId effect event id to stop
   */
  public final void stopEffect(final EffectEventId effectEventId) {
    effectManager.stopEffect(effectEventId);

    // notify all child elements of the start effect
    for (Element w : getElements()) {
      w.stopEffect(effectEventId);
    }

    if (effectEventId == EffectEventId.onFocus) {
      if (attachedInputControl != null) {
        attachedInputControl.onFocus(false);
      }
    }
  }

  /**
   * check if a certain effect is still active. travels down to child elements.
   * @param effectEventId the effect type id to check
   * @return true, if the effect has ended and false otherwise
   */
  public final boolean isEffectActive(final EffectEventId effectEventId) {
    for (Element w : getElements()) {
      if (w.isEffectActive(effectEventId)) {
        return true;
      }
    }
    return effectManager.isActive(effectEventId);
  }

  /**
   * enable this element.
   */
  public final void enable() {
    enabled = true;
  }

  /**
   * disable this element.
   */
  public final void disable() {
    enabled = false;
  }

  /**
   * is this element enabled?
   * @return true, if enabled and false otherwise.
   */
  public final boolean isEnabled() {
    return enabled;
  }

  /**
   * show this element.
   */
  public final void show() {
    visible = true;
  }

  /**
   * hide this element.
   */
  public final void hide() {
    visible = false;
  }

  /**
   * check if this element is visible.
   * @return true, if this element is visible and false otherwise.
   */
  public final boolean isVisible() {
    return visible;
  }

  /**
   * set a new Falloff.
   * @param newFalloff new Falloff
   */
  public final void setHotSpotFalloff(final Falloff newFalloff) {
    this.falloff = newFalloff;
  }

  /**
   * mouse event handler.
   * @param x x position of mouse
   * @param y y position of mouse
   * @param leftMouseDown leftMouse button down
   * @param eventTime time this event occured in ms
   */
  public final void mouseEvent(final int x, final int y, final boolean leftMouseDown, final long eventTime) {
    // can't interact while onStartScreen is active
    if (isEffectActive(EffectEventId.onStartScreen)
        ||
        isEffectActive(EffectEventId.onEndScreen)
        ||
        !visible
        ||
        done) {
      return;
    }

    if (visibleToMouseEvents) {
      // if some other element has exclusive access to mouse events we are done
      if (focusHandler != null && !focusHandler.canProcessMouseEvents(this)) {
        return;
      }

      if (!done) {
        if (onClickRepeat) {
          if (isInside(x, y) && isMouseDown() && leftMouseDown) {
            long deltaTime = eventTime - mouseDownTime;
            if (deltaTime > REPEATED_CLICK_START_TIME) {
              long pastTime = deltaTime - REPEATED_CLICK_START_TIME;
              long repeatTime = pastTime - lastRepeatStartTime;
              if (repeatTime > REPEATED_CLICK_TIME) {
                onClick(x, y);
                lastRepeatStartTime = pastTime;
              }
            }
          }
        }

        if (isInside(x, y) && !isMouseDown()) {
          if (leftMouseDown) {
            setMouseDown(true, eventTime);
            if (focusHandler != null) {
              focusHandler.requestExclusiveFocus(this);
            }
            effectManager.startEffect(EffectEventId.onClick, this, new TimeProvider(), null);
            onClick(x, y);
          }
        } else if (!leftMouseDown && isMouseDown()) {
            setMouseDown(false, eventTime);
            effectManager.stopEffect(EffectEventId.onClick);
            if (focusHandler != null) {
              focusHandler.lostFocus(this);
            }
        }

        if (isMouseDown()) {
          onClickMouseMove(x, y);
        }
      }

      if (!isMouseDown()) {
        effectManager.handleHover(this, x, y);
      }
    }
    for (Element w : getElements()) {
      w.mouseEvent(x, y, leftMouseDown, eventTime);
    }
  }

  /**
   * checks to see if the given mouse position is inside of this element.
   * @param x the x position
   * @param y the y position
   * @return true when inside, false otherwise
   */
  private boolean isInside(final int x, final int y) {
    if (falloff != null) {
      return falloff.isInside(this, x, y);
    } else {
      return
        x >= getX()
        &&
        x <= (getX() + getWidth())
        &&
        y > (getY())
        &&
        y < (getY() + getHeight());
    }
  }

  /**
   * on click method.
   * @param mouseX TODO
   * @param mouseY TODO
   */
  public final void onClick(final int mouseX, final int mouseY) {
    lastMouseX = mouseX;
    lastMouseY = mouseY;

    if (onClickMethod != null) {
      nifty.setAlternateKey(onClickAlternateKey);
      onClickMethod.invoke(mouseX, mouseY);
    }
  }

  /**
   */
  public void onClick() {
    onClickMethod.invoke();
  }

  /**
   * on click mouse move method.
   * @param mouseX TODO
   * @param mouseY TODO
   */
  private void onClickMouseMove(final int mouseX, final int mouseY) {
    if (lastMouseX == mouseX && lastMouseY == mouseY) {
      return;
    }

    lastMouseX = mouseX;
    lastMouseY = mouseY;

    if (onClickMouseMoveMethod != null) {
      onClickMouseMoveMethod.invoke(mouseX, mouseY);
    }
  }

  /**
   * set on click method for the given screen.
   * @param methodInvoker the method to invoke
   * @param useRepeat repeat on click (true) or single event (false)
   */
  public final void setOnClickMethod(final MethodInvoker methodInvoker, final boolean useRepeat) {
    this.onClickMethod = methodInvoker;
    this.onClickRepeat = useRepeat;
  }

  /**
   * Set on click mouse move method.
   * @param methodInvoker the method to invoke
   */
  public void setOnClickMouseMoveMethod(final MethodInvoker methodInvoker) {
    this.onClickMouseMoveMethod = methodInvoker;
  }

  /**
   * set mouse down.
   * @param newMouseDown new state of mouse button.
   * @param eventTime the time in ms the event occured
   */
  private void setMouseDown(final boolean newMouseDown, final long eventTime) {
    this.mouseDownTime = eventTime;
    this.lastRepeatStartTime = 0;
    this.mouseDown = newMouseDown;
  }

  /**
   * is mouse down.
   * @return mouse down state.
   */
  private boolean isMouseDown() {
    return mouseDown;
  }

  /**
   * set screen controller.
   * @param newNifty the screen
   */
  public final void bindToScreen(final Nifty newNifty) {
    this.nifty = newNifty;
  }

  /**
   * find an element by name.
   *
   * @param name the name of the element (id)
   * @return the element or null
   */
  public final Element findElementByName(final String name) {
    if (id != null && id.equals(name)) {
      return this;
    }

    for (Element e : elements) {
      Element found = e.findElementByName(name);
      if (found != null) {
        return found;
      }
    }

    return null;
  }

  /**
   * set a new alternate key.
   * @param newAlternateKey new alternate key to use
   */
  public void setOnClickAlternateKey(final String newAlternateKey) {
    this.onClickAlternateKey = newAlternateKey;
  }

  /**
   * set alternate key.
   * @param alternateKey new alternate key
   */
  public void setAlternateKey(final String alternateKey) {
    effectManager.setAlternateKey(alternateKey);

    for (Element e : elements) {
      e.setAlternateKey(alternateKey);
    }
  }

  /**
   * get the effect manager.
   * @return the EffectManager
   */
  public EffectManager getEffectManager() {
    return effectManager;
  }

  /**
   * On start screen event.
   */
  public void onStartScreen() {
    if (attachedInputControl != null) {
      attachedInputControl.onStartScreen();
    }

    for (Element e : elements) {
      e.onStartScreen();
    }
  }

  /**
   *
   * @param <T> the ElementRenderer type
   * @param requestedRendererClass the special ElementRenderer type to check for
   * @return the ElementRenderer that matches the class
   */
  public < T extends ElementRenderer > T getRenderer(final Class < T > requestedRendererClass) {
    for (ElementRenderer renderer : elementRenderer) {
      if (requestedRendererClass.isInstance(renderer)) {
        return requestedRendererClass.cast(renderer);
      }
    }
    return null;
  }

  /**
   * Set visible to mouse flag.
   * @param newVisibleToMouseEvents true or false
   */
  public void setVisibleToMouseEvents(final boolean newVisibleToMouseEvents) {
    this.visibleToMouseEvents = newVisibleToMouseEvents;
  }

  /**
   * key event.
   * @param eventKey eventKey
   * @param eventCharacter eventCharacter
   * @param keyDown keyDown
   */
  public void keyEvent(final int eventKey, final char eventCharacter, final boolean keyDown) {
    if (attachedInputControl != null) {
      attachedInputControl.keyEvent(eventKey, eventCharacter, keyDown);
    }
  }

  /**
   * Set clip children.
   * @param clipChildrenParam clip children flag
   */
  public void setClipChildren(final boolean clipChildrenParam) {
    this.clipChildren = clipChildrenParam;
  }

  /**
   * Set the focus to this element.
   */
  public void setFocus() {
    nifty.getCurrentScreen().setFocus(this);
  }

  /**
   * attach an input control to this element.
   * @param newInputControl input control
   */
  public void attachInputControl(final NiftyInputControl newInputControl) {
    attachedInputControl = newInputControl;
  }
  
  public void attachPopup(final ScreenController screenController) {
    attach(onClickMethod, screenController);
    attach(onClickMouseMoveMethod, screenController);
  }

  private void attach(final MethodInvoker method, final ScreenController screenController) {
    method.setFirst(screenController);
    for (Element e : elements) {
      e.attachPopup(screenController);
    }
  }

  public class LocalEndNotify implements EndNotify {
    private EffectEventId effectEventId;
    private EndNotify effectEndNotiy;

    public LocalEndNotify(final EffectEventId effectEventIdParam, final EndNotify effectEndNotiyParam) {
      effectEventId = effectEventIdParam;
      effectEndNotiy = effectEndNotiyParam;
    }

    public void perform() {
      // notify parent if:
      // a) the effect is done for ourself
      // b) the effect is done for all of our children
      if (!isEffectActive(effectEventId)) {
        // all fine. we can notify the actual event handler
        if (effectEndNotiy != null) {
          effectEndNotiy.perform();
        }
      }
    }
  }

  /**
   * Get HotSpotFalloff.
   * @return Falloff
   */
  public Falloff getFalloff() {
    return falloff;
  }

  /**
   * set id.
   * @param newId new id
   */
  public void setId(final String newId) {
    this.id = newId;
  }

  public void remove() {
    parent.removeChild(this);
    if (focusHandler != null) {
      focusHandler.lostFocus(this);
    }
  }

  private void removeChild(final Element element) {
    elements.remove(element);
  }
}