package jemu.core.device.crtc;

import jemu.core.*;
import jemu.core.device.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
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