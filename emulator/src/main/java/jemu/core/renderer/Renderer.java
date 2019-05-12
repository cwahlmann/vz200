package jemu.core.renderer;

import jemu.core.device.*;
import jemu.ui.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class Renderer extends Device {

  protected Display display;
  protected int[] pixels;

  public Renderer(String type) {
    super(type);
  }

  public void setDisplay(Display value) {
    display = value;
    pixels = display.getPixels();
  }

  public Display getDisplay() {
    return display;
  }

  public int[] getPixels() {
    return pixels;
  }

}