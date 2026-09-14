package be.zeldown.joid.lib.resource.dto.resolver.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.ResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.resource.dto.resolver.IResourceResolver;
import lombok.NonNull;

public class UrlResourceResolver implements IResourceResolver {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof String;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final String url = (String) input;
		return builder.compute(url, () -> new ResourceData(url, null), resource -> resource.dispatch(() -> {
			final InputStream stream = UrlResourceResolver.openStream(url);
			if (stream == null) {
				return;
			}

			try {
				final InputStream supportedStream = stream.markSupported() ? stream : new BufferedInputStream(stream);
				supportedStream.mark(12);

				final byte[] header = new byte[12];
				final int read = supportedStream.read(header);
				supportedStream.reset();

				if (VideoResourceDecoder.isVideoHeader(header, read)) {
					resource.decoder(ResourceDecoder.video(supportedStream, VideoResourceDecoder.isLoopByDefault(header, read)));
				} else {
					resource.decoder(ResourceDecoder.image(supportedStream));
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

			if (callback != null) {
				callback.accept(resource);
			}
		}));
	}

	private static InputStream openStream(final @NonNull String url) {
		try {
			return UrlResourceResolver.openConnection(url);
		} catch (final Exception silent) {
			try {
				return UrlResourceResolver.openConnection(url.replace("https", "http"));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static InputStream openConnection(final @NonNull String url) throws Exception {
		final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", UrlResourceResolver.USER_AGENT);

		final int status = connection.getResponseCode();
		final InputStream inputStream = connection.getInputStream();
		if (inputStream == null) {
			System.out.println("[DrawUtils] Unable to read image from " + url + " [status=" + status + "]");
		}
		return inputStream;
	}

}