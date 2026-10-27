package com.example.running;

import java.util.concurrent.CompletableFuture;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.thread.RenderThreadFuture;
import dev.joid.lib.utils.thread.ThreadUtils;

public class ThreadsUI extends UI {

	private final StringSignal status = new StringSignal("Loading...");

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(Color.decode("#999999"));

		final TextNode label = TextNode.create(0, 150, 400, 150).text(Text.create("...", info));

		RectNode
		.create(760, 390, 400, 300)
		.color(Color.decode("#DDDDDD"))
		.body(card -> {
			TextNode.create(0, 0, 400, 150).text(Text.create(this.status::get, info).align(Align.CENTER)).attach(card);
			label.attach(card);
		})
		.attach(this);

		// #region signal
		CompletableFuture
		.supplyAsync(ThreadsUI::download)
		.thenAccept(this.status::set);
		// #endregion

		// #region wrong
		CompletableFuture
		.supplyAsync(ThreadsUI::download)
		.thenAccept(result -> label.text(Text.create(result, info))); // ✗ changes a node on the worker thread
		// #endregion

		// #region render-thread
		CompletableFuture
		.supplyAsync(ThreadsUI::download)
		.thenAccept(result -> ThreadUtils.runOnRenderThread(() -> label.text(Text.create(result, info)))); // ✓
		// #endregion

		// #region future
		RenderThreadFuture
		.from(CompletableFuture.supplyAsync(ThreadsUI::download))
		.thenAccept(result -> label.text(Text.create(result, info)));
		// #endregion
	}

	private static String download() {
		return "Ready";
	}

}