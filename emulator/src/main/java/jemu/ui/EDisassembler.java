package jemu.ui;

import java.awt.*;
import java.awt.event.*;
import jemu.core.device.*;
import jemu.util.diss.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class EDisassembler extends JComponent implements AdjustmentListener, TimerListener {
	private static final long serialVersionUID = 1L;

	protected static final Color selBackground = new Color(0xe0e0e0);

	protected JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL);
	protected int address = 0;
	protected Computer computer;
	protected int lineHeight = 0;
	protected int[] addresses = new int[0];
	protected int selAnchor = 0;
	protected int selStart = 0;
	protected int selEnd = 0;
	protected int timerY = 0;
	protected Counter counter = null;
	protected boolean inSet = false;

	public EDisassembler() {
		enableEvents(MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK);
		setBackground(Color.white);
		setForeground(Color.black);
		setBorder(new BevelBorder(BevelBorder.LOWERED));
		setFont(new Font("Courier", 0, 12));
		setLayout(new BorderLayout());
		scrollBar.addAdjustmentListener(this);
		scrollBar.setBlockIncrement(0x10);
		add(scrollBar, BorderLayout.EAST);
	}

	public void setComputer(Computer value) {
		computer = value;
		scrollBar.setMinimum(0);
		scrollBar.setMaximum(0x10010);
		scrollBar.setVisibleAmount(0x10);
		repaint();
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 200);
	}

	protected void paintComponent(Graphics g) {
		Insets insets = getInsets();
		Rectangle rect = new Rectangle(insets.left + 1, insets.top + 1,
				getWidth() - scrollBar.getWidth() - insets.left - insets.right - 2,
				getHeight() - insets.top - insets.bottom - 2);
		g.setColor(Color.black);
		g.drawRect(rect.x - 1, rect.y - 1, rect.width + 1, rect.height + 1);
		g.setColor(getBackground());
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
		g.setFont(getFont());
		FontMetrics fm = g.getFontMetrics();
		if (computer != null) {
			Disassembler diss = computer.getDisassembler();
			if (diss != null) {
				int a = fm.getAscent();
				int[] addr = new int[] { address };
				int h = lineHeight = fm.getHeight();
				addresses = new int[(rect.height + h - 1) / h];
				int n = 0;
				for (int y = rect.y; y < rect.y + rect.height; y += fm.getHeight()) {
					addresses[n++] = addr[0];
					if (addr[0] >= selStart && addr[0] <= selEnd) {
						g.setColor(selBackground);
						g.fillRect(0, y, rect.width, fm.getHeight());
					}
					String line = diss.disassemble(computer.getMemory(), addr, true, 30);
					g.setColor(getForeground());
					g.drawString(line, rect.x, y + a);
				}
				scrollBar.setVisibleAmount(rect.height / h);
			}
		}
	}

	public void setAddress(int value, boolean scroll) {
		address = Math.min(0xffff, value);
		repaint();
		if (scroll) {
			inSet = true;
			scrollBar.setValue(address);
			inSet = false;
		}
	}

	public void setAddress(int value) {
		setAddress(value, true);
	}

	public int getAddress(int y) {
		Insets insets = getInsets();
		y = (y - insets.top + 1) / lineHeight;
		return y < 0 || y >= addresses.length ? -1 : addresses[y];
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (!inSet) {
			// Bloody Swing Still Sux (AdjustmentEvent is ALWAYS TRACK)
			switch (e.getValue() - address) {
			case 1 /* AdjustmentEvent.UNIT_INCREMENT */:
				nextAddress();
				break;
			case -1 /* AdjustmentEvent.UNIT_DECREMENT */:
				prevAddress();
				break;
			case 0x10 /* AdjustmentEvent.BLOCK_INCREMENT */:
				for (int i = Math.max(1, addresses.length - 1); i > 0; i--)
					nextAddress();
				break;
			case -0x10 /* AdjustmentEvent.BLOCK_DECREMENT */:
				for (int i = Math.max(1, addresses.length - 1); i > 0; i--)
					prevAddress();
				break;
			default:
				/* case AdjustmentEvent.TRACK: */ setAddress(scrollBar.getValue());
				break;
			}
			scrollBar.setValue(address);
		}
	}

	protected void nextAddress() {
		Disassembler diss = computer.getDisassembler();
		diss.disassemble(computer.getMemory(), addresses, false, 0);
		setAddress(addresses[0], false);
	}

	protected void prevAddress() {
		int end = addresses[0];
		int addr = (end - 6) & 0xffff;
		int result;
		Disassembler diss = computer.getDisassembler();
		do {
			result = addr;
			addresses[0] = addr;
			diss.disassemble(computer.getMemory(), addresses, false, 0);
			addr = addresses[0];
			if (addr != end)
				addr = (result + 1) & 0xffff;
		} while (addr != end);
		setAddress(addresses[0] = result, false);
	}

	protected void setSelection(int addr) {
		if (addr < selAnchor) {
			selStart = addr;
			selEnd = selAnchor;
		} else {
			selStart = selAnchor;
			selEnd = addr;
		}
	}

	protected void startTimer(int y) {
		if (counter == null)
			counter = new Counter(this, 50, null);
		timerY = y;
	}

	protected void stopTimer() {
		if (counter != null) {
			counter.stop();
			counter = null;
		}
	}

	public void timerTick(Counter counter) {
		if (this.counter == counter) {
			if (timerY < 0) {
				prevAddress();
				setSelection(address);
			} else {
				nextAddress();
				setSelection(addresses[addresses.length - 1]);
			}
			scrollBar.setValue(address);
		}
	}

	protected void processMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			int addr = getAddress(e.getY());
			if ((e.getModifiers() & MouseEvent.SHIFT_MASK) == 0)
				selAnchor = selStart = selEnd = addr;
			else
				setSelection(addr);
			repaint();
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED)
			stopTimer();
		super.processMouseEvent(e);
	}

	protected void processMouseMotionEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
			Dimension size = getSize();
			int y = e.getY();
			if (y >= size.height) {
				setSelection(addresses[addresses.length - 1]);
				startTimer(y);
			} else if (y < 0) {
				setSelection(addresses[0]);
				startTimer(y);
			} else {
				setSelection(getAddress(e.getY()));
				stopTimer();
			}
			repaint();
		}
		super.processMouseMotionEvent(e);
	}

}