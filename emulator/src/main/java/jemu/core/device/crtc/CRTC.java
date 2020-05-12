package jemu.core.device.crtc;

import jemu.core.*;
import jemu.core.device.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public abstract class CRTC extends Device {
  protected CRTCListener listener;

  public CRTC(String type) {
    super(type);
  }

  public void setCRTCListener(CRTCListener listener) {
    this.listener = listener;
  }

}