package dev.joid.lib.signal.replay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.demo.replay.ReplayHiddenFixture;

public class SignalReplayClassTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void readsTheBytecodeFromTheFolderOfTheLoadedClass() throws Exception {
		final File file = this.copy();
		try (URLClassLoader loader = new URLClassLoader(new URL[] {this.folder.getRoot().toURI().toURL()}, null)) {
			final Class<?> type = Class.forName(ReplayHiddenFixture.class.getName(), false, loader);
			final SignalReplayClass replayClass = SignalReplayClass.read(ReplayHiddenFixture.class.getName(), type, SignalReplayClassTest.class.getClassLoader());
			Assert.assertEquals(file.getCanonicalFile(), replayClass.getFile().getCanonicalFile());
			Assert.assertEquals("dev/joid/demo/replay/ReplayHiddenFixture", replayClass.getNode().name);
		}
	}

	@Test
	public void becomesStaleOnceItsClassFileChanges() throws Exception {
		final File file = this.copy();
		try (URLClassLoader loader = new URLClassLoader(new URL[] {this.folder.getRoot().toURI().toURL()}, null)) {
			final Class<?> type = Class.forName(ReplayHiddenFixture.class.getName(), false, loader);
			final SignalReplayClass replayClass = SignalReplayClass.read(ReplayHiddenFixture.class.getName(), type, SignalReplayClassTest.class.getClassLoader());
			Assert.assertFalse(replayClass.isStale());
			Assert.assertTrue(file.setLastModified(file.lastModified() + 10000L));
			Assert.assertTrue(replayClass.isStale());
		}
	}

	@Test
	public void readsTheBytecodeThroughTheLoadersWithoutClass() {
		final SignalReplayClass replayClass = SignalReplayClass.read(ReplayHiddenFixture.class.getName(), null, SignalReplayClassTest.class.getClassLoader());
		Assert.assertNull(replayClass.getFile());
		Assert.assertFalse(replayClass.isStale());
		Assert.assertEquals("dev/joid/demo/replay/ReplayHiddenFixture", replayClass.getNode().name);
	}

	private File copy() throws IOException {
		final File file = new File(this.folder.getRoot(), "dev/joid/demo/replay/ReplayHiddenFixture.class");
		Assert.assertTrue(file.getParentFile().mkdirs());
		try (InputStream input = ReplayHiddenFixture.class.getResourceAsStream("ReplayHiddenFixture.class")) {
			Files.copy(input, file.toPath());
		}
		return file;
	}

}