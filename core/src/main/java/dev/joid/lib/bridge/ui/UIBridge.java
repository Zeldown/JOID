package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public abstract class UIBridge implements IUIBridge {

	@NonNull
	private final IndexedLinkedList<@NonNull UI> uiList;

	public UIBridge() {
		this.uiList = new IndexedLinkedList<>();
	}

	public final void load() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final double width = window.getWidth();
		final double height = window.getHeight();
		this.uiList.forEach(ui -> ui.load(width, height));
	}

	public final void mousePressed(final @NonNull ClickType clickType) {
		for (final UI ui : new ArrayList<>(this.uiList.reversed())) {
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (ui.onMousePressed(clickType) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseDragged(final @NonNull ClickType clickType, final long delaTime) {
		for (final UI ui : new ArrayList<>(this.uiList.reversed())) {
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (ui.onMouseDragged(clickType, delaTime) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseReleased(final @NonNull ClickType clickType) {
		for (final UI ui : new ArrayList<>(this.uiList.reversed())) {
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (ui.onMouseReleased(clickType) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseScroll(final int value) {
		if (value == 0) {
			return;
		}

		for (final UI ui : new ArrayList<>(this.uiList.reversed())) {
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (ui.onMouseScroll(value) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void keyTyped(final char c, final @NonNull Key key) {
		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (key == Key.ESCAPE && ui.getData().closeable() && ui.onClose()) {
				this.close(ui);
				return;
			}

			if (ui.onKeyPressed(c, key) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void update() {
		for (final UI ui : new ArrayList<>(this.uiList.ordered())) {
			ui.onUpdate();
		}
	}

	public final void draw() {
		try {
			if (this.uiList.isEmpty()) {
				return;
			}

			final IWindowBridge window = BridgeHandler.WINDOW.get();
			final IRenderBridge render = BridgeHandler.RENDER.get();

			double renderPipeline = 0D;
			render.pushMatrix();
			try {
				render.translate(0D, 0D, -2000D);
				for (final UI ui : new ArrayList<>(this.uiList.ordered())) {
					if (!ui.getData().visible()) {
						continue;
					}

					renderPipeline += ui.getData().zlevel();
					render.translate(0D, 0D, renderPipeline);
					ui.draw(window.getMouseX(), window.getMouseY());
					renderPipeline = ui.getRenderPipelineLevel() + 10D;
				}
				render.translate(0D, 0D, -renderPipeline);
			} finally {
				render.popMatrix();
			}
		} catch (final Exception throwable) {
			throwable.printStackTrace();
		}
	}

	@Override
	public @NonNull IndexedLinkedList<@NonNull UI> getUiList() {
		return this.uiList;
	}

	@Override
	public boolean isOpened(final @NonNull UI ui) {
		return this.uiList.contains(ui);
	}

}