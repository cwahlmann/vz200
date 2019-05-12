package jemu.ui;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

/**
 * This interface allows a class instance to listen for timed Events from a
 * Counter.
 *
 * @author Richard Wilson
 * @version 1.0
 */
public interface TimerListener {

  /**
   * Notification that a timed Event fired.
   *
   * @param counter The Counter which cause the Event to fire
   */
  public void timerTick(Counter counter);

}