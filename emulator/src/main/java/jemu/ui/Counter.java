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
 * This class provides a counter for a requested timed event to be generated.
 *
 * @author Richard Wilson
 * @version 1.0
 */
public class Counter {

  /**
   * The time in milliseconds betweeen the generated Events.
   */
  protected long duration;

  /**
   * The total time since the counter was started.
   */
  protected long total;

  /**
   * The time since the last Event was generated.
   */
  protected long count;

  /**
   * The time of the last Event.
   */
  protected long last;

  /**
   * The Object listening for the timed Event.
   */
  protected TimerListener listener;

  /**
   * Data associated with the Counter.
   */
  protected Object data;

  /**
   * Constructs a Counter and starts sending Events after the given duration.
   *
   * @param listener The Object listening for the timed Event
   * @param duration The time in milliseconds between each Event
   * @param data Data to be associated with the Counter
   */
  public Counter(TimerListener listener, long duration, Object data) {
    this.listener = listener;
    this.duration = duration;
    this.data = data;
    last = System.currentTimeMillis();
    total = 0;
    Timer.addCounter(this);
  }

  /**
   * Sets the duration (time between Events) for the Counter.
   *
   * @param value The time in milliseconds between Events
   */
  public void setDuration(long value) {
    duration = value;
  }

  /**
   * Gets the duration (time between Events) for the Counter.
   *
   * @return The time in milliseconds between Events
   */
  public long getDuration() {
    return duration;
  }

  /**
   * Sets the Data associated with the Counter.
   *
   * @param value The Data to be associated with the Counter
   */
  public void setData(Object value) {
    data = value;
  }

  /**
   * Gets the Data associated with the Counter.
   *
   * @return The Data associated with the Counter
   */
  public Object getData() {
    return data;
  }

  /**
   * Gets the total time of Events fired.
   *
   * @return The total time of Events fired
   */
  public long getTime() {
    return total;
  }

  /**
   * Stops the Counter.
   */
  public void stop() {
    Timer.removeCounter(this);
  }

  /**
   * Increments internal counters and posts the Event if necessary.
   *
   * @param timer The Timer controlling the timed Events
   * @param time The total time the Timer has been running
   */
  protected void tick(Timer timer, long time) {
    long diff = time - last;
    last = time;
    count += diff;
    total += diff;
    if (count >= duration && duration > 0) {
      timer.post(new UserEvent(timer,UserEvent.FIRST_ID,this));
      // This skips ticks when the timer is really busy
      while (count >= duration && duration > 0)
        count -= duration;
    }
  }

}