/*
 * SoundUtil.java
 *
 * Created on 13 June 2006, 12:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jemu.core.device.sound;

/**
 *
 * @author Richard
 */
public class SoundUtil {

	public static final int ULAW = 0;
	public static final int PCM8 = 1;
	public static final int UPCM8 = 2;
	public static final int PCM16 = 3;
	public static final int UPCM16 = 4;

	public static SoundPlayer getSoundPlayer(int samples, boolean stereo) {
		return new JavaSound(samples, stereo);
	}

	public static SoundPlayer getSoundPlayer(boolean stereo) {
		return new JavaSound(441, stereo);
	}

	protected static int[] ULAW_TO_PCM16 = new int[128];
	protected static byte[] PCM16_TO_ULAW = new byte[8160];
	protected static int[] PCM16_RANGE = { 0, 32, 96, 224, 480, 992, 2016, 4064, 8160 };
	protected static int[] ULAW_PREFIX = { 0xf0, 0xe0, 0xd0, 0xc0, 0xb0, 0xa0, 0x90, 0x80 };
	static {
		for (int i = 0; i < 8160; i++) {
			for (int j = 0; j < PCM16_RANGE.length - 1; j++) {
				if (i < PCM16_RANGE[j + 1]) {
					PCM16_TO_ULAW[i] = (byte) (ULAW_PREFIX[j] | 15 - (i - PCM16_RANGE[j]) / (2 << j));
					break;
				}
			}
		}
		for (int i = 0; i < 127; i++) {
			int scale = 7 - (i >> 4);
			int base = PCM16_RANGE[scale];
			ULAW_TO_PCM16[i] = base + (((15 - (i & 0x0f)) << 1) + 1) * (1 << scale);
		}
	};

	public static final byte pcm16ToULaw(int value) {
		int mask;
		if (value < 0) {
			value = -value;
			mask = 0xff;
		} else
			mask = 0x7f;

		value >>= 3; // Divide by 8 (65536 / 8160).
		if (value >= 8160)
			value = 8159;
		return (byte) (PCM16_TO_ULAW[value] & mask);
	}

	public static final byte upcm16ToULaw(int value) {
		return pcm16ToULaw(value - 32768);
	}

	public static final byte pcm8ToULaw(byte value) {
		return pcm16ToULaw(value * 256);
	}

	public static final byte upcm8ToULaw(byte value) {
		return pcm16ToULaw((value - 128) * 256);
	}

	public static final int ulawToPCM16(byte value) {
		return (value & 0x80) == 0 ? (ULAW_TO_PCM16[value] << 3) : -(ULAW_TO_PCM16[value & 0x7f] << 3);
	}

	public static final int ulawToUPCM16(byte value) {
		return ulawToPCM16(value) + 32768;
	}

	public static final byte ulawToPCM8(byte value) {
		return (byte) ((value & 0x80) == 0 ? (ULAW_TO_PCM16[value] >> 5) : -(ULAW_TO_PCM16[value & 0x7f] >> 5));
	}

	public static final byte ulawToUPCM8(byte value) {
		return (byte) (ulawToPCM8(value) + 128);
	}

}
