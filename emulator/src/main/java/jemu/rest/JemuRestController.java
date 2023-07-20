package jemu.rest;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jemu.Jemu;
import jemu.core.cpu.Z80;
import jemu.core.device.memory.Memory;
import jemu.exception.JemuException;
import jemu.rest.dto.JemuVersion;
import jemu.rest.dto.KeyboardInput;
import jemu.rest.dto.TapeInfo;
import jemu.rest.dto.VzSource;
import jemu.rest.security.SecurityService;
import jemu.system.vz.*;
import jemu.system.vz.export.Loader;
import jemu.system.vz.export.StaticLoaderFactory;
import jemu.ui.JemuUi;
import jemu.util.vz.KeyboardController;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@RestController
@CrossOrigin
@RequestMapping("/api")
@OpenAPIDefinition(info = @Info(title = "JEMU VZ200-Remake", version = "2.5",
        description = "Rest API to controle the emulator Jemu-VZ200-Remake",
        license = @License(name = "GPL 3.0", url = "https://fsf.org"),
        contact = @Contact(url = "cwahlmann.github.io/vz200-remake", name = "Christian Wahlmann", email = "")))
public class JemuRestController {
    private static final Logger log = LoggerFactory.getLogger(JemuRestController.class);

    private final JemuUi jemuUi;
    private final VzDirectory vzDirectory;
    //    private final SecurityService securityService;
    private final KeyboardController keyboardController;

    @Autowired
    public JemuRestController(JemuUi jemuUi, VzDirectory vzDirectory,
            //                              SecurityService securityService,
                              KeyboardController keyboardController) {
        this.jemuUi = jemuUi;
        this.vzDirectory = vzDirectory;
        //        this.securityService = securityService;
        this.keyboardController = keyboardController;
    }

    private VZ computer() {
        return (VZ) jemuUi.getComputer();
    }

    private VZTapeDevice tape() {
        return computer().getTapeDevice();
    }

    // disable security methods for now
    //    @RequestMapping(method = RequestMethod.GET, path = "/vz200/token", produces = "application/json;charset=UTF-8")
    //    public Token createToken(HttpServletRequest request) {
    //      String ip = request.getRemoteAddr();
    //  UUID id = UUID.randomUUID();
    //   String token = securityService.createToken(id);
    //  int n = securityService.newAuthorizationRequest(ip, id);
    //        computer().alert(String.format("OUT(249,%d) TO GRANT ACCESS FOR\n%s", n,
    //                                       securityService.getRequest(n).getSplitIp()));
    //  return new Token().withValue(token);
    //}

    @RequestMapping(path = "/swagger")
    @Hidden
    public void swagger(HttpServletResponse response) throws IOException {
        response.sendRedirect("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config");
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/version", produces = "application/json;charset=UTF-8")
    @Operation(summary = "get version of emulator", responses = {@ApiResponse(description = "the version",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = JemuVersion.class)))})
    public JemuVersion getVersion(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);
        return new JemuVersion(Jemu.VERSION);
    }

    // memory read / write

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/memory/{type}",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "read data from systems memory", responses = {@ApiResponse(description = "a source object",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = VzSource.class)))})
    public VzSource memoryRead(
            @PathVariable(name = "type") VzSource.SourceType type,
            @RequestParam(name = "from", defaultValue = "", required = false) String from,
            @RequestParam(name = "to", defaultValue = "", required = false) String to,
            @RequestParam(name = "name", defaultValue = "DEFAULT", required = false) String name
            //        , @RequestHeader String token
    ) {
        //        securityService.validateToken(token);

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

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/memory", consumes = "application/json;charset=UTF-8")
    @Operation(summary = "write data to systems memory",
            responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "500")})
    public ResponseEntity memoryWrite(@RequestBody VzSource source
                                      // , @RequestHeader String token
    ) {
        //    securityService.validateToken(token);

        Loader<?> loader = StaticLoaderFactory.create(source.getType(), computer().getMemory()).orElse(null);
        if (loader == null) {
            throw new JemuException(String.format("no loader found for source type [%s]", source.getType().name()));
        }
        loader.withName(source.getName()).withAutorun(source.isAutorun());
        try {
            loader.importData(source);
        } catch (Exception e) {
            log.error("Error importing data", e);
            String message = e.getMessage();
            if (e.getCause() != null) {
                message += ": " + e.getCause().getMessage();
            }
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
        if (loader.isAutorun() && source.getType() != VzSource.SourceType.basic) {
            ((Z80) computer().getProcessor()).setPC(loader.getStartAddress());
        } else {
            this.keyboardType(KeyboardInput.of("run\n"));
        }
        return ResponseEntity.ok().build();
    }

    // ------------ vz dir read / write

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/dir", produces = "application/json;charset=UTF-8")
    @Operation(summary = "read fileinfos of all vz-files in the vz directory", responses = {
            @ApiResponse(description = "a set of VZ-fileinfos",
                    content = @Content(mediaType = "application/json;charset=UTF-8",
                            schema = @Schema(implementation = Set.class)))})
    public Set<VzFileInfo> dirGetVzFileInfos(
            // @RequestHeader String token,
            HttpServletResponse response) {
        // securityService.validateToken(token);

        // Set the content type and attachment header.
        response.setContentType("application/json");
        return vzDirectory.readFileInfos();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/vz200/dir/{id}", consumes = "application/json;charset=UTF-8")
    @Operation(summary = "write data as vz-file to the vz directory", responses = {@ApiResponse(responseCode = "200")})
    public ResponseEntity dirWriteData(@PathVariable("id") int id, @RequestBody VzSource source
                                       //        , @RequestHeader String token
    ) throws IOException {
        //        securityService.validateToken(token);

        Memory memory = new VZMemory(true);
        Loader<?> loaderFrom = StaticLoaderFactory.create(source.getType(), memory).orElse(null);
        if (loaderFrom == null) {
            throw new JemuException(String.format("no loader found for source type [%s]", source.getType().name()));
        }
        loaderFrom.withName(source.getName()).withAutorun(source.isAutorun());
        loaderFrom.importData(source);

        Loader<?> loaderTo = StaticLoaderFactory.create(VzSource.SourceType.vz, memory).orElse(null)
                                                .withName(source.getName()).withAutorun(source.isAutorun())
                                                .withStartAddress(loaderFrom.getStartAddress())
                                                .withEndAddress(loaderFrom.getEndAddress());
        VzSource vz = loaderTo.exportData();

        try (InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(vz.getSource()))) {
            Files.copy(is, Paths.get(VzDirectory.getFilename(id)), StandardCopyOption.REPLACE_EXISTING);
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/dir/{type}/{id}",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "read data from the given vz-file in the vz-directory", responses = {
            @ApiResponse(description = "a source object",
                    content = @Content(mediaType = "application/json;charset=UTF-8",
                            schema = @Schema(implementation = VzSource.class)))})
    public VzSource dirReadData(
            @PathVariable(name = "type") VzSource.SourceType type, @PathVariable(name = "id") int id,
            // @RequestHeader String token,
            HttpServletResponse response) throws IOException {
        // securityService.validateToken(token);

        // Set the content type and attachment header.
        String filename = String.format(VzDirectory.getFilename(id));
        File file = Paths.get(filename).toFile();
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        VzSource source = new VzSource()
                .withSource(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(file))).withName("unknown")
                .withType(VzSource.SourceType.vz);

        Memory memory = new VZMemory(true);

        Loader<?> loaderFrom = StaticLoaderFactory.create(VzSource.SourceType.vz, memory).orElse(null);
        loaderFrom.importData(source);

        Loader<?> loaderTo = StaticLoaderFactory.create(type, memory).orElse(null);
        if (loaderTo == null) {
            throw new JemuException(String.format("no loader found for source type [%s]", type));
        }

        loaderTo.withName(loaderFrom.getName()).withAutorun(loaderFrom.isAutorun())
                .withStartAddress(loaderFrom.getStartAddress()).withEndAddress(loaderFrom.getEndAddress());
        return loaderTo.exportData();
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/vz200/dir/{id}")
    @Operation(summary = "delete given vz-file from the vz-directory", responses = {@ApiResponse(responseCode = "200")})
    public void deleteVzFileFromDir(@PathVariable("id") int id,
                                    // @RequestHeader String token,
                                    HttpServletResponse response) throws IOException {
        // securityService.validateToken(token);

        Path path = Paths.get(VzDirectory.getFilename(id));
        if (!Files.exists(path)) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "vzfile " + id + " does not exist");
            return;
        }
        Files.delete(path);
    }

    // ------------ data conversion

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/convert/{fromtype}/{totype}",
            consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @Operation(summary = "convert data from one type to another", responses = {
            @ApiResponse(description = "a source object",
                    content = @Content(mediaType = "application/json;charset=UTF-8",
                            schema = @Schema(implementation = VzSource.class)))})
    public VzSource convertSource(@PathVariable("totype") VzSource.SourceType totype, @RequestBody VzSource source
                                  //        , @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        Memory memory = new VZMemory(true);
        Loader<?> from = StaticLoaderFactory.create(source.getType(), memory).orElse(null);
        Loader<?> to = StaticLoaderFactory.create(totype, memory).orElse(null);
        from.importData(source);
        to.withName(from.getName());
        to.withAutorun(source.isAutorun());
        to.withStartAddress(from.getStartAddress());
        to.withEndAddress(from.getEndAddress());
        return to.exportData();
    }

    // ------------ printer

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/printer/flush",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "flush the buffered printer output", responses = {
            @ApiResponse(description = "a list containing the printed lines",
                    content = @Content(mediaType = "application/json;charset=UTF-8",
                            schema = @Schema(implementation = List.class)))})
    public List<String> printerFlush(
            //       @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        return computer().flushPrinter();
    }

    // ------------ keyboard

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/keyboard", consumes = "application/json;charset=UTF-8")
    @Operation(summary = "type some text to the systems keyboard", responses = {@ApiResponse(responseCode = "200")})
    public ResponseEntity keyboardType(@RequestBody KeyboardInput keyboardInput
                                       //        , @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        new Thread(() -> {
            try {
                keyboardController.type(keyboardInput.getValue());
            } catch (InterruptedException e) {
                log.warn("Warnung: Thread-Wartezeit wurde unterbrochen beim Übermitteln an den Keyboard-Controller:",
                         e);
            } catch (Exception e) {
                log.error("Fehler beim Übermitteln an den Keyboard-Controller", e);
            }
        }).start();
        return ResponseEntity.ok().build();
    }

    // ------------ tapes

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/tape", produces = "application/json;charset=UTF-8")
    @Operation(summary = "get infos about all available tapes", responses = {
            @ApiResponse(description = "a list of Tapeinfo",
                    content = @Content(mediaType = "application/json;charset=UTF-8",
                            schema = @Schema(implementation = List.class)))})
    public List<TapeInfo> tapeGetAllInfos(
            //        @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        return computer().getTapeDevice().readTapeInfos();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/tape/{tapename}")
    @Operation(summary = "get infos about the given tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo tapeGetInfo(@PathVariable(name = "tapename") String tapename
                                // , @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        return computer().getTapeDevice().readTapeInfo(tapename);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/vz200/tape/{tapename}")
    // @ApiOperation(value = "create a new tape", produces = "application/json;charset=UTF-8", response = TapeInfo.class)
    @Operation(summary = "create a new tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo tapeCreate(@PathVariable(name = "tapename") String tapename
                               //        , @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().getTapeDevice().createTape(tapename);
        return new TapeInfo(tapename, 0, 0, VZTapeDevice.Mode.idle);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/vz200/tape/{tapename}")
    @Operation(summary = "delete a tape", responses = {@ApiResponse(responseCode = "200")})
    public ResponseEntity tapeDelete(@PathVariable(name = "tapename") String tapename
                                     //        , @RequestHeader String token
    ) {
        //securityService.validateToken(token);

        computer().getTapeDevice().deleteTape(tapename);
        return ResponseEntity.ok().build();
    }

    // ------------ player control

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/player", produces = "application/json;charset=UTF-8")
    @Operation(summary = "get info about the current tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerGetInfo(
            //@RequestHeader String token
    ) {
        //        securityService.validateToken(token);

        String tapename = computer().getTapeDevice().getTapeName();
        int position = computer().getTapeDevice().getPosition();
        int positionCount = computer().getTapeDevice().getSlotsSize();
        VZTapeDevice.Mode mode = computer().getTapeDevice().getMode();
        return new TapeInfo(tapename, position, positionCount, mode);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/player/{tapename}")
    @Operation(summary = "insert tape with the given name", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerInsertTape(@PathVariable(name = "tapename") String tapename
                                     //        , @RequestHeader String token
    ) {
        //        securityService.validateToken(token);

        computer().getTapeDevice().changeTape(tapename);
        return playerGetInfo();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/player/play",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "start playing current tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerStartReading(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().getTapeDevice().play();
        return playerGetInfo();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/player/record",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "start recording current tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerStartRecording(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().getTapeDevice().record();
        return playerGetInfo();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/player/reel/{position}",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "reel current tape to given position", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerMoveToPosition(@PathVariable(name = "position") int position
                                         //        , @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().getTapeDevice().setPosition(position);
        return playerGetInfo();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/player/stop",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "stop playing current tape", responses = {@ApiResponse(description = "a tapeinfo",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = TapeInfo.class)))})
    public TapeInfo playerStop(
            //        @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().getTapeDevice().stop();
        return playerGetInfo();
    }

    // ------------ sound control

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/volume/{volume}")
    @Operation(summary = "set the audio volume", responses = {@ApiResponse(responseCode = "200")})
    public ResponseEntity soundSetVolume(@PathVariable(name = "volume") int volume
                                         //, @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        computer().setVolume(volume);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/volume", produces = "application/json;charset=UTF-8")
    @Operation(summary = "get current audio volume", responses = {@ApiResponse(description = "the volume",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = Map.class)))})
    public Map<String, Integer> soundGetVolume(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);
        return Collections.singletonMap("volume", computer().getVolume());
    }

    // ------------ cpu control

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/cpu/registers",
            produces = "application/json;charset=UTF-8")
    @Operation(summary = "get cpu register values", responses = {@ApiResponse(description = "the register values",
            content = @Content(mediaType = "application/json;charset=UTF-8",
                    schema = @Schema(implementation = Map.class)))})
    public Map<String, Integer> cpuGetRegisters(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        Map<String, Integer> regs = new HashMap<>();
        String[] names = computer().getProcessor().getRegisterNames();
        for (int i = 0; i < names.length; i++) {
            regs.put(names[i], computer().getProcessor().getRegisterValue(i));
        }
        return regs;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/vz200/cpu/reset")
    @Operation(summary = "soft reset the system", responses = {@ApiResponse(responseCode = "200")})
    public ResponseEntity cpuReset(
            // @RequestHeader String token
    ) {
        // securityService.validateToken(token);

        jemuUi.softReset();
        return ResponseEntity.ok().build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/vz200/screen")
    @Operation(summary = "get a screenshot", responses = {@ApiResponse(description = "a png image",
            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(implementation = Map.class)))})
    public ResponseEntity<byte[]> getScreenshot() throws IOException {
        byte[] data;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            computer().exportScreenshot(out);
            out.flush();
            data = out.toByteArray();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE));
            responseHeaders.setContentLength(data.length);
            responseHeaders.set("Content-disposition", "attachment; filename=screenshot.png");
            return new ResponseEntity<byte[]>(data, responseHeaders, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
