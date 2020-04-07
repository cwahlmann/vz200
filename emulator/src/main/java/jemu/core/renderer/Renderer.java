package jemu.core.renderer;

import jemu.core.device.*;
import jemu.ui.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
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