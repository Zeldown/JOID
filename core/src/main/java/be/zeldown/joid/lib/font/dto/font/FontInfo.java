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
	private final Map<Long, Float>    kerningMap;

	public float getKerning(final int previous, final int current) {
		final Float value = this.kerningMap.get(((long) previous << 32) | (current & 0xFFFFFFFFL));
		return value == null ? 0F : value;
	}

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

		final Map<Long, Float> tempKerning = new HashMap<>();
		if (json.has("kerning")) {
			json.getAsJsonArray("kerning").forEach(element -> {
				final JsonObject entry = element.getAsJsonObject();
				final long key = ((long) entry.get("unicode1").getAsInt() << 32) | (entry.get("unicode2").getAsInt() & 0xFFFFFFFFL);
				tempKerning.put(key, entry.get("advance").getAsFloat());
			});
		}

		return new FontInfo(tempAtlas, tempMetrics, tempGlyphs, tempKerning);
	}

}