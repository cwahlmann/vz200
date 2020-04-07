/*
 * FileDescriptor.java
 *
 * Created on 18 January 2007, 12:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class FileDescriptor implements Comparable<FileDescriptor> {

	public String description, filename, instructions;

	/** Creates a new instance of FileDescriptor */
	public FileDescriptor(String description, String filename, String instructions) {
		this.description = description;
		this.filename = filename;
		this.instructions = instructions;
	}

	@Override
	public int compareTo(FileDescriptor o) {
		return this.description.compareToIgnoreCase(o.description);
	}

	public String toString() {
		return description;
	}

}
