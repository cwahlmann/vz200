package jemu;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * This file is part of JemuVz200, an enhanced VZ200 emulator,
 * based on the works of Richard Wilson (2002) - see http://jemu.winape.net
 * <p>
 * The software is open source by the conditions of the GNU General Public Licence 3.0. See the copy of the GPL 3.0
 * (gpl-3.0.txt) you received with this software.
 *
 * @author Christian Wahlmann
 */

@SpringBootApplication
public class Jemu {

    public static final String VERSION = "jemu-vz200-remake-2.5";

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Jemu.class);
        builder.headless(false).run(args);
    }
}
