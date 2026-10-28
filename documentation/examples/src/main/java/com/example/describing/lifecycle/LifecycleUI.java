package com.example.describing.lifecycle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.example.ExampleFont;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.thread.ThreadUtils;

public class LifecycleUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(ThreadUtils.daemonFactory("poll"));

	private long               start;
	private double             open;
	private volatile long      checks;
	private ScheduledFuture<?> poll;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 32F).color(LifecycleUI.TEXT);

		// #region timer
		RectNode
		.create(660, 290, 600, 160)
		.color(LifecycleUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LifecycleUI.ELEMENT, 1.5F))
		.onInit(event -> this.start = BridgeHandler.CLOCK.get().currentTimeMillis())
		.onUpdate(event -> this.open = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 1000D)
		.body(card -> TextNode
		.create(300, 80)
		.text(Text.create(() -> String.format("%.1f s", this.open), info))
		.anchor(Align.CENTER)
		.attach(card))
		.attach(this);
		// #endregion

		// #region draw
		RectNode
		.create(760, 480, 400, 120)
		.color(LifecycleUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LifecycleUI.ELEMENT, 1.5F))
		.body(card -> TextNode
		.create(200, 60)
		.text(Text.create("Underlined", info))
		.anchor(Align.CENTER)
		.onDraw(event -> {
			final Node node = event.getNode();
			final double bottom = node.getY() + node.getHeight();
			DrawUtils.SHAPE.drawRect(node.getX(), bottom + 4D, node.getWidth(), 4D, LifecycleUI.ACCENT);
		})
		.attach(card))
		.attach(this);
		// #endregion

		// #region release
		RectNode
		.create(660, 630, 600, 160)
		.color(LifecycleUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LifecycleUI.ELEMENT, 1.5F))
		.onInit(event -> this.poll = LifecycleUI.EXECUTOR.scheduleAtFixedRate(() -> this.checks++, 0L, 5L, TimeUnit.SECONDS))
		.onDetach(event -> this.poll.cancel(false))
		.body(card -> TextNode
		.create(300, 80)
		.text(Text.create(() -> this.checks + " checks", info))
		.anchor(Align.CENTER)
		.attach(card))
		.attach(this);
		// #endregion
	}

}