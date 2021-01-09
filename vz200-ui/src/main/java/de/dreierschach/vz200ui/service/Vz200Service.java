package de.dreierschach.vz200ui.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringComponent
@VaadinSessionScope
public class Vz200Service {
    private static final Logger log = LoggerFactory.getLogger(Vz200Service.class);

    private final RestTemplate restTemplate;
    private final Config config;

    @Autowired
    public Vz200Service(RestTemplate restTemplate, Config config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public VzSource loadBasicFromMemory() {
        return restTemplate.getForObject(getBaseUrl() + "memory/basic", VzSource.class);
    }

    public void saveBasicToMemory(VzSource source) {
        source.setType(VzSource.SourceType.basic);
        restTemplate.postForLocation(getBaseUrl() + "memory", source);
    }

    public VzSource loadAssemblerFromMemory(String from, String to) {
        return restTemplate
                .getForObject(getBaseUrl() + String.format("memory/asm?from=%s&to=%s", from, to), VzSource.class);
    }

    public void saveAssemblerToMemory(VzSource source) {
        source.setType(VzSource.SourceType.asm);
        restTemplate.postForLocation(getBaseUrl() + "memory", source);
    }

    public void type(String s) {
        restTemplate.postForLocation(getBaseUrl() + "keyboard", new KeyboardInput().withValue(s));
    }

    public void reset() {
        restTemplate.postForLocation(getBaseUrl() + "cpu/reset", "none");
    }

    public String getJemuVersion() {
        JemuVersion version = restTemplate.getForObject(getBaseUrl() + "version", JemuVersion.class);
        return version.getVersion();
    }

    private final String getBaseUrl() {
        return "http://" + config.getOrDefault(Config.HOSTNAME, "localhost") + ":" +
               config.getOrDefault(Config.PORT, "8080") + "/api/vz200/";
    }

    private Thread scanDevicesThread = null;

    public void scanForDevices(String baseAdress, int port, Consumer<String> onDeviceFound, Consumer<String> onProgress) {
        if (scanDevicesThread != null) {
            scanDevicesThread.interrupt();
        }
        scanDevicesThread = new Thread(
                () -> IntStream.rangeClosed(1, 254).parallel().mapToObj(num -> baseAdress + "." + num).forEach(adr -> {
                    if (ping(adr)) {
                        String url = "http://" + adr + ":" + port + "/api/vz200/version";
                        if (isHttpOk(url)) {
                            onDeviceFound.accept(adr);
                        }
                    }
                    onProgress.accept(adr);
                }));
        scanDevicesThread.start();
    }

    private boolean ping(String address) {
        try {
            return InetAddress.getByName(address).isReachable(100);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isHttpOk(String url) {
        try {
            URL myURL = new URL(url);
            HttpURLConnection myConnection = (HttpURLConnection) myURL.openConnection();
            myConnection.setConnectTimeout(2000);
            myConnection.setReadTimeout(2000);
            return myConnection.getResponseCode() == HttpStatus.OK.value();
        } catch (Exception e) {
            return false;
        }
    }

    public String getFlushPrinter() {
        return String.valueOf(restTemplate.getForObject(getBaseUrl() + "printer/flush", List.class).stream()
                                          .collect(Collectors.joining("\n")));
    }

    // Tapedeck

    public List<TapeInfo> getAllTapeInfos() {
        ResponseEntity<TapeInfo[]> result = restTemplate.getForEntity(getBaseUrl() + "tape", TapeInfo[].class);
        if (result.getBody() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(result.getBody());
    }

    public void createTape(String name) {
        restTemplate.put(getBaseUrl() + "tape/" + name, null);
    }

    public void deleteTape(String name) {
        restTemplate.delete(getBaseUrl() + "tape/" + name);
    }

    public TapeInfo getCurrentTapeInfo() {
        return restTemplate.getForObject(getBaseUrl() + "player", TapeInfo.class);
    }

    public void insertTape(String name) {
        restTemplate.postForLocation(getBaseUrl() + "player/" + name, null);
    }

    public void playTape() {
        restTemplate.postForLocation(getBaseUrl() + "player/play", null);
    }

    public void recordTape() {
        restTemplate.postForLocation(getBaseUrl() + "player/record", null);
    }

    public void stopTape() {
        restTemplate.postForLocation(getBaseUrl() + "player/stop", null);
    }

    public void reelTape(int position) {
        restTemplate.postForLocation(getBaseUrl() + "player/reel/" + position, null);
    }
}
