package com.example.interactivity.controls;

import dev.joid.lib.color.Color;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.control.slider.IntegerSliderNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode.ProgressDirection;
import dev.joid.lib.ui.node.impl.shape.RectNode;

public class ProgressUI extends UI {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private final IntegerSignal loaded = new IntegerSignal(6);

	@Override
	public void init() {
		RectNode
		.create(660, 390, 600, 300)
		.color(ProgressUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ProgressUI.ELEMENT, 1.5F))
		.body(card -> {
			new IntegerSliderNode(60, 40, 480, 40) {}
			.range(0, 10)
			.track(RectNode.create(0, 0, 0, 0).color(ProgressUI.ELEMENT))
			.thumb(RectNode.create(0, 0, 0, 0).color(ProgressUI.ACCENT))
			.value(this.loaded)
			.attach(card);
			// #region progress
			ProgressNode
			.create(60, 130, 480, 30)
			.track(RectNode.create(0, 0, 0, 0).color(ProgressUI.ELEMENT))
			.fill(RectNode.create(0, 0, 0, 0).color(ProgressUI.ACCENT))
			.progress(this.loaded.get() / 10F)
			.attach(card);
			ProgressNode
			.create(60, 200, 480, 30)
			.track(RectNode.create(0, 0, 0, 0).color(ProgressUI.ELEMENT))
			.fill(RectNode.create(0, 0, 0, 0).color(ProgressUI.ACCENT))
			.direction(ProgressDirection.RIGHT_TO_LEFT)
			.progress(this.loaded.get() / 10F)
			.attach(card);
			// #endregion
		})
		.attach(this);
	}

}