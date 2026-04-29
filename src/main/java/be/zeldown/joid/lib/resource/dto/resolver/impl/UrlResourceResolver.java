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

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof String;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final String url = (String) input;
		return builder.compute(url, () -> new ResourceData(url, null), resource -> new ResourceDownloadThread(url, inputStream -> {
			try {
				final InputStream supportedStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
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
		}).start());
	}

	private static class ResourceDownloadThread extends Thread {

		private final Consumer<InputStream> callback;
		private final String url;

		private ResourceDownloadThread(final String url, final Consumer<InputStream> callback) {
			super("ResourceDownloadThread/" + url);
			this.url = url;
			this.callback = callback;
		}

		@Override
		public void run() {
			try {
				final URL rul = new URL(this.url);
				final HttpURLConnection connection = (HttpURLConnection) rul.openConnection();
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

				final int status = connection.getResponseCode();
				final InputStream inputStream = connection.getInputStream();
				if (inputStream == null) {
					System.out.println("[DrawUtils] Unable to read image from " + this.url + " [status=" + status + "]");
				}

				this.callback.accept(inputStream);
			} catch (final Exception silent) {
				try {
					final URL rul = new URL(this.url.replace("https", "http"));
					final HttpURLConnection connection = (HttpURLConnection) rul.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

					final int status = connection.getResponseCode();
					final InputStream inputStream = connection.getInputStream();
					if (inputStream == null) {
						System.out.println("[DrawUtils] Unable to read image from " + this.url.replace("https", "http") + " [status=" + status + "]");
					}

					this.callback.accept(inputStream);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}