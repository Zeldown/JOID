package dev.joid.lib.bridge.render.shader;

import java.util.List;

import dev.joid.lib.bridge.render.RenderBridge;
import dev.joid.lib.bridge.render.shader.source.ShaderVariable;
import dev.joid.lib.bridge.render.shader.uniform.UniformBlock;
import dev.joid.lib.bridge.render.state.BlendState;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Shader extends UniformShader {

	private final RenderBridge bridge;
	private final BlendState   blend;
	private final boolean      active;

	private boolean    bound;
	private BlendState previousBlend;

	protected Shader(final @NonNull RenderBridge bridge, final @NonNull BlendState blend, final boolean active, final @NonNull UniformBlock block, final @NonNull List<@NonNull ShaderVariable> samplers) {
		super(block, samplers);
		this.bridge = bridge;
		this.blend  = blend;
		this.active = active;
	}

	@Override
	public final void bind() {
		this.previousBlend = this.bridge.getState().getBlend();
		this.bridge.shader(this);
		this.bridge.blend(this.blend);
		this.bound = true;
	}

	@Override
	public final void unbind() {
		this.bridge.shader(null);
		if (this.previousBlend != null) {
			this.bridge.blend(this.previousBlend);
			this.previousBlend = null;
		}

		this.bound = false;
	}

}