package jemu;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jemu.config.JemuConfiguration;
import jemu.util.assembler.z80.Constants;


@Configuration
public class JemuContext {

	@Bean
	public JemuConfiguration jemuConfiguration() {
		Path propertiesPath = Paths.get(System.getProperty("user.home") + "/.jemu");
		JemuConfiguration config = new JemuConfiguration(propertiesPath).updateImmediatliy();
		config.setIfMissing(Constants.LAST_WORKING_DIR, System.getProperty("user.home"));
		config.setIfMissing(Constants.SCREEN_WIDTH, "800");
		config.setIfMissing(Constants.SCREEN_HEIGHT, "980");
		config.setIfMissing(Constants.FULLSCREEN, "false");
		return config;
	};
}
