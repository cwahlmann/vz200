package jemu;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jemu.config.Constants;
import jemu.config.JemuConfiguration;


@Configuration
public class JemuContext {

	@Bean
	public JemuConfiguration jemuConfiguration() {
		Path propertiesPath = Paths.get(System.getProperty("user.home") + "/.jemu");
		JemuConfiguration config = new JemuConfiguration(propertiesPath).updateImmediatliy();
		config.setIfMissing(Constants.LAST_WORKING_DIR, System.getProperty("user.home"));
		config.setIfMissing(Constants.SCREEN_WIDTH, "800");
		config.setIfMissing(Constants.SCREEN_HEIGHT, "480");
		config.setIfMissing(Constants.FULLSCREEN, "true");
		config.setIfMissing(Constants.SOUND_VOLUME, "100");
		config.setIfMissing(Constants.ENABLE_DOS_ROM, "false");
		return config;
	};
}
