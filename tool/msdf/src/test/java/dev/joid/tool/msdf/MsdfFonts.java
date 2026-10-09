package dev.joid.tool.msdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFonts {

	public static final String CUBIC       = "JoidTest-Cubic.otf";
	public static final String REGULAR     = "JoidTest-Regular.ttf";
	public static final String QUADRATIC   = "JoidTest-Quadratic.ttf";
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

	public static byte[] build(final Map<String, int[]> tables) {
		int offset = 12 + tables.size() * 16;
		int size = offset;
		for (final int[] words : tables.values()) {
			size += words.length * 2;
		}

		final ByteBuffer buffer = ByteBuffer.allocate(size);
		buffer.putInt(0x00010000).putShort((short) tables.size()).position(12);
		for (final Map.Entry<String, int[]> table : tables.entrySet()) {
			buffer.put(table.getKey().getBytes(StandardCharsets.US_ASCII)).putInt(0).putInt(offset).putInt(table.getValue().length * 2);
			offset += table.getValue().length * 2;
		}

		for (final int[] words : tables.values()) {
			for (final int word : words) {
				buffer.putShort((short) word);
			}
		}
		return buffer.array();
	}

	public static byte[] build(final String tag, final int... words) {
		return MsdfFonts.build(Collections.singletonMap(tag, words));
	}

}