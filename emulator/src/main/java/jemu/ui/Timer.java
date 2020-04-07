package jemu.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class Timer extends Component implements Runnable {
	private static final long serialVersionUID = 1L;

protected Vector<AWTEvent> events = new Vector<>(1);

  /**
   * The Timer instance.
   */
  private static Timer timer = new Timer();

  /**
   * The current Timer Thread.
   */
  protected Thread timerThread = null;

  /**
   * A Vector containing all Counter instances currently running.
   */
  protected Vector<Counter> counters = new Vector<>(1);

  /**
   * Constructs a Timer.
   */
  private Timer() {
    enableEvents(ComponentEvent.COMPONENT_EVENT_MASK);
  }

  public synchronized void post(UserEvent event) {
    events.addElement(event);
    setVisible(!isVisible());
  }

  /**
   * Adds a Counter to the Timer.
   *
   * @param counter The Counter to be added
   */
  protected static void addCounter(Counter counter) {
    synchronized(timer) {
      timer.counters.addElement(counter);
      timer.checkThread();
    }
  }

  /**
   * Removes a Counter from the Timer.
   *
   * @param counter The Counter to be removed
   */
  protected static void removeCounter(Counter counter) {
    synchronized(timer) {
      timer.counters.removeElement(counter);
    }
  }

  /**
   * Ensures that the Timer Thread is running.
   */
  protected void checkThread() {
    if (timerThread == null) {
      timerThread = new Thread(this);
      //timerThread.setPriority(Thread.NORM_PRIORITY);
      timerThread.start();
    }
  }

  /**
   * Processes an AWTEvent (A timed Event), passing the notification on to the
   * TimerListener for the Counter.
   *
   * @param e The AWTEvent (UserEvent)
   */
  public void processEvent(AWTEvent e) {
    UserEvent event = null;
    synchronized(this) {
      if (events.size() > 0) {
        event = (UserEvent)events.firstElement();
        events.removeElementAt(0);
      }
    }
    if (event != null) {
      Counter counter = (Counter)event.getData();
      counter.listener.timerTick(counter);
    }
  }

  /**
   * Implements the run() method of the Runnable instance for the Timer Thread.
   * Cycles through the list of Counters, calling their tick method
   * periodically.
   */
  public void run() {
    int count;
    do {
      synchronized(this) {
        count = counters.size();
        if (count == 0)
          timerThread = null;
        else {
          long time = System.currentTimeMillis();
          for (int i = 0; i < count; i++)
            ((Counter)counters.elementAt(i)).tick(this,time);
        }
        try {
          Thread.sleep(10);
        } catch (Exception e) { }
      }
    } while (count != 0);
  }

}