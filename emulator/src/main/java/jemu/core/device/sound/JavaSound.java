/*
 * JavaSound.java
 *
 * Created on 12 June 2006, 20:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class JavaSound extends SunAudio {
	private static final Logger log = LoggerFactory.getLogger(JavaSound.class);
	public static final int SAMPLE_RATE = 22050;

	protected static AudioFormat STEREO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 2, false, false);
	protected static AudioFormat MONO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);

	protected SourceDataLine line;
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

	protected void init() {
		format = SoundUtil.UPCM8;
		channels = stereo ? 2 : 1;
		data = new byte[samples * channels];
		AudioFormat format = stereo ? STEREO_FORMAT : MONO_FORMAT;
		try {
			line = (SourceDataLine) AudioSystem
					.getLine(new DataLine.Info(SourceDataLine.class, format, SAMPLE_RATE * channels));
			line.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sync() {
	}

	public void play() {
		line.start();
		// line.write(data,0,data.length);
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
		// line.write(data,offset,1);
		if (++offset >= data.length) {
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
