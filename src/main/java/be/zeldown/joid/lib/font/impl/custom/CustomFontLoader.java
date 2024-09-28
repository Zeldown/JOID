package be.zeldown.joid.lib.font.impl.custom;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontInfo;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.dto.font.impl.CustomFont;
import lombok.NonNull;

public final class CustomFontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

	public static @NonNull CompletableFuture<@NonNull CustomFont> load(final @NonNull FontInputStream regular) {
		return CustomFontLoader.load(regular, null);
	}

	public static @NonNull CompletableFuture<@NonNull CustomFont> load(final @NonNull FontInputStream regular, final FontInputStream bold) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();

		CustomFontLoader.loadFont(regular, regularFont -> {
			if (bold == null) {
				future.complete(new CustomFont(regularFont, regularFont));
				return;
			}

			CustomFontLoader.loadFont(bold, boldFont -> {
				future.complete(new CustomFont(regularFont, boldFont));
			});
		});

		return future;
	}

	private static void loadFont(final @NonNull FontInputStream fontInputStream, final @NonNull Consumer<@NonNull Font> callback) {
		CustomFontLoader.EXECUTOR.submit(() -> {
			final FontInfo tempFontInfo = FontInfo.fromJson(CustomFontLoader.GSON.fromJson(new InputStreamReader(fontInputStream.getData(), StandardCharsets.UTF_8), JsonObject.class));
			final Font font = new Font(tempFontInfo, fontInputStream.getTexture());
			font.getTexture();

			callback.accept(font);
		});
	}

}