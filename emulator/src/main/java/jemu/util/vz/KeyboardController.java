package jemu.util.vz;

import static java.awt.AWTKeyStroke.getAWTKeyStroke;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

import java.awt.AWTKeyStroke;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.system.vz.Keyboard;
import org.springframework.stereotype.Component;

/**
 * Title: JEMU Description: The Java Emulation Platform Copyright: Copyright (c)
 * 2002 / 2020 Company:
 *
 * @author Juergen Wahlmann
 * @version 1.0
 */

@Component
public class KeyboardController {

    private static final Logger log = LoggerFactory.getLogger(KeyboardController.class);
    private BlockingDeque<String> cache = new LinkedBlockingDeque<>();
    private Keyboard keyboard = null;
    private long delayMs = 100;
    private Thread thread;

    public KeyboardController() {
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                typeNextEntry();
            }
        });
        thread.start();
    }

    public void clearCache() {
        this.cache.clear();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public KeyboardController withKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public KeyboardController withDelayMs(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

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
                case '"':
                    keyCode = KeyEvent.VK_2;
                    shift = true;
                    break;
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

    public void type(final String chars) throws InterruptedException {
        log.debug(String.format("Type string: %s", chars));
        cache.add(chars);
    }

    private void typeNextEntry() {
        String chars;
        try {
            chars = cache.pollFirst(60, TimeUnit.SECONDS);
            if (chars == null) {
                return;
            }
        } catch (InterruptedException e) {
            log.warn("stop polling");
            return;
        }

        int keyCode = 0;
        boolean shift = false;
        for (int i = 0, len = chars.length(); i < len; i++) {
            try {
                final char c = chars.charAt(i);
                final AWTKeyStroke keyStroke = KeyboardController.getKeyStroke(c);
                 keyCode = keyStroke.getKeyCode();
                 shift = Character.isUpperCase(c) || keyStroke.getModifiers() == (SHIFT_DOWN_MASK + 1);
                if (shift) {
                    keyboard.keyPressed(KeyEvent.VK_SHIFT);
                }
                keyboard.keyPressed(keyCode);
                Thread.sleep(50);
                keyboard.keyReleased(keyCode);

                if (shift) {
                    keyboard.keyReleased(KeyEvent.VK_SHIFT);
                }
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                log.warn("stop typing");
                if (keyCode != 0) {
                    keyboard.keyReleased(keyCode);
                }
                if (shift) {
                    keyboard.keyReleased(KeyEvent.VK_SHIFT);
                }
                return;
            }
        }
    }

}