package de.dreierschach.vz200ui.config;

import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

@SpringComponent
@VaadinSessionScope
public class Config {
    public static final Logger log = LoggerFactory.getLogger(Config.class);

    public static final String COOKIE_KEY = "vz200-ui-config";
    public static final int COOKIE_MAXAGE = 3600 * 24 * 90;
    public static final String HOSTNAME = "HOSTNAME";
    public static final String PORT = "PORT";
    public static final String ACE_THEME = "ACE_THEME";
    public static final String APP_THEME_VARIANT = "APP_THEME_VARIANT";

    private final Properties properties = new Properties();
    private boolean changed = false;

    public void load() throws IOException {
        String value = Arrays.stream(Optional.ofNullable(VaadinRequest.getCurrent()).map(VaadinRequest::getCookies)
                                             .orElse(new Cookie[]{}))
                             .filter(cookie -> COOKIE_KEY.equals(cookie.getName())).findAny().map(Cookie::getValue)
                             .map(Base64.getDecoder()::decode).map(b -> new String(b, StandardCharsets.UTF_8))
                             .orElse(null);
        if (value != null) {
            properties.load(new StringReader(value));
            changed = false;
        }
    }

    public void save() throws IOException {
        StringWriter sw = new StringWriter();
        properties.store(sw, null);
        String base64 = Base64.getEncoder().withoutPadding()
                              .encodeToString(sw.toString().getBytes(StandardCharsets.UTF_8));
        Cookie myCookie = new Cookie(COOKIE_KEY, base64);
        myCookie.setMaxAge(COOKIE_MAXAGE);
        myCookie.setPath("/");
        VaadinService.getCurrentResponse().addCookie(myCookie);
        changed = false;
    }

    public String getOrDefault(String key, String defaultValue) {
        if (!properties.containsKey(key)) {
            set(key, defaultValue);
        }
        return properties.getProperty(key);
    }

    public void set(String key, String value) {
        changed = !StringUtils.equals(properties.getProperty(key), value);
        properties.setProperty(key, value);
    }

    public boolean isChanged() {
        return changed;
    }
}
