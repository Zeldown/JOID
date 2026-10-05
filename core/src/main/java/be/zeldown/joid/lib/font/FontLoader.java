package be.zeldown.joid.lib.font;

import java.io.InputStream;
import java.util.function.Consumer;

import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;
import lombok.NonNull;

public class FontLoader {

	public static void load(final @NonNull FontInputStream regular, final @NonNull Consumer<@NonNull CustomFont> callback) {
		FontLoader.load(regular, null, callback);
	}

	public static void load(final @NonNull FontInputStream regular, final FontInputStream bold, final @NonNull Consumer<@NonNull CustomFont> callback) {
		(bold == null ? CustomFontLoader.load(regular) : CustomFontLoader.load(regular, bold)).thenAccept(callback);
	}

	public static void load(final @NonNull InputStream packed, final @NonNull Consumer<@NonNull CustomFont> callback) {
		CustomFontLoader.load(packed).thenAccept(callback);
	}

	public static void load(final @NonNull InputStream regular, final InputStream bold, final @NonNull Consumer<@NonNull CustomFont> callback) {
		CustomFontLoader.load(regular, bold).thenAccept(callback);
	}

}