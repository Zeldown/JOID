package dev.joid.demo.ui.model;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.obj.OBJModel;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.impl.design.model.ModelNode;
import dev.joid.lib.ui.node.impl.design.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class UIDemoModel extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153);
	private static final Color PLACEHOLDER = new Color(221, 221, 221);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoModel.INK);
		final OBJModel model = OBJModel.load("demo", JOID.class.getResourceAsStream("/assets/demo/models/model.obj"), Resource.of(JOID.class.getResourceAsStream("/assets/demo/models/texture.png")));
		final TweenAnimator spin = TweenAnimator.create(0F).sequence(4000F, 1F);
		spin.getTimeline().repeat(-1, 0F);
		spin.start();

		RectNode
		.create(100, 210, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Model", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 210, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).size(0.5D).attach(rect);
			TextNode.create(200, 275).text(Text.create("Smaller", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 210, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).rotationYaw(45D).attach(rect);
			TextNode.create(200, 275).text(Text.create("Yaw", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 210, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).rotationPitch(30D).attach(rect);
			TextNode.create(200, 275).text(Text.create("Pitch", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 550, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Drag and zoom", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 550, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).rotationYawRange(-45D, 45D).rotationPitchRange(0D, 0D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Yaw range", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 550, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelViewerNode.create(100, 20, 200, 200).sizeRange(0.3D, 1D).zoom(0.8D).model(model).attach(rect);
			TextNode.create(200, 275).text(Text.create("Zoomed", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 550, 400, 260)
		.color(UIDemoModel.PLACEHOLDER)
		.body(rect -> {
			ModelNode.create(100, 20, 200, 200).model(model).rotationYaw(() -> spin.getValue() * 360D).animate(spin).attach(rect);
			TextNode.create(200, 275).text(Text.create("Spin", info, Align.CENTER)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}