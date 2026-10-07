package dev.joid.lib.asset.dto.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileAssetTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void readsItsFile() throws IOException {
		final File file = this.folder.newFile("asset.bin");
		Files.write(file.toPath(), "joid".getBytes(StandardCharsets.UTF_8));

		final FileAsset asset = FileAsset.create(file);
		Assert.assertSame(file, asset.getFile());
		Assert.assertArrayEquals("joid".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void followsTheChangesOfItsFile() throws IOException {
		final File file = this.folder.newFile("asset.bin");
		final FileAsset asset = FileAsset.create(file);
		Files.write(file.toPath(), "first".getBytes(StandardCharsets.UTF_8));
		Assert.assertArrayEquals("first".getBytes(StandardCharsets.UTF_8), asset.read());
		Files.write(file.toPath(), "second".getBytes(StandardCharsets.UTF_8));
		Assert.assertArrayEquals("second".getBytes(StandardCharsets.UTF_8), asset.read());
	}

	@Test
	public void isIdentifiedByItsAbsolutePath() {
		Assert.assertEquals(new File("image.png").getAbsolutePath(), FileAsset.create(new File("image.png")).getUniqueId());
	}

	@Test
	public void isLocal() {
		Assert.assertFalse(FileAsset.create(new File("image.png")).isRemote());
	}

	@Test(expected = FileNotFoundException.class)
	public void failsToReadAMissingFile() throws IOException {
		FileAsset.create(new File(this.folder.getRoot(), "missing.bin")).read();
	}

	@Test(expected = NullPointerException.class)
	public void refusesANullFile() {
		FileAsset.create(null);
	}

}