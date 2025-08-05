package be.zeldown.joid.lib.ui.bridge;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.list.IndexedLinkedList;
import lombok.NonNull;

public abstract class UIBridge implements IUIBridge {

	@NonNull private final IndexedLinkedList<@NonNull UI> uiList;

	public UIBridge() {
		this.uiList = new IndexedLinkedList<>();
	}

	public final void load() {
		final double width = Display.getWidth();
		final double height = Display.getHeight();
		this.uiList.forEach(ui -> ui.load(width, height));
	}

	public final void mousePressed(final @NonNull ClickType clickType) {
		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (ui.getData().active() && ui.getData().visible() && ui.onMousePressed(clickType) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseDragged(final @NonNull ClickType clickType, final long delaTime) {
		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (ui.getData().active() && ui.getData().visible() && ui.onMouseDragged(clickType, delaTime) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseReleased(final @NonNull ClickType clickType) {
		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (ui.getData().active() && ui.getData().visible() && ui.onMouseReleased(clickType) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void mouseScroll(final int value) {
		if (value == 0) {
			return;
		}

		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (ui.getData().active() && ui.getData().visible() && ui.onMouseScroll(value) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void keyTyped(final char c, final int keyCode) {
		final List<UI> uiList = this.uiList.reversed();
		for (int i = 0; i < uiList.size(); i++) {
			final UI ui = uiList.get(i);
			if (!ui.getData().active() || !ui.getData().visible()) {
				continue;
			}

			if (keyCode == Keyboard.KEY_ESCAPE && ui.onClose()) {
				this.close(ui);
				return;
			}

			if (ui.onKeyPressed(c, keyCode) || ui.getPopup().active()) {
				break;
			}
		}
	}

	public final void update() {
		this.uiList.forEach(UI::onUpdate);
	}

	public final void draw() {
		try {
			if (this.uiList.isEmpty()) {
				return;
			}

			final Iterator<UI> iterator = this.uiList.iterator();

			double renderPipeline = 0D;
			GL11.glPushMatrix();
			GL11.glTranslated(0D, 0D, -2000D);
			while (iterator.hasNext()) {
				final UI ui = iterator.next();
				if (!ui.getData().visible()) {
					continue;
				}

				renderPipeline += ui.getData().zlevel();
				GL11.glTranslated(0D, 0D, renderPipeline);
				ui.draw(Mouse.getX(), Mouse.getY());
				renderPipeline = ui.getRenderPipelineLevel() + 10D;
			}
			GL11.glTranslated(0D, 0D, -renderPipeline);
			GL11.glPopMatrix();
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