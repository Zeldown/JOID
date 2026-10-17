package dev.joid.lib.asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import dev.joid.lib.asset.locator.AssetLocator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Asset {

	@NonNull
	private final String uniqueId;

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
			return Asset.shrink(header, IOUtils.read(stream, header));
		} catch (final IOException exception) {
			throw new UncheckedIOException("The start of " + this.uniqueId + " cannot be read", exception);
		}
	}

	public final @NonNull byte[] read() throws IOException {
		try (InputStream stream = this.open()) {
			return IOUtils.toByteArray(stream);
		}
	}

	private static @NonNull byte[] shrink(final byte[] buffer, final int length) {
		return length == buffer.length ? buffer : Arrays.copyOf(buffer, length);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.uniqueId + "]";
	}

}