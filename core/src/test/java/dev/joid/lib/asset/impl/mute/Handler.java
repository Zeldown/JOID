package dev.joid.lib.asset.impl.mute;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(final URL url) {
		return new URLConnection(url) {

			@Override
			public void connect() {}

			@Override
			public InputStream getInputStream() throws SocketTimeoutException {
				throw new SocketTimeoutException("Connect timeout " + super.getConnectTimeout() + " ms, read timeout " + super.getReadTimeout() + " ms");
			}

		};
	}

}