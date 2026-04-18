package be.zeldown.joid.lib.resource;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.ResourceProperties;
import be.zeldown.joid.lib.resource.dto.decoder.ResourceDecoder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ResourceBuilder {

	private static final List<ResourceBuilder> BUILDER_LIST = new ArrayList<>();

	public static final Cache<String, ResourceData> DEFAULT_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

	private Cache<String, ResourceData> cache;
	private ResourceProperties properties;

	private ResourceBuilder() {
		this.cache = ResourceBuilder.DEFAULT_CACHE;
		this.properties = new ResourceProperties();

		ResourceBuilder.BUILDER_LIST.add(this);
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
	public @NonNull Resource of(final @NonNull InputStream stream) {
		final String uniqueId = stream.toString();
		try {
			final InputStream supportedStream = stream.markSupported() ? stream : new BufferedInputStream(stream);
			supportedStream.mark(6);

			final byte[] header = new byte[6];
			final int read = supportedStream.read(header);
			supportedStream.reset();

			if (read >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a') {
				return this.cache(uniqueId, () -> new Resource(this, new ResourceData(uniqueId, ResourceDecoder.gif(supportedStream))));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return this.cache(uniqueId, () -> new Resource(this, new ResourceData(uniqueId, ResourceDecoder.image(stream))));
	}

	public @NonNull Resource of(final @NonNull BufferedImage image) {
		final String uniqueId = image.toString();
		return this.cache(uniqueId, () -> new Resource(this, new ResourceData(uniqueId, ResourceDecoder.image(image))));
	}

	public @NonNull Resource of(final @NonNull String url) {
		return this.of(url, null);
	}

	public @NonNull Resource of(final @NonNull String url, final Consumer<Resource> callback) {
		final String uniqueId = url;
		return this.cache(uniqueId, () -> {
			final Resource resource = new Resource(this, new ResourceData(uniqueId, null));
			new ResourceDownloadThread(url, inputStream -> {
				resource.decoder(url.endsWith(".gif") ? ResourceDecoder.gif(inputStream) : ResourceDecoder.image(inputStream));
				if (callback != null) {
					callback.accept(resource);
				}
			}).start();
			return resource;
		});
	}

	public @NonNull Resource of(final int id) {
		final String uniqueId = "texture_" + String.valueOf(id);
		return this.cache(uniqueId, () -> new Resource(this, new ResourceData(uniqueId, null).textureId(id)));
	}

	/* [ Cache Section ] */
	private final @NonNull Resource cache(final @NonNull String uniqueId, final @NonNull Supplier<Resource> callable) {
		if (this.cache == null) {
			return callable.get();
		}

		final ResourceData data = this.cache.getIfPresent(uniqueId);
		if (data != null) {
			return new Resource(this, data);
		}

		final Resource resource = callable.get();
		this.cache.put(uniqueId, resource.getResourceData());
		return resource;
	}

	public final void reload() {
		if (this.cache == null) {
			return;
		}

		this.cache.invalidateAll();
	}

	/* [ Static Section ] */
	public static @NonNull List<@NonNull ResourceBuilder> getBuilders() {
		return ResourceBuilder.BUILDER_LIST;
	}

	private class ResourceDownloadThread extends Thread {

		@NonNull private final Consumer<InputStream> callback;
		@NonNull private final String url;

		private ResourceDownloadThread(final @NonNull String url, final @NonNull Consumer<@NonNull InputStream> callback) {
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