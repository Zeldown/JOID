package com.example.interactivity.controls;

import com.example.kit.KitIntegerSliderNode;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ProgressUI extends UI {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	private final IntegerSignal loaded = new IntegerSignal(6);

	@Override
	public void init() {
		RectNode
		.create(660, 390, 600, 300)
		.color(ProgressUI.PLACEHOLDER)
		.body(card -> {
			KitIntegerSliderNode.create(60, 40, 480, 40).range(0, 10).value(this.loaded).attach(card);
			// #region progress
			ProgressNode
			.create(60, 130, 480, 30)
			.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE))
			.fill(RectNode.create(0, 0, 0, 0).color(ProgressUI.INK))
			.progress(this.loaded.get() / 10F)
			.attach(card);
			ProgressNode
			.create(60, 200, 480, 30)
			.track(RectNode.create(0, 0, 0, 0).color(Color.WHITE))
			.fill(RectNode.create(0, 0, 0, 0).color(ProgressUI.INK))
			.direction(ProgressDirection.RIGHT_TO_LEFT)
			.progress(this.loaded.get() / 10F)
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}