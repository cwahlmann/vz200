package jemu.system.vz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
		byte[] buffer = new byte[24];
		long size = file.length();
		int id = Integer.valueOf(path.getFileName().toString().substring(7, 10));
		try (FileInputStream in = new FileInputStream(file)) {
			in.read(buffer, 0, 24);
			int endOfName = 4;
			while (endOfName < 21 && (buffer[endOfName] & 0xff) != 0) {
				endOfName++;
			}
			String name = new String(buffer, 4, endOfName-4, StandardCharsets.US_ASCII);
			return new VzFileInfo().withId(id).withLength((int) size).withName(name)
					.withAutorun((buffer[21] & 0xff) == 0xf1)
					.withStart((buffer[22] & 0xff) + 256 * (buffer[23] & 0xff));
		} catch (IOException e) {
			log.error("unable to read the name stored in the vz-file", e);
			return VzFileInfo.EMPTY;
		}
	}
}
