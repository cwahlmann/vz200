package jemu.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class EButton extends JButton implements TimerListener {
	private static final long serialVersionUID = 1L;
	protected int autoRepeatDelay = 1000;
	protected int autoRepeatTime = 50;
	protected Counter counter = null;
	protected boolean over = false;

	public EButton() {
		super();
	}

	/*
	 * public EButton(String text) { super(text); }
	 */

	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			over = true;
			if (autoRepeatDelay >= 0) {
				fireActionPerformed();
				startTimer();
			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
			stopTimer();
			over = false;
		} else if (e.getID() == MouseEvent.MOUSE_ENTERED)
			over = true;
		else if (e.getID() == MouseEvent.MOUSE_EXITED)
			over = false;
		super.processMouseEvent(e);
	}

	protected void fireActionPerformed() {
		fireActionPerformed(new ActionEvent(this, 0, null));
	}

	protected void fireActionPerformed(ActionEvent e) {
		if (autoRepeatDelay < 0 || over)
			super.fireActionPerformed(e);
	}

	protected void startTimer() {
		if (counter == null)
			counter = new Counter(this, autoRepeatDelay, null);
	}

	protected void stopTimer() {
		if (counter != null) {
			counter.stop();
			counter = null;
		}
	}

	public void setAutoRepeatDelay(int value) {
		autoRepeatDelay = value;
	}

	public int getAutoRepeatDelay() {
		return autoRepeatDelay;
	}

	public void setAutoRepeatTime(int value) {
		autoRepeatTime = value;
	}

	public int getAutoRepeatTime() {
		return autoRepeatTime;
	}

	public void timerTick(Counter counter) {
		counter.setDuration(autoRepeatTime);
		if (over && this.counter != null)
			fireActionPerformed();
	}

}