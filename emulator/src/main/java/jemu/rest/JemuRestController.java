package jemu.rest;

import jemu.Jemu;
import jemu.exception.JemuException;
import jemu.system.vz.VZ;
import jemu.system.vz.VZTapeDevice;
import jemu.system.vz.VzDirectory;
import jemu.system.vz.VzFileInfo;
import jemu.system.vz.export.Loader;
import jemu.system.vz.export.StaticLoaderFactory;
import jemu.ui.JemuUi;
import jemu.util.vz.VZUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class JemuRestController {
    private static final Logger log = LoggerFactory.getLogger(JemuRestController.class);

    @Autowired
    private JemuUi jemuUi;

    @Autowired
    private VzDirectory vzDirectory;

    private VZ computer() {
        return (VZ) jemuUi.getComputer();
    }

    private VZTapeDevice tape() {
        return computer().getTapeDevice();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/info", produces = "application/json;charset=UTF-8")
    public String info() {
        return Jemu.VERSION;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/reset")
    public ResponseEntity reset() {
        jemuUi.softReset();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/memory/{type}",
                    consumes = "application/json;charset=UTF-8")
    public ResponseEntity importVzSource(@PathVariable("type") VzSource.SourceType type, @RequestBody VzSource source) {
        Loader<?> loader = StaticLoaderFactory.create(type, computer().getMemory()).orElse(null);
        if (loader == null) {
            throw new JemuException(String.format("no loader found for source type [%s]", type.name()));
        }
        loader.withName(source.getName()).withAutorun(source.isAutorun());
        loader.importData(source);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/memory/{type}", produces = "application/json;charset=UTF-8")
    public VzSource exportVzSource(
            @PathVariable(name = "type") VzSource.SourceType type,
            @RequestParam(name = "from", defaultValue = "", required = false) String from,
            @RequestParam(name = "to", defaultValue = "", required = false) String to,
            @RequestParam(name = "name", defaultValue = "DEFAULT", required = false)
                    String name, HttpServletResponse response) throws IOException {
        Loader<?> loader = StaticLoaderFactory.create(type, computer().getMemory()).orElse(null);
        if (loader == null) {
            throw new JemuException(String.format("no loader found for source type [%s]", type.name()));
        }
        loader.withName(name);
        if (!StringUtils.isEmpty(from)) {
            loader.withStartAddress(Integer.valueOf(from, 16));
        }
        if (!StringUtils.isEmpty(to)) {
            loader.withEndAddress(Integer.valueOf(to, 16));
        }
        return loader.exportData().withAutorun(false).withName(name);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/asmzip",
                    consumes = "application/octet-stream;charset=UTF-8")
    public String loadAsmZip(@RequestParam(defaultValue = "True") Boolean autorun, RequestEntity<InputStream> entity) {
        try (InputStream is = entity.getBody()) {
            return computer().loadAsmZipFile(is, autorun);
        } catch (IOException e) {
            throw new JemuException("Fehler beim Einspielen des Assembler-Programms", e);
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/printer/flush",
                    produces = "application/json;charset=UTF-8")
    public List<String> flushPrinter() {
        return computer().flushPrinter();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/typetext/", consumes = "text/plain;charset=UTF-8")
    public ResponseEntity typeText(RequestEntity<String> entity) {
        try {
            String text2Type = entity.getBody();
            VZUtils.type(text2Type, 250, computer().getKeyboard());
        } catch (InterruptedException e) {
            log.warn("Warnung: Thread-Wartezeit wurde unterbrochen:", e);
        } catch (Exception e) {
            throw new JemuException("Fehler beim Einspielen des HEX-Dumps", e);
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/tape", produces = "application/json;charset=UTF-8")
    public String getTapeName() {
        return tape().getTapeName();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/{tapename}")
    public ResponseEntity setTapeName(@PathVariable(name = "tapename") String tapename) {
        tape().changeTape(tapename);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/tape/slot", produces = "application/json;charset=UTF-8")
    public int getTapeSlot() {
        return tape().slot();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/slot/{id}")
    public ResponseEntity setTapeSlot(@PathVariable(name = "id") int id) {
        tape().slot(id);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/play", produces = "application/json;charset=UTF-8")
    public int playTape() {
        tape().play();
        return tape().slot();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/record",
                    produces = "application/json;charset=UTF-8")
    public int recordTape() {
        tape().record();
        return tape().slot();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/tape/stop", produces = "application/json;charset=UTF-8")
    public int stopType() {
        tape().stop();
        return tape().slot();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/sound/{volume}")
    public ResponseEntity setVolume(@PathVariable(name = "volume") int volume) {
        computer().setVolume(volume);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/sound", produces = "application/json;charset=UTF-8")
    public int getVolume() {
        return computer().getVolume();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/registers", produces = "application/json;charset=UTF-8")
    public Map<String, Integer> getRegisters() {
        Map<String, Integer> regs = new HashMap<>();
        String[] names = computer().getProcessor().getRegisterNames();
        for (int i = 0; i < names.length; i++) {
            regs.put(names[i], computer().getProcessor().getRegisterValue(i));
        }
        return regs;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/dir", produces = "application/json;charset=UTF-8")
    public Set<VzFileInfo> getDir(HttpServletResponse response) {
        // Set the content type and attachment header.
        response.setContentType("application/json");
        return vzDirectory.readFileInfos();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/dir/{id}",
                    produces = "application/octet-stream;" + "charset=UTF-8")
    public void getVzFileFromDir(@PathVariable("id") int id, HttpServletResponse response) throws IOException {
        // Set the content type and attachment header.
        String filename = String.format(VzDirectory.VZFILE_NAME_FORMAT, id);
        response.addHeader("Content-disposition", "attachment;filename=" + filename);
        response.setContentType("application/octet-stream");
        try (OutputStream out = response.getOutputStream()) {
            Path path = Paths.get(VzDirectory.getFilename(id));
            if (!Files.exists(path)) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "vzfile " + id + " does not exist");
                return;
            }
            Files.copy(path, out);
            response.flushBuffer();
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/dir/{id}",
                    consumes = "application/octet-stream;" + "charset=UTF-8")
    public void postVzFileToDir(@PathVariable("id") int id, RequestEntity<InputStream> entity) throws IOException {
        try (InputStream is = entity.getBody()) {
            Files.copy(is, Paths.get(VzDirectory.getFilename(id)), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/vz200/dir/{id}")
    public void deleteVzFileFromDir(@PathVariable("id") int id, HttpServletResponse response) throws IOException {
        Path path = Paths.get(VzDirectory.getFilename(id));
        if (!Files.exists(path)) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "vzfile " + id + " does not exist");
            return;
        }
        Files.delete(path);
    }
}
