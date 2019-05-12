package jemu.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jemu.ui.JemuUi;

@RestController
public class JemuRestController {
	private static final Logger log = LoggerFactory.getLogger(JemuRestController.class);

	@Autowired
	private JemuUi jemuUi;

	@RequestMapping("/jemu")
	public String sayHello() {
		return "Hello JEMU";
	}

	@RequestMapping("/jemu/reset")
	public String reset() {
		jemuUi.resetComputer();
		return "reset done";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/jemu/vz", consumes = "application/octet-stream;charset=UTF-8")
	public String loadVZ(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			jemuUi.loadBinaryFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des VZ-Programms", e);
			return "Fehler beim Einspielen des VZ-Programms: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/jemu/vz", consumes = "application/octet-stream;charset=UTF-8")
	public void readVz(HttpServletResponse response) {

		// Set the content type and attachment header.
		response.addHeader("Content-disposition", "attachment;filename=myprogram.vz");
		response.setContentType("application/octet-stream");

		// Copy the stream to the response's output stream.
		try {
			jemuUi.getComputer().saveFile(response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {
			log.error("Fehler beim Schreiben", e);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "/jemu/bas", consumes = "application/octet-stream;charset=UTF-8")
	public String loadBas(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			jemuUi.loadSourceFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des Basic-Programms", e);
			return "Fehler beim Einspielen des Basic-Programms: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/jemu/asm", consumes = "application/octet-stream;charset=UTF-8")
	public String loadAsm(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			jemuUi.loadAsmFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des Assembler-Programms", e);
			return "Fehler beim Einspielen des Assembler-Programms: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.POST, path = "/jemu/hex", consumes = "application/octet-stream;charset=UTF-8")
	public String loadHex(RequestEntity<InputStream> entity) {
		try (InputStream is = entity.getBody()) {
			jemuUi.loadHexFile(is);
		} catch (Exception e) {
			log.error("Fehler beim Einspielen des HEX-Dumps", e);
			return "Fehler beim Einspielen des HEX-Dumps: " + e.getMessage();
		}
		return "Daten eingespielt.";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/jemu/hex/{address}")
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
			b++;
		}
		StringBuilder result = new StringBuilder();
		int n = 0;
		for (int i = a; i < b; i++) {
			if (n % width == 0) {
				result.append(String.format("%04x: ", i));
			}
			int value = jemuUi.getComputer().getMemory().readByte(i);
			result.append(String.format("%02x ", value));
			n++;
			if (n % width == 0) {
				result.append("\n");
			}
		}
		return result.toString();
	}

	@RequestMapping(method = RequestMethod.GET, path = "/jemu/printer/flush")
	public String flushPrinter() {
		List<String> lines = jemuUi.flushPrinter();
		return lines.stream().collect(Collectors.joining("\n"));
	}
}
