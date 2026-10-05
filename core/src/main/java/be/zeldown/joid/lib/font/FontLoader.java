package be.zeldown.joid.lib.font;

import java.util.concurrent.CompletableFuture;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.dto.font.FontInputStream;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;
import lombok.NonNull;

public class FontLoader {

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull Object packed) {
		return CustomFontLoader.load(Asset.of(packed));
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream regular) {
		return CustomFontLoader.load(regular);
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull Object regular, final Object bold) {
		return CustomFontLoader.load(Asset.of(regular), bold == null ? null : Asset.of(bold));
	}

	public static @NonNull CompletableFuture<CustomFont> load(final @NonNull FontInputStream regular, final FontInputStream bold) {
		return CustomFontLoader.load(regular, bold);
	}

}