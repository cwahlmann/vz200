package jemu.ui;

import java.awt.AWTEvent;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
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