package be.zeldown.joid.lib.font.impl.msdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;
import be.zeldown.joid.lib.utils.thread.ThreadUtils;
import lombok.NonNull;

public final class MsdfFontLoader {

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, ThreadUtils.daemonFactory("MsdfFontLoader"));

	public static @NonNull CompletableFuture<MsdfFont> load(final @NonNull Object @NonNull... faces) {
		if (faces.length == 0) {
			final CompletableFuture<MsdfFont> future = new CompletableFuture<>();
			future.completeExceptionally(new IllegalArgumentException("A msdf font needs at least one face"));
			return future;
		}

		final List<CompletableFuture<MsdfFace>> futures = new ArrayList<>(faces.length);
		for (final Object handle : faces) {
			futures.add(MsdfFontLoader.read(handle));
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).thenApply(done -> {
			final MsdfFace[] loaded = new MsdfFace[futures.size()];
			for (int i = 0; i < loaded.length; i++) {
				loaded[i] = futures.get(i).join();
			}
			return MsdfFont.create(loaded);
		});
	}

	private static @NonNull CompletableFuture<MsdfFace> read(final @NonNull Object handle) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return MsdfFontLoader.source(handle).read();
			} catch (final IOException exception) {
				throw new CompletionException(exception);
			}
		}, MsdfFontLoader.EXECUTOR);
	}

	private static @NonNull IMsdfSource source(final @NonNull Object handle) {
		return handle instanceof IMsdfSource ? (IMsdfSource) handle : MsdfBinarySource.of(handle);
	}

}