package dev.joid.base.opengl.render.shader;

import java.util.HashMap;
import java.util.Map;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlProgramBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformType;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GlShader extends Shader {

	private final IGlProgramBinding    programs;
	private final Map<String, Integer> locationMap;

	private int     program;
	private boolean active;

	private GlShader(final GlRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		super(bridge, bridge.getStrategies().createTranslator(), vertex, fragment, blend);
		this.programs    = bridge.getBinding().getProgramBinding();
		this.locationMap = new HashMap<>();
	}

	public static @NonNull GlShader create(final @NonNull GlRenderBridge bridge, final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return new GlShader(bridge, vertex, fragment, blend);
	}

	public void use(final @NonNull RenderState state, final @NonNull VertexBuffer buffer) {
		this.programs.useProgram(this.program);
		super.builtins(state, buffer, super.getBridge().getProjection().getMatrix(), super.getBridge().getModelView()).upload(this::upload);
	}

	public int getLocation(final @NonNull String name) {
		return this.locationMap.computeIfAbsent(name, key -> this.programs.getUniformLocation(this.program, key));
	}

	@Override
	protected void compileProgram(final @NonNull ShaderTranslation translation) {
		final IGlProgramBinding programs = ((GlRenderBridge) super.getBridge()).getBinding().getProgramBinding();
		this.program = programs.createProgram();
		this.active  = GlShader.link(programs, translation.getDialect(), this.program, translation.getVertex(), translation.getFragment());
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