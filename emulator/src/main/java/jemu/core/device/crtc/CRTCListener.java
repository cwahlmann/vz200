package jemu.core.device.crtc;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public interface CRTCListener {

  public void hSyncStart();

  public void hSyncEnd();

  public void vDispStart();
  
  public void hDispStart();

  public void vSyncStart();

  public void vSyncEnd();

  public void hDispEnd();
  
  public void cursor();
  
}