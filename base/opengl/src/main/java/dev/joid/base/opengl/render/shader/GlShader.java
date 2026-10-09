package dev.joid.base.opengl.render.shader;

import java.util.HashMap;
import java.util.Map;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformType;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlShader extends Shader {

	private final int                  program;
	private final IGlProgramBinding    programs;
	private final Map<String, Integer> locationMap;

	private GlShader(final GlRenderBridge bridge, final GlslShaderTranslator translator, final ShaderSource vertex, final ShaderSource fragment, final int program, final boolean active, final BlendState blend) {
		super(bridge, translator, vertex, fragment, blend, active);
		this.programs    = bridge.getBinding().getProgramBinding();
		this.program     = program;
		this.locationMap = new HashMap<>();
	}

	public static @NonNull GlShader create(final @NonNull GlRenderBridge bridge, final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		final GlslShaderTranslator translator = bridge.getStrategies().createTranslator();
		final IGlProgramBinding programs = bridge.getBinding().getProgramBinding();
		final int program = programs.createProgram();
		final boolean active = GlShader.link(programs, translator.getDialect(), program, translator.translateVertex(vertex, fragment), translator.translateFragment(vertex, fragment));
		return new GlShader(bridge, translator, vertex, fragment, program, active, blend);
	}

	public void use(final @NonNull RenderState state) {
		this.programs.useProgram(this.program);
		super.builtins(state, super.getBridge().getProjection().getMatrix(), super.getBridge().getModelView()).upload(this::upload);
	}

	public int getLocation(final @NonNull String name) {
		return this.locationMap.computeIfAbsent(name, key -> this.programs.getUniformLocation(this.program, key));
	}

	private void upload(final UniformMember member) {
		final int location = this.getLocation(member.getName());
		final UniformType type = member.getType();
		if (location == -1) {
			return;
		}

		if (type.isMatrix()) {
			this.programs.uniformMatrixfv(location, type.getColumns(), member.getValues().asFloatBuffer());
		} else if (type == UniformType.UINT || type == UniformType.UVEC2 || type == UniformType.UVEC3 || type == UniformType.UVEC4) {
			this.programs.uniformuiv(location, type.getComponents(), member.getValues().asIntBuffer());
		} else if (type.isInteger()) {
			this.programs.uniformiv(location, type.getComponents(), member.getValues().asIntBuffer());
		} else {
			this.programs.uniformfv(location, type.getComponents(), member.getValues().asFloatBuffer());
		}
	}

	private static boolean link(final IGlProgramBinding programs, final GlslDialect dialect, final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = GlShader.compile(programs, GlConstants.VERTEX_SHADER, vertexSource);
		final int fragment = GlShader.compile(programs, GlConstants.FRAGMENT_SHADER, fragmentSource);
		if (vertex == 0 || fragment == 0) {
			programs.deleteShader(vertex);
			programs.deleteShader(fragment);
			return false;
		}

		programs.attachShader(program, vertex);
		programs.attachShader(program, fragment);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			programs.bindAttribLocation(program, attribute.getLocation(), attribute.getBuiltin().getIdentifier());
		}
		if (dialect.hasInputOutputs() && !dialect.hasExplicitLocations()) {
			programs.bindFragDataLocation(program, 0, "fragColor");
		}
		programs.linkProgram(program);

		final boolean linked = programs.getProgrami(program, GlConstants.LINK_STATUS) == GlConstants.TRUE;
		if (!linked) {
			System.err.println("Failed to link shader program: " + programs.getProgramInfoLog(program));
		}

		programs.detachShader(program, vertex);
		programs.detachShader(program, fragment);
		programs.deleteShader(vertex);
		programs.deleteShader(fragment);
		return linked;
	}

	private static int compile(final IGlProgramBinding programs, final int type, final String source) {
		final int shader = programs.createShader(type);
		programs.shaderSource(shader, source);
		programs.compileShader(shader);
		if (programs.getShaderi(shader, GlConstants.COMPILE_STATUS) != GlConstants.TRUE) {
			System.err.println((type == GlConstants.VERTEX_SHADER ? "Vertex" : "Fragment") + " shader compilation failed: " + programs.getShaderInfoLog(shader));
			programs.deleteShader(shader);
			return 0;
		}

		return shader;
	}

}