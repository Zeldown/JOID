package be.zeldown.joid.impl.lwjgl2.render.state;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import be.zeldown.joid.impl.lwjgl2.render.RenderBridge;
import lombok.NonNull;

public final class BlendSnapshot {

	private final boolean enabled;
	private final int     equation;
	private final int     sourceColor;
	private final int     destinationColor;
	private final int     sourceAlpha;
	private final int     destinationAlpha;

	private BlendSnapshot() {
		this.enabled          = GL11.glIsEnabled(GL11.GL_BLEND);
		this.equation         = GL11.glGetInteger(GL14.GL_BLEND_EQUATION);
		this.sourceColor      = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
		this.destinationColor = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
		this.sourceAlpha      = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
		this.destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
	}

	public static @NonNull BlendSnapshot capture() {
		return new BlendSnapshot();
	}

	public void restore() {
		RenderBridge.toggle(GL11.GL_BLEND, this.enabled);
		GL14.glBlendEquation(this.equation);
		GL14.glBlendFuncSeparate(this.sourceColor, this.destinationColor, this.sourceAlpha, this.destinationAlpha);
	}

}