package dev.joid.lib.font.impl.msdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfOpenTypeSource;

public class MsdfFontCacheTest {

	private File previous;
	private File directory;

	@Before
	public void useATemporaryCache() throws IOException {
		this.previous = MsdfFontCache.getDirectory();
		this.directory = Files.createTempDirectory("joid-msdf-").toFile();
		MsdfFontCache.directory(this.directory);
	}

	@After
	public void restoreTheCache() throws IOException {
		MsdfFontCache.directory(this.previous);
		for (final File file : this.directory.listFiles()) {
			Files.delete(file.toPath());
		}
		Files.delete(this.directory.toPath());
	}

	@Test(timeout = 60000L)
	public void generatesAFontOnlyOnce() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final File file = MsdfFontCache.resolve(font);
		final long modified = file.lastModified();
		Assert.assertEquals(this.directory, file.getParentFile());
		Assert.assertEquals(file, MsdfFontCache.resolve(font));
		Assert.assertEquals(modified, file.lastModified());
		Assert.assertArrayEquals(new String[] {file.getName()}, this.directory.list());
	}

	@Test(timeout = 60000L)
	public void keysTheCacheByTheFontContent() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		Assert.assertNotEquals(MsdfFontCache.resolve(font), MsdfFontCache.resolve(Arrays.copyOf(font, font.length + 4)));
	}

	@Test(timeout = 60000L)
	public void loadsAnOpenTypeFontThroughTheCache() {
		final MsdfFontFace face = MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join().getFace(FontWeight.REGULAR, false);
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertEquals("JOID Test Regular", face.getName());
		Assert.assertTrue(face.getKerning('A', 'V') < 0F);
		Assert.assertEquals(1, this.directory.list().length);
	}

	@Test(timeout = 60000L)
	public void restylesAnOpenTypeSource() {
		final MsdfFont font = MsdfFontLoader.load(MsdfOpenTypeSource.of(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).weight(FontWeight.BOLD)).join();
		Assert.assertSame(FontWeight.BOLD, font.getFace(FontWeight.BOLD, false).getWeight());
	}

	@Test(timeout = 60000L)
	public void logsHowEachFaceIsLoadedInDevMode() throws IOException {
		final byte[] font = MsdfFontCacheTest.font();
		final String[] lines = MsdfFontCacheTest.capture(() -> {
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(new ByteArrayInputStream(font)).join();
			MsdfFontLoader.load(MsdfBinarySource.of(MsdfFontCache.locate(font))).join();
		}).split(System.lineSeparator());
		Assert.assertTrue(lines[0], lines[0].startsWith("[JOID] Font JOID Test Regular generated into the msdf cache (" + MsdfFontCache.locate(font) + ") in "));
		Assert.assertTrue(lines[1], lines[1].startsWith("[JOID] Font JOID Test Regular read from the msdf cache (" + MsdfFontCache.locate(font) + ") in "));
		Assert.assertTrue(lines[2], lines[2].startsWith("[JOID] Font JOID Test Regular read from a .msdf file in "));
		Assert.assertTrue(lines[2], lines[2].endsWith("ms"));
	}

	@Test(timeout = 60000L)
	public void logsNothingOutsideDevMode() {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final PrintStream previous = System.out;
		try {
			System.setOut(new PrintStream(output, true));
			MsdfFontLoader.load(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).join();
		} finally {
			System.setOut(previous);
		}
		Assert.assertEquals(0, output.size());
	}

	private static String capture(final Runnable runnable) {
		final PrintStream previous = System.out;
		final boolean devMode = JOID.inst().isDevMode();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			JOID.inst().setDevMode(true);
			System.setOut(new PrintStream(output, true));
			runnable.run();
		} finally {
			System.setOut(previous);
			JOID.inst().setDevMode(devMode);
		}
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	private static byte[] font() throws IOException {
		return Asset.of(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).read();
	}

}