package be.zeldown.joid.lib.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import be.zeldown.joid.lib.asset.dto.locator.AssetLocator;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Asset {

	private static final int BUFFER = 8192;

	private final String uniqueId;

	protected Asset(final @NonNull String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public static @NonNull Asset of(final @NonNull Object handle) {
		return AssetLocator.locate(handle);
	}

	public abstract @NonNull InputStream open() throws IOException;

	public boolean isRemote() {
		return false;
	}

	public boolean isReopenable() {
		return true;
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
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (InputStream stream = this.open()) {
			final byte[] buffer = new byte[Asset.BUFFER];
			int count;
			while ((count = stream.read(buffer)) != -1) {
				output.write(buffer, 0, count);
			}
		}
		return output.toByteArray();
	}

	protected static int fill(final @NonNull InputStream stream, final @NonNull byte[] buffer) throws IOException {
		int total = 0;
		while (total < buffer.length) {
			final int count = stream.read(buffer, total, buffer.length - total);
			if (count == -1) {
				break;
			}
			total += count;
		}
		return total;
	}

	protected static @NonNull byte[] shrink(final @NonNull byte[] buffer, final int length) {
		if (length == buffer.length) {
			return buffer;
		}

		final byte[] shrunk = new byte[length];
		System.arraycopy(buffer, 0, shrunk, 0, length);
		return shrunk;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + this.uniqueId + "]";
	}

}