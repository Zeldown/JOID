package be.zeldown.joid.msdf.font;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public final class FontFile {

	private static final int TTC        = 0x74746366;
	private static final int ITALIC     = 1;
	private static final int OBLIQUE    = 1 << 9;
	private static final int MAC_ITALIC = 1 << 1;

	private final byte[] data;
	private final Map<String, Integer> tables = new HashMap<>();

	private FontFile(final byte[] data) {
		this.data = data;
		final int offset = this.integer(0) == FontFile.TTC ? this.integer(12) : 0;
		final int count = this.unsigned(offset + 4);
		for (int i = 0; i < count; i++) {
			final int record = offset + 12 + i * 16;
			this.tables.put(new String(data, record, 4, StandardCharsets.US_ASCII), this.integer(record + 8));
		}
	}

	public static FontFile read(final File file) throws IOException {
		return new FontFile(Files.readAllBytes(file.toPath()));
	}

	public int getWeight() {
		final Integer os2 = this.tables.get("OS/2");
		return os2 == null ? 400 : this.unsigned(os2 + 4);
	}

	public boolean isItalic() {
		final Integer os2 = this.tables.get("OS/2");
		final Integer head = this.tables.get("head");
		return os2 != null && (this.unsigned(os2 + 62) & (FontFile.ITALIC | FontFile.OBLIQUE)) != 0 || head != null && (this.unsigned(head + 44) & FontFile.MAC_ITALIC) != 0;
	}

	public int getUnitsPerEm() {
		final Integer head = this.tables.get("head");
		return head == null ? 1000 : this.unsigned(head + 18);
	}

	public int signed(final int offset) {
		return (short) this.unsigned(offset);
	}

	public int integer(final int offset) {
		return (this.data[offset] & 255) << 24 | (this.data[offset + 1] & 255) << 16 | (this.data[offset + 2] & 255) << 8 | this.data[offset + 3] & 255;
	}

	public int unsigned(final int offset) {
		return (this.data[offset] & 255) << 8 | this.data[offset + 1] & 255;
	}

}