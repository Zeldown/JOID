package be.zeldown.joid.lib.font.impl.custom;

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
import lombok.NonNull;

public final class CustomFontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

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