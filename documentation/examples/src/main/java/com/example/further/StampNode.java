package com.example.further;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.framebuffer.FrameBuffer;
import dev.joid.lib.ui.node.Node;

// #region node
public class StampNode extends Node {

	private static final Color INK = new Color(153, 153, 153).lock();

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
				DrawUtils.SHAPE.drawCircle(64D, 64D, Color.WHITE, 64D);
				DrawUtils.SHAPE.drawCircle(64D, 64D, StampNode.INK, 40D);
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