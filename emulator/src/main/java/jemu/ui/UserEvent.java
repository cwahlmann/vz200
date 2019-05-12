package jemu.ui;

import java.awt.AWTEvent;

/**
 * Title:        JEMU
 * Description:  The Java Emulation Platform
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

/**
 * This class provides an AWTEvent which can have arbitrary data associated with
 * it. It is used to provide the Event for the Timer class when a Counter
 * notifies the main Timer instance that it should notify the listeners of a
 * timed Event firing.
 *
 * @author Richard Wilson
 * @version 1.0
 */
public class UserEvent extends AWTEvent {
	private static final long serialVersionUID = 1L;

/**
   * The first available ID to use for the Event.
   */
  public static final int FIRST_ID = RESERVED_ID_MAX + 2001;

  /**
   * Data associated with the Event.
   */
  protected Object data;

  /**
   * Constructs a UserEvent.
   *
   * @param source The source for the Event
   * @param id The ID of the Event
   * @param data Data to be associated with the Event
   */
  public UserEvent(Object source, int id, Object data) {
    super(source,id);
    this.data = data;
  }

  /**
   * Gets the Data associated with the Event.
   *
   * @return The Data associated with the Event
   */
  public Object getData() {
    return data;
  }

}