package com.example.state;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class LoaderUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	private final Signal<String> status = new Signal<>("Not loaded");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(LoaderUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		RectNode
		.create(560, 390, 800, 300)
		.color(Color.WHITE)
		.body(card -> {
			// #region load
			RectNode
			.create(60, 60, 260, 60)
			.color(LoaderUI.INK)
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
		try {
			TimeUnit.SECONDS.sleep(1L);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
		return "Loaded";
	}
	// #endregion

}