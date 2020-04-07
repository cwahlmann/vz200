package jemu.util.assembler.z80;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

class Token {
	private Optional<Integer> a = Optional.empty();
	private Optional<Integer> b = Optional.empty();
	private BiFunction<Integer, Integer, Integer> f;
	private String sourceA = "";
	private String sourceB = "";
	private boolean relativeA = false;
	private boolean relativeB = false;
	private int nextCursorAddress;

	public Token(Optional<Integer> a, Optional<Integer> b, String varA, boolean relativeA, String varB,
			boolean relativeB, BiFunction<Integer, Integer, Integer> f) {
		this.a = a;
		this.b = b;
		this.sourceA = varA;
		this.sourceB = varB;
		this.relativeA = relativeA;
		this.relativeB = relativeB;
		this.f = f;
	}

	public Optional<Integer> value() {
		return a.isPresent() && b.isPresent() ? Optional.of(f.apply(a.get(), b.get())) : Optional.empty();
	}

	public final Token valueA(Integer a) {
		this.a = Optional.of(a);
		return this;
	}

	public final Token valueB(Integer b) {
		this.b = Optional.of(b);
		return this;
	}

	public Token nextCursorAddress(int nextCursorAddress) {
		this.nextCursorAddress = nextCursorAddress;
		return this;
	}
	
	public String sourceA() {
		return sourceA;
	}

	public boolean isRelativeA() {
		return relativeA;
	}

	public String sourceB() {
		return sourceB;
	}

	public int nextCursorAddress() {
		return nextCursorAddress;
	}
	
	public boolean isRelativeB() {
		return relativeB;
	}
}
