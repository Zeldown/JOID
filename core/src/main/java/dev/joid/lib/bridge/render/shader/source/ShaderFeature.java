package dev.joid.lib.bridge.render.shader.source;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ShaderFeature {

	FLAT_VARYINGS("flat varyings", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("\\bflat\\b")),
	UNSIGNED_INTEGERS("unsigned integers", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("\\b(?:uint|uvec[234])\\b")),
	BITWISE_OPERATORS("bitwise operators", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("<<|>>|(?<!&)&(?!&)|(?<!\\|)\\|(?!\\|)|(?<!\\^)\\^(?!\\^)|~|%")),
	SWITCH("switch", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("\\bswitch\\b")),
	TEXEL_FETCH("texelFetch", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("\\btexelFetch(?:Offset)?\\b")),
	TEXTURE_SIZE("textureSize", GlslDialect.GLSL_130, GlslDialect.ESSL_300, Pattern.compile("\\btextureSize\\b")),
	DERIVATIVES("derivatives", GlslDialect.GLSL_110, GlslDialect.ESSL_300, Pattern.compile("\\b(?:dFdx|dFdy|fwidth)\\b"));

	private final String      description;
	private final GlslDialect glsl;
	private final GlslDialect essl;
	private final Pattern     pattern;

	public static @NonNull Set<@NonNull ShaderFeature> find(final @NonNull CharSequence code) {
		final Set<ShaderFeature> featureSet = EnumSet.noneOf(ShaderFeature.class);
		for (final ShaderFeature feature : ShaderFeature.values()) {
			if (feature.getPattern().matcher(code).find()) {
				featureSet.add(feature);
			}
		}
		return featureSet;
	}

}