package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.joid.internal.JOID;
import dev.joid.internal.font.InternalFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.converter.TextConverter;
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlayObject;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.cursor.Cursor;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.list.IndexedLinkedList;
import lombok.Getter;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class UIBridge implements IUIBridge {

	private static Cursor        windowCursor;
	private static IWindowBridge cursorWindow;

	@NonNull
	private final IndexedLinkedList<@NonNull UI> uiList;

	private long      pressTime;
	private ClickType pressed;

	private Node   pressedNode;
	private Cursor hoveredCursor;

	@Getter private TextInfo hoverInfo;
	@Getter private Color    hoverColor;
	@Getter private Color    hoverBorderColor;

	private boolean hoverWarned;

	public UIBridge() {
		this.uiList           = new IndexedLinkedList<>();
		this.hoverColor       = Color.decode("#18181b");
		this.hoverBorderColor = Color.decode("#27272a");
	}

	public final void load() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final double width = window.getWidth();
		final double height = window.getHeight();
		this.uiList.forEach(ui -> ui.load(width, height, ui.getView().getZoom()));
	}

	public final boolean mousePressed(final @NonNull ClickType clickType) {
		this.pressed     = clickType;
		this.pressTime   = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.pressedNode = this.getHoveredNode();
		for (final UI ui : this.getInputList()) {
			if (ui.onMousePressed(clickType) || ui.getPopup().active()) {
				return UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick());
			}
		}
		return false;
	}

	public final boolean mouseMoved() {
		if (this.pressed == null) {
			return false;
		}

		final ClickType clickType = this.pressed;
		final long deltaTime = BridgeHandler.CLOCK.get().currentTimeMillis() - this.pressTime;
		for (final UI ui : this.getInputList()) {
			if (ui.onMouseDragged(clickType, deltaTime) || ui.getPopup().active()) {
				return UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick());
			}
		}
		return false;
	}

	public final boolean mouseReleased(final @NonNull ClickType clickType) {
		if (this.pressed == clickType) {
			this.pressed     = null;
			this.pressedNode = null;
		}

		for (final UI ui : this.getInputList()) {
			if (ui.onMouseReleased(clickType) || ui.getPopup().active()) {
				return UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick());
			}
		}
		return false;
	}

	public final boolean mouseScroll(final double notchesX, final double notchesY) {
		if (notchesX == 0D && notchesY == 0D) {
			return false;
		}

		for (final UI ui : this.getInputList()) {
			if (ui.onMouseScroll(notchesX, notchesY) || ui.getPopup().active()) {
				return UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelScroll());
			}
		}
		return false;
	}

	public final boolean keyTyped(final char c, final @NonNull Key key) {
		final char typed = Character.isISOControl(c) ? (char) 0 : c;
		for (final UI ui : this.getInputList()) {
			if (key == Key.ESCAPE && ui.getData().closeable() && !ui.getOverlay().active()) {
				if (!ui.onKeyPressed(typed, key) && ui.onClose()) {
					this.close(ui);
				}
				return true;
			}

			if (ui.onKeyPressed(typed, key) || ui.getPopup().active()) {
				return UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelKeyboard());
			}
		}
		return false;
	}

	public final void update() {
		for (final UI ui : new ArrayList<>(this.uiList.ordered())) {
			ui.onUpdate();
		}
	}

	public final void draw() {
		try {
			ResourceData.releaseCollected();
			ShaderPipeline.releaseUnused();
			this.drawLayers();
			this.updateCursor();
		} catch (final Exception throwable) {
			throwable.printStackTrace();
		}
	}

	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
		final List<String> lines = TextConverter.convertLines(content);
		if (lines.isEmpty()) {
			return;
		}

		final TextInfo info = this.hoverInfo != null ? this.hoverInfo : UIBridge.getDefaultHoverInfo();
		if (info == null) {
			if (!this.hoverWarned) {
				this.hoverWarned = true;
				System.err.println("[JOID] " + this.getClass().getSimpleName() + " has no text info for its tooltips, set one with hoverInfo(TextInfo)");
			}
			return;
		}

		final double paddingX = 10D;
		final double paddingY = 6D;
		final double lineGap = 2D;
		final double lineHeight = info.getHeight();

		double width = 0D;
		for (final String line : lines) {
			width = Math.max(width, info.getWidth(line));
		}
		width += paddingX * 2D;
		final double height = paddingY * 2D + lines.size() * lineHeight + Math.max(0, lines.size() - 1) * lineGap;

		double x = mouseX + 14D;
		double y = mouseY + 14D;

		final double left = ui.getView().toUiX(0D) + 4D;
		final double top = ui.getView().toUiY(0D) + 4D;
		if (x + width > ui.getView().toUiX(ui.getWidth()) - 4D) {
			x = mouseX - width - 14D;
		}
		if (y + height > ui.getView().toUiY(ui.getHeight()) - 4D) {
			y = mouseY - height - 14D;
		}
		if (x < left) {
			x = left;
		}
		if (y < top) {
			y = top;
		}

		DrawUtils.SHAPE.drawRoundedRect(x, y, width, height, this.hoverBorderColor, 6F);
		DrawUtils.SHAPE.drawRoundedRect(x + 1D, y + 1D, width - 2D, height - 2D, this.hoverColor, 5F);

		double textY = y + paddingY;
		for (final String line : lines) {
			DrawUtils.TEXT.drawText(x + paddingX, textY, line, info, Align.START, Align.START);
			textY += lineHeight + lineGap;
		}
	}

	public final <T extends UIBridge> @NonNull T hoverInfo(final TextInfo hoverInfo) {
		this.hoverInfo = hoverInfo;
		return (T) this;
	}

	public final <T extends UIBridge> @NonNull T hoverColor(final @NonNull Color hoverColor) {
		this.hoverColor = hoverColor;
		return (T) this;
	}

	public final <T extends UIBridge> @NonNull T hoverBorderColor(final @NonNull Color hoverBorderColor) {
		this.hoverBorderColor = hoverBorderColor;
		return (T) this;
	}

	@Override
	public @NonNull IndexedLinkedList<@NonNull UI> getUiList() {
		return this.uiList;
	}

	@Override
	public boolean isOnTop(final @NonNull UI ui) {
		for (final UI current : this.getInputList()) {
			if (current.getOverlay().active() == ui.getOverlay().active()) {
				return current == ui;
			}
		}
		return false;
	}

	@Override
	public boolean isOpened(final @NonNull UI ui) {
		return this.uiList.contains(ui);
	}

	public boolean isScreenOpen() {
		return this.hasScreen();
	}

	public boolean isOverlayHidden() {
		return false;
	}

	protected final boolean hasScreen() {
		for (final UI ui : this.uiList) {
			if (!ui.getOverlay().active()) {
				return true;
			}
		}
		return false;
	}

	private boolean isShown(final UI ui) {
		final UIDataOverlayObject overlay = ui.getOverlay();
		return !overlay.active() || (overlay.render().always() || !this.isOverlayHidden()) && (overlay.render().screens() || !this.isScreenOpen());
	}

	private void drawLayers() {
		if (this.uiList.isEmpty()) {
			return;
		}

		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		double renderPipeline = 0D;
		render.pushMatrix();
		try {
			render.translate(0D, 0D, -2000D);
			for (final UI ui : this.getLayerList()) {
				if (!ui.getData().visible() || !this.isShown(ui)) {
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
	}

	private void updateCursor() {
		final boolean pressed = this.pressedNode != null && this.pressedNode.hasUi() && this.uiList.contains(this.pressedNode.getUi());
		final Node hovered = pressed ? this.pressedNode : this.getHoveredNode();
		this.hoveredCursor = hovered != null ? hovered.getResolvedCursor() : null;

		final IWindowBridge window = BridgeHandler.WINDOW.get();
		if (window != UIBridge.cursorWindow) {
			UIBridge.cursorWindow = window;
			UIBridge.windowCursor = Cursor.DEFAULT;
		}

		if (window.isMouseGrabbed()) {
			return;
		}

		final IUIBridge bridge = BridgeHandler.UI.find(current -> current instanceof UIBridge && ((UIBridge) current).hoveredCursor != null);
		final Cursor cursor = bridge != null ? ((UIBridge) bridge).hoveredCursor : Cursor.DEFAULT;
		if (cursor != UIBridge.windowCursor) {
			UIBridge.windowCursor = cursor;
			window.setCursor(cursor);
		}
	}

	private Node getHoveredNode() {
		for (final UI ui : this.getInputList()) {
			final Node hovered = ui.getHoveredNode();
			if (hovered != null || ui.getPopup().active()) {
				return hovered;
			}
		}
		return null;
	}

	private List<UI> getLayerList() {
		this.uiList.sort();
		final List<UI> layerList = new ArrayList<>();
		final List<UI> overlayList = new ArrayList<>();
		for (final UI ui : this.uiList.ordered()) {
			(ui.getOverlay().active() ? overlayList : layerList).add(ui);
		}

		overlayList.sort(Comparator.comparingInt(ui -> ui.getOverlay().render().zindex()));
		layerList.addAll(overlayList);
		return layerList;
	}

	private List<UI> getInputList() {
		final List<UI> inputList = new ArrayList<>();
		final List<UI> layerList = this.getLayerList();
		for (int i = layerList.size() - 1; i >= 0; i--) {
			final UI ui = layerList.get(i);
			if (ui.getData().active() && ui.getData().visible() && this.isShown(ui) && (!ui.getOverlay().active() || ui.getOverlay().interaction().active())) {
				inputList.add(ui);
			}
		}
		return inputList;
	}

	private static TextInfo getDefaultHoverInfo() {
		final JOID joid = JOID.inst();
		return (joid.isDevMode() || joid.isDemoMode()) && InternalFont.MONTSERRAT != null ? TextInfo.create(InternalFont.MONTSERRAT, 20, Color.WHITE) : null;
	}

	private static boolean isConsumed(final UI ui, final boolean cancel) {
		return !ui.getOverlay().active() || cancel;
	}

}