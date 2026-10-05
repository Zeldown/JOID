package be.zeldown.joid.lib.font.impl.custom;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontInfo;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.dto.font.MsdfFile;
import be.zeldown.joid.lib.utils.thread.ThreadUtils;
import lombok.NonNull;

public final class CustomFontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5, ThreadUtils.daemonFactory("CustomFontLoader"));

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream fontInputStream) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.loadFont(fontInputStream, fontInputStream, (regular, bold) -> {
			future.complete(new CustomFont(regular, bold));
		});
		return future;
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream regularInputStream, final @NonNull FontInputStream boldInputStream) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.loadFont(regularInputStream, boldInputStream, (regular, bold) -> {
			future.complete(new CustomFont(regular, bold));
		});
		return future;
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull InputStream packed) {
		return CustomFontLoader.load(packed, null);
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull InputStream regular, final InputStream bold) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.EXECUTOR.submit(() -> {
			try {
				final Font regularFont = CustomFontLoader.read(regular);
				future.complete(new CustomFont(regularFont, bold == null ? regularFont : CustomFontLoader.read(bold)));
			} catch (final Throwable throwable) {
				future.completeExceptionally(throwable);
			}
		});
		return future;
	}

	private static @NonNull Font read(final InputStream stream) {
		final MsdfFile file = MsdfFile.read(stream);
		final Font font = new Font(file.getFontInfo(), file.getImage());
		font.getTexture();
		return font;
	}

	private static void loadFont(final @NonNull FontInputStream regularInputStream, final @NonNull FontInputStream boldInputStream, final @NonNull BiConsumer<@NonNull Font, @NonNull Font> callback) {
		CustomFontLoader.EXECUTOR.submit(() -> {
			final FontInfo regularFontInfo = FontInfo.fromJson(CustomFontLoader.GSON.fromJson(new InputStreamReader(regularInputStream.getData(), StandardCharsets.UTF_8), JsonObject.class));
			final Font regular = new Font(regularFontInfo, regularInputStream.getTexture());
			regular.getTexture();

			if (regularInputStream == boldInputStream) {
				callback.accept(regular, regular);
				return;
			}

			final FontInfo boldFontInfo = FontInfo.fromJson(CustomFontLoader.GSON.fromJson(new InputStreamReader(boldInputStream.getData(), StandardCharsets.UTF_8), JsonObject.class));
			final Font bold = new Font(boldFontInfo, boldInputStream.getTexture());
			bold.getTexture();
			callback.accept(regular, bold);
		});
	}

}