package dev.joid.lib.font.impl.msdf.dto.source;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.dto.MsdfAtlas;
import dev.joid.lib.font.impl.msdf.dto.MsdfBounds;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.font.impl.msdf.dto.MsdfGlyph;
import dev.joid.lib.font.impl.msdf.dto.MsdfMetrics;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MsdfJsonSource extends MsdfSource {

	private static final Gson GSON = new GsonBuilder().create();

	private final Asset json;
	private final Asset texture;

	public static @NonNull MsdfJsonSource of(final @NonNull Object json, final @NonNull Object texture) {
		return new MsdfJsonSource(Asset.of(json), Asset.of(texture));
	}

	@Override
	public @NonNull String describe() {
		return "read from a json atlas";
	}

	@Override
	protected @NonNull MsdfFontFace parse() throws IOException {
		final JsonObject root;
		try (InputStream stream = this.json.open()) {
			root = MsdfJsonSource.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
		}

		final BufferedImage image;
		try (InputStream stream = this.texture.open()) {
			image = ImageIO.read(stream);
		}

		if (image == null) {
			throw new IOException("Unable to decode the atlas texture " + this.texture.getUniqueId());
		}

		return MsdfFontFace.create(MsdfJsonSource.atlas(root.getAsJsonObject("atlas")), MsdfJsonSource.metrics(root.getAsJsonObject("metrics")), MsdfJsonSource.glyphs(root), MsdfJsonSource.kerningPairs(root), image, "", FontWeight.REGULAR, false);
	}

	private static @NonNull MsdfAtlas atlas(final @NonNull JsonObject json) {
		return new MsdfAtlas(MsdfJsonSource.number(json, "distanceRange"), MsdfJsonSource.number(json, "size"), json.get("width").getAsInt(), json.get("height").getAsInt());
	}

	private static @NonNull MsdfMetrics metrics(final @NonNull JsonObject json) {
		return new MsdfMetrics(MsdfJsonSource.number(json, "lineHeight"), MsdfJsonSource.number(json, "ascender"), MsdfJsonSource.number(json, "descender"), MsdfJsonSource.number(json, "underlineY"), MsdfJsonSource.number(json, "underlineThickness"));
	}

	private static @NonNull Map<Long, Float> kerningPairs(final @NonNull JsonObject root) {
		final Map<Long, Float> kerningPairs = new HashMap<>();
		if (!root.has("kerning")) {
			return kerningPairs;
		}

		for (final JsonElement element : root.getAsJsonArray("kerning")) {
			final JsonObject pair = element.getAsJsonObject();
			kerningPairs.put(MsdfFontFace.pair(pair.get("unicode1").getAsInt(), pair.get("unicode2").getAsInt()), MsdfJsonSource.number(pair, "advance"));
		}
		return kerningPairs;
	}

	private static @NonNull Map<Integer, MsdfGlyph> glyphs(final @NonNull JsonObject root) {
		final Map<Integer, MsdfGlyph> glyphs = new HashMap<>();
		for (final JsonElement element : root.getAsJsonArray("glyphs")) {
			final JsonObject glyph = element.getAsJsonObject();
			final int codepoint = glyph.get("unicode").getAsInt();
			glyphs.put(codepoint, new MsdfGlyph(codepoint, MsdfJsonSource.number(glyph, "advance"), MsdfJsonSource.bounds(glyph, "planeBounds"), MsdfJsonSource.bounds(glyph, "atlasBounds")));
		}
		return glyphs;
	}

	private static float number(final @NonNull JsonObject json, final @NonNull String name) {
		final JsonElement element = json.get(name);
		return element == null ? 0F : element.getAsFloat();
	}

	private static MsdfBounds bounds(final @NonNull JsonObject glyph, final @NonNull String name) {
		final JsonObject bounds = glyph.getAsJsonObject(name);
		if (bounds == null) {
			return null;
		}
		return new MsdfBounds(MsdfJsonSource.number(bounds, "left"), MsdfJsonSource.number(bounds, "bottom"), MsdfJsonSource.number(bounds, "right"), MsdfJsonSource.number(bounds, "top"));
	}

}