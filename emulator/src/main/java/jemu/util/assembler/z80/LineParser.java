package jemu.util.assembler.z80;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

public class LineParser {

	public static String[] split(String source) {
		String[] result = new String[] { "", "", "" };
		String d = source.trim();
		int index = d.indexOf(" ");
		if (index < 0) {
			result[0] = d;
			return result;
		}
		result[0] = d.substring(0, index).trim();
		String p = d.substring(index + 1).trim();
		index = p.indexOf(",");
		if (index < 0) {
			result[1] = p;
			return result;
		}
		result[1] = p.substring(0, index).trim();
		result[2] = p.substring(index + 1).trim();
		return result;
	}

	public static boolean matches(Command command, String source) {
		String s[] = split(source);
		return Pattern.matches(command.getCommandToken(), s[0]) && Pattern.matches(command.getParameter1(), s[1])
				&& Pattern.matches(command.getParameter2(), s[2]);
	}

	public static boolean isLabel(String argument) {
		return Pattern.matches(Constants.PATTERN_LABEL, argument);
	}

	public static Optional<Command> findCommand(String source) {
		return Command.commands.stream().filter(c -> matches(c, source)).findAny();
	}

	public static Optional<Constants.DefToken> findDefinition(String source) {
		return Stream.of(Constants.DefToken.values()).filter(d -> d.matches(source)).findAny();
	}

	public static List<Token> parseLine(int lineNo, String source) {
		String line = source.trim();

		Optional<Constants.DefToken> definition = findDefinition(line);
		if (definition.isPresent()) {
			String l[] = line.split("\\s+", 2);
			switch (definition.get()) {
			case DEFB:
				return parseBytes(l[1]);
			case DEFW:
				return parseWords(l[1]);
			case DEFS:
				return parseString(l[1]);
			}
		}
		Optional<Command> command = findCommand(line);
		if (command.isPresent()) {
			String s[] = split(line);
			String sourceA = s.length >= 2 ? s[1].trim() : "0";
			String sourceB = s.length >= 3 ? s[2].trim() : "0";
			return command
					.get().getOpcode().stream().map(f -> new Token(parseArg(sourceA), parseArg(sourceB), sourceA,
							command.get().isRelative1(), sourceB, command.get().isRelative2(), f))
					.collect(Collectors.toList());
		}
		throw new RuntimeException(String.format("Line %d: Could not parse [%s]", lineNo, source));
	}

	private static List<Token> parseBytes(String line) {
		return Stream.of(line.split(","))
				.map(s -> new Token(parseConst(s.trim()), Optional.of(0), s, false, "", false, (a, b) -> a))
				.collect(Collectors.toList());
	}

	private static List<Token> parseWords(String line) {
		List<Token> result = new ArrayList<>();
		for (String s : line.split(",")) {
			Optional<Integer> i = parseConst(s);
			if (i.isPresent()) {
				result.add(new Token(Optional.of(i.get() & 0xff), Optional.of(0), s, false, "", false,
						(a, b) -> a & 0xff));
				result.add(new Token(Optional.of(i.get() >> 8), Optional.of(0), s, false, "", false, (a, b) -> a));
				System.out.println("");
			} else {
				result.add(new Token(i, Optional.of(0), s, false, "", false, (a, b) -> a & 0xff));
				result.add(new Token(i, Optional.of(0), s, false, "", false, (a, b) -> a >> 8));
			}
		}
		return result;
	}

	private static Optional<Integer> parseConst(String s) {
		if (s.endsWith(":")) {
			return Optional.empty();
		}
		Optional<Integer> arg = Constants.NumToken.parse(s);
		if (arg.isPresent()) {
			return arg;
		}
		return Optional.of(new Integer(0));
	}

	private static AsciiMapper asciiMapper = new AsciiMapper();

	private static List<Token> parseString(String line) {
		int index0 = line.indexOf("\"");
		int index1 = line.lastIndexOf("\"");
		return line.substring(index0 + 1, index1).chars().map(c -> asciiMapper.map((char) c))
				.mapToObj(n -> new Token(Optional.of(n), Optional.of(0), "", false, "", false, (a, b) -> a))
				.collect(Collectors.toList());
	}

	private static Optional<Integer> parseArg(String source) {
		String s = source.startsWith("(") && source.endsWith(")") ? source.substring(1, source.length() - 1).trim()
				: source;
		if (s.endsWith(":")) {
			return Optional.empty();
		}
		Optional<Integer> arg = Constants.RegisterToken.parse(s);
		if (arg.isPresent()) {
			return arg;
		}
		arg = Constants.NumToken.parse(s);
		if (arg.isPresent()) {
			return arg;
		}
		return Optional.of(new Integer(0));
	}
}
