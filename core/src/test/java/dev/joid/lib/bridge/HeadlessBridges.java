package dev.joid.lib.bridge;

import java.util.ArrayList;

import org.junit.rules.ExternalResource;

import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.clock.ManualClockBridge;
import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.window.RecordingWindowBridge;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class HeadlessBridges extends ExternalResource {

	private final DemoUIBridge          ui     = new DemoUIBridge();
	private final ManualClockBridge     clock  = ManualClockBridge.create(0L);
	private final RecordingWindowBridge window = new RecordingWindowBridge();
	private final RecordingRenderBridge render = new RecordingRenderBridge();

	@Override
	protected void before() {
		BridgeHandler.UI.register(this.ui);
		BridgeHandler.CLOCK.register(this.clock);
		BridgeHandler.WINDOW.register(this.window);
		BridgeHandler.RENDER.register(this.render);
		this.resize(1920, 1080);
	}

	@Override
	protected void after() {
		for (final UI opened : new ArrayList<>(this.ui.getUiList().ordered())) {
			opened.properlyClose();
			this.ui.getUiList().remove(opened);
		}

		BridgeHandler.UI.unregister(this.ui);
		BridgeHandler.CLOCK.unregister(this.clock);
		BridgeHandler.WINDOW.unregister(this.window);
		BridgeHandler.RENDER.unregister(this.render);
	}

	public @NonNull HeadlessBridges resize(final int width, final int height) {
		this.window.setWidth(width);
		this.window.setHeight(height);
		this.render.ortho(0D, width, height, 0D, 0D, 10000D);
		this.render.viewport(0, 0, width, height);
		this.ui.load();
		return this;
	}

	public @NonNull HeadlessBridges open(final @NonNull UI opened) {
		this.ui.add(opened);
		return this.frame();
	}

	public @NonNull HeadlessBridges move(final double mouseX, final double mouseY) {
		this.window.setMouseX(mouseX);
		this.window.setMouseY(mouseY);
		return this;
	}

	public @NonNull HeadlessBridges scroll(final double notches) {
		return this.scroll(0D, notches);
	}

	public @NonNull HeadlessBridges scroll(final double notchesX, final double notchesY) {
		this.ui.mouseScroll(notchesX, notchesY);
		return this;
	}

	public @NonNull HeadlessBridges frame() {
		this.clock.advance(16L);
		this.render.getDraws().clear();
		this.ui.update();
		this.ui.draw();
		return this;
	}

	public @NonNull HeadlessBridges frames(final int count) {
		for (int i = 0; i < count; i++) {
			this.frame();
		}
		return this;
	}

}