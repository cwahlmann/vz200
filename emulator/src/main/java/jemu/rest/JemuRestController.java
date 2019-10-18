package jemu.rest;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jemu.system.vz.VZ;
import jemu.system.vz.VZTapeDevice;
import jemu.ui.JemuUi;

@RestController
public class JemuRestController {
	private static final Logger log = LoggerFactory.getLogger(JemuRestController.class);

	@Autowired
	private JemuUi jemuUi;

	private VZ computer() {
		return (VZ) jemuUi.getComputer();
	}

	private VZTapeDevice tape() {
		return computer().getTapeDevice();
	}

	@RequestMapping("/vz200")
	public String info() {
		return "VZ200 Emulator (JEMU) - modified version";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/reset")
	public String reset() {
		jemuUi.resetComputer();
		return "reset done";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/vz", consumes = "application/octet-stream;charset=UTF-8")
	public String loadVZ(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			computer().loadBinaryFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des VZ-Programms", e);
			return "Fehler beim Einspielen des VZ-Programms: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/vz", consumes = "application/octet-stream;charset=UTF-8")
	public void readVz(@RequestParam(defaultValue = "") String range,
			@RequestParam(defaultValue = "false") Boolean autorun, HttpServletResponse response) {
		// Set the content type and attachment header.
		response.addHeader("Content-disposition", "attachment;filename=myprogram.vz");
		response.setContentType("application/octet-stream");

		// Copy the stream to the response's output stream.
		try {
			computer().saveFile(response.getOutputStream(), range, autorun);
			response.flushBuffer();
		} catch (Exception e) {
			log.error("Fehler beim Schreiben", e);
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/bas", consumes = "application/octet-stream;charset=UTF-8")
	public String loadBas(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			computer().loadSourceFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des Basic-Programms", e);
			return "Fehler beim Einspielen des Basic-Programms: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/asm", consumes = "application/octet-stream;charset=UTF-8")
	public String loadAsm(@RequestParam(defaultValue = "True") Boolean autorun, RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			return computer().loadAsmFile(is, autorun);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des Assembler-Programms", e);
			return "Fehler beim Einspielen des Assembler-Programms:\n" + e.getMessage();
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/asmzip", consumes = "application/octet-stream;charset=UTF-8")
	public String loadAsmZip(@RequestParam(defaultValue = "True") Boolean autorun, RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			return computer().loadAsmZip(is, autorun);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des Assembler-Programms", e);
			return "Fehler beim Einspielen des Assembler-Programms:\n" + e.getMessage();
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/hex", consumes = "application/octet-stream;charset=UTF-8")
	public String loadHex(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			computer().loadHexFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des HEX-Dumps", e);
			return "Fehler beim Einspielen des HEX-Dumps: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/hex/{address}")
	public String readHex(@PathVariable(name = "address") String address,
			@RequestParam(name = "width", defaultValue = "16", required = false) int width) {
		String addressFrom = address;
		String addressTo = address;
		if (address.contains("-")) {
			addressFrom = address.split("-")[0];
			addressTo = address.split("-")[1];
		}
		int a = Integer.valueOf(addressFrom, 16);
		int b = Integer.valueOf(addressTo, 16);
		if (b == a) {
			b = a + 1024;
		}
		StringBuilder result = new StringBuilder();
		int n = 0;
		StringBuilder ascii = new StringBuilder();
		for (int i = a; i < b; i++) {
			if (n % width == 0) {
				result.append(String.format("%04x: ", i));
			}
			int value = computer().getMemory().readByte(i);
			ascii.append(value >= 32 && value < 127 ? (char) value : ".");
			result.append(String.format("%02x ", value));
			n++;
			if (n % width == 0) {
				result.append(" ").append(ascii).append("\n");
				ascii = new StringBuilder();
			}
		}
		return result.toString();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/printer/flush")
	public String flushPrinter() {
		List<String> lines = computer().flushPrinter();
		return lines.stream().collect(Collectors.joining("\n"));
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/tape")
	public String getTapeName() {
		return tape().getTapeName();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/{tapename}")
	public String setTapeName(@PathVariable(name = "tapename") String tapename) {
		tape().changeTape(tapename);
		return "Tape " + tapename + " eingelegt";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/tape/slot")
	public int getTapeSlot() {
		return tape().slot();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/slot/{id}")
	public void setTapeSlot(@PathVariable(name = "id") int id) {
		tape().slot(id);
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/play")
	public int playTape() {
		tape().play();
		return tape().slot();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/record")
	public int recordTape() {
		tape().record();
		return tape().slot();
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/stop")
	public int stopType() {
		tape().stop();
		return tape().slot();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/asm/{address}")
	public String readAsm(@PathVariable(name = "address") String address) {
		String addressFrom = address;
		String addressTo = address;
		if (address.contains("-")) {
			addressFrom = address.split("-")[0];
			addressTo = address.split("-")[1];
		}
		int a = Integer.valueOf(addressFrom, 16);
		int b = Integer.valueOf(addressTo, 16);
		if (b == a) {
			b = a + 1024;
		}
		return computer().disassemble(a, b);
	}

	@RequestMapping(method = RequestMethod.POST, path = "/vz200/sound/{volume}")
	public String setVolume(@PathVariable(name = "volume") int volume) {
		computer().setVolume(volume);
		return "OK.";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/sound")
	public int getVolume() {
		return computer().getVolume();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/vz200/registers")
	public String getRegisters() {
		String[] names = computer().getProcessor().getRegisterNames();

		List<Pair<String, Integer>> regs = new ArrayList<>();  
		for (int i = 0; i < names.length; i++) {
			regs.add(Pair.of(names[i], computer().getProcessor().getRegisterValue(i)));
		}
		
		return regs.stream().sorted((a, b) -> a.getLeft().compareTo(b.getLeft()))
		.map(p -> String.format("\"%s\": \"%04X\"", p.getLeft(), p.getRight()))
		.collect(Collectors.joining(", ", "{", "}"));
	}
}
