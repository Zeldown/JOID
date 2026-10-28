package com.example.further;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.framebuffer.FrameBuffer;
import dev.joid.lib.ui.node.Node;

// #region node
public class StampNode extends Node {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private transient FrameBuffer frameBuffer;

	protected StampNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static StampNode create(final double x, final double y, final double size) {
		return new StampNode(x, y, size, size);
	}

	// #region fill
	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.frameBuffer == null) {
			this.frameBuffer = FrameBuffer.create(128, 128, TextureFilter.LINEAR).fill(() -> {
				BridgeHandler.RENDER.get().clearColor(0F, 0F, 0F, 0F);
				DrawUtils.SHAPE.drawCircle(64D, 64D, StampNode.ELEMENT, 64D);
				DrawUtils.SHAPE.drawCircle(64D, 64D, StampNode.ACCENT, 40D);
			});
		}

		this.frameBuffer.draw(super.getX(), super.getY(), super.w(), super.h());
	}
	// #endregion

	// #region detach
	@Override
	public void detach() {
		if (this.frameBuffer != null) {
			this.frameBuffer.delete();
			this.frameBuffer = null;
		}
	}
	// #endregion

}
// #endregion