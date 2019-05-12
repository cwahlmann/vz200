/*
 *
 *   Created on: 22.9.2018
 *   Author: Christian Wahlmann
 *   
 */
package jemu.util.assembler.z80;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jemu.core.device.memory.Memory;
import jemu.util.assembler.z80.Constants.StatementToken;

/**
 *
 * @author christian Wahlmann
 */
public class Assembler {
	private static final Logger log = LoggerFactory.getLogger(Assembler.class);

	private Memory memory;

	private int runAddress = 0;
	private int cursorAddress = 0;
	private Map<String, Integer> labelMap = new HashMap<>();
	private List<Path> visitedPaths = new ArrayList<>();
	private List<Integer> currentLineNo = new ArrayList<>();
	private List<Pair<Integer, Token>> tokensToBeResolved = new ArrayList<>();

	public Assembler(Memory memory) {
		this.memory = memory;
	}

	public void assemble(Path path) {
		parseFile(path);
		resolveOpenTokens();
	}

	public void assemble(InputStream is) {
		parseStream(is);
		resolveOpenTokens();
	}

	private void parseFile(Path path) {
		if (visitedPaths.contains(path)) {
			log.warn("Ignore file {} to avoid an endless include loop", path);
			return;
		}
		visitedPaths.add(path);
		currentLineNo.add(0);
		try {
			Files.lines(path).forEach(line -> {
				currentLineNo.set(currentLineNo.size() - 1, currentLineNo.get(currentLineNo.size() - 1) + 1);
				parseLine(line);
			});
		} catch (Exception e) {
			throw new RuntimeException("Error parsing file " + path, e);
		}
		visitedPaths.remove(path);
		currentLineNo.remove(currentLineNo.size() - 1);
	}

	private void parseStream(InputStream is) {
		currentLineNo.add(0);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			reader.lines().forEach(line -> {
				currentLineNo.set(currentLineNo.size() - 1, currentLineNo.get(currentLineNo.size() - 1) + 1);
				parseLine(line);
			});
		} catch (Exception e) {
			log.warn("error parsing assembler line {}", currentLineNo.get(0), e);
			throw new RuntimeException("error parsing assembler line " + currentLineNo.get(0) + ": " + e.getMessage());
		}
	}

	public void parseLine(String l) {
		String line = l.trim();
		if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) {
			return;
		}
		Optional<Constants.StatementToken> token = Constants.StatementToken.find(line);
		if (token.isPresent()) {
			parseStatement(token, line);
			return;
		}

		// parse label if present

		if (Pattern.matches("^" + Constants.PATTERN_LABEL + ".*", line)) {
			String s[] = line.split("[\t ]+", 2);
			String label = s[0];
			line = s[1];

			if (labelMap.containsKey(label)) {
				log.warn("label {} will be overwritten", label);
			}
			labelMap.put(label, cursorAddress);
		}

		// parse command

		List<Token> tokens = LineParser.parseLine(currentLineNo.get(currentLineNo.size() - 1), line);
		int nextCursorAddress = cursorAddress + tokens.size() % 0xffff;
		tokens.forEach(t -> {
			if (t.value().isPresent()) {
				memory.writeByte(cursorAddress, t.value().get());
			} else {
				memory.writeByte(cursorAddress, 0);
				tokensToBeResolved.add(Pair.of(cursorAddress, t.nextCursorAddress(nextCursorAddress)));
			}
			cursorAddress++;
		});
	}

	public void resolveOpenTokens() {
		tokensToBeResolved.forEach(p -> {
			int address = p.getLeft();
			Token t = p.getRight();
			String sourceA = t.sourceA().replaceAll("[\\(\\)]", "");
			String sourceB = t.sourceB().replaceAll("[\\(\\)]", "");
			if (Pattern.matches(Constants.PATTERN_LABEL, sourceA)) {
				if (!labelMap.containsKey(sourceA)) {
					log.error("unable to resolve label {}", sourceA);
				} else {
					int value = labelMap.get(sourceA);
					if (t.isRelativeA()) {
						value -= t.nextCursorAddress();
					}
					t.valueA(value);
				}
			}
			if (Pattern.matches(Constants.PATTERN_LABEL, sourceB)) {
				if (!labelMap.containsKey(sourceB)) {
					log.error("unable to resolve label {}", sourceB);
				} else {
					int value = labelMap.get(sourceB);
					if (t.isRelativeB()) {
						value -= t.nextCursorAddress();
					}
					t.valueB(value);
				}
			}
			memory.writeByte(address, t.value().orElse(0));
		});
	}

	private void parseStatement(Optional<StatementToken> token, String line) {
		String s[] = line.split("[\t ]+", 2);
		line = s[1];
		switch (token.get()) {
		case DEF:
			s = line.split("[\t ]+", 2);
			String label = s[0];
			int value = parseValue(s[1]);
			labelMap.put(label, value);
			break;
		case ORG:
			cursorAddress = parseValue(s[1]);
			break;
		case RUN:
			runAddress = parseValue(s[1]);
			break;
		case INCLUDE:
			Path newPath = visitedPaths.get(visitedPaths.size() - 1).getParent().resolve(s[1]);
			parseFile(newPath);
			break;
		case IMPORT:
			// TODO
			break;
		}
	}

	public int parseValue(String s) {
		return Constants.NumToken.parse(s).orElse(0);
	}

	public int getRunAddress() {
		return runAddress;
	}

	public void setRunAddress(int runAddress) {
		this.runAddress = runAddress;
	}

	public int getCursorAddress() {
		return cursorAddress;
	}

	public void setCursorAddress(int cursorAddress) {
		this.cursorAddress = cursorAddress;
	}

	public Map<String, Integer> getLabelMap() {
		return labelMap;
	}

	public List<Pair<Integer, Token>> getTokensToBeResolved() {
		return tokensToBeResolved;
	}

	public List<Integer> getCurrentLineNo() {
		return currentLineNo;
	}

	public List<Path> getVisitedPaths() {
		return visitedPaths;
	}

	public Memory getMemory() {
		return memory;
	}
}
