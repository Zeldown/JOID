package dev.joid.lib.shader.impl;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.color.Color;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;
import lombok.NonNull;

public abstract class ShaderProgram {

	private IShader      shader;
	private ShaderSource vertex;
	private ShaderSource fragment;

	protected void load(final @NonNull Object vertexShader, final @NonNull Object fragmentShader) {
		try {
			try {
				this.vertex = ShaderSource.read(ShaderStage.VERTEX, vertexShader);
			} finally {
				this.fragment = ShaderSource.read(ShaderStage.FRAGMENT, fragmentShader);
			}
		} catch (final Exception e) {
			this.vertex = null;
			JOIDLog.warn("The shader " + JOIDLog.nameOf(this.getClass()) + " cannot be loaded (" + e.getMessage() + "), what it draws is skipped: check the paths of its vertex and fragment sources", e);
		}
	}

	public void delete() {
		if (this.shader != null) {
			this.shader.delete();
			this.shader = null;
		}
	}

	public void bind() {
		final IShader current = this.getShader();
		if (current != null && current.isActive()) {
			current.bind();
		}
	}

	public void unbind() {
		if (this.shader != null) {
			this.shader.unbind();
		}
	}

	public IShader getShader() {
		if (this.shader == null && this.vertex != null) {
			try {
				this.shader = BridgeHandler.RENDER.get().createShader(this.vertex, this.fragment, BlendState.NORMAL);
			} catch (final Exception e) {
				this.vertex = null;
				JOIDLog.warn("The shader " + JOIDLog.nameOf(this.getClass()) + " cannot be created (" + e.getMessage() + "), what it draws is skipped: fix its vertex and fragment sources from this error", e);
			}
		}
		return this.shader;
	}

	public boolean canDraw() {
		final boolean available = this.isAvailable();
		if (!available) {
			JOIDLog.devWarnOnce("ShaderProgram.unavailable." + System.identityHashCode(this), () -> "The shader " + JOIDLog.nameOf(this.getClass()) + " is unavailable, what it draws is skipped");
		}
		return available;
	}

	public boolean isAvailable() {
		final IShader current = this.getShader();
		return current != null && current.isActive();
	}

	public final void gradient(final @NonNull ColorGradient gradient, final @NonNull BoundingBox canvas) {
		ShaderProgram.uniformGradient(this.getShader(), gradient, (float) canvas.getMinX(), (float) canvas.getMinY(), (float) canvas.getMaxX(), (float) canvas.getMaxY());
	}

	protected final void use(final @NonNull Runnable bind, final @NonNull Runnable body) {
		if (!this.canDraw()) {
			return;
		}

		final RenderState state = BridgeHandler.RENDER.get().getState();
		final IShader previous = state.getShader();
		final BlendState blend = state.getBlend();
		bind.run();
		try {
			body.run();
		} finally {
			this.unbind();
			state.shader(previous).blend(blend);
		}
	}

	public static void uniformGradient(final @NonNull IShader shader, final @NonNull ColorGradient gradient, final float x1, final float y1, final float x2, final float y2) {
		shader.uniform("u_HasGradient", 1);
		ShaderProgram.uniformGradient(shader, gradient.getStartPoint(), gradient.getEndPoint(), gradient.getStartColor(), gradient.getEndColor(), x1, y1, x2, y2);
	}

	public static void uniformGradient(final @NonNull IShader shader, final @NonNull Point startPoint, final @NonNull Point endPoint, final @NonNull Color startColor, final @NonNull Color endColor, final float x1, final float y1, final float x2, final float y2) {
		final Color start = startColor.update();
		final Color end = endColor.update();
		shader
		.uniform("u_GradientStart", start.getRedF(), start.getGreenF(), start.getBlueF(), start.getAlphaF())
		.uniform("u_GradientEnd", end.getRedF(), end.getGreenF(), end.getBlueF(), end.getAlphaF())
		.uniform("u_GradientStartPos", (float) startPoint.getX(), (float) startPoint.getY())
		.uniform("u_GradientEndPos", (float) endPoint.getX(), (float) endPoint.getY())
		.uniform("u_GradientCanvas", x1, y1, x2, y2);
	}

}