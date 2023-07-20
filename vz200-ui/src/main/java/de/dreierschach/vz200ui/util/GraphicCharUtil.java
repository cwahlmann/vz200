package de.dreierschach.vz200ui.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphicCharUtil {

    public static class GraphicChar {
        private final int vz200Code;
        private final char unicodeChar;

        private GraphicChar(int vz200Code, char unicodeChar) {
            this.vz200Code = vz200Code;
            this.unicodeChar = unicodeChar;
        }

        public static GraphicChar of(int vz200Code, char unicodeChar) {
            return new GraphicChar(vz200Code, unicodeChar);
        }

        public int getVz200Code() {
            return vz200Code;
        }

        public char getUnicodeChar() {
            return unicodeChar;
        }
    }

    public static final Map<Integer, GraphicChar> GRAPHIC_CHARS = Stream.of(
            //@formatter:off
            GraphicChar.of(0x80, '\u25af'), // Z blank
            GraphicChar.of(0x81, '\u2597'), // A ru
            GraphicChar.of(0x82, '\u2596'), // S lu
            GraphicChar.of(0x83, '\u2584'), // Y ru + lu
            GraphicChar.of(0x84, '\u259d'), // D ro
            GraphicChar.of(0x85, '\u2590'), // I ro + ru
            GraphicChar.of(0x86, '\u259e'), // H ro + lu
            GraphicChar.of(0x87, '\u259f'), // R ro + ru + lu

            GraphicChar.of(0x88, '\u2598'), // F lo
            GraphicChar.of(0x89, '\u259a'), // G lo + ru
            GraphicChar.of(0x8a, '\u258c'), // U lo + lu
            GraphicChar.of(0x8b, '\u2599'), // E lo + ru + lu
            GraphicChar.of(0x8c, '\u2580'), // T lo + ro
            GraphicChar.of(0x8d, '\u259c'), // W lo + ro + ru
            GraphicChar.of(0x8e, '\u259b'), // Q lo + ro + lu
            GraphicChar.of(0x8f, '\u2588')  // J lo + ro + ru + lu
            //@formatter:on
    ).collect(Collectors.toMap(GraphicChar::getVz200Code, Function.identity()));

}
