package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.obj.ObjModel;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.model.ModelNode;
import dev.joid.lib.ui.node.impl.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ModelsUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	// #region load
	private static final ObjModel CUBE = ObjModel.of("cube", "models/box.obj", Resource.of("models/box.png"));
	// #endregion

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 22F).color(ModelsUI.ON_ACCENT);
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 22F).color(ModelsUI.TEXT);
		// #region signal
		final DoubleSignal yaw = new DoubleSignal(30D);
		// #endregion

		// #region node
		ModelNode.create(100, 100, 200, 200).rotationYaw(yaw).rotationPitch(20D).model(ModelsUI.CUBE).attach(this);
		// #endregion

		// #region turn
		RectNode
		.create(100, 340, 200, 50)
		.color(ModelsUI.ACCENT)
		.label(Text.create("Turn", label))
		.onClick(event -> yaw.set(yaw.peek() + 45D))
		.attach(this);
		// #endregion

		// #region viewer
		final ModelViewerNode viewer = ModelViewerNode
		.create(400, 100, 200, 200)
		.rotationPitch(20D)
		.rotationPitchRange(-45D, 45D)
		.zoomRange(0.5D, 1.5D)
		.model(ModelsUI.CUBE)
		.attach(this);
		// #endregion

		// #region read
		TextNode.create(400, 340).text(() -> Text.create("Yaw " + Math.round(viewer.getRotationYaw()), info)).attach(this);
		// #endregion
	}

}