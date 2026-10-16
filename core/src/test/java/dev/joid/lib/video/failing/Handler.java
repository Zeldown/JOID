package dev.joid.lib.video.failing;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(final URL url) {
		throw new LinkageError("The natives of " + url + " cannot be linked");
	}

}