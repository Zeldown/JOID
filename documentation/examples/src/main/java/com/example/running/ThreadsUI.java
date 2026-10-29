package com.example.running;

import java.util.concurrent.CompletableFuture;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.effect.impl.BorderNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.thread.RenderThreadFuture;
import dev.joid.lib.utils.thread.ThreadUtils;

public class ThreadsUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color SURFACE = new Color(23, 26, 33).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(ThreadsUI.TEXT);

		final TextNode label = TextNode.create(0, 150, 400, 150).text(Text.create("...", info));

		RectNode
		.create(760, 390, 400, 300)
		.color(ThreadsUI.SURFACE)
		.effect(RoundedNodeEffect.create(12F))
		.effect(BorderNodeEffect.create(ThreadsUI.ELEMENT, 1.5F))
		.body(card -> label.attach(card))
		.attach(this);

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