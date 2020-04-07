package jemu.rest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import jemu.exception.JemuException;
import jemu.exception.JemuForbiddenException;
import org.apache.tomcat.util.bcel.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.security.auth.login.Configuration;
import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@Component
public class SecurityService {
    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);

    private final JemuConfiguration config;

    private AuthorizationRequest[] authorizationRequests = new AuthorizationRequest[256];
    private AtomicInteger nextAuthorizationRequestIndex = new AtomicInteger(0);

    public SecurityService(JemuConfiguration config) {
        this.config = config;
        this.config.setIfMissing(Constants.JWT_SECRET, UUID.randomUUID().toString());
        this.config.setIfMissing(Constants.JWT_EXPIRE_MIN, Constants.JWT_DEFAULT_EXPIRE_MIN);
    }

    public String createToken(UUID id) {
        long expire = config.getLong(Constants.JWT_EXPIRE_MIN, Constants.JWT_DEFAULT_EXPIRE_MIN);
        String secret = this.config.get(Constants.JWT_SECRET);
        Map<String, Object> claims = new HashMap<>();
        //claims.put("ID", id.toString());
        return Jwts.builder().addClaims(claims).setId(id.toString())
                   .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + 60000 * expire)).compact();
    }

    public void validateToken(String token) {
        String secret = this.config.get(Constants.JWT_SECRET);
        try {
            Claims claims = Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
            if (!validateId((String) claims.getId()) && validateExpiration(claims.getExpiration())) {
                throw new JemuForbiddenException("NOT AUTHORIZED");
            }
        } catch (Exception e) {
            throw new JemuForbiddenException("NOT AUTHORIZED", e);
        }
    }

    public int newAuthorizationRequest(String ip, UUID id) {
        int index = nextAuthorizationRequestIndex.getAndAccumulate(1, (a, b) -> (a + b) & 0xff);
        authorizationRequests[index] = new AuthorizationRequest(ip, id);
        return index;
    }

    public boolean authorize(int index) {
        AuthorizationRequest request = authorizationRequests[index];
        if (request == null || request.isExpired()) {
            authorizationRequests[index] = null;
            return false;
        }
        try {
            setValid(request.getUuid());
            log.info(String.format("request no. %d / IP %s has been authorized", index, request.getIp()));
            return true;
        } catch (JemuException e) {
            log.error(String.format("error authorizing request no. %d / IP %s", index, request.getIp()), e);
            return false;
        }
    }

    public AuthorizationRequest getRequest(int index) {
        return index >= 0 && index < authorizationRequests.length ? authorizationRequests[index] : null;
    }

    // private methods

    private boolean validateId(String id) {
        Path dir = getSecurityDir();
        return Files.exists(getIdFile(dir, id));
    }

    private void setValid(UUID id) {
        Path dir = getSecurityDir();
        try {
            Files.createFile(getIdFile(dir, id.toString()));
        } catch (IOException e) {
            throw new JemuException(String.format("unable to create file in path %s", dir.toString()), e);
        }
        doHousekeeping();
    }

    private Path getSecurityDir() {
        Path dir = Paths.get(System.getProperty("user.home"), "vz200", "security");
        try {
            Files.createDirectories(dir);
            return dir;
        } catch (IOException e) {
            throw new JemuException(String.format("unable to create path %s", dir.toString()), e);
        }
    }

    private Path getIdFile(Path dir, String id) {
        return dir.resolve(id + ".valid");
    }

    private boolean validateExpiration(Date expirationDate) {
        return expirationDate.after(new Date(System.currentTimeMillis()));
    }


    private void doHousekeeping() {
        // delete all files in security path that are older than 14 days
        Path dir = getSecurityDir();
        try {
            Files.newDirectoryStream(dir, "*.valid").forEach(path -> {
                try {
                    if (Files.getLastModifiedTime(path).compareTo(FileTime.fromMillis(
                            System.currentTimeMillis() + Constants.JWT_HOUSKEEPING_EXPIRE_MIN * 60000)) > 0) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    throw new JemuException(String.format("unable to access file in %d", dir.toString()), e);
                }
            });
        } catch (IOException e) {
            throw new JemuException(String.format("unable to read content of %d", dir.toString()), e);
        }
    }
}
