package jemu.system.vz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jemu.system.vz.export.VzFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
public class VzDirectory {
	private static final Logger log = LoggerFactory.getLogger(VzDirectory.class);
	public static final String VZFILE_NAME_FORMAT = "vzfile_%03d.vz";

	public static String getDir() throws IOException {
		String dir = System.getProperty("user.home") + "/vz200/vz";
		Files.createDirectories(Paths.get(dir));
		return dir;
	}

	public static String getFilename(int value) throws IOException {
		return getDir() + "/" + String.format(VZFILE_NAME_FORMAT, value);
	}

	public Set<VzFileInfo> readFileInfos() {
		Set<VzFileInfo> result = new HashSet<>();
		try {
			String dir = getDir();
			Files.newDirectoryStream(Paths.get(dir), "*.vz").forEach(path -> {
				String filename = path.getFileName().toString();
				if (filename.matches("vzfile_[0-9]{3}\\.vz")) {
					result.add(readFileInfo(path));
				}
			});
			return result;
		} catch (IOException e) {
			log.error("error reading vz-file infos", e);
		}
		return Collections.emptySet();
	}

	private VzFileInfo readFileInfo(Path path) {
		File file = path.toFile();
		byte[] header = new byte[24];
		long size = file.length();
		int id = Integer.parseInt(path.getFileName().toString().substring(7, 10));
		try (FileInputStream in = new FileInputStream(file)) {
			in.read(header, 0, 24);
			return new VzFileInfo().withId(id).withLength((int) size).withName(VzFileLoader.decodeName(header))
					.withAutorun(VzFileLoader.decodeAutorun(header))
					.withStart(VzFileLoader.decodeStartAddress(header));
		} catch (IOException e) {
			log.error("unable to read the name stored in the vz-file", e);
			return VzFileInfo.EMPTY;
		}
	}
}
