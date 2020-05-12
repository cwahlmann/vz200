/*
 * Debugger.java
 *
 * Created on 18 January 2007, 15:07
 */

package jemu.ui;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.FileOutputStream;

import javax.swing.JFrame;

import jemu.core.Util;
import jemu.core.device.Computer;
import jemu.core.device.memory.Memory;
import jemu.util.diss.Disassembler;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class Debugger extends JFrame implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;

	public static final Color navy = new Color(0, 0, 127);

	protected Computer computer;
	protected long startCycles = 0;

	/** Creates new form Debugger */
	public Debugger() {
		initComponents();
		bRun.addActionListener(this);
		bStop.addActionListener(this);
		bStep.addActionListener(this);
		bStepOver.addActionListener(this);
		bSave.addActionListener(this);
	}

	public void setComputer(Computer value) {
		if (computer != null)
			computer.removeActionListener(this);
		computer = value;
		eDisassembler.setComputer(computer);
		eMemory.setComputer(computer);
		if (computer != null) {
			computer.addActionListener(this);
			eRegisters.setProcessor(computer.getProcessor());
			lCycleCount.setText(Long.toString(computer.getProcessor().getCycles() - startCycles));
		} else
			eRegisters.setProcessor(null);
	}

	public void actionPerformed(ActionEvent e) {
		computer.clearRunToAddress();
		if (e.getSource() == bRun)
			computer.start();
		else if (e.getSource() == bStop)
			computer.stop();
		else if (e.getSource() == bStep)
			computer.step();
		else if (e.getSource() == bStepOver)
			computer.stepOver();
		else if (e.getSource() == computer) {
			eDisassembler.setAddress(computer.getProcessor().getProgramCounter());
			lCycleCount.setText(Long.toString(computer.getProcessor().getCycles() - startCycles));
			repaint();
		} else if (e.getSource() == bSave) {
			FileDialog dlg = new FileDialog(this, "Save Disassembly", FileDialog.SAVE);
			dlg.setVisible(true);
			if (dlg.getFile() != null) {
				int[] start = new int[] { eDisassembler.selStart };
				int end = eDisassembler.selEnd;
				try (FileOutputStream io = new FileOutputStream(dlg.getDirectory() + dlg.getFile())) {
					Disassembler diss = computer.getDisassembler();
					Memory mem = computer.getMemory();
					while (start[0] <= end) {
						String s = Util.hex((short) start[0]) + ": ";
						io.write((s + diss.disassemble(mem, start) + "\r\n").getBytes());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		computer.setFrameSkip(0);
		computer.updateDisplay(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code
	// ">//GEN-BEGIN:initComponents
	private void initComponents() {
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		bRun = new javax.swing.JButton();
		bStop = new javax.swing.JButton();
		bStep = new jemu.ui.EButton();
		bStepOver = new jemu.ui.EButton();
		bSave = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		lCycles = new javax.swing.JLabel();
		lCycleCount = new javax.swing.JLabel();
		eRegisters = new jemu.ui.ERegisters();
		jSplitPane1 = new javax.swing.JSplitPane();
		eDisassembler = new jemu.ui.EDisassembler();
		jScrollPane1 = new javax.swing.JScrollPane();
		eMemory = new jemu.ui.EMemory();

		setTitle("JEMU Debugger");
		jPanel1.setLayout(new java.awt.BorderLayout());

		jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

		bRun.setText("Run");
		jPanel2.add(bRun);

		bStop.setText("Stop");
		jPanel2.add(bStop);

		bStep.setText("Step");
		jPanel2.add(bStep);

		bStepOver.setText("Step Over");
		jPanel2.add(bStepOver);

		bSave.setText("Save");
		jPanel2.add(bSave);

		jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);

		lCycles.setForeground(new java.awt.Color(0, 0, 127));
		lCycles.setText("Cycles:");
		jPanel3.add(lCycles);

		lCycleCount.setText("0");
		lCycleCount.addMouseListener(this);

		jPanel3.add(lCycleCount);

		jPanel1.add(jPanel3, java.awt.BorderLayout.EAST);

		getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

		eRegisters.setLayout(null);

		getContentPane().add(eRegisters, java.awt.BorderLayout.LINE_END);

		jSplitPane1.setDividerLocation(200);
		jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setContinuousLayout(true);
		eDisassembler.addMouseListener(this);

		jSplitPane1.setTopComponent(eDisassembler);

		jScrollPane1.setViewportView(eMemory);

		jSplitPane1.setRightComponent(jScrollPane1);

		getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

		pack();
	}

	// Code for dispatching events from components to event handlers.

	public void mouseClicked(java.awt.event.MouseEvent e) {
		if (e.getSource() == lCycleCount) {
			Debugger.this.lCycleCountMouseClicked(e);
		} else if (e.getSource() == eDisassembler) {
			Debugger.this.eDisassemblerMouseClicked(e);
		}
	}

	public void mouseEntered(java.awt.event.MouseEvent e) {
	}

	public void mouseExited(java.awt.event.MouseEvent e) {
	}

	public void mousePressed(java.awt.event.MouseEvent e) {
	}

	public void mouseReleased(java.awt.event.MouseEvent e) {
	}// </editor-fold>//GEN-END:initComponents

	private void eDisassemblerMouseClicked(java.awt.event.MouseEvent e) {// GEN-FIRST:event_eDisassemblerMouseClicked
		if (e.getClickCount() == 2) {
			int addr = eDisassembler.getAddress(e.getY());
			if (addr != -1) {
				computer.setRunToAddress(addr);
				computer.start();
			}
		}
	}// GEN-LAST:event_eDisassemblerMouseClicked

	private void lCycleCountMouseClicked(java.awt.event.MouseEvent e) {// GEN-FIRST:event_lCycleCountMouseClicked
		if (e.getClickCount() == 2) {
			startCycles = computer.getProcessor().getCycles();
			lCycleCount.setText("0");
		}
	}// GEN-LAST:event_lCycleCountMouseClicked

	// Variables declaration - do not modify//GEN-BEGIN:variables
	protected javax.swing.JButton bRun;
	protected javax.swing.JButton bSave;
	protected jemu.ui.EButton bStep;
	protected jemu.ui.EButton bStepOver;
	protected javax.swing.JButton bStop;
	protected jemu.ui.EDisassembler eDisassembler;
	protected jemu.ui.EMemory eMemory;
	protected jemu.ui.ERegisters eRegisters;
	protected javax.swing.JPanel jPanel1;
	protected javax.swing.JPanel jPanel2;
	protected javax.swing.JPanel jPanel3;
	protected javax.swing.JScrollPane jScrollPane1;
	protected javax.swing.JSplitPane jSplitPane1;
	protected javax.swing.JLabel lCycleCount;
	protected javax.swing.JLabel lCycles;
	// End of variables declaration//GEN-END:variables

}
