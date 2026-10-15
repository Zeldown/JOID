package dev.joid.lib.asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import dev.joid.lib.asset.locator.AssetLocator;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Asset {

	private final String uniqueId;

	protected Asset(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public static @NonNull Asset of(final @NonNull Object handle) {
		return AssetLocator.locate(handle);
	}

	public abstract @NonNull InputStream open() throws IOException;

	public File getFile() {
		return null;
	}

	public boolean isRemote() {
		return false;
	}

	public @NonNull byte[] peek(final int length) {
		final byte[] header = new byte[length];
		try (InputStream stream = this.open()) {
			return Asset.shrink(header, Asset.fill(stream, header));
		} catch (final IOException exception) {
			return new byte[0];
		}
	}

	public final @NonNull byte[] read() throws IOException {
		try (InputStream stream = this.open()) {
			return IOUtils.toByteArray(stream);
		}
	}

	protected static int fill(final @NonNull InputStream stream, final @NonNull byte[] buffer) throws IOException {
		return IOUtils.read(stream, buffer);
	}

	protected static @NonNull byte[] shrink(final @NonNull byte[] buffer, final int length) {
		return length == buffer.length ? buffer : Arrays.copyOf(buffer, length);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.uniqueId + "]";
	}

}