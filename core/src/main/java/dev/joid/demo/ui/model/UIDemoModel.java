package dev.joid.demo.ui.model;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.obj.ObjModel;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.model.ModelNode;
import dev.joid.lib.ui.node.impl.design.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoModel extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).freeze();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).freeze();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoModel.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22, Color.WHITE);
		final ObjModel model = ObjModel.load("demo", "assets/demo/models/model.obj", Resource.of("assets/demo/models/texture.png"));
		final TweenAnimator spin = TweenAnimator.create(0F).sequence(4000F, 1F);
		spin.getTimeline().repeat(-1, 0F);
		spin.start();
		final DoubleSignal zoom = new DoubleSignal(1D);
		final DoubleSignal yaw = new DoubleSignal(0D);
		final IntegerSignal refusedTurns = new IntegerSignal(0);

		RectNode
		.create(100, 40, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Model", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 40, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).zoom(0.5D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Smaller", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 40, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).rotationYaw(45D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Yaw", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 40, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).rotationPitch(30D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Pitch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 380, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Drag and zoom", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 380, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).rotationYawRange(-45D, 45D).rotationPitchRange(0D, 0D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Yaw range", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 380, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).zoomRange(0.3D, 1D).zoom(0.8D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Zoomed", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 380, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).rotationYaw(() -> spin.get() * 360D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Spin", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 720, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(20, 20, 200, 200).zoom(zoom).rotationYaw(yaw).model(model).attach(rect);
			TextNode.create(240, 50).text(() -> Text.create("Yaw: " + Math.round(yaw.get()), info)).attach(rect);
			TextNode.create(240, 90).text(() -> Text.create("Zoom: " + Math.round(zoom.get() * 100D) + " %", info)).attach(rect);
			RectNode
			.create(240, 150, 120, 50)
			.color(UIDemoModel.INK)
			.onClick(event -> yaw.set(0D))
			.body(container -> {
				TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Face", label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
			})
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Bound", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 720, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode
			.create(20, 20, 200, 200)
			.beforeRotate(event -> {
				refusedTurns.increment();
				event.cancel();
			})
			.model(model)
			.attach(rect);
			TextNode.create(240, 90).text(Text.create("Refused: " + refusedTurns.get(), info)).attach(rect);
			TextNode.create(200, 275).text(Text.create("Refused turn", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}