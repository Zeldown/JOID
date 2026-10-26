package dev.joid.base.opengl.capability;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlCapabilities {

	private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)");

	private final int         version;
	private final int         glslVersion;
	private final GlProfile   profile;
	private final String      renderer;
	private final Set<String> extensionSet;
	private final int         maxTextureSize;

	public static @NonNull GlCapabilities read(final IGlBinding binding) {
		final String name = binding.getString(GlConstants.VERSION);
		if (name == null) {
			throw new IllegalStateException("No OpenGL context is current: create the context of JOID and make it current before registering the backend");
		}

		final int version = GlCapabilities.parseVersion(name);
		final Set<String> extensionSet = new HashSet<>();
		if (version >= 300) {
			for (int i = 0; i < binding.getInteger(GlConstants.NUM_EXTENSIONS); i++) {
				extensionSet.add(binding.getString(GlConstants.EXTENSIONS, i));
			}
		} else {
			extensionSet.addAll(Arrays.asList(binding.getString(GlConstants.EXTENSIONS).trim().split("\\s+")));
		}

		GlProfile profile = GlProfile.COMPATIBILITY;
		if (version >= 300 && (binding.getInteger(GlConstants.CONTEXT_FLAGS) & GlConstants.CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT) != 0) {
			profile = GlProfile.CORE_FORWARD_COMPATIBLE;
		} else if ((version >= 320 && (binding.getInteger(GlConstants.CONTEXT_PROFILE_MASK) & GlConstants.CONTEXT_CORE_PROFILE_BIT) != 0) || (version == 310 && !extensionSet.contains("GL_ARB_compatibility"))) {
			profile = GlProfile.CORE;
		}

		return new GlCapabilities(version, GlCapabilities.parseVersion(binding.getString(GlConstants.SHADING_LANGUAGE_VERSION)), profile, binding.getString(GlConstants.RENDERER), Collections.unmodifiableSet(extensionSet), binding.getInteger(GlConstants.MAX_TEXTURE_SIZE));
	}

	public @NonNull GlCapabilities require() {
		if (this.version < 210 || this.glslVersion < GlslDialect.GLSL_120.getVersion()) {
			throw new IllegalStateException("JOID needs OpenGL 2.1 with GLSL 1.20, this context offers " + this.getName() + ": update the graphics driver or create a newer OpenGL context");
		}
		if (this.getFrameBufferFamily() == GlFrameBufferFamily.UNSUPPORTED) {
			throw new IllegalStateException("JOID needs framebuffer objects (OpenGL 3.0, GL_ARB_framebuffer_object or GL_EXT_framebuffer_object), this context offers " + this.getName() + ": update the graphics driver or create a newer OpenGL context");
		}
		return this;
	}

	public boolean hasExtension(final String name) {
		return this.extensionSet.contains(name);
	}

	public boolean hasDepthClamp() {
		return this.version >= 320 || this.hasExtension("GL_ARB_depth_clamp");
	}

	public boolean hasVertexArrays() {
		return this.version >= 300 || this.hasExtension("GL_ARB_vertex_array_object");
	}

	public boolean hasPixelBuffers() {
		return this.version >= 210 || this.hasExtension("GL_ARB_pixel_buffer_object") || this.hasExtension("GL_EXT_pixel_buffer_object");
	}

	public boolean hasSamplerObjects() {
		return this.version >= 330 || this.hasExtension("GL_ARB_sampler_objects");
	}

	public boolean hasFrameBufferSrgb() {
		return this.version >= 300 || this.hasExtension("GL_ARB_framebuffer_sRGB") || this.hasExtension("GL_EXT_framebuffer_sRGB");
	}

	public boolean hasFrameBufferBlit() {
		return this.getFrameBufferFamily() == GlFrameBufferFamily.CORE || this.hasExtension("GL_EXT_framebuffer_blit");
	}

	public boolean hasPrimitiveRestart() {
		return this.version >= 310;
	}

	public boolean hasRasterizerDiscard() {
		return this.version >= 300;
	}

	public @NonNull String getName() {
		return "OpenGL " + this.version / 100 + "." + this.version / 10 % 10 + " with GLSL " + this.glslVersion / 100 + "." + String.format(Locale.ROOT, "%02d", this.glslVersion % 100) + " (" + this.renderer + ")";
	}

	public @NonNull GlslDialect getDialect() {
		GlslDialect dialect = GlslDialect.GLSL_120;
		for (final GlslDialect candidate : GlslDialect.values()) {
			if (!candidate.isEs() && candidate.getVersion() <= Math.min(this.glslVersion, GlslDialect.GLSL_330.getVersion()) && candidate.getVersion() > dialect.getVersion()) {
				dialect = candidate;
			}
		}
		return dialect;
	}

	public @NonNull GlFrameBufferFamily getFrameBufferFamily() {
		if (this.version >= 300 || this.hasExtension("GL_ARB_framebuffer_object")) {
			return GlFrameBufferFamily.CORE;
		}
		return this.hasExtension("GL_EXT_framebuffer_object") ? GlFrameBufferFamily.EXT : GlFrameBufferFamily.UNSUPPORTED;
	}

	private static int parseVersion(final String version) {
		final Matcher matcher = GlCapabilities.VERSION.matcher(version == null ? "" : version);
		if (!matcher.find()) {
			return 0;
		}

		final String minor = matcher.group(2);
		return Integer.parseInt(matcher.group(1)) * 100 + Integer.parseInt(minor.length() == 1 ? minor + "0" : minor.substring(0, 2));
	}

}