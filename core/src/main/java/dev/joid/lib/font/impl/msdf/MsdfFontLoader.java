package dev.joid.lib.font.impl.msdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;
import dev.joid.lib.font.impl.msdf.dto.source.MsdfOpenTypeSource;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfFontLoader {

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, ThreadUtils.daemonFactory("MsdfFontLoader"));

	public static @NonNull CompletableFuture<MsdfFont> load(final @NonNull Object @NonNull... faces) {
		if (faces.length == 0) {
			final CompletableFuture<MsdfFont> future = new CompletableFuture<>();
			future.completeExceptionally(new IllegalArgumentException("A msdf font needs at least one face"));
			return future;
		}

		final List<CompletableFuture<MsdfFontFace>> futures = new ArrayList<>(faces.length);
		for (final Object handle : faces) {
			futures.add(MsdfFontLoader.read(handle));
		}

		return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).thenApply(done -> {
			final MsdfFontFace[] loaded = new MsdfFontFace[futures.size()];
			for (int i = 0; i < loaded.length; i++) {
				loaded[i] = futures.get(i).join();
			}
			return MsdfFont.create(loaded);
		});
	}

	private static @NonNull CompletableFuture<MsdfFontFace> read(final @NonNull Object handle) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				final long start = System.nanoTime();
				final IMsdfSource source = MsdfFontLoader.source(handle);
				final MsdfFontFace face = source.read();
				if (JOID.inst().isDevMode()) {
					System.out.println("[JOID] Font " + face.getName() + " " + source.describe() + " in " + String.format("%.2f", (System.nanoTime() - start) / 1000000F) + "ms");
				}
				return face;
			} catch (final IOException exception) {
				throw new CompletionException(exception);
			}
		}, MsdfFontLoader.EXECUTOR);
	}

	private static @NonNull IMsdfSource source(final @NonNull Object handle) {
		if (handle instanceof IMsdfSource) {
			return (IMsdfSource) handle;
		}

		final Asset asset = Asset.of(handle);
		return MsdfOpenTypeSource.supports(asset.peek(MsdfOpenTypeSource.HEADER)) ? MsdfOpenTypeSource.of(asset) : MsdfBinarySource.of(asset);
	}

}