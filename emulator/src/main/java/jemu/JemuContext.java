package jemu;

import jemu.config.Constants;
import jemu.config.JemuConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@EnableWebMvc
@Configuration
@Import(SpringFoxConfig.class)
public class JemuContext implements WebMvcConfigurer {

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


}
