package dev.joid.backend.lwjgl3.snapshot;

import java.util.Arrays;
import java.util.List;

import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.base.opengl.capability.GlCapabilities;
import dev.joid.base.opengl.capability.GlProfile;
import lombok.Getter;
import lombok.NonNull;

@Getter
public enum SnapshotProfile {

	DEFAULT(GlContextRequest.CORE_33, 0, null, null),
	GL_20(GlContextRequest.COMPATIBILITY, 200, GlProfile.COMPATIBILITY, "1.10"),
	GL_21(GlContextRequest.COMPATIBILITY, 210, GlProfile.COMPATIBILITY, null),
	GL_21_EXT(GlContextRequest.COMPATIBILITY, 210, GlProfile.COMPATIBILITY, null, "GL_ARB_framebuffer_object", "GL_ARB_vertex_array_object"),
	GL_21_EXT_NO_BLIT(GlContextRequest.COMPATIBILITY, 210, GlProfile.COMPATIBILITY, null, "GL_ARB_framebuffer_object", "GL_ARB_vertex_array_object", "GL_EXT_framebuffer_blit"),
	GL_30(GlContextRequest.COMPATIBILITY, 300, GlProfile.COMPATIBILITY, null),
	GL_32_FORWARD_COMPATIBLE(GlContextRequest.CORE_32_FORWARD_COMPATIBLE, 320, GlProfile.CORE_FORWARD_COMPATIBLE, null),
	GL_33(GlContextRequest.CORE_33, 330, GlProfile.CORE, null),
	GL_45_COMPATIBILITY(GlContextRequest.COMPATIBILITY, 450, GlProfile.COMPATIBILITY, null);

	private final int              version;
	private final GlProfile        profile;
	private final GlContextRequest request;
	private final String           shadingLanguageVersion;
	private final List<String>     hiddenExtensionList;

	private SnapshotProfile(final GlContextRequest request, final int version, final GlProfile profile, final String shadingLanguageVersion, final String... hiddenExtensions) {
		this.request                = request;
		this.version                = version;
		this.profile                = profile;
		this.shadingLanguageVersion = shadingLanguageVersion;
		this.hiddenExtensionList    = Arrays.asList(hiddenExtensions);
	}

	public static @NonNull SnapshotProfile current() {
		return SnapshotProfile.valueOf(System.getProperty("joid.snapshot.profile", SnapshotProfile.DEFAULT.name()));
	}

	public void check(final @NonNull GlCapabilities capabilities) {
		if (this.version != 0 && (capabilities.getVersion() != this.version || capabilities.getProfile() != this.profile)) {
			throw new IllegalStateException("The snapshot profile " + this.name() + " needs OpenGL " + this.version / 100 + "." + this.version / 10 % 10 + " " + this.profile + ", this context offers " + capabilities.getName() + " " + capabilities.getProfile() + ": force its version with MESA_GL_VERSION_OVERRIDE and MESA_GLSL_VERSION_OVERRIDE");
		}
	}

}