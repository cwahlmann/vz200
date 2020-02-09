package jemu.util.vz;

import static java.awt.AWTKeyStroke.getAWTKeyStroke;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.AWTKeyStroke;
import java.awt.event.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.rest.JemuRestController;
import jemu.system.vz.Keyboard;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 / 2020 Company:
 * 
 * @author Juergen Wahlmann
 * @version 1.0
 */

public abstract class VZUtils {

    private static final Logger log = LoggerFactory.getLogger(JemuRestController.class);
    // Code adapted from
    // https://stackoverflow.com/questions/1248510/convert-string-to-keyevents/37179377#37179377?newreg=8882e71698a54fe5aa0da94a6dad4786
    // By Sensei Sho
    public static AWTKeyStroke getKeyStroke(final char c) {
        final String upper = "`~'\"!@#$%^&*()_+{}|:<>?";
        final String lower = "`~'\"1234567890-=[]\\;,./";

        final int index = upper.indexOf(c);
        if (index != -1) {
            int keyCode;
            boolean shift = false;
            switch (c) {
            // these chars need to be handled specially because
            // they don't directly translate into the correct keycode
            case '~':
                shift = true;
            case '`':
                keyCode = KeyEvent.VK_BACK_QUOTE;
                break;
            case '\"':
                shift = true;
            case '\'':
                keyCode = KeyEvent.VK_QUOTE;
                break;
            default:
                keyCode = (int) Character.toUpperCase(lower.charAt(index));
                shift = true;
            }
            return getAWTKeyStroke(keyCode, shift ? SHIFT_DOWN_MASK : 0);
        }
        return getAWTKeyStroke((int) Character.toUpperCase(c), 0);
    }

	public static void type(final CharSequence chars, final Keyboard keyboard) throws InterruptedException {
		VZUtils.type(chars, 0, keyboard);
	}

	public static void type(final CharSequence chars, int ms, final Keyboard keyboard) throws InterruptedException {
		log.debug(String.format("Type string: %s", chars));
		ms = ms > 0 ? ms : 0;

		for (int i = 0, len = chars.length(); i < len; i++) {
			final char c = chars.charAt(i);
			final AWTKeyStroke keyStroke = VZUtils.getKeyStroke(c);
			final int keyCode = keyStroke.getKeyCode();
			final boolean shift = false; // Character.isUpperCase(c) || keyStroke.getModifiers() == (SHIFT_DOWN_MASK
											// + 1);
			if (shift) {
				keyboard.keyPressed(KeyEvent.VK_SHIFT);
			}
			keyboard.keyPressed(keyCode);
			Thread.sleep(50);
			keyboard.keyReleased(keyCode);

			if (shift) {
				keyboard.keyReleased(KeyEvent.VK_SHIFT);
			}
			if (ms > 0) {
				Thread.sleep(ms);
			}
		}
	}

}