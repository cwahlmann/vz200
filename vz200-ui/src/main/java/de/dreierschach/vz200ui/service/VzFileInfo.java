package de.dreierschach.vz200ui.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAutorun() {
		return autorun;
	}

	public void setAutorun(boolean autorun) {
		this.autorun = autorun;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
