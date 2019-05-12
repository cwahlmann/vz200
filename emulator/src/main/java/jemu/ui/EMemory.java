package jemu.ui;

import java.awt.*;
import java.awt.event.*;
import jemu.core.*;
import jemu.core.device.*;
import jemu.core.device.memory.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class EMemory extends JComponent {
	private static final long serialVersionUID = 1L;
	protected Memory mem;

	public EMemory() {
		setBackground(Color.white);
		setLayout(new BorderLayout());
		setFont(new Font("Courier", 0, 12));
	}

	public void setMemory(Memory value) {
		mem = value;
	}

	public void setComputer(Computer value) {
		setMemory(value == null ? null : value.getMemory());
	}

	protected void paintComponent(Graphics g) {
		byte[] buff = new byte[16];
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		if (mem != null) {
			g.setFont(getFont());
			FontMetrics fm = g.getFontMetrics();
			g.setColor(getForeground());
			int a = fm.getAscent();
			int h = fm.getHeight();
			Rectangle rect = g.getClipBounds();
			Insets insets = getInsets();
			int row = (rect.y - insets.top) / h;
			int y = insets.top + row * h;
			int address = row * 0x10;
			for (; y < rect.y + rect.height; y += fm.getHeight()) {
				String line = Util.hex((short) address) + ": "; // TODO: Use Memory.getAddressSize() to determine digits
				for (int i = 0; i < 16; i++)
					buff[i] = (byte) mem.readByte(address + i, null);
				line += Util.dumpBytes(buff, 0, 16, false, true, false);
				g.drawString(line, insets.left, y + a);
				address += 16;
				if (address >= 0x10000)
					break; // TODO: Use Memory.getAddressSize();
			}
		}
	}

	public Dimension getPreferredSize() {
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
		// TODO: The 70 below may need to be slightly increased for larger memory
		// footprints
		// Also the 0x1000.
		// Maybe make this customisable number of columns
		return new Dimension(fm.charWidth('0') * 70, fm.getHeight() * 0x1000);
	}

}