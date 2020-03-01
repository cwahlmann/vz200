package jemu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Jemu {

	public static final String VERSION = "jemu-vz200-remake-1.0";

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Jemu.class);
        builder.headless(false).run(args);   
	}
}
