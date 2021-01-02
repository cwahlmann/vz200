package de.dreierschach.vz200ui.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.dreierschach.vz200ui.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

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
}
