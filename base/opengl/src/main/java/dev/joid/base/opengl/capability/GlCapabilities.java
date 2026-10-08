package dev.joid.base.opengl.capability;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
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
	private final float[]     smoothLineWidthRange;
	private final float[]     aliasedLineWidthRange;

	public static @NonNull GlCapabilities read(final @NonNull IGlBinding binding) {
		final int version = GlCapabilities.parseVersion(binding.getString(GlConstants.VERSION));
		final Set<String> extensionSet = new HashSet<>();
		if (version >= 300) {
			for (int i = 0; i < binding.getInteger(GlConstants.NUM_EXTENSIONS); i++) {
				extensionSet.add(binding.getString(GlConstants.EXTENSIONS, i));
			}
		} else {
			final String extensions = binding.getString(GlConstants.EXTENSIONS);
			extensionSet.addAll(Arrays.asList(extensions == null ? new String[0] : extensions.trim().split("\\s+")));
		}

		GlProfile profile = GlProfile.COMPATIBILITY;
		if (version >= 300 && (binding.getInteger(GlConstants.CONTEXT_FLAGS) & GlConstants.CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT) != 0) {
			profile = GlProfile.FORWARD_COMPATIBLE_CORE;
		} else if (version >= 320 && (binding.getInteger(GlConstants.CONTEXT_PROFILE_MASK) & GlConstants.CONTEXT_CORE_PROFILE_BIT) != 0) {
			profile = GlProfile.CORE;
		}

		final float[] smoothLineWidthRange = new float[2];
		final float[] aliasedLineWidthRange = new float[2];
		binding.getFloats(GlConstants.SMOOTH_LINE_WIDTH_RANGE, smoothLineWidthRange);
		binding.getFloats(GlConstants.ALIASED_LINE_WIDTH_RANGE, aliasedLineWidthRange);
		return new GlCapabilities(version, GlCapabilities.parseVersion(binding.getString(GlConstants.SHADING_LANGUAGE_VERSION)), profile, binding.getString(GlConstants.RENDERER), Collections.unmodifiableSet(extensionSet), binding.getInteger(GlConstants.MAX_TEXTURE_SIZE), smoothLineWidthRange, aliasedLineWidthRange);
	}

	public boolean hasExtension(final @NonNull String name) {
		return this.extensionSet.contains(name);
	}

	public boolean hasVertexArrays() {
		return this.version >= 300 || this.hasExtension("GL_ARB_vertex_array_object");
	}

	public boolean hasUniformBuffers() {
		return this.version >= 310 || this.hasExtension("GL_ARB_uniform_buffer_object");
	}

	public boolean hasSamplerObjects() {
		return this.version >= 330 || this.hasExtension("GL_ARB_sampler_objects");
	}

	public boolean hasFrameBufferObjects() {
		return this.version >= 300 || this.hasExtension("GL_ARB_framebuffer_object");
	}

	public @NonNull String getName() {
		return "OpenGL " + this.version / 100 + "." + this.version / 10 % 10;
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