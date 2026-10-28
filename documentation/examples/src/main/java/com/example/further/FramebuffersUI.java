package com.example.further;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.framebuffer.FrameBufferPool;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;

public class FramebuffersUI extends UI {

	// #region model
	private static final Color TEXT = new Color(231, 233, 238).lock();

	private final PyramidModel pyramid = new PyramidModel();

	private final Transformation turn = Transformation
	.create()
	.translate(Vector.create(100D, 120D))
	.rotate(20D, Rotation.YAW)
	.rotate(-30D, Rotation.PITCH);
	// #endregion

	@Override
	public void init() {
		// #region stamp
		ContainerNode
		.create(80, 80, 300, 200)
		.body(container -> {
			StampNode.create(20, 36, 128).attach(container);
			StampNode.create(188, 68, 64).attach(container);
		})
		.attach(this);
		// #endregion

		// #region triangle
		ContainerNode
		.create(460, 80, 200, 200)
		.layer((node, mouseX, mouseY) -> {
			final Tessellator tessellator = Tessellator.inst();
			BridgeHandler.RENDER.get().withState(render -> {
				render.getState().blend(BlendState.NORMAL).texture(null);
				tessellator.start(DrawMode.TRIANGLES);
				tessellator.setColor(231, 233, 238);
				tessellator.addVertex(100D, 0D, 0D);
				tessellator.setColor(91, 140, 255);
				tessellator.addVertex(200D, 200D, 0D);
				tessellator.setColor(38, 43, 53);
				tessellator.addVertex(0D, 200D, 0D);
				tessellator.draw();
			});
		})
		.attach(this);
		// #endregion

		// #region pool
		ContainerNode
		.create(740, 80, 300, 200)
		.layer((node, mouseX, mouseY) -> {
			final IRenderBridge render = BridgeHandler.RENDER.get();
			final PixelGrid grid = render.getPixelGrid();
			final int width = grid.toPixelWidth(node.getWidth());
			final int height = grid.toPixelHeight(node.getHeight());
			final IFrameBuffer target = FrameBufferPool.allocate(FramebuffersUI.class, 0, 1, width, height)[0];
			render.withState(state -> render.getProjection().with(projection -> render.getModelView().with(view -> {
				state.getState().frameBuffer(target).viewport(0, 0, width, height);
				projection.ortho(0D, node.getWidth(), node.getHeight(), 0D, -1000D, 1000D);
				view.identity();
				render.clearColor(0F, 0F, 0F, 0F);
				DrawUtils.SHAPE.drawCircle(100D, 100D, FramebuffersUI.TEXT, 80D);
				DrawUtils.SHAPE.drawCircle(200D, 100D, FramebuffersUI.TEXT, 80D);
			})));

			final double u = (double) width / target.getWidth();
			final double v = (double) height / target.getHeight();
			render.withState(state -> {
				state.getState().color(0.5F, 0.5F, 0.5F, 0.5F);
				final TextureFilter filter = TextureFilter.LINEAR;
				final BlendState blend = BlendState.PREMULTIPLIED;
				final ITexture texture = target.getTexture();
				DrawUtils.RESOURCE.drawTexture(0D, 0D, node.w(), node.h(), texture, 0D, v, u, 0D, filter, blend);
			});
		})
		.attach(this);
		// #endregion

		// #region draw-model
		ContainerNode
		.create(1120, 80, 200, 200)
		.layer((node, mouseX, mouseY) -> this.turn.apply(() -> DrawUtils.MODEL.drawModel(0D, 0D, 120D, this.pyramid)))
		.attach(this);
		// #endregion
	}

}