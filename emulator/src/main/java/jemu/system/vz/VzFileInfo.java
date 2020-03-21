package jemu.system.vz;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VzFileInfo {
	private String name;
	private boolean autorun;
	private int start;
	private int length;
	private int id;

	public VzFileInfo() {
		this.name = "";
		this.autorun = false;
		this.start = 0;
		this.length = 0;
		this.id = -1;
	}

	public static final VzFileInfo EMPTY = new VzFileInfo();

	@JsonIgnore
	public boolean isEmpty() {
		return this.id == -1;
	}

	public int getId() {
		return id;
	}

	public VzFileInfo withId(int id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public VzFileInfo withName(String name) {
		this.name = name;
		return this;
	}

	public boolean isAutorun() {
		return autorun;
	}

	public VzFileInfo withAutorun(boolean autorun) {
		this.autorun = autorun;
		return this;
	}

	public int getStart() {
		return start;
	}

	public VzFileInfo withStart(int start) {
		this.start = start;
		return this;
	}

	public int getLength() {
		return length;
	}

	public VzFileInfo withLength(int length) {
		this.length = length;
		return this;
	}
}
