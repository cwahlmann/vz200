package jemu.rest.security;

import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import jemu.exception.JemuException;
import jemu.exception.JemuForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityServiceTest {
    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    private JemuConfiguration config;
    private List<Path> filesToDelete;

    @BeforeEach
    private void init() {
        config = Mockito.mock(JemuConfiguration.class);
        Mockito.when(config.getLong(Mockito.anyString(), Mockito.anyInt())).thenReturn(12l * 60);
        Mockito.when(config.get(Constants.JWT_SECRET)).thenReturn("12345");
        filesToDelete = new ArrayList<>();
    }

    @AfterEach
    private void cleanup() {
        filesToDelete.forEach(this::deleteSilently);
    }

    @Test
    public void testOverallGood() throws IOException {
        UUID id = UUID.randomUUID();
        SecurityService securityService = new SecurityService(config);
        filesToDelete.add(Paths.get(System.getProperty("user.home"), "vz200", "security", id.toString() + ".valid"));

        // Token abrufen
        String token = securityService.createToken(id);
        // Token autorisieren
        int requestid = securityService.newAuthorizationRequest("123.456.789.000", id);
        securityService.authorize(requestid);
        // Token validieren
        securityService.validateToken(token);
    }

    @Test
    public void testOverallBad1() throws IOException {
        UUID id = UUID.randomUUID();
        SecurityService securityService = new SecurityService(config);

        // Token abrufen
        String token = securityService.createToken(id);
        // Token autorisieren
        int requestid = securityService.newAuthorizationRequest("123.456.789.000", id);
        // do not authorize:
        // securityService.authorize(requestid);

        // Token validieren
        JemuForbiddenException exception = Assertions.assertThrows(JemuForbiddenException.class, () -> {
            securityService.validateToken(token);
        });
        Assertions.assertTrue(exception.getMessage().contains("NOT AUTHORIZED"));
    }

    @Test
    public void testOverallBad2() throws IOException {
        UUID id = UUID.randomUUID();
        SecurityService securityService = new SecurityService(config);

        // Token abrufen
        String token = securityService.createToken(id);
        // Token autorisieren
        int requestid = securityService.newAuthorizationRequest("123.456.789.000", id);
        securityService.authorize(requestid);

        // Token validieren
        JemuForbiddenException exception = Assertions.assertThrows(JemuForbiddenException.class, () -> {
            securityService.validateToken("falsches Token");
        });
        Assertions.assertTrue(exception.getMessage().contains("NOT AUTHORIZED"));
    }

    private void deleteSilently(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (Exception e) {
            log.error("unable to delete file {}", file.toString(), e);
        }
    }
}

