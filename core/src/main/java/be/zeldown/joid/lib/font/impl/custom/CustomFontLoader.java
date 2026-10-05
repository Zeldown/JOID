package be.zeldown.joid.lib.font.impl.custom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontInfo;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.dto.font.MsdfFile;
import be.zeldown.joid.lib.utils.thread.ThreadUtils;
import lombok.NonNull;

public final class CustomFontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, ThreadUtils.daemonFactory("CustomFontLoader"));

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull Object packed) {
		return CustomFontLoader.load(packed, null);
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream regular) {
		return CustomFontLoader.load(regular, null);
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull Object regular, final Object bold) {
		final Asset regularAsset = Asset.of(regular);
		final Asset boldAsset = bold == null ? null : Asset.of(bold);

		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.EXECUTOR.submit(() -> {
			try {
				final Font regularFont = CustomFontLoader.read(regularAsset);
				future.complete(new CustomFont(regularFont, boldAsset == null || boldAsset == regularAsset ? regularFont : CustomFontLoader.read(boldAsset)));
			} catch (final Throwable throwable) {
				future.completeExceptionally(throwable);
			}
		});
		return future;
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream regular, final FontInputStream bold) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.EXECUTOR.submit(() -> {
			try {
				final Font regularFont = CustomFontLoader.read(regular);
				future.complete(new CustomFont(regularFont, bold == null || bold == regular ? regularFont : CustomFontLoader.read(bold)));
			} catch (final Throwable throwable) {
				future.completeExceptionally(throwable);
			}
		});
		return future;
	}

	private static @NonNull Font read(final @NonNull Asset asset) throws IOException {
		try (InputStream stream = asset.open()) {
			final MsdfFile file = MsdfFile.read(stream);
			final Font font = new Font(file.getFontInfo(), file.getImage());
			font.getTexture();
			return font;
		}
	}

	private static @NonNull Font read(final @NonNull FontInputStream stream) {
		final FontInfo info = FontInfo.fromJson(CustomFontLoader.GSON.fromJson(new InputStreamReader(stream.getData(), StandardCharsets.UTF_8), JsonObject.class));
		final Font font = new Font(info, stream.getTexture());
		font.getTexture();
		return font;
	}

}