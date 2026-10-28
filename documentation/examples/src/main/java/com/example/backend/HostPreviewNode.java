package com.example.backend;

import com.example.backend.engine.Engine;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.ui.node.Node;

public class HostPreviewNode extends Node {

	private final Engine engine;

	protected HostPreviewNode(final Engine engine, final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		this.engine = engine;
	}

	public static HostPreviewNode create(final Engine engine, final double x, final double y, final double width, final double height) {
		return new HostPreviewNode(engine, x, y, width, height);
	}

	// #region draw
	@Override
	public void draw(final double mouseX, final double mouseY) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		final int x = (int) Math.round(grid.toScreenX(super.getX()));
		final int y = (int) Math.round(grid.toScreenY(super.getY() + super.getHeight()));
		final int width = grid.toPixelWidth(super.getWidth());
		final int height = grid.toPixelHeight(super.getHeight());
		render.suspend(() -> this.engine.drawPreview(x, y, width, height));
	}
	// #endregion

}