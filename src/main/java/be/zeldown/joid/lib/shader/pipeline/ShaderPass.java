package be.zeldown.joid.lib.shader.pipeline;

import be.zeldown.joid.lib.ui.node.Node;

public interface ShaderPass {

	public void bindDirect(final Node node);

	public void bindForTexture(final Node node);

	public void unbind();

	public int priority();

	default public float expansion() { return 0F; }

	default public boolean supportsDirectBind() { return false; }

}