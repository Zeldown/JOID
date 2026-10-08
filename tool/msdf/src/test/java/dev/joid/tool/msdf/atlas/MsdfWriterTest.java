package dev.joid.tool.msdf.atlas;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.zip.InflaterInputStream;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.tool.msdf.MsdfFonts;
import dev.joid.tool.msdf.font.FontFile;
import dev.joid.tool.msdf.font.Glyphs;
import dev.joid.tool.msdf.font.Kerning;

public class MsdfWriterTest {

	@Test
	public void writesTheHeaderOfTheCurrentFormat() throws Exception {
		final byte[] data = MsdfFonts.read(MsdfFonts.REGULAR);
		final Kerning kerning = Kerning.read(FontFile.read(data), Glyphs.load(data), new int[] {'A'});
		final File file = File.createTempFile("joid-msdf-", ".msdf");
		file.deleteOnExit();
		MsdfWriter.write(file, Collections.emptyList(), kerning, new double[] {1D, 0.8D, -0.2D, -0.1D, 0.05D}, "JOID Test Regular", 400, true, 32D, 4D, new int[4], 2, 2);

		try (InputStream stream = new FileInputStream(file)) {
			final byte[] magic = new byte[8];
			new DataInputStream(stream).readFully(magic);
			Assert.assertEquals("JOIDMSDF", new String(magic, StandardCharsets.US_ASCII));

			final DataInputStream input = new DataInputStream(new InflaterInputStream(stream));
			Assert.assertEquals(MsdfWriter.VERSION, input.readUnsignedByte());
			Assert.assertEquals(400, input.readUnsignedShort());
			Assert.assertTrue(input.readBoolean());
			Assert.assertEquals("JOID Test Regular", input.readUTF());
			Assert.assertEquals(2, input.readInt());
			Assert.assertEquals(2, input.readInt());
			Assert.assertEquals(4F, input.readFloat(), 0F);
			Assert.assertEquals(32F, input.readFloat(), 0F);
		}
	}

}