package jemu;

import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@Configuration
@EnableWebMvc
public class JemuContext {
    private static final Logger log = LoggerFactory.getLogger(JemuContext.class);

    @Bean
    public JemuConfiguration jemuConfiguration() {
        Path propertiesPath = Paths.get(System.getProperty("user.home") + "/.jemu");
        JemuConfiguration config = new JemuConfiguration(propertiesPath).updateImmediatliy();
        config.setIfMissing(Constants.LAST_WORKING_DIR, System.getProperty("user.home"));
        config.setIfMissing(Constants.SCREEN_WIDTH, "800");
        config.setIfMissing(Constants.SCREEN_HEIGHT, "480");
        config.setIfMissing(Constants.SOUND_VOLUME, "100");
        config.setIfMissing(Constants.ENABLE_DOS_ROM, "false");
        return config;
    }
}
