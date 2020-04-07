package jemu.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class JemuConfiguration {
	private final static Logger log = LoggerFactory.getLogger(JemuConfiguration.class);

	private Properties properties;
	private Path propertiesPath;
	private boolean changed;
	private boolean updateImmediatliy;

	public JemuConfiguration(Path propertiesPath) {
		this.propertiesPath = propertiesPath;
		this.updateImmediatliy = false;
		readOrCreateProperties();
	}

	public JemuConfiguration updateImmediatliy() {
		this.updateImmediatliy = true;
		return this;
	}

	private void readOrCreateProperties() {
		properties = new Properties();
		this.changed = false;
		if (!Files.exists(propertiesPath)) {
			try {
				if (propertiesPath.getParent() != null) {
					Files.createDirectories(propertiesPath.getParent());
				}
				Files.createFile(propertiesPath);
				log.info("new properties file '{}' created", propertiesPath);
				return;
			} catch (IOException e) {
				log.error("Unable to create properties file '{}'", propertiesPath, e);
				return;
			}
		}
		try {
			properties.load(Files.newInputStream(propertiesPath));
			log.info("properties file '{}' loaded", propertiesPath);
		} catch (IOException e) {
			log.error("Unable to read properties file '{}'", propertiesPath, e);
		}
	}

	public void persist() {
		if (!changed) {
			return;
		}
		try {
			properties.store(Files.newOutputStream(propertiesPath), "JEMU properties file");
			changed = false;
			log.info("properties file '{}' updated", propertiesPath);
		} catch (IOException e) {
			log.error("Unable to write properties file '{}'", propertiesPath, e);
		}
	}

	public boolean contains(String key) {
		return properties.containsKey(key);
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int defaultValue) {
		if (!contains(key)) {
			return defaultValue;
		}
		return Integer.parseInt(get(key));
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public long getLong(String key, long defaultValue) {
		if (!contains(key)) {
			return defaultValue;
		}
		return Long.parseLong(get(key));
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (!contains(key)) {
			return defaultValue;
		}
		return Boolean.parseBoolean(get(key));
	}

	public String get(String key) {
		return get(key, "");
	}

	public String get(String key, String defaultValue) {
		if (!contains(key)) {
			return defaultValue;
		}
		return properties.getProperty(key);
	}

	public void set(String key, Number value) {
		set(key, value.toString());
	}
	
	public void setIfMissing(String key, Number value) {
		setIfMissing(key, value.toString());
	}

	public void set(String key, String value) {
		properties.setProperty(key, value);
		this.changed = true;
		if (updateImmediatliy) {
			persist();
		}
	}

	public void setIfMissing(String key, String value) {
		if (!contains(key)) {
			set(key, value);
		}
	}

	public boolean isChanged() {
		return changed;
	}
}
