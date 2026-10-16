package dev.joid.base.opengl.render.shader;

import java.util.HashMap;
import java.util.Map;

import dev.joid.base.opengl.binding.GlConstants;
import dev.joid.base.opengl.binding.IGlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.render.shader.Shader;
import dev.joid.lib.bridge.render.shader.source.GlslDialect;
import dev.joid.lib.bridge.render.shader.source.GlslShaderTranslator;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderTranslation;
import dev.joid.lib.bridge.render.shader.source.UniformLayout;
import dev.joid.lib.bridge.render.shader.uniform.UniformMember;
import dev.joid.lib.bridge.render.shader.uniform.UniformType;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

public final class GlShader extends Shader {

	private final IGlBinding           binding;
	private final Map<String, Integer> locationMap;

	@Getter private int     program;
	@Getter private boolean programActive;

	private GlShader(final GlRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		super(bridge, GlslShaderTranslator.create(bridge.getCapabilities().getDialect(), UniformLayout.LOOSE), vertex, fragment, blend);
		this.binding     = bridge.getBinding();
		this.locationMap = new HashMap<>();
	}

	public static @NonNull GlShader create(final GlRenderBridge bridge, final ShaderSource vertex, final ShaderSource fragment, final BlendState blend) {
		return new GlShader(bridge, vertex, fragment, blend);
	}

	public void use(final @NonNull RenderState state, final @NonNull VertexBuffer buffer) {
		this.binding.useProgram(this.program);
		super.builtins(state, buffer, super.getBridge().getProjection().getRawMatrix(), super.getBridge().getModelView()).upload(this::upload);
	}

	public int getLocation(final String name) {
		return this.locationMap.computeIfAbsent(name, key -> this.binding.getUniformLocation(this.program, key));
	}

	@Override
	protected void deleteProgram() {
		this.binding.deleteProgram(this.program);
		this.locationMap.clear();
		this.program       = 0;
		this.programActive = false;
	}

	@Override
	protected void compileProgram(final @NonNull ShaderTranslation translation) {
		this.program       = this.binding.createProgram();
		this.programActive = GlShader.link(this.binding, translation.getDialect(), this.program, translation.getVertex(), translation.getFragment());
	}

	private void upload(final UniformMember member) {
		final int location = this.getLocation(member.getName());
		final UniformType type = member.getType();
		if (location == -1) {
			return;
		}

		if (type.isMatrix()) {
			this.binding.uniformMatrixfv(location, type.getColumns(), member.getValues().asFloatBuffer());
		} else if (type == UniformType.UINT || type == UniformType.UVEC2 || type == UniformType.UVEC3 || type == UniformType.UVEC4) {
			this.binding.uniformuiv(location, type.getComponents(), member.getValues().asIntBuffer());
		} else if (type.isInteger()) {
			this.binding.uniformiv(location, type.getComponents(), member.getValues().asIntBuffer());
		} else {
			this.binding.uniformfv(location, type.getComponents(), member.getValues().asFloatBuffer());
		}
	}

	private static boolean link(final IGlBinding binding, final GlslDialect dialect, final int program, final String vertexSource, final String fragmentSource) {
		final int vertex = GlShader.compile(binding, GlConstants.VERTEX_SHADER, vertexSource);
		final int fragment = GlShader.compile(binding, GlConstants.FRAGMENT_SHADER, fragmentSource);
		if (vertex == 0 || fragment == 0) {
			binding.deleteShader(vertex);
			binding.deleteShader(fragment);
			return false;
		}

		binding.attachShader(program, vertex);
		binding.attachShader(program, fragment);
		for (final VertexAttribute attribute : VertexAttribute.values()) {
			binding.bindAttribLocation(program, attribute.getLocation(), attribute.getBuiltin().getIdentifier());
		}
		if (dialect.hasInputOutputs() && !dialect.hasExplicitLocations()) {
			binding.bindFragDataLocation(program, 0, "fragColor");
		}
		binding.linkProgram(program);

		final boolean linked = binding.getProgrami(program, GlConstants.LINK_STATUS) == GlConstants.TRUE;
		if (!linked) {
			JOIDLog.warn("OpenGL cannot link a shader program, fix its sources from the log of the driver: " + binding.getProgramInfoLog(program));
		}

		binding.detachShader(program, vertex);
		binding.detachShader(program, fragment);
		binding.deleteShader(vertex);
		binding.deleteShader(fragment);
		return linked;
	}

	private static int compile(final IGlBinding binding, final int type, final String source) {
		final int shader = binding.createShader(type);
		binding.shaderSource(shader, source);
		binding.compileShader(shader);
		if (binding.getShaderi(shader, GlConstants.COMPILE_STATUS) != GlConstants.TRUE) {
			JOIDLog.warn("OpenGL cannot compile a " + (type == GlConstants.VERTEX_SHADER ? "vertex" : "fragment") + " shader, fix its source at the line the driver reports: " + binding.getShaderInfoLog(shader));
			binding.deleteShader(shader);
			return 0;
		}

		return shader;
	}

}