package be.zeldown.joid.lib.font.dto.font;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.lib.font.dto.atlas.Atlas;
import be.zeldown.joid.lib.font.dto.data.Glyph;
import be.zeldown.joid.lib.font.dto.data.Metrics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class FontInfo {

	private static final Gson GSON = new GsonBuilder().create();

	private final Atlas               atlas;
	private final Metrics             metrics;
	private final Map<Integer, Glyph> glyphMap;

	public static @NonNull FontInfo fromJson(final @NonNull JsonObject json) {
		final Atlas tempAtlas = FontInfo.GSON.fromJson(json.getAsJsonObject("atlas"), Atlas.class);
		final Metrics tempMetrics = FontInfo.GSON.fromJson(json.getAsJsonObject("metrics"), Metrics.class);

		final Map<Integer, Glyph> tempGlyphs = new HashMap<>();
		final StringBuilder charList = new StringBuilder();
		json.getAsJsonArray("glyphs").forEach(element -> {
			final Glyph glyph = FontInfo.GSON.fromJson(element, Glyph.class);
			tempGlyphs.put(glyph.getUnicode(), glyph);
			charList.append((char) glyph.getUnicode());
		});

		return new FontInfo(tempAtlas, tempMetrics, tempGlyphs);
	}

}