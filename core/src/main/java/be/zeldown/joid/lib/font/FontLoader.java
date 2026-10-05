package be.zeldown.joid.lib.font;

import java.util.function.Consumer;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;
import lombok.NonNull;

public class FontLoader {

	public static void load(final @NonNull Object packed, final @NonNull Consumer<@NonNull CustomFont> callback) {
		CustomFontLoader.load(Asset.of(packed)).thenAccept(callback);
	}

	public static void load(final @NonNull Object regular, final Object bold, final @NonNull Consumer<@NonNull CustomFont> callback) {
		CustomFontLoader.load(Asset.of(regular), bold == null ? null : Asset.of(bold)).thenAccept(callback);
	}

	public static void load(final @NonNull FontInputStream regular, final @NonNull Consumer<@NonNull CustomFont> callback) {
		FontLoader.load(regular, null, callback);
	}

	public static void load(final @NonNull FontInputStream regular, final FontInputStream bold, final @NonNull Consumer<@NonNull CustomFont> callback) {
		CustomFontLoader.load(regular, bold).thenAccept(callback);
	}

}