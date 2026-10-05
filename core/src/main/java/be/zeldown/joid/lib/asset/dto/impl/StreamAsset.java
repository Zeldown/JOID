package be.zeldown.joid.lib.asset.dto.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import be.zeldown.joid.lib.asset.Asset;
import lombok.NonNull;

public final class StreamAsset extends Asset {

	private final BufferedInputStream stream;

	private StreamAsset(final InputStream stream) {
		super(stream.toString());
		this.stream = stream instanceof BufferedInputStream ? (BufferedInputStream) stream : new BufferedInputStream(stream);
	}

	public static @NonNull StreamAsset create(final @NonNull InputStream stream) {
		return new StreamAsset(stream);
	}

	@Override
	public boolean isReopenable() {
		return false;
	}

	@Override
	public @NonNull InputStream open() {
		return this.stream;
	}

	@Override
	public @NonNull byte[] peek(final int length) {
		final byte[] header = new byte[length];
		try {
			this.stream.mark(length);
			final int read = Asset.fill(this.stream, header);
			this.stream.reset();
			return Asset.shrink(header, read);
		} catch (final IOException exception) {
			return new byte[0];
		}
	}

}