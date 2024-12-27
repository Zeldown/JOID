package be.zeldown.joid.lib.font;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontInfo;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import lombok.NonNull;

public class FontLoader {

	private static final Gson GSON = new GsonBuilder().create();
	private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

	public static void load(final @NonNull FontInputStream input, final @NonNull Consumer<CustomFont> callback) {
		FontLoader.loadFont(input, font -> {
			callback.accept(new CustomFont(font));
		});
	}

	private static void loadFont(final @NonNull FontInputStream fontInputStream, final @NonNull Consumer<@NonNull Font> callback) {
		FontLoader.EXECUTOR.submit(() -> {
			final FontInfo tempFontInfo = FontInfo.fromJson(FontLoader.GSON.fromJson(new InputStreamReader(fontInputStream.getData(), StandardCharsets.UTF_8), JsonObject.class));
			final Font font = new Font(tempFontInfo, fontInputStream.getTexture());

			font.getTexture();
			callback.accept(font);
		});
	}

}
