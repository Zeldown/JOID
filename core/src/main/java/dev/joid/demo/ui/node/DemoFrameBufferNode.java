package dev.joid.demo.ui.node;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.framebuffer.FrameBuffer;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class DemoFrameBufferNode extends Node {

	private static final Color INK = new Color(153, 153, 153).freeze();

	private FrameBuffer frameBuffer;

	protected DemoFrameBufferNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoFrameBufferNode create(final double x, final double y, final double width, final double height) {
		return new DemoFrameBufferNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.frameBuffer == null) {
			this.frameBuffer = FrameBuffer.create(40, 25, TextureFilter.NEAREST).fill(() -> {
				BridgeHandler.RENDER.get().clearColor(1F, 1F, 1F, 1F);
				DrawUtils.SHAPE.drawRect(4D, 4D, 14D, 17D, DemoFrameBufferNode.INK);
				DrawUtils.SHAPE.drawCircle(29D, 12.5D, DemoFrameBufferNode.INK, 7D);
			});
		}

		this.frameBuffer.draw(super.getX(), super.getY(), super.getWidth(), super.getHeight());
	}

	@Override
	public void detach() {
		if (this.frameBuffer != null) {
			this.frameBuffer.delete();
			this.frameBuffer = null;
		}
	}

}