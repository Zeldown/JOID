package dev.joid.backend.lwjgl2;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.internal.JOIDEnv;

public class Lwjgl2NativesTest {

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void extractsTheNativesAgainAfterAFailedExtraction() throws Exception {
		final Field installed = Lwjgl2Natives.class.getDeclaredField("installed");
		installed.setAccessible(true);
		final boolean wasInstalled = installed.getBoolean(null);
		final String libraryPath = System.getProperty("org.lwjgl.librarypath");
		final File cacheDir = JOIDEnv.getCacheDir();
		installed.setBoolean(null, false);
		System.clearProperty("org.lwjgl.librarypath");
		try {
			JOIDEnv.cacheDir(this.folder.newFile());
			Assert.assertThrows(IllegalStateException.class, Lwjgl2Natives::install);

			JOIDEnv.cacheDir(this.folder.newFolder());
			Lwjgl2Natives.install();
			Assert.assertNotNull(System.getProperty("org.lwjgl.librarypath"));
		} finally {
			JOIDEnv.cacheDir(cacheDir);
			installed.setBoolean(null, wasInstalled);
			if (libraryPath == null) {
				System.clearProperty("org.lwjgl.librarypath");
			} else {
				System.setProperty("org.lwjgl.librarypath", libraryPath);
			}
		}
	}

}