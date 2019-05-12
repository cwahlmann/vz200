package jemu.core.renderer;

import jemu.core.*;
import jemu.core.renderer.Renderer;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */


/**
 * This class has logic to emulate a TV style monitor.
 */
public class MonitorRenderer extends Renderer {

  public static final int HSYNC_MIN         = 63 * 0x10000;
  public static final int HSYNC_MAX         = 65 * 0x10000;
  public static final int HSYNC_MID         = 64 * 0x10000;
  public static final int HSYNC_MAX_DEC     = 800 * 0x100;
  public static final int HSYNC_MAX_INC     = 800 * 0x100;
  public static final int HSYNC_ADJUST      = 0x08;
  public static final int HSYNC_FREE_ADJUST = 0x80;

  public static final int VHOLD_MIN       = 250;
  public static final int VHOLD_MAX       = 380;
  public static final int VHOLD_MID       = 295;
  public static final int VHOLD_MIN_RANGE = 46;
  public static final int VHOLD_MAX_RANGE = 74;

  protected long    frequency   = 1000000;       // 1MHz
  protected int     adder       = 0x10000;       // Clock Adder
  protected int     hPos        = 0;             // Current Horiztonal Position
  protected int     monHFree    = HSYNC_MID;     // Free running HSYNC position
  protected int     monHSync    = HSYNC_MID;     // Position of monitor HSYNC
  protected int     monHHalf    = HSYNC_MID / 2; // Centre for PLL
  protected boolean inHSync     = false;         // Currently in HSync?
  protected boolean hadHSync    = false;         // Had a HSync?
  protected int     hStart      = 0;             // Position of HSYNC Start
  protected int     hLess       = 0;             // Amount of SYNC before middle
  protected int     hMore       = 0;             // Amount of SYNC after middle

  protected int     vPos        = 0;             // Current Vertical Position
  protected int     monitorLine = 0;             // Current monitor scan line
  protected int     vAdjust     = 0;             // Vertical Adjust
  protected int     vSync       = 0;             // Length of VSYNC
  protected boolean inVSync     = false;         // Currently in VSYNC

  protected int     vSyncMin    = 0;             // Minimum VSYNC position
  protected int     vSyncMax    = 0;             // Maximum VSYNC position
  protected int     vSyncLen    = HSYNC_MID * 2; // Minimum VSYNC width

  protected boolean debug = false;

  public MonitorRenderer(String type) {
    super(type);
    setVHold(0);
  }

  public void setCycleFrequency(long value) {
    adder = (int)(0x10000L * 1000000L / value);
    System.out.println("Frequency=" + value + ", adder=" + adder);
  }

  public void hSyncStart() {
/*    if (debug)
      System.out.println("HSync start: " + Integer.toHexString(hPos)); */
    hStart = hPos;
    inHSync = hadHSync = true;
  }

  public void hSyncEnd() {
/*    if (debug)
      System.out.println("HSync end: " + Integer.toHexString(hPos)); */
    if (inHSync) {
      inHSync = false;
      adjustMoreLess(hPos);
    }
  }

  protected void adjustMoreLess(int pos) {
/*    if (debug)
      System.out.println("adjustMoreLess: " + Integer.toHexString(pos)); */
    if (pos <= monHHalf)
      hLess += pos - hStart;
    else if (hStart >= monHHalf)
      hMore += pos - hStart;
    else { // hStart < monHHalf && pos > monHHalf
      hLess += monHHalf - hStart;
      hMore += pos - monHHalf;
    }
  }

  public void vSyncStart() {
    if (!inVSync) {
      if (debug)
        System.out.println("vSync Start: " + vPos + ", hPos=" + hPos);
      vSync = -hPos;
      inVSync = true;
    }
  }

  public void vSyncEnd() {
    if (inVSync) {
      inVSync = false;
      vSync += hPos;
      checkVSync();
      if (debug)
        System.out.println("vSync End: " + vPos + ", hPos=" + hPos + ", length=" +
          Integer.toHexString(vSync));
    }
  }

  public void hSync() { }

  public void vSync(boolean interlace) { }

  public void clock() {
    hPos += adder;
    if (hPos >= monHSync) {
      // Adjust HSYNC
      if (inHSync) {
        adjustMoreLess(monHSync);
        hStart = 0;
      }
/*      if (debug)
        System.out.println("Monitor HSync: " + Integer.toHexString(monHSync) + ", more=" +
          Integer.toHexString(hMore) + ", less=" + Integer.toHexString(hLess)); */
      int adjust, base;
      if (hLess > hMore) {        // Increase Sync position
        int diff = hLess - hMore;
        adjust = Math.min(HSYNC_MAX_INC,diff / HSYNC_ADJUST);
        if (adjust == 0)
          adjust = diff;
        base = adjust / HSYNC_FREE_ADJUST;
        if (base == 0)
          base = 1;
      }
      else if (hMore > hLess) {
        int diff = hMore - hLess;
        adjust = -Math.min(HSYNC_MAX_DEC,diff / HSYNC_ADJUST);
        if (adjust == 0)
          adjust = -diff;
        base = adjust / HSYNC_FREE_ADJUST;
        if (base == 0)
          base = -1;
      }
      else if (hadHSync)
        // If the HSYNC is not over happening now, need to adjust (just a little)
        base = (adjust = inHSync ? 0 : 0x80) / HSYNC_FREE_ADJUST;
      else
        base = adjust = 0; //HSYNC_MID - monHFree;

      hPos -= monHSync;
      vSync += monHSync;
      monitorLine++;
      hSync();

/*      if (debug)
        System.out.println("Adjustments: inHSync=" + inHSync + ", hadHSync=" + hadHSync +
          ", adjust=" + Integer.toHexString(adjust) + ", base=" +
          Integer.toHexString(base) + ", free=" + Integer.toHexString(monHFree)); */
      monHSync = Math.max(HSYNC_MIN,Math.min(HSYNC_MAX,monHFree + adjust));
      monHFree = Math.max(HSYNC_MIN,Math.min(HSYNC_MAX,monHFree + base));
      //monHHalf = monHSync / 2;
      hStart = hMore = hLess = 0;
      hadHSync = inHSync;

      vPos++;
      if (inVSync)
        checkVSync();
    }
  }
  
  protected void checkVSync() {
    if (vPos >= vSyncMin) {
      if (vSync > monHSync || vPos >= vSyncMax) {
        boolean interlace = (vSync - monHSync) < monHHalf;
        monitorLine = (VHOLD_MIN - vPos) / 2 + vAdjust - (interlace ? 1 : 0);
        if (debug)
          System.out.println("VSync: vPos=" + vPos + ", VH-vP=" + (VHOLD_MIN - vPos) + ", vSync=" + Util.hex(vSync) + ", row=" +
            monitorLine + ", monHSync=" + Util.hex(monHSync) + ", inVSync=" + inVSync +
            ", interlace=" + interlace + ", hPos=" + hPos);
        vSync(interlace);
        vPos = 0;
      }
    }
  }

  public void setVHold(int value) {
    vSyncMin = value + VHOLD_MID;
    vSyncMax = vSyncMin + VHOLD_MIN_RANGE + (int)Math.round((vSyncMin - VHOLD_MIN) *
      (VHOLD_MAX_RANGE - VHOLD_MIN_RANGE) / (VHOLD_MAX - VHOLD_MIN));
    System.out.println("VHold=" + value + ", vSyncMin=" + vSyncMin + ", vSyncMax=" + vSyncMax);
  }
  
  public void setVerticalAdjust(int value) {
    vAdjust = value;
  }

}