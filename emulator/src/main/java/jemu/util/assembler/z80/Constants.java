package jemu.util.assembler.z80;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Constants {
	private Constants() {
	}
		
	public static final String WHITESPACE_PATTERN = "[\t ]+";

	public enum StatementToken {
		ORG("^\\.org"), DEF("^\\.def"), RUN("^\\.run"), IMPORT("^\\.import"), INCLUDE("^\\.include");
		private String regex;

		StatementToken(String regex) {
			this.regex = regex;
		}

		public boolean matches(String s) {
			return Pattern.matches(regex + WHITESPACE_PATTERN + ".*", s);
		}

		public static Optional<StatementToken> find(String s) {
			return Arrays.stream(StatementToken.values()).filter(t -> t.matches(s)).findAny();
		}
	}

	public static enum RegisterToken {
		B("^B$", 0), C("^C$", 1), D("^D$", 2), E("^E$", 3), H("^H$", 4), IX_H("^IXh$", 4), IY_H("^IYh$", 4), L("^L$",
				5), IX_L("^IXl$", 5), IY_L("^IYl$", 5), OF_HL("^(\\s*HL\\s*)$", 6), A("^A$", 7);

		RegisterToken(String regex, int code) {
			this.regex = regex;
			this.code = code;
		}

		private String regex;
		private int code;

		public int code() {
			return code;
		}

		public boolean matches(String s) {
			return Pattern.matches(regex, s);
		}

		public static Optional<Integer> parse(String s) {
			return Stream.of(RegisterToken.values()).filter(t -> t.matches(s)).map(t -> t.code()).findAny();
		}
	}

	public static enum NumToken {
		// @formatter:off
		hex("0x[0-9a-fA-F]{2,4}", s -> Integer.valueOf(s.substring(2), 16)), 
		dez("(\\+|\\-){0,1}[0-9]+",
				s -> Integer.valueOf(s, 10)), 
		okt("0o[0-7]+", s -> Integer.valueOf(s.substring(2), 8)), 
		qua("0q[0-3]{4,8}", s -> Integer.valueOf(s.substring(2), 4)), 
		bin("0b[0-1]{8,16}", s -> Integer.valueOf(s.substring(2), 2)), 
		IXY_O("I[XY].*[+-]{1}.*[0-9]+", s -> Integer.valueOf(s.substring(2))), 
		CHR("'.'", s -> (int) s.charAt(1)), 
		CHR2("'..'", s -> (int) s.charAt(1) + ((int) s.charAt(2) << 8));
		// @formatter:on
		NumToken(String regex, Function<String, Integer> mapper) {
			this.regex = regex;
			this.mapper = mapper;
		}

		private String regex;
		private Function<String, Integer> mapper;

		public boolean matches(String s) {
			return Pattern.matches(regex, s);
		}

		public static Optional<Integer> parse(String s) {
			return Stream.of(NumToken.values()).filter(t -> t.matches(s)).map(t -> t.mapper.apply(s)).findAny();
		}
	}

	public static enum DefToken {
		DEFB("^defb[\t ]+.*"), DEFW("^defw[\t ]+.*"), DEFS("^defs[\t ]+.*");
		private String regex;

		DefToken(String regex) {
			this.regex = regex;
		}

		public boolean matches(String s) {
			return Pattern.matches(regex, s);
		}
	}

	public static final String PATTERN_LABEL = "[a-zA-Z][0-9a-zA-Z_]*\\:";
	public static final String PATTERN_PLUS_O = "[\\+\\-][0-9]+";
	public static final String PATTERN_O = "([\\+\\-]{0,1}[0-9]+)|(" + PATTERN_LABEL + ")";
	public static final String PATTERN_NN = "(0x[0-9ABCDEFabcdef]{4})|(" + PATTERN_LABEL + ")";
	public static final String PATTERN_N = "(0x[0-9ABCDEFabcdef]{2})|(" + PATTERN_LABEL + ")";
	public static final String PATTERN_B = "[0-7]{1}";
	public static final String PATTERN_R = "(B|C|D|E|H|L|\\(HL\\)|A)";
	public static final String PATTERN_P = "(B|C|D|E|IXh|IXl|A)";
	public static final String PATTERN_Q = "(B|C|D|E|IYh|IYl|A)";

	public static final String TOKEN_O = "o";
	public static final String TOKEN_N = "n";
	public static final String TOKEN_NN = "nn";
	public static final String TOKEN_P = "p";
	public static final String TOKEN_B = "b";

	public static final String[] R = new String[] { "B", "C", "D", "E", "H", "L", "(HL)", "A" };
	public static final String[] P = new String[] { "B", "C", "D", "E", "IXh", "IXl", null, "A" };
	public static final String[] Q = new String[] { "B", "C", "D", "E", "IYh", "IYl", null, "A" };
}
