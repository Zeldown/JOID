package be.zeldown.joid.test.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.ResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.resource.dto.resolver.IResourceResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SnapshotUrlResolver implements IResourceResolver {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";

	private final File directory;

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof String;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final String url = (String) input;
		return builder.compute(url, () -> new ResourceData(url, null), resource -> resource.dispatch(() -> {
			final byte[] bytes = this.read(url);
			final int length = Math.min(12, bytes.length);
			if (VideoResourceDecoder.isVideoHeader(bytes, length)) {
				resource.decoder(ResourceDecoder.video(new ByteArrayInputStream(bytes), VideoResourceDecoder.isLoopByDefault(bytes, length)));
			} else {
				resource.decoder(ResourceDecoder.image(new ByteArrayInputStream(bytes)));
			}

			if (callback != null) {
				callback.accept(resource);
			}
		}));
	}

	private byte[] read(final String url) {
		try {
			final File file = new File(this.directory, SnapshotUrlResolver.hash(url));
			if (file.exists()) {
				return Files.readAllBytes(file.toPath());
			}

			final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("User-Agent", SnapshotUrlResolver.USER_AGENT);

			final ByteArrayOutputStream output = new ByteArrayOutputStream();
			try (InputStream stream = connection.getInputStream()) {
				final byte[] buffer = new byte[8192];
				for (int read = stream.read(buffer); read != -1; read = stream.read(buffer)) {
					output.write(buffer, 0, read);
				}
			}

			this.directory.mkdirs();
			Files.write(file.toPath(), output.toByteArray());
			return output.toByteArray();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String hash(final String url) {
		try {
			return String.format("%040x", new BigInteger(1, MessageDigest.getInstance("SHA-1").digest(url.getBytes(StandardCharsets.UTF_8))));
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

}