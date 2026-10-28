package com.example.further;

import java.util.function.Supplier;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.BlendState.Equation;
import dev.joid.lib.bridge.render.state.BlendState.Factor;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.render.transform.Rotation;
import dev.joid.lib.render.transform.Transformation;
import dev.joid.lib.render.transform.Vector;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;

public class TransformsUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	// #region multiply
	private static final BlendState MULTIPLY = BlendState.of(Equation.ADD, Factor.DST_COLOR, Factor.ZERO);
	// #endregion

	// #region image
	private final Resource image = Resource.of("images/placeholder.png");
	// #endregion

	// #region spin
	private static final Supplier<Double> ANGLE = () -> BridgeHandler.CLOCK.get().currentTimeMillis() % 4000L * 0.09D;

	private final Transformation spin = Transformation
	.create()
	.rotate(TransformsUI.ANGLE, Rotation.ROLL, Vector.create(60D, 60D));
	// #endregion

	@Override
	public void init() {
		// #region resource
		ContainerNode
		.create(80, 80, 1000, 200)
		.layer((node, mouseX, mouseY) -> {
			DrawUtils.RESOURCE.drawResource(0D, 0D, 320D, 180D, this.image);
			DrawUtils.RESOURCE.drawResource(360D, 50D, 280D, 80D, this.image);
			DrawUtils.RESOURCE.drawResource(680D, 50D, 80D, 80D, 120D, 50D, 80D, 80D, this.image);
			final BoundingBox tint = BoundingBox.create(800D, 0D, 200D, 112D);
			TransformsUI.ACCENT.bind(() -> DrawUtils.RESOURCE.drawResource(800D, 0D, 200D, 112D, this.image), tint, true);
		})
		.attach(this);
		// #endregion

		// #region pivot
		ContainerNode
		.create(1160, 80, 600, 240)
		.layer((node, mouseX, mouseY) -> {
			Transformation
			.create()
			.rotate(20D, Rotation.ROLL, Vector.create(150D, 120D))
			.apply(() -> DrawUtils.SHAPE.drawRect(80D, 80D, 140D, 80D, TransformsUI.ACCENT));

			Transformation
			.create()
			.rotate(20D, Rotation.ROLL, Vector.create(380D, 80D))
			.apply(() -> DrawUtils.SHAPE.drawRect(380D, 80D, 140D, 80D, TransformsUI.ACCENT));

			DrawUtils.SHAPE.drawBorder(80D, 80D, 140D, 80D, TransformsUI.MUTED, 2D);
			DrawUtils.SHAPE.drawBorder(380D, 80D, 140D, 80D, TransformsUI.MUTED, 2D);
			DrawUtils.SHAPE.drawCircle(150D, 120D, TransformsUI.TEXT, 7D);
			DrawUtils.SHAPE.drawCircle(380D, 80D, TransformsUI.TEXT, 7D);
		})
		.attach(this);
		// #endregion

		// #region spin-draw
		ContainerNode
		.create(1640, 840, 120, 120)
		.layer((node, mouseX, mouseY) -> {
			this.spin.apply(() -> DrawUtils.SHAPE.drawRect(20D, 20D, 80D, 80D, TransformsUI.ACCENT));
		})
		.attach(this);
		// #endregion

		// #region matrix
		ContainerNode
		.create(230, 500, 200, 100)
		.layer((node, mouseX, mouseY) -> {
			BridgeHandler.RENDER.get().getModelView().with(matrix -> {
				matrix.translate(node.dw(2D), node.dh(2D), 0D);
				matrix.rotate(-10D, 0D, 0D, 1D);
				matrix.scale(1.5D, 1.5D, 1D);
				DrawUtils.SHAPE.drawRect(-node.dw(2D), -node.dh(2D), node.getWidth(), node.getHeight(), TransformsUI.ELEMENT);
			});
			DrawUtils.SHAPE.drawBorder(0D, 0D, node.getWidth(), node.getHeight(), TransformsUI.ACCENT, 2D);
		})
		.attach(this);
		// #endregion

		// #region state
		ContainerNode
		.create(700, 400, 500, 300)
		.layer((node, mouseX, mouseY) -> {
			BridgeHandler.RENDER.get().withState(render -> {
				render.getState().lineSmooth(true).lineWidth(6F);
				final Point[] points = {Point.of(150D, 60D), Point.of(350D, 60D), Point.of(250D, 240D)};
				DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, TransformsUI.ACCENT, points);
			});
		})
		.attach(this);
		// #endregion

		RectNode.create(1300, 400, 500, 300).color(TransformsUI.TEXT).attach(this);

		// #region blend
		ContainerNode
		.create(1300, 400, 500, 300)
		.layer((node, mouseX, mouseY) -> {
			final Tessellator tessellator = Tessellator.inst();
			BridgeHandler.RENDER.get().withState(render -> {
				render.getState().blend(TransformsUI.MULTIPLY).texture(null).color(0.6F, 0.6F, 0.6F, 1F);
				tessellator.start(DrawMode.QUADS);
				for (final double offset : new double[] {0D, 80D}) {
					tessellator.addVertex(130D + offset, 200D + offset / 2D, 0D);
					tessellator.addVertex(290D + offset, 200D + offset / 2D, 0D);
					tessellator.addVertex(290D + offset, 60D + offset / 2D, 0D);
					tessellator.addVertex(130D + offset, 60D + offset / 2D, 0D);
				}
				tessellator.draw();
			});
		})
		.attach(this);
		// #endregion
	}

}