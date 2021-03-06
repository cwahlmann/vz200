/*
 *
 *   Created on: 22.9.2018
 *   Author: Christian Wahlmann
 *
 */
package jemu.util.assembler.z80;

import jemu.core.device.memory.Memory;
import jemu.exception.JemuException;
import jemu.rest.dto.VzSource;
import jemu.util.assembler.z80.Constants.StatementToken;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
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

public class Assembler {
    private static final Logger log = LoggerFactory.getLogger(Assembler.class);

    private Memory memory;

    private int runAddress = 0;
    private int cursorAddress = 0;
    private int minCursorAddress = 0x10000;
    private int maxCursorAddress = 0;
    private Map<String, Integer> labelMap = new HashMap<>();
    private List<Path> visitedPaths = new ArrayList<>();
    private List<Integer> currentLineNo = new ArrayList<>();
    private List<Pair<Integer, Token>> tokensToBeResolved = new ArrayList<>();

    public Assembler(Memory memory) {
        this.memory = memory;
    }

    public String assemble(Path path) {
        parseFile(path);
        resolveOpenTokens();
        return String.format("%04x-%04x", runAddress, maxCursorAddress);
    }

    public String assemble(String s) {
        parseStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
        resolveOpenTokens();
        return String.format("%04x-%04x", runAddress, maxCursorAddress);
    }

    public String assemble(InputStream is) {
        parseStream(is);
        resolveOpenTokens();
        return String.format("%04x-%04x", runAddress, maxCursorAddress);
    }

    public void assembleSource(VzSource source) {
        File propertiesFile = null;
        Path tempPath = null;
        try {
            tempPath = createTempPath();
            writeToTemp(tempPath, "main.asm", source.getSource());
            for (VzSource.Lib lib : source.getLibs()) {
                writeToTemp(tempPath, lib.getName(), lib.getSource());
            }
            Path mainAsm = tempPath.resolve("main.asm");
            log.info("assemble main file: {}", mainAsm);
            parseFile(mainAsm);
            resolveOpenTokens();
        } catch (IOException e) {
            throw new JemuException("Error assembling source: " + e.getMessage(), e);
        } finally {
            if (tempPath != null) {
                deleteTempDir(tempPath);
            }
        }
    }

    private void writeToTemp(Path tempPath, String name, String source) throws IOException {
        Path dir = tempPath.resolve(name).getParent();
        Files.createDirectories(dir);
        try (FileWriter writer = new FileWriter(tempPath.resolve(name).toFile())) {
            writer.write(source);
        }
    }

    private Path createTempPath() throws IOException {
        Path temp = Paths.get(System.getProperty("user.home"), "/jemu/temp_" + UUID.randomUUID().toString());
        Files.createDirectories(temp);
        return temp;
    }

    private void deleteTempDir(Path tempPath) {
        FileUtils.deleteQuietly(tempPath.toFile());
    }

    private void parseFile(Path path) {
        if (visitedPaths.contains(path)) {
            log.warn("Ignore file {} to avoid an endless include loop", path);
            return;
        }
        visitedPaths.add(path);
        currentLineNo.add(0);
        try (Stream<String> s = Files.lines(path)) {
            s.forEach(line -> {
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
        String line = removeComment(l).trim();
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
            line = s.length == 2 ? s[1].trim() : "";

            if (labelMap.containsKey(label)) {
                log.warn("label {} will be overwritten", label);
            }
            labelMap.put(label, getCursorAddress());

            if (line.isEmpty() || line.startsWith(";") || line.startsWith("//")) {
                return;
            }
        }

        // parse command

        List<Token> tokens = LineParser.parseLine(currentLineNo.get(currentLineNo.size() - 1), line);
        int nextCursorAddress = cursorAddress + tokens.size() % 0xffff;
        tokens.forEach(t -> {
            if (t.value().isPresent()) {
                memory.writeByte(getCursorAddress(), t.value().get());
            } else {
                memory.writeByte(getCursorAddress(), 0);
                tokensToBeResolved.add(Pair.of(getCursorAddress(), t.nextCursorAddress(nextCursorAddress)));
            }
            addCursorAddress(1);
        });
    }

    public String removeComment(String line) {
        boolean commentFound = false;
        boolean firstSlashFound = false;
        boolean isStringConstant = false;
        int index = 0;
        while (index < line.length() && !commentFound) {
            switch (line.charAt(index)) {
                case '"':
                    isStringConstant = !isStringConstant;
                    break;
                case ';':
                    if (!isStringConstant) {
                        commentFound = true;
                    }
                    break;
                case '/':
                    if (!isStringConstant) {
                        if (firstSlashFound) {
                            commentFound = true;
                            index--;
                        } else {
                            firstSlashFound = true;
                        }
                    } else {
                        firstSlashFound = false;
                    }
                    break;
                default:
                    firstSlashFound = false;
            }
            index++;
        }
        if (commentFound) {
            return line.substring(0, index - 1);
        }
        return line;
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
                setCursorAddress(parseValue(s[1]));
                break;
            case RUN:
                setRunAddress(parseValue(s[1]));
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

    public int getMaxCursorAddress() {
        return maxCursorAddress;
    }

    public int getMinCursorAddress() {
        return minCursorAddress;
    }

    public void setCursorAddress(int cursorAddress) {
        this.cursorAddress = cursorAddress;
        if (cursorAddress < minCursorAddress) {
            minCursorAddress = cursorAddress;
        }
        if (cursorAddress > maxCursorAddress) {
            maxCursorAddress = cursorAddress;
        }
    }

    public void addCursorAddress(int n) {
        setCursorAddress(this.cursorAddress + n);
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
