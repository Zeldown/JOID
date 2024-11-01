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
import lombok.NonNull;

public final class CustomFontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

	public static @NonNull CompletableFuture<be.zeldown.joid.lib.font.impl.custom.CustomFont> load(final @NonNull FontInputStream input) {
		final CompletableFuture<CustomFont> future = new CompletableFuture<>();
		CustomFontLoader.loadFont(input, font -> {
			future.complete(new CustomFont(font));
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