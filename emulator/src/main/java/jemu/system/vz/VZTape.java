package jemu.system.vz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VZTape {
	private static final Logger log = LoggerFactory.getLogger(VZTape.class);

	private List<Pair<Integer, Integer>> data; // -1 -> no data, 0-255 data
	private int pos;
	
	public VZTape() {
		this.data = new ArrayList<>();
		clear();
	}

	public Pair<Integer, Integer> read() {
		if (pos < data.size()) {
			Pair<Integer, Integer> r = data.get(pos);
			pos++;
			return r;
		}
		return null;
	}

	public boolean endOfTape() {
		return pos >= data.size();
	}

	public void write(Pair<Integer, Integer> value) {
		data.add(value);
	}

	public int pos() {
		return pos;
	}

	public void pos(int pos) {
		this.pos = pos;
	}

	public void clear() {
		this.data.clear();
		this.pos = 0;
	}

	public void write(OutputStream os) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
			for (Pair<Integer, Integer> p : data) {
				writer.append(String.format("%s,%s\n", p.getLeft(), p.getRight()));
			}
			writer.flush();
		}
	}

	public void read(InputStream is) throws IOException {
		data.clear();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line = reader.readLine();
			while (line != null && line.contains(",")) {
				String s[] = line.split(",");
				data.add(Pair.of(Integer.valueOf(s[0]), Integer.valueOf(s[1])));
				line = reader.readLine();
			}
		}
	}
}
