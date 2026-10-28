package com.example.state;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.example.ExampleFont;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class LoaderUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ELEMENT   = new Color(38, 43, 53).lock();
	private static final Color SURFACE   = new Color(23, 26, 33).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	private final Signal<String> status = new Signal<>("Not loaded");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(LoaderUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(LoaderUI.ON_ACCENT);

		RectNode
		.create(560, 390, 800, 300)
		.color(LoaderUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(LoaderUI.ELEMENT, 1.5F))
		.body(card -> {
			// #region load
			RectNode
			.create(60, 60, 260, 60)
			.color(LoaderUI.ACCENT)
			.onClick(event -> {
				this.status.set("Loading...");
				CompletableFuture.supplyAsync(this::fetch).thenAccept(this.status::set);
			})
			.label(Text.create("Load", label))
			.attach(card);
			// #endregion
			TextNode.create(60, 180).text(Text.create(this.status, info)).attach(card);
		})
		.attach(this);
	}

	// #region fetch
	private String fetch() {
		final long end = BridgeHandler.CLOCK.get().currentTimeMillis() + 1000L;
		try {
			while (BridgeHandler.CLOCK.get().currentTimeMillis() < end) {
				TimeUnit.MILLISECONDS.sleep(10L);
			}
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
		return "Loaded";
	}
	// #endregion

}