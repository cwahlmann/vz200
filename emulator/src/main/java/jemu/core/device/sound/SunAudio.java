package jemu.core.device.sound;

import java.io.*;
import jemu.core.*;
import jemu.core.device.*;
import sun.audio.*;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 Company:
 * 
 * @author
 * @version 1.0
 */

public class SunAudio extends SoundPlayer implements Runnable {

	protected AudioStream stream;
	protected boolean playing = false;
	protected int samples;
	protected boolean stereo;
	protected int writeAhead = Util.determineJVM() == Util.JVM_UNKNOWN ? 800 : 600;
//  protected AudioPlayer player = AudioPlayer.player;

	public SunAudio(int samples, boolean stereo) {
		this.samples = samples;
		this.stereo = stereo;
		init();
	}

	protected void init() {
		stream = new AudioStream(samples);
	}

	public int getSampleRate() {
		return 8000;
	}

	public void play() {
		if (!playing) {
			// sync();
			playing = true;
			Thread thread = new Thread(this);
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
	}

	public void stop() {
		if (playing) {
//      player.stop(stream);
			playing = false;
		}
	}

	public void sync() {
		stream.sync();
	}

	public void run() {
		if (playing)
//      player.start(stream);
			while (playing) {
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	public void writeMono(int value) {
		switch (format) {
		case SoundUtil.ULAW:
			stream.writeulaw((byte) value);
			break;
		case SoundUtil.PCM8:
			stream.writeulaw(SoundUtil.pcm8ToULaw((byte) value));
			break;
		case SoundUtil.UPCM8:
			stream.writeulaw(SoundUtil.upcm8ToULaw((byte) value));
			break;
		case SoundUtil.PCM16:
			stream.writeulaw(SoundUtil.pcm16ToULaw(value));
			break;
		case SoundUtil.UPCM16:
			stream.writeulaw(SoundUtil.upcm16ToULaw(value));
			break;
		}
	}

	public void writeStereo(int a, int b) {
		stream.writeulaw((byte) (a | b)); // TODO: How does this sound??? Not using anyway
		switch (format) {
		case SoundUtil.ULAW:
			stream.writeulaw((byte) (a | b));
			break;
		case SoundUtil.PCM8:
			stream.writeulaw(SoundUtil.pcm8ToULaw((byte) (a | b)));
			break;
		case SoundUtil.UPCM8:
			stream.writeulaw(SoundUtil.upcm8ToULaw((byte) (a | b)));
			break;
		case SoundUtil.PCM16:
			stream.writeulaw(SoundUtil.pcm16ToULaw(a + b));
			break;
		case SoundUtil.UPCM16:
			stream.writeulaw(SoundUtil.upcm16ToULaw(a + b));
			break;
		}
	}

	public void setWriteAhead(int value) {
		writeAhead = value;
	}

	protected class AudioStream extends InputStream {

		byte[] buffer;
		int pos = 0;
		int wrPos = 0;
		int size = 0;

		protected AudioStream(int samples) {
			buffer = new byte[samples];
		}

		public int read() {
			waitForData(1);
			int result = (int) (buffer[pos] & 0xff);
			pos = (pos + 1) % buffer.length;
			size--;
			return result;
		}

		public void waitForData(int count) {
			while (size < count) {
				synchronized (this) {
					try {
						wait();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		public int read(byte[] buff, int offs, int len) {
			waitForData(len);
			int end = len + offs;
			for (; offs < end; offs++) {
				buff[offs] = buffer[pos];
				pos = (pos + 1) % buffer.length;
			}
			size -= len;
			return len;
		}

		public void writeulaw(byte value) {
			buffer[wrPos] = value;
			wrPos = (wrPos + 1) % buffer.length;
			size++;
			synchronized (this) {
				notify();
			}
		}

		public int available() {
			return 8000;
		}

		public void close() {
			playing = false;
		}

		public void sync() {
			/*
			 * int newPos = wrPos - writeAhead; while (newPos < 0) newPos += buffer.length;
			 * pos = newPos;
			 */
		}

	}

}