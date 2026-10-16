package dev.joid.lib.bridge.ui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.render.framebuffer.FrameBufferPool;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataOverlayObject;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.list.IndexedList;
import lombok.NonNull;

public abstract class UIBridge implements IUIBridge {

	private static Cursor        windowCursor;
	private static IWindowBridge cursorWindow;

	@NonNull
	private final IndexedList<@NonNull UI> uiList;

	private final Set<Key>         consumedKeySet;
	private final Set<MouseButton> consumedButtonSet;

	private long        pressTime;
	private MouseButton pressed;

	private Node   pressedNode;
	private Cursor hoveredCursor;

	protected UIBridge() {
		this.uiList = IndexedList.concurrent();
		this.consumedKeySet = EnumSet.noneOf(Key.class);
		this.consumedButtonSet = EnumSet.noneOf(MouseButton.class);
	}

	public final void load() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		this.uiList.forEach(ui -> ui.load(window.getWidth(), window.getHeight()));
	}

	public final boolean mouseMoved() {
		if (this.pressed == null) {
			return false;
		}

		final MouseButton button = this.pressed;
		final long deltaTime = BridgeHandler.CLOCK.get().currentTimeMillis() - this.pressTime;
		final UI ui = this.dispatch(current -> current.fireMouseDragged(button, deltaTime));
		return ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick());
	}

	public final boolean mousePressed(final @NonNull MouseButton button) {
		this.pressed     = button;
		this.pressTime   = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.pressedNode = this.getHoveredNode();
		this.consumedButtonSet.remove(button);
		final UI ui = this.dispatch(current -> current.fireMousePressed(button));
		final boolean consumed = ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick());
		if (consumed) {
			this.consumedButtonSet.add(button);
		}
		return consumed;
	}

	public final boolean mouseReleased(final @NonNull MouseButton button) {
		final boolean consumed = this.consumedButtonSet.remove(button);
		if (this.pressed == button) {
			this.pressed     = null;
			this.pressedNode = null;
		}

		final UI ui = this.dispatch(current -> current.fireMouseReleased(button));
		return ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelClick()) || consumed;
	}

	public final boolean mouseScroll(final double notchesX, final double notchesY) {
		if (notchesX == 0D && notchesY == 0D) {
			return false;
		}

		final UI ui = this.dispatch(current -> current.fireMouseScroll(notchesX, notchesY));
		return ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelScroll());
	}

	public final boolean charTyped(final int codepoint) {
		if (Character.isISOControl(codepoint)) {
			return false;
		}

		final UI ui = this.dispatch(current -> current.fireCharTyped(codepoint));
		return ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelKeyboard());
	}

	public final boolean keyPressed(final @NonNull Key physicalKey, final @NonNull Key key) {
		this.consumedKeySet.remove(key);
		final UI ui = this.dispatch(current -> UIBridge.isClosedBy(current, key) || current.fireKeyPressed(physicalKey, key));
		if (ui != null && UIBridge.isClosedBy(ui, key)) {
			this.consumedKeySet.add(key);
			if (!ui.fireKeyPressed(physicalKey, key) && ui.fireClose()) {
				this.close(ui);
			}
			return true;
		}

		final boolean consumed = ui != null && UIBridge.isConsumed(ui, ui.getOverlay().interaction().cancelKeyboard());
		if (consumed) {
			this.consumedKeySet.add(key);
		}
		return consumed;
	}

	public final boolean keyReleased(final @NonNull Key physicalKey, final @NonNull Key key) {
		this.dispatch(current -> current.fireKeyReleased(physicalKey, key));
		return this.consumedKeySet.remove(key);
	}

	public final boolean isKeyConsumed(final @NonNull Key key) {
		return this.consumedKeySet.contains(key);
	}

	public final void update() {
		for (final UI ui : new ArrayList<>(this.uiList.ordered())) {
			ui.fireUpdate();
		}
	}

	public final void draw() {
		this.draw(ui -> true);
	}

	public final void draw(final @NonNull Predicate<@NonNull UI> filter) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (!render.isFrameActive()) {
			throw new IllegalStateException(this.getClass().getSimpleName() + ".draw() needs an open frame and the render bridge has none: call render.beginFrame() before and render.endFrame() after, or use BridgeHandler.UI.frame()");
		}

		final int stateDepth = render.getStateDepth();
		final int modelViewDepth = render.getModelView().getDepth();
		final int projectionDepth = render.getProjection().getDepth();
		try {
			ResourceData.releaseCollected();
			FrameBufferPool.releaseUnused();
			this.drawLayers(filter);
			this.updateCursor();
		} catch (final Exception exception) {
			Tessellator.inst().abort();
			render.getModelView().pop(modelViewDepth);
			render.getProjection().pop(projectionDepth);
			while (render.getStateDepth() > stateDepth) {
				render.popState();
			}

			JOIDLog.warnOnce("UIBridge.draw." + System.identityHashCode(this) + "." + exception.getClass().getName(), this.getClass().getSimpleName() + " cannot draw a frame and skips the rest of it (" + exception + "), fix the draw code from this error", exception);
		}
	}

	@Override
	public void drawHover(final @NonNull UI ui, final @NonNull Object content, final double mouseX, final double mouseY) {
		JOIDLog.devWarnOnce("UIBridge.drawHover." + System.identityHashCode(this), () -> this.getClass().getSimpleName() + " draws no tooltip, override drawHover(UI, Object, double, double) to draw them");
	}

	@Override
	public void add(final @NonNull UI ui) {
		this.uiList.add(ui);
	}

	@Override
	public void remove(final @NonNull UI ui) {
		this.uiList.remove(ui);
	}

	@Override
	public @NonNull List<@NonNull UI> getUiList() {
		return this.uiList.ordered();
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
	public boolean isOpen(final @NonNull UI ui) {
		return this.uiList.contains(ui);
	}

	public boolean isOverlayHidden() {
		return false;
	}

	protected void drawBackground() {}

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
		return !overlay.active() || overlay.render().always() || !this.isOverlayHidden();
	}

	private void drawLayers(final Predicate<UI> filter) {
		if (this.uiList.isEmpty()) {
			return;
		}

		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		render.getModelView().with(matrix -> {
			double depthLevel = 0D;
			matrix.translate(0D, 0D, -2000D);
			for (final UI ui : this.getLayerList()) {
				if (!ui.getData().visible() || !this.isShown(ui) || !filter.test(ui)) {
					continue;
				}

				depthLevel += ui.getData().zlevel();
				matrix.translate(0D, 0D, depthLevel);
				ui.draw(window.getMouseX(), window.getMouseY());
				depthLevel = ui.getDepthLevel() + 10D;
			}
		});
	}

	private void updateCursor() {
		final boolean pressed = this.pressedNode != null && this.pressedNode.isAttached() && this.uiList.contains(this.pressedNode.getUi());
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

		final IUIBridge bridge = BridgeHandler.UI.find(current -> current instanceof UIBridge && !((UIBridge) current).uiList.isEmpty() && ((UIBridge) current).hoveredCursor != null);
		final Cursor cursor = bridge != null ? ((UIBridge) bridge).hoveredCursor : Cursor.DEFAULT;
		if (cursor != UIBridge.windowCursor) {
			UIBridge.windowCursor = cursor;
			window.setCursor(cursor);
		}
	}

	private UI dispatch(final Predicate<UI> input) {
		for (final UI ui : this.getInputList()) {
			if (input.test(ui) || ui.getPopup().active()) {
				return ui;
			}
		}
		return null;
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
		final List<UI> belowList = new ArrayList<>();
		final List<UI> screenList = new ArrayList<>();
		final List<UI> aboveList = new ArrayList<>();
		for (final UI ui : this.uiList.ordered()) {
			if (!ui.getOverlay().active()) {
				screenList.add(ui);
			} else {
				(ui.getOverlay().render().aboveScreens() ? aboveList : belowList).add(ui);
			}
		}

		final List<UI> layerList = new ArrayList<>(belowList);
		layerList.addAll(screenList);
		layerList.addAll(aboveList);
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

	private static boolean isConsumed(final UI ui, final boolean cancel) {
		return !ui.getOverlay().active() || cancel;
	}

	private static boolean isClosedBy(final UI ui, final Key key) {
		return key == Key.ESCAPE && ui.getData().closeable() && !ui.getOverlay().active();
	}

}