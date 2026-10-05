package dev.joid.lib.font.impl.msdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
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

	private static byte[] font() throws IOException {
		return Asset.of(MsdfFontCacheTest.class.getResourceAsStream("/font/JoidTest-Regular.ttf")).read();
	}

}