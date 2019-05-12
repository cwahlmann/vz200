package jemu.ui;

import java.awt.*;
import jemu.core.*;
import jemu.core.cpu.*;
import javax.swing.*;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class ERegisters extends JPanel {
	private static final long serialVersionUID = 1L;

	protected static Font FIXED;

	protected Processor processor;

	public ERegisters() {
	}

	public void setProcessor(Processor value) {
		processor = value;
	}

	protected void paintComponent(Graphics g) {
		g.setFont(getFont());
		if (FIXED == null)
			FIXED = new Font("Courier", Font.PLAIN, getFont().getSize());
		if (processor != null) {
			String[] names = processor.getRegisterNames();
			FontMetrics fm = g.getFontMetrics();
			int y = fm.getAscent();
			for (int i = 0; i < names.length; i++) {
				String fmt = processor.getRegisterFormat(i);
				if (fmt != null) {
					g.setFont(FIXED);
					g.drawString(fmt, 35, y);
					y += fm.getAscent();
				}
				g.setFont(getFont());
				g.setColor(Debugger.navy);
				String str = names[i] + ":";
				int w = fm.stringWidth(str);
				g.drawString(str, 30 - w, y);
				g.setColor(Color.black);
				if (fmt != null) {
					str = "0000000000000000" + Integer.toBinaryString(processor.getRegisterValue(i));
					w = processor.getRegisterBits(i);
					g.setFont(FIXED);
				} else {
					str = "000000000000000" + Util.hex(processor.getRegisterValue(i));
					w = (processor.getRegisterBits(i) + 3) / 4;
				}
				str = str.substring(str.length() - w);
				g.drawString(str, 35, y);
				y += fm.getHeight();
			}
		}
	}

	// TODO: Need to measure actual required width for number
	public Dimension getPreferredSize() {
		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
		return new Dimension(fm.charWidth('0') * 14,
				processor == null ? 0 : fm.getHeight() * processor.getRegisterNames().length);
	}

}