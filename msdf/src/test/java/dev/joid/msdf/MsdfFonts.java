package dev.joid.msdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFonts {

	public static final String REGULAR     = "JoidTest-Regular.ttf";
	public static final String COLLECTION  = "JoidTest.ttc";
	public static final String BOLD_ITALIC = "JoidTest-BoldItalic.otf";

	public static byte[] read(final String name) throws IOException {
		return Files.readAllBytes(MsdfFonts.copy(name).toPath());
	}

	public static File copy(final String name) throws IOException {
		final File file = File.createTempFile("joid-font-", name);
		file.deleteOnExit();
		try (InputStream stream = MsdfFonts.class.getResourceAsStream("/font/" + name)) {
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		return file;
	}

}