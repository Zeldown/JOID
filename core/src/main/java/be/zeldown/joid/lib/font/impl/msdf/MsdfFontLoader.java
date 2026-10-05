package be.zeldown.joid.lib.font.impl.msdf;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;
import be.zeldown.joid.lib.utils.thread.ThreadUtils;
import lombok.NonNull;

public final class MsdfFontLoader {

	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, ThreadUtils.daemonFactory("MsdfFontLoader"));

	public static @NonNull CompletableFuture<MsdfFont> load(final @NonNull Object regular) {
		return MsdfFontLoader.load(regular, null);
	}

	public static @NonNull CompletableFuture<MsdfFont> load(final @NonNull Object regular, final Object bold) {
		final IMsdfSource regularSource = MsdfFontLoader.source(regular);
		final IMsdfSource boldSource = bold == null || bold == regular ? null : MsdfFontLoader.source(bold);

		final CompletableFuture<MsdfFont> future = new CompletableFuture<>();
		MsdfFontLoader.EXECUTOR.submit(() -> {
			try {
				final MsdfFace regularFace = regularSource.read();
				future.complete(new MsdfFont(regularFace, boldSource == null ? regularFace : boldSource.read()));
			} catch (final Throwable throwable) {
				future.completeExceptionally(throwable);
			}
		});
		return future;
	}

	private static @NonNull IMsdfSource source(final @NonNull Object handle) {
		return handle instanceof IMsdfSource ? (IMsdfSource) handle : MsdfBinarySource.of(handle);
	}

}