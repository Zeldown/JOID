package dev.joid.lib.asset.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.asset.Asset;
import lombok.NonNull;

public final class StreamAsset extends Asset {

	private static final AtomicLong COUNTER = new AtomicLong();

	private final InputStream stream;

	private byte[] bytes;

	private StreamAsset(final InputStream stream) {
		super("stream_" + StreamAsset.COUNTER.incrementAndGet());
		this.stream = stream;
	}

	public static @NonNull StreamAsset create(final @NonNull InputStream stream) {
		return new StreamAsset(stream);
	}

	@Override
	public synchronized @NonNull InputStream open() throws IOException {
		if (this.bytes == null) {
			try {
				this.bytes = IOUtils.toByteArray(this.stream);
			} finally {
				try {
					this.stream.close();
				} catch (final IOException exception) {
					JOIDLog.warn("The stream " + super.getUniqueId() + " cannot be closed (" + exception.getMessage() + "), its content is read anyway: check the stream");
				}
			}
		}
		return new ByteArrayInputStream(this.bytes);
	}

}