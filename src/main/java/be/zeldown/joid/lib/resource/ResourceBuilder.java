package be.zeldown.joid.lib.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import com.google.common.cache.Cache;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceBuilder {

	private Cache<String, ResourceData> cache;
	private ResourceProperties properties;

	private ResourceBuilder() {
		this.cache = null;
		this.properties = new ResourceProperties();
	}

	public static @NonNull ResourceBuilder create() {
		return new ResourceBuilder();
	}

	/* [ Query Section ] */
	public final @NonNull ResourceBuilder cache(final Cache<String, ResourceData> cache) {
		this.cache = cache;
		return this;
	}

	public final @NonNull ResourceBuilder async() {
		this.properties.async();
		return this;
	}

	public final @NonNull ResourceBuilder blocking() {
		this.properties.blocking();
		return this;
	}

	public final @NonNull ResourceBuilder interpolation(final int interpolation) {
		this.properties.interpolation(interpolation);
		return this;
	}

	public final @NonNull ResourceBuilder linear() {
		this.properties.linear();
		return this;
	}

	public final @NonNull ResourceBuilder nearest() {
		this.properties.nearest();
		return this;
	}

	public final @NonNull ResourceBuilder textureCoords(final double u, final double v, final double u2, final double v2) {
		this.properties.textureCoords(u, v, u2, v2);
		return this;
	}

	public final @NonNull ResourceBuilder copy() {
		final ResourceBuilder copy = new ResourceBuilder();
		copy.cache = this.cache;
		copy.properties = this.properties.copy();
		return copy;
	}

	/* [ Resource Section ] */
	public @NonNull Resource of(final @NonNull InputStream stream) throws IOException {
		final String uniqueId = stream.toString();
		if (this.cache != null) {
			final ResourceData cached = this.cache.getIfPresent(uniqueId);
			if (cached != null) {
				return new Resource(this, cached);
			}
		}

		final ResourceData data = new ResourceData(uniqueId, ImageIO.read(stream));
		final Resource resource = new Resource(this, data);
		if (this.cache != null) {
			this.cache.put(uniqueId, data);
		}

		return resource;
	}

	public @NonNull Resource of(final @NonNull BufferedImage image) {
		final String uniqueId = image.toString();
		if (this.cache != null) {
			final ResourceData cached = this.cache.getIfPresent(uniqueId);
			if (cached != null) {
				return new Resource(this, cached);
			}
		}

		final ResourceData data = new ResourceData(uniqueId, image);
		final Resource resource = new Resource(this, data);
		if (this.cache != null) {
			this.cache.put(uniqueId, data);
		}

		return resource;
	}

	public @NonNull Resource of(final @NonNull String url) {
		final String uniqueId = url;
		if (this.cache != null) {
			final ResourceData cached = this.cache.getIfPresent(uniqueId);
			if (cached != null) {
				return new Resource(this, cached);
			}
		}

		final ResourceData data = new ResourceData(uniqueId, null);
		final Resource resource = new Resource(this, data);
		if (this.cache != null) {
			this.cache.put(uniqueId, data);
		}

		new DownloadBufferedImageThread(url, image -> {
			resource.image(image);
		}).start();

		return resource;
	}

	private class DownloadBufferedImageThread extends Thread {

		private final @NonNull Consumer<BufferedImage> callback;
		private final @NonNull String url;

		public DownloadBufferedImageThread(final @NonNull String url, final @NonNull Consumer<@NonNull BufferedImage> callback) {
			super("DownloadBufferedImageThread/" + url);

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
				final BufferedImage image = ImageIO.read(inputStream);

				if (image == null) {
					System.out.println("Unable to read image from " + this.url + " [status=" + status + "]");
				}

				this.callback.accept(image);
			} catch (final Exception silent) {
				try {
					final URL rul = new URL(this.url.replace("https", "http"));
					final HttpURLConnection connection = (HttpURLConnection) rul.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");

					final int status = connection.getResponseCode();
					final InputStream inputStream = connection.getInputStream();
					final BufferedImage image = ImageIO.read(inputStream);

					if (image == null) {
						System.out.println("Unable to read image from " + this.url.replace("https", "http") + " [status=" + status + "]");
					}

					this.callback.accept(image);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

}