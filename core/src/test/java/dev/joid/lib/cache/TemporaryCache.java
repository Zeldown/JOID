package dev.joid.lib.cache;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.junit.rules.ExternalResource;

import dev.joid.lib.JOIDEnv;

public final class TemporaryCache extends ExternalResource {

	private static final File FOLDER = TemporaryCache.createFolder();

	private File previous;

	@Override
	protected void before() {
		this.previous = JOIDEnv.getCacheDir();
		JOIDEnv.cacheDir(TemporaryCache.FOLDER);
	}

	@Override
	protected void after() {
		JOIDEnv.cacheDir(this.previous);
	}

	private static File createFolder() {
		try {
			final File folder = Files.createTempDirectory("joid-cache-").toFile();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> TemporaryCache.delete(folder)));
			return folder;
		} catch (final IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	private static void delete(final File file) {
		final File[] children = file.listFiles();
		if (children != null) {
			for (final File child : children) {
				TemporaryCache.delete(child);
			}
		}
		file.delete();
	}

}