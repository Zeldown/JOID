package dev.joid.lib.bridge.render.shader.source;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlslDialect {

	GLSL_110(110, false, "#version 110"),
	GLSL_120(120, false, "#version 120"),
	GLSL_130(130, false, "#version 130"),
	GLSL_140(140, false, "#version 140"),
	GLSL_150(150, false, "#version 150"),
	GLSL_330(330, false, "#version 330 core"),
	GLSL_450(450, false, "#version 450"),
	ESSL_100(100, true, "#version 100"),
	ESSL_300(300, true, "#version 300 es");

	private final int     version;
	private final boolean es;
	private final String  declaration;

	public boolean supports(final @NonNull ShaderFeature feature) {
		return this.version >= (this.es ? feature.getEssl() : feature.getGlsl()).getVersion();
	}

	public boolean hasInputOutputs() {
		return this.version >= (this.es ? 300 : 130);
	}

	public boolean hasUniformBlocks() {
		return this.version >= (this.es ? 300 : 140);
	}

	public boolean hasExplicitLocations() {
		return this.version >= (this.es ? 300 : 330);
	}

	public @NonNull String getLineDirective() {
		return this.version >= (this.es ? 300 : 330) ? "#line 1\n" : "#line 0\n";
	}

	public @NonNull String getPrecision() {
		if (!this.es) {
			return "";
		}
		return this.version >= 300 ? "precision highp float;\nprecision highp int;\n" : "#ifdef GL_FRAGMENT_PRECISION_HIGH\nprecision highp float;\n#else\nprecision mediump float;\n#endif\n";
	}

	public @NonNull String getName() {
		return (this.es ? "ESSL " : "GLSL ") + this.version / 100 + "." + String.format("%02d", this.version % 100);
	}

}