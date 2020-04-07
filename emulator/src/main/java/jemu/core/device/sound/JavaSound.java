/*
 * JavaSound.java
 *
 * Created on 12 June 2006, 20:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

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

public class JavaSound extends SunAudio {
	private static final Logger log = LoggerFactory.getLogger(JavaSound.class);
	public static final int SAMPLE_RATE = 22050;

	protected static AudioFormat STEREO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 2, false, false);
	protected static AudioFormat MONO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);

	protected SourceDataLine line;
	private FloatControl control;
	protected byte[] data;
	protected int offset = 0;
	protected int count = 0;
	protected int channels;

	/**
	 * Creates a new instance of JavaSound.
	 *
	 * @samples Number of samples written to DataLine at a time. Keep low ~32
	 * @stereo true for Stereo, false for Mono
	 */
	public JavaSound(int samples, boolean stereo) {
		super(samples, stereo);
	}

	public int getSampleRate() {
		return SAMPLE_RATE;
	}

	public void setVolume(int volume) {
		if (control == null) {
			log.error("Unable to set volume to [{}] - no control available.");
			return;
		}
		float newValue = (control.getMaximum() - control.getMinimum()) * (float) volume / 255f + control.getMinimum();
		control.setValue(newValue);
	}

	public int getVolume() {
		if (control == null) {
			log.error("Unable to determine volume - no control available.");
			return 0;
		}
		return (int) ((control.getValue() - control.getMinimum()) / (control.getMaximum() - control.getMinimum())
				* 255f);
	}

	protected void init() {
		format = SoundUtil.UPCM8;
		channels = stereo ? 2 : 1;
		data = new byte[samples * channels];
		AudioFormat format = stereo ? STEREO_FORMAT : MONO_FORMAT;
		try {
			line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, format));
			line.open(format, SAMPLE_RATE * channels);
		} catch (Exception e) {
			e.printStackTrace();
		}
		control = findControl(line);
	}

	private static Set<String> VOLUME_CONTROL = Stream.of("volume", "master gain").collect(Collectors.toSet());

	private FloatControl findControl(SourceDataLine line) {
		control = Stream.of(line.getControls()).filter(c -> c instanceof FloatControl).map(c -> (FloatControl) c)
				.filter(c -> VOLUME_CONTROL.contains(c.getType().toString().toLowerCase())).findAny().orElse(null);
		if (control != null) {
			log.info("Audio control found: {}", control.toString());
		} else {
			log.error("No volume control available.");
		}
		return control;
	}

	public void sync() {
		if (line.available() < 500) {
			line.drain();
		}
	}

	public void play() {
		line.start();
	}

	public void stop() {
		line.stop();
	}

	public void writeMono(int value) {
		switch (format) {
		case SoundUtil.ULAW:
			data[offset] = SoundUtil.ulawToUPCM8((byte) value);
			break;
		case SoundUtil.UPCM8:
			data[offset] = (byte) value;
			break;
		}
		offset++;
		if (offset >= data.length) {
			line.write(data, 0, data.length);
			offset = 0;
		}
		count++;
	}

	public void writeStereo(int a, int b) {
		switch (format) {
		case SoundUtil.ULAW:
			data[offset] = SoundUtil.ulawToUPCM8((byte) a);
			data[offset + 1] = SoundUtil.ulawToUPCM8((byte) b);
			break;

		case SoundUtil.UPCM8:
			data[offset] = (byte) a;
			data[offset + 1] = (byte) b;
			break;
		}
		if ((offset += 2) >= data.length) {
			line.write(data, 0, data.length);
			offset = 0;
		}
		count += 2;
	}

}
