package dev.joid.lib.ui.core;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.render.Drawing;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.impl.primitive.DoubleSignal;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.ui.core.data.UIDataObject;
import dev.joid.lib.ui.core.data.debug.UIDataDebugObject;
import dev.joid.lib.ui.core.data.overlay.UIDataOverlayObject;
import dev.joid.lib.ui.core.data.popup.UIDataPopupObject;
import dev.joid.lib.ui.core.data.scale.UIDataScaleObject;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.impl.PopTransition;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.ui.UICharEvent;
import dev.joid.lib.ui.event.impl.ui.UICloseEvent;
import dev.joid.lib.ui.event.impl.ui.UIKeyEvent;
import dev.joid.lib.ui.event.impl.ui.UIMouseEvent;
import dev.joid.lib.ui.event.impl.ui.UIMouseScrollEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.list.IndexedConcurrentList;
import dev.joid.lib.utils.list.IndexedElement;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UI implements IUI, IndexedElement {

	private static final Map<File, Set<UI>>               WATCHER_MAP      = new HashMap<>();
	private static final Map<File, FileAlterationMonitor> MONITOR_MAP      = new HashMap<>();
	private static final Set<FileAlterationMonitor>       RUNNING_SET      = new HashSet<>();
	private static final ExecutorService                  MONITOR_EXECUTOR = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("UI/monitor"));

	private static final EventType<UIKeyEvent<UI>>         HANDLE_KEY_PRESSED    = EventType.create("handleKeyPressed");
	private static final EventType<UICharEvent<UI>>        HANDLE_CHAR_TYPED     = EventType.create("handleCharTyped");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_PRESSED  = EventType.create("handleMousePressed");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_DRAGGED  = EventType.create("handleMouseDragged");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_RELEASED = EventType.create("handleMouseReleased");
	private static final EventType<UIMouseScrollEvent<UI>> HANDLE_MOUSE_SCROLL   = EventType.create("handleMouseScroll");

	private static final EventType<UICloseEvent.Post<UI>> CLOSE        = EventType.create("onClose");
	private static final EventType<UICloseEvent.Pre<UI>>  BEFORE_CLOSE = EventType.create("beforeClose");

	@Getter
	private static UI current;

	@NonNull private final EventBus                             eventBus;
	@NonNull private final Stack<MaskRegion>                    stencilStack;
	@NonNull private final IndexedConcurrentList<@NonNull Node> nodeList;

	private final UIView       view;
	private final DoubleSignal zoomLevel;
	private final DoubleSignal scaledWidth;
	private final DoubleSignal scaledHeight;

	@NonNull private final UIDataObject        data;
	@NonNull private final UIDataDebugObject   debug;
	@NonNull private final UIDataPopupObject   popup;
	@NonNull private final UIDataScaleObject   scale;
	@NonNull private final UIDataOverlayObject overlay;

	@NonNull private UIDataObject        annotatedData;
	@NonNull private UIDataDebugObject   annotatedDebug;
	@NonNull private UIDataPopupObject   annotatedPopup;
	@NonNull private UIDataScaleObject   annotatedScale;
	@NonNull private UIDataPopupObject   transitionPopup;
	@NonNull private UIDataOverlayObject annotatedOverlay;

	private transient File                                   watchedLocation;
	private transient Transition                             transition;
	private transient Map<Class<? extends UIStore>, UIStore> storeMap;

	private boolean closed;
	private boolean loaded;
	private boolean monitored;
	private boolean initialized;
	private boolean customTransition;
	private volatile boolean reloadPending;

	private double fps;
	private long   lastFrame;
	private long   fpsCounter;
	private long   renderTime;
	private long   frameCount;
	private double frameTime;
	private long   lastFpsUpdate;

	private double  mouseX;
	private double  mouseY;
	private boolean onTop;
	private double  depthLevel;

	private Node devNode;

	private List<Node> hoveredPath;
	private double     lastHoveredX;
	private double     lastHoveredY;
	private long       lastHoveredFrame;

	protected UI() {
		this.annotatedData    = UIDataObject.getOrDefault(this.getClass());
		this.annotatedDebug   = UIDataDebugObject.getOrDefault(this.getClass());
		this.annotatedPopup   = UIDataPopupObject.getOrDefault(this.getClass());
		this.annotatedScale   = UIDataScaleObject.getOrDefault(this.getClass());
		this.annotatedOverlay = UIDataOverlayObject.getOrDefault(this.getClass());
		this.data             = new UIDataObject(this.annotatedData);
		this.debug            = new UIDataDebugObject(this.annotatedDebug);
		this.popup            = new UIDataPopupObject(this.annotatedPopup);
		this.scale            = new UIDataScaleObject(this.annotatedScale);
		this.overlay          = new UIDataOverlayObject(this.annotatedOverlay);
		this.transitionPopup  = new UIDataPopupObject(this.popup);

		if (this.popup.active() && this.overlay.active()) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be a popup and an overlay at the same time");
		}

		this.stencilStack = new Stack<>();
		this.eventBus = EventBus.create(() -> UI.nameOf(this.getClass()));
		this.nodeList = new IndexedConcurrentList<>();
		this.storeMap = new HashMap<>();

		this.transition = this.createPopupTransition();

		this.view = UIView.create(this.data.getAnchorPositionX(), this.data.getAnchorPositionY());
		this.zoomLevel = new DoubleSignal(1D);
		this.scaledWidth = new DoubleSignal(UIView.DESIGN_WIDTH);
		this.scaledHeight = new DoubleSignal(UIView.DESIGN_HEIGHT);

		this.devNode = null;
		this.lastHoveredFrame = -1L;
		this.hoveredPath = Collections.emptyList();
	}

	public final void load(final double finalWidth, final double finalHeight) {
		this.load(finalWidth, finalHeight, 1D);
	}

	public final void load(final double finalWidth, final double finalHeight, final double zoomLevel) {
		final long start = System.nanoTime();

		this.view.resize(finalWidth, finalHeight).zoom(zoomLevel);
		this.refreshView();

		if (!this.initialized) {
			UIPropertyHook.load(this);

			final boolean devNodeEnabled = this.nodeList.contains(this.devNode);
			this.getShortcutList().forEach(Shortcut::detach);
			if (!this.loaded) {
				this.eventBus.keep();
				this.loaded = true;
			}
			this.eventBus.reset();
			this.scoped(() -> {
				if (!this.closed) {
					for (final Node node : this.nodeList) {
						if (node != this.devNode) {
							node.fireDetach();
						}
					}
					this.getTaskList().forEach(Task::detach);
				}
				this.nodeList.clear();

				SignalReplay.reset();
				SignalReplay.enter(this);
				try {
					this.init();
				} finally {
					SignalReplay.exit();
				}
			});
			this.initialized = true;

			if (this.transition != null) {
				if (this.transition.getIn() != null && this.transition.getIn().isEnabled()) {
					this.transition.getIn().init(this);
					this.transition.getIn().start();
				}

				if (this.transition.getOut() != null && this.transition.getOut().isEnabled()) {
					this.transition.getOut().init(this);
				}
			}

			if (JOIDEnv.isDevMode()) {
				if (devNodeEnabled) {
					this.scoped(() -> this.devNode.attach(this));
				} else {
					this.devNode = DevNode.create(1900, 1060).visible(node -> node.getUi().isOnTop()).enabled(node -> node.getUi().isOnTop()).draggable(DraggableProperty.screen()).zindex(Integer.MAX_VALUE).zlevel(999);
				}
			}

			if (JOIDEnv.isDevMode() && this.debug.profiler()) {
				final long end = System.nanoTime();
				JOIDLog.info(this.getClass().getSimpleName() + " loaded in " + String.format(Locale.ROOT, "%.2f", (end - start) / 1000000F) + "ms");
			}
		}

		this.closed = false;
		this.refreshMonitor();
	}

	public final <T extends UI> @NonNull T handleMouseScroll(final @NonNull EventListener<UIMouseScrollEvent<T>> listener) {
		return this.handleMouseScroll(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleMouseScroll(final @NonNull EventPriority priority, final @NonNull EventListener<UIMouseScrollEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_SCROLL, priority, listener);
	}

	public final boolean fireMouseScroll(final double notchesX, final double notchesY) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final UIMouseScrollEvent<UI> event = UIMouseScrollEvent.create(this, mx, my, notchesX, notchesY);
		this.dispatch("mouseScroll", () -> this.nodeList.reversed().forEach(node -> node.fireMouseScroll(mx, my, notchesX, notchesY, event)));
		this.fireListeners(UI.HANDLE_MOUSE_SCROLL, event);

		if (!event.isConsumed() && JOIDEnv.isDevMode() && Key.LEFT_ALT.isDown() && notchesY != 0D) {
			this.view.zoom(this.view.getZoom() + notchesY * (Key.LEFT_SHIFT.isDown() ? 0.12D : 0.012D));
			event.consume();
		}

		this.fireMonitor(UI.HANDLE_MOUSE_SCROLL, event);
		return event.isConsumed();
	}

	public final <T extends UI> @NonNull T handleMousePressed(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.handleMousePressed(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleMousePressed(final @NonNull EventPriority priority, final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_PRESSED, priority, listener);
	}

	public final boolean fireMousePressed(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, 0L);
		this.dispatch("mousePressed", () -> this.nodeList.reversed().forEach(node -> node.fireMousePressed(mx, my, button, event)));
		this.fireListeners(UI.HANDLE_MOUSE_PRESSED, event);
		this.fireMonitor(UI.HANDLE_MOUSE_PRESSED, event);
		return event.isConsumed();
	}

	public final <T extends UI> @NonNull T handleMouseReleased(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.handleMouseReleased(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleMouseReleased(final @NonNull EventPriority priority, final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_RELEASED, priority, listener);
	}

	public final boolean fireMouseReleased(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, 0L);
		this.dispatch("mouseReleased", () -> this.nodeList.reversed().forEach(node -> node.fireMouseReleased(mx, my, button, event)));
		this.fireListeners(UI.HANDLE_MOUSE_RELEASED, event);
		this.fireMonitor(UI.HANDLE_MOUSE_RELEASED, event);
		return event.isConsumed();
	}

	public final <T extends UI> @NonNull T handleMouseDragged(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.handleMouseDragged(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleMouseDragged(final @NonNull EventPriority priority, final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_DRAGGED, priority, listener);
	}

	public final boolean fireMouseDragged(final @NonNull MouseButton button, final long deltaTime) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, deltaTime);
		this.dispatch("mouseDragged", () -> this.nodeList.reversed().forEach(node -> node.fireMouseDragged(mx, my, button, deltaTime, event)));
		this.fireListeners(UI.HANDLE_MOUSE_DRAGGED, event);
		this.fireMonitor(UI.HANDLE_MOUSE_DRAGGED, event);
		return event.isConsumed();
	}

	public final <T extends UI> @NonNull T handleCharTyped(final @NonNull EventListener<UICharEvent<T>> listener) {
		return this.handleCharTyped(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleCharTyped(final @NonNull EventPriority priority, final @NonNull EventListener<UICharEvent<T>> listener) {
		return this.listen(UI.HANDLE_CHAR_TYPED, priority, listener);
	}

	public final boolean fireCharTyped(final int codepoint) {
		if (!this.initialized) {
			return false;
		}

		final UICharEvent<UI> event = UICharEvent.create(this, codepoint);
		this.dispatch("charTyped", () -> this.nodeList.reversed().forEach(node -> node.fireCharTyped(codepoint, event)));
		this.fireListeners(UI.HANDLE_CHAR_TYPED, event);
		this.fireMonitor(UI.HANDLE_CHAR_TYPED, event);
		return event.isConsumed();
	}

	public final <T extends UI> @NonNull T handleKeyPressed(final @NonNull EventListener<UIKeyEvent<T>> listener) {
		return this.handleKeyPressed(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T handleKeyPressed(final @NonNull EventPriority priority, final @NonNull EventListener<UIKeyEvent<T>> listener) {
		return this.listen(UI.HANDLE_KEY_PRESSED, priority, listener);
	}

	public final boolean fireKeyPressed(final @NonNull Key key) {
		if (!this.initialized) {
			return false;
		}

		final UIKeyEvent<UI> event = UIKeyEvent.create(this, key);
		this.fireShortcuts(key, true, event);
		if (!event.isConsumed()) {
			this.dispatch("keyPressed", () -> this.nodeList.reversed().forEach(node -> node.fireKeyPressed(key, event)));
		}
		this.fireListeners(UI.HANDLE_KEY_PRESSED, event);
		this.fireShortcuts(key, false, event);

		if (this.data.zoomable() && !event.isConsumed()) {
			if ((key == Key.NUMPAD_ADD || key == Key.EQUAL) && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.view.zoom(previous + 0.1D);
				if (this.view.getZoom() != previous) {
					event.consume();
				}
			}

			if ((key == Key.NUMPAD_SUBTRACT || key == Key.MINUS) && (UI.isCtrlKeyDown() || UI.isAltKeyDown())) {
				final double previous = this.view.getZoom();
				this.view.zoom(previous - 0.1D);
				if (this.view.getZoom() != previous) {
					event.consume();
				}
			}
		}

		if (JOIDEnv.isDevMode() && !event.isConsumed()) {
			if (key == Key.R && Key.LEFT_CONTROL.isDown() || key == Key.F5) {
				if (!Key.LEFT_SHIFT.isDown()) {
					this.reload();
				} else {
					try {
						this.renew();
					} catch (final IllegalStateException exception) {
						JOIDLog.warn(exception.getMessage());
					}
				}
				event.consume();
			} else if (key == Key.F3 && this.devNode != null) {
				final boolean enabled = this.nodeList.contains(this.devNode);
				if (enabled) {
					this.nodeList.remove(this.devNode);
				} else {
					this.scoped(() -> this.devNode.attach(this));
				}
				event.consume();
			}
		}

		this.fireMonitor(UI.HANDLE_KEY_PRESSED, event);
		return event.isConsumed();
	}

	public final void fireUpdate() {
		this.dispatch("update", () -> {
			this.nodeList.forEach(Node::fireUpdate);
			this.update();
		});
	}

	public final <T extends UI> @NonNull T beforeClose(final @NonNull EventListener<UICloseEvent.Pre<T>> listener) {
		return this.beforeClose(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T beforeClose(final @NonNull EventPriority priority, final @NonNull EventListener<UICloseEvent.Pre<T>> listener) {
		return this.listen(UI.BEFORE_CLOSE, priority, listener);
	}

	public final <T extends UI> @NonNull T onClose(final @NonNull EventListener<UICloseEvent.Post<T>> listener) {
		return this.onClose(EventPriority.NORMAL, listener);
	}

	public final <T extends UI> @NonNull T onClose(final @NonNull EventPriority priority, final @NonNull EventListener<UICloseEvent.Post<T>> listener) {
		return this.listen(UI.CLOSE, priority, listener);
	}

	public final boolean fireClose() {
		if (this.transition != null && this.transition.getOut() != null && this.transition.getOut().isRunning()) {
			return false;
		}

		final UICloseEvent.Pre<UI> event = this.fire(UI.BEFORE_CLOSE, () -> UICloseEvent.Pre.create(this));
		if (event != null && event.isCancelled()) {
			return false;
		}

		if (this.transition != null && this.transition.getOut() != null && this.transition.getOut().isEnabled()) {
			this.transition.getOut().start();
			this.transition.getOut().getAnimator().onComplete(tween -> {
				this.dispose();
				this.getBridge().close(this);
			});
			return false;
		}

		this.dispose();
		return true;
	}

	public final @NonNull List<MaskRegion> getStencilStack() {
		return Collections.unmodifiableList(this.stencilStack);
	}

	public final @NonNull Map<Class<? extends UIStore>, UIStore> getStoreMap() {
		return Collections.unmodifiableMap(this.storeMap);
	}

	public final @NonNull List<@NonNull Task> getTaskList() {
		return Task.getTaskList(this);
	}

	public final @NonNull List<@NonNull Node> getNodeList() {
		return this.nodeList.ordered();
	}

	public final @NonNull List<@NonNull Shortcut> getShortcutList() {
		return Shortcut.getShortcutList(this);
	}

	@Override
	public int getIndex() {
		return this.data.zindex();
	}

	public final double getWindowWidth() {
		return this.view.getWidth();
	}

	public final double getWindowHeight() {
		return this.view.getHeight();
	}

	public final double getViewX() {
		return this.view.toUiX(0D);
	}

	public final double getViewY() {
		return this.view.toUiY(0D);
	}

	public final double getViewWidth() {
		return this.view.getVisibleWidth();
	}

	public final double getViewHeight() {
		return this.view.getVisibleHeight();
	}

	public final double getMouseX() {
		return this.view.toUiX(this.mouseX);
	}

	public final double getMouseY() {
		return this.view.toUiY(this.mouseY);
	}

	public final Node getHoveredNode() {
		final List<Node> path = this.getHoveredPath();
		return path.isEmpty() ? null : path.get(0);
	}

	public final @NonNull List<@NonNull Node> getHoveredPath() {
		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		if (this.lastHoveredFrame != this.frameCount || this.lastHoveredX != mx || this.lastHoveredY != my) {
			final List<Node> path = new ArrayList<>();
			for (Node node = this.onTop ? this.getNodeAt(mx, my) : null; node != null; node = node.getParent()) {
				path.add(node);
			}

			this.hoveredPath = Collections.unmodifiableList(path);
			this.lastHoveredFrame = this.frameCount;
			this.lastHoveredX = mx;
			this.lastHoveredY = my;
		}
		return this.hoveredPath;
	}

	public final Node getNodeAt(final double x, final double y) {
		for (final Node node : this.getNodeListAt(x, y)) {
			if (node.isInteractive()) {
				return node;
			}
		}
		return null;
	}

	public final @NonNull List<@NonNull Node> getNodeListAt(final double x, final double y) {
		final List<Node> nodeList = new ArrayList<>();
		for (final Node node : this.nodeList.reversed()) {
			nodeList.addAll(node.getNodeListAt(x, y));
		}
		return nodeList;
	}

	public final IUIBridge getBridge() {
		return BridgeHandler.UI.get(this);
	}

	public final void dispose() {
		this.scoped(() -> this.nodeList.forEach(Node::fireDetach));
		this.getTaskList().forEach(Task::detach);
		this.closed = true;
		this.initialized = false;

		this.stopMonitor();

		UIStoreHook.saveAll();
		for (final Entry<Class<? extends UIStore>, UIStore> storeEntry : this.storeMap.entrySet()) {
			UIStoreHook.destroyStore(storeEntry.getValue());
		}
		this.storeMap.clear();

		UIPropertyHook.save(this);
		this.fire(UI.CLOSE, () -> UICloseEvent.Post.create(this));
	}

	public final void draw(final double mouseX, final double mouseY) {
		if (this.reloadPending) {
			this.reloadPending = false;

			final long start = System.nanoTime();
			this.reload();
			final long end = System.nanoTime();

			JOIDLog.info(this.getClass().getSimpleName() + " reloaded in " + String.format(Locale.ROOT, "%.2f", (end - start) / 1000000F) + "ms");
		}

		this.untraced(() -> this.drawFrame(mouseX, mouseY));
	}

	public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
		this.getBridge().drawHover(this, content, mouseX, mouseY);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing) {
		this.mask(maskX, maskY, maskWidth, maskHeight, drawing, true);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing, final boolean enabled) {
		if (enabled) {
			this.startMask(maskX, maskY, maskWidth, maskHeight);
			try {
				drawing.draw();
			} finally {
				this.stopMask();
			}
		} else {
			drawing.draw();
		}
	}

	public final void mask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing) {
		this.mask(resource, maskX, maskY, maskWidth, maskHeight, drawing, true);
	}

	public final void mask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Drawing drawing, final boolean enabled) {
		if (enabled) {
			this.startMask(resource, maskX, maskY, maskWidth, maskHeight);
			try {
				drawing.draw();
			} finally {
				this.stopMask();
			}
		} else {
			drawing.draw();
		}
	}

	public final void stopMask() {
		this.stencilStack.pop();
		final int stencilValue = this.stencilStack.size();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 0) {
			render.getState().stencil(StencilState.DISABLED);
			render.clearStencil();
		} else {
			render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
		}
	}

	public final void startMask(final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new MaskRegion(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue - 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT));

		render.getState().colorWrite(false);
		DrawUtils.SHAPE.drawRect(maskX, maskY, maskWidth, maskHeight, Color.RED);
		render.getState().colorWrite(true);

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
	}

	public final void startMask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight) {
		final int stencilValue = this.stencilStack.size() + 1;
		this.stencilStack.push(new MaskRegion(stencilValue, maskX, maskY, maskWidth, maskHeight));
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (stencilValue == 1) {
			render.clearStencil();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue - 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT));

		render.pushState();
		try {
			render.getState().colorWrite(false).alphaCutoff(0.5F);
			DrawUtils.RESOURCE.drawResource(maskX, maskY, maskWidth, maskHeight, resource);
		} finally {
			render.popState();
		}

		render.getState().stencil(StencilState.create(StencilFunction.EQUAL, stencilValue, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
	}

	public final void reload() {
		this.scoped(() -> {
			if (this.devNode != null) {
				final DevNode devNode = (DevNode) this.devNode;
				devNode.getReloadAnimator().sequence(100F, 1F).push(100F, 0F);
				devNode.getReloadAnimator().start();
			}

			UIPropertyHook.save(this);
			SignalReplay.clear();

			this.readData();
			this.refreshTransition();

			this.initialized = false;
			this.load(this.view.getWidth(), this.view.getHeight(), this.view.getZoom());
		});
	}

	public final @NonNull UI renew() {
		final IUIBridge bridge = BridgeHandler.UI.get(this);
		if (bridge == null || !bridge.isOpen(this)) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " is not open, only an open UI can be renewed");
		}

		final UI renewed;
		try {
			final Constructor<? extends UI> constructor = this.getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			renewed = constructor.newInstance();
		} catch (final NoSuchMethodException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", exception);
		} catch (final ReflectiveOperationException | RuntimeException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be renewed: its constructor without argument failed", exception);
		}

		this.dispose();
		bridge.remove(this);
		bridge.add(renewed);
		return renewed;
	}

	public final double lerpByFramerate(final double value, final double target, final double speed, final double snapDiff, final boolean snap) {
		final double diff = target - value;
		final double absDiff = Math.abs(diff);

		final double offset = Math.min(absDiff, speed * this.frameTime / (1000D / 60D) * absDiff / 3D);

		if (absDiff > snapDiff) {
			return value + (diff > 0 ? offset : -offset);
		}

		return snap ? target : value;
	}

	public final void add(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			node.load(this);
			this.nodeList.add(node);
		}
	}

	public final void remove(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (this.nodeList.contains(node)) {
				node.fireDetach();
				this.nodeList.remove(node);
			}
		}
	}

	public final void setDepthLevel(final double level) {
		this.depthLevel = level;
	}

	public final @NonNull UI setTransition(final Transition transition) {
		this.transition = transition;
		this.customTransition = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	public final <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final Object... args) {
		if (this.storeMap.containsKey(clazz)) {
			return (T) this.storeMap.get(clazz);
		}

		final T store = UIStoreHook.useStore(clazz, args);
		if (store.getData().scope().isLocal()) {
			this.storeMap.put(clazz, store);
		}

		return store;
	}

	public static boolean isAltKeyDown() {
		return Key.LEFT_ALT.isDown() || Key.RIGHT_ALT.isDown();
	}

	public static boolean isCtrlKeyDown() {
		return Key.LEFT_CONTROL.isDown() || Key.RIGHT_CONTROL.isDown();
	}

	public static boolean isShiftKeyDown() {
		return Key.LEFT_SHIFT.isDown() || Key.RIGHT_SHIFT.isDown();
	}

	private void readData() {
		final UIDataObject data = UIDataObject.getOrDefault(this.getClass());
		final UIDataDebugObject debug = UIDataDebugObject.getOrDefault(this.getClass());
		final UIDataPopupObject popup = UIDataPopupObject.getOrDefault(this.getClass());
		final UIDataScaleObject scale = UIDataScaleObject.getOrDefault(this.getClass());
		final UIDataOverlayObject overlay = UIDataOverlayObject.getOrDefault(this.getClass());
		this.data.update(this.annotatedData, data);
		this.debug.update(this.annotatedDebug, debug);
		this.popup.update(this.annotatedPopup, popup);
		this.scale.update(this.annotatedScale, scale);
		this.overlay.update(this.annotatedOverlay, overlay);
		this.annotatedData    = data;
		this.annotatedDebug   = debug;
		this.annotatedPopup   = popup;
		this.annotatedScale   = scale;
		this.annotatedOverlay = overlay;
	}

	private void refreshView() {
		this.zoomLevel.set(this.view.getZoom());
		this.scaledWidth.set(this.view.getVisibleWidth());
		this.scaledHeight.set(this.view.getVisibleHeight());
	}

	private void refreshTransition() {
		if (this.customTransition || this.popup.active() == this.transitionPopup.active() && this.popup.transition() == this.transitionPopup.transition()) {
			return;
		}

		this.transitionPopup = new UIDataPopupObject(this.popup);
		this.transition = this.createPopupTransition();
	}

	private void refreshMonitor() {
		this.monitored = this.debug.hotreload();
		if (!this.monitored) {
			this.stopMonitor();
		}

		if (JOIDEnv.isDevMode() && this.debug.hotreload() && this.watchedLocation == null) {
			final File location = UI.locate(this.getClass());
			if (location == null) {
				JOIDLog.warnOnce("UI.hotReload", "Hot reload is off for " + this.getClass().getSimpleName() + ": its classes are not loaded from a folder or a jar file");
				return;
			}

			this.watchedLocation = location;
			UI.watch(location, this);
			JOIDLog.info("Hot reload is on for " + this.getClass().getSimpleName() + ", it reloads when its classes change");
		}
	}

	private void stopMonitor() {
		if (this.watchedLocation == null) {
			return;
		}

		this.watchedLocation = null;
		UI.unwatch(UI.locate(this.getClass()), this);
	}

	private Transition createPopupTransition() {
		if (!this.popup.active() || !this.popup.transition().isActive()) {
			return null;
		}

		final Transition transition = new PopTransition();
		if (!this.popup.transition().isIn()) {
			transition.getIn().disable();
		}

		if (!this.popup.transition().isOut()) {
			transition.getOut().disable();
		}
		return transition;
	}

	private void drawFrame(final double mouseX, final double mouseY) {
		final long start = BridgeHandler.CLOCK.get().nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		final long frame = BridgeHandler.CLOCK.get().nanoTime();
		this.frameTime = this.lastFrame == 0L ? 1000D / 60D : (frame - this.lastFrame) / 1_000_000D;
		this.lastFrame = frame;
		this.frameCount++;

		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.view.anchorX(this.data.getAnchorPositionX()).anchorY(this.data.getAnchorPositionY());
		this.refreshView();
		this.refreshTransition();
		if (this.monitored != this.debug.hotreload()) {
			this.refreshMonitor();
		}
		this.onTop = this.getBridge() != null && this.getBridge().isOnTop(this);

		final List<Task> taskList = new ArrayList<>(this.getTaskList());
		this.nodeList.forEach(node -> UI.collectTasks(node, taskList));
		for (final Task task : taskList) {
			if (task.isDue()) {
				this.traced(task::fireRun);
			}
		}

		final IUIBridge bridge = this.getBridge();
		final double interfaceScale = bridge == null || !this.scale.active() ? 1D : this.scale.apply(bridge.getInterfaceScale(this));
		if (interfaceScale != this.view.getInterfaceScale()) {
			this.view.interfaceScale(interfaceScale);
			this.refreshView();
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		if (this.data.background()) {
			float opacity = this.data.getBackgroundColor().getAlpha() / 255F;
			if (this.transition != null) {
				if (this.transition.getIn() != null && this.transition.getIn().isRunning()) {
					opacity = Math.min(1F, Math.max(0F, this.transition.getIn().getAnimator().get())) * (this.data.getBackgroundColor().getAlpha() / 255F);
				}

				if (this.transition.getOut() != null && this.transition.getOut().isRunning()) {
					opacity = Math.min(1F, Math.max(0F, this.transition.getOut().getAnimator().get())) * (this.data.getBackgroundColor().getAlpha() / 255F);
				}
			}
			DrawUtils.SHAPE.drawRect(0, 0, this.view.getWidth(), this.view.getHeight(), this.data.getBackgroundColor().withAlpha(opacity));
		}

		final boolean transitionIn = this.transition != null && this.transition.getIn() != null && this.transition.getIn().isRunning();
		final boolean transitionOut = this.transition != null && this.transition.getOut() != null && this.transition.getOut().isRunning();
		if (transitionIn) {
			this.transition.getIn().update();
			this.transition.getIn().pre(this, mx, my);
		}

		if (transitionOut) {
			this.transition.getOut().update();
			this.transition.getOut().pre(this, mx, my);
		}

		render.getState().alphaCutoff(0F);
		try {
			this.view.render(render, this.data.projection(), () -> {
				this.depthLevel = 0;
				final AtomicDouble lastDepthLevel = new AtomicDouble(this.depthLevel);

				this.nodeList.ordered().forEach(node -> {
					render.getModelView().translate(0, 0, this.depthLevel - lastDepthLevel.get());
					lastDepthLevel.set(this.depthLevel);
					node.render(mx, my);
				});

				Node hovered = this.getHoveredNode();
				if (hovered != null) {
					render.getModelView().push();
					render.pushState();
					try {
						render.getState().depthTest(false).depthWrite(false);
						while (hovered != null && !hovered.renderHover(mx, my)) {
							hovered = hovered.getParent();
						}
					} finally {
						render.popState();
						render.getModelView().pop();
					}
				}
			});
		} finally {
			if (transitionIn) {
				this.transition.getIn().post(this, mx, my);
				this.transition.getIn().endFrame();
			}

			if (transitionOut) {
				this.transition.getOut().post(this, mx, my);
				this.transition.getOut().endFrame();
			}
		}

		final long end = BridgeHandler.CLOCK.get().nanoTime();
		this.renderTime = end - start;

		if (JOIDEnv.isDevMode() && this.debug.profiler()) {
			final float ms = this.renderTime / 1_000_000F;
			if (ms > 16.66F) {
				JOIDLog.warn(this.getClass().getSimpleName() + " took " + String.format(Locale.ROOT, "%.2f", ms) + "ms to draw a frame, more than the 16.66ms of 60 fps");
			}
		}

		if (this.lastFpsUpdate == 0L) {
			this.fps = 0;
			this.fpsCounter = 0;
			this.lastFpsUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
		} else if (this.frameTime > 0D) {
			this.fpsCounter++;
			final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
			if (now - this.lastFpsUpdate >= 1000L) {
				this.fps = this.fpsCounter / ((now - this.lastFpsUpdate) / 1000D);
				this.fpsCounter = 0;
				this.lastFpsUpdate = now;
			}
		}
	}

	private void scoped(final Runnable runnable) {
		final UI previous = UI.current;
		UI.current = this;
		try {
			runnable.run();
		} finally {
			UI.current = previous;
		}
	}

	private void fireShortcuts(final Key key, final boolean overrideFocus, final UIKeyEvent<UI> event) {
		if (event.isConsumed()) {
			return;
		}

		final List<Shortcut> shortcutList = new ArrayList<>();
		this.untraced(() -> this.nodeList.reversed().forEach(node -> UI.collectShortcuts(node, shortcutList)));
		shortcutList.addAll(this.getShortcutList());
		for (final Shortcut shortcut : shortcutList) {
			if (shortcut.isOverrideFocus() == overrideFocus && shortcut.isTriggeredBy(key)) {
				this.traced(() -> shortcut.fireTrigger(key));
				event.consume();
				return;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends UI> T listen(final EventType<?> type, final EventPriority priority, final EventListener<?> listener) {
		this.eventBus.listen(type, priority, listener);
		return (T) this;
	}

	private <E extends Event> E fire(final EventType<? super E> type, final Supplier<E> event) {
		if (!this.eventBus.has(type)) {
			return null;
		}

		final E fired = event.get();
		this.traced(() -> this.eventBus.fire(type, fired));
		return fired;
	}

	private void fireListeners(final EventType<?> type, final Event event) {
		if (this.eventBus.has(type)) {
			this.traced(() -> this.eventBus.fireListeners(type, event));
		}
	}

	private void fireMonitor(final EventType<?> type, final Event event) {
		if (this.eventBus.has(type)) {
			this.traced(() -> this.eventBus.fireMonitor(type, event));
		}
	}

	private void traced(final Runnable runnable) {
		SignalReplay.enter(this);
		try {
			this.scoped(runnable);
		} finally {
			SignalReplay.exit();
		}
	}

	private void dispatch(final String input, final Runnable runnable) {
		try {
			this.untraced(runnable);
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("UI." + input + "." + System.identityHashCode(this) + exception, "The " + input + " of " + UI.nameOf(this.getClass()) + " failed: " + exception, exception);
		}
	}

	private void untraced(final Runnable runnable) {
		final boolean tracing = SignalContext.current().tracing(false);
		try {
			this.scoped(runnable);
		} finally {
			SignalContext.current().tracing(tracing);
		}
	}

	private static String nameOf(final Class<?> type) {
		return type.getSimpleName().isEmpty() ? type.getName() : type.getSimpleName();
	}

	private static void collectTasks(final Node node, final List<Task> taskList) {
		taskList.addAll(node.getTaskList());
		node.getChildren().forEach(child -> UI.collectTasks(child, taskList));
	}

	private static void collectShortcuts(final Node node, final List<Shortcut> shortcutList) {
		if (!node.isVisible() || !node.isEnabled()) {
			return;
		}

		Lists.reverse(node.getChildren()).forEach(child -> UI.collectShortcuts(child, shortcutList));
		shortcutList.addAll(node.getShortcutList());
	}

	private static void watch(final File location, final UI ui) {
		synchronized (UI.WATCHER_MAP) {
			UI.WATCHER_MAP.computeIfAbsent(location, key -> new HashSet<>()).add(ui);
			if (UI.MONITOR_MAP.containsKey(location)) {
				return;
			}

			final FileAlterationMonitor monitor = UI.createMonitor(location);
			UI.MONITOR_MAP.put(location, monitor);
			UI.MONITOR_EXECUTOR.execute(() -> UI.openMonitor(location, monitor));
		}
	}

	private static void unwatch(final File location, final UI ui) {
		synchronized (UI.WATCHER_MAP) {
			final Set<UI> watchers = UI.WATCHER_MAP.get(location);
			if (watchers == null || !watchers.remove(ui) || !watchers.isEmpty()) {
				return;
			}

			UI.WATCHER_MAP.remove(location);
			final FileAlterationMonitor monitor = UI.MONITOR_MAP.remove(location);
			UI.MONITOR_EXECUTOR.execute(() -> UI.closeMonitor(monitor));
		}
	}

	private static void openMonitor(final File location, final FileAlterationMonitor monitor) {
		synchronized (UI.WATCHER_MAP) {
			if (UI.MONITOR_MAP.get(location) != monitor) {
				return;
			}
		}

		try {
			monitor.start();
			UI.RUNNING_SET.add(monitor);
		} catch (final Exception e) {
			JOIDLog.warn("The hot reload of " + location.getAbsolutePath() + " cannot start: " + e, e);
		}
	}

	private static void closeMonitor(final FileAlterationMonitor monitor) {
		if (!UI.RUNNING_SET.remove(monitor)) {
			return;
		}

		try {
			monitor.stop();
		} catch (final Exception e) {
			JOIDLog.warn("A hot reload watcher cannot stop: " + e, e);
		}
	}

	private static FileAlterationMonitor createMonitor(final File location) {
		final FileAlterationObserver observer;
		try {
			observer = FileAlterationObserver.builder().setFile(location.isDirectory() ? location : location.getParentFile()).get();
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}

		observer.addListener(new FileAlterationListener() {

			private File change;

			@Override
			public void onFileChange(final @NonNull File file) {
				this.track(file);
			}

			@Override
			public void onStop(final @NonNull FileAlterationObserver observer) {
				if (this.change == null) {
					return;
				}

				try {
					Thread.sleep(1000L);
				} catch (final Exception e) {}

				JOIDLog.info(this.change.getName() + " changed, the UIs that use it reload");
				this.change = null;
				synchronized (UI.WATCHER_MAP) {
					final Set<UI> watchers = UI.WATCHER_MAP.get(location);
					if (watchers != null) {
						watchers.forEach(watcher -> watcher.reloadPending = true);
					}
				}
			}

			@Override
			public void onStart(final @NonNull FileAlterationObserver observer) {}

			@Override
			public void onFileDelete(final @NonNull File file) {}

			@Override
			public void onFileCreate(final @NonNull File file) {
				this.track(file);
			}

			@Override
			public void onDirectoryDelete(final @NonNull File file) {}

			@Override
			public void onDirectoryCreate(final @NonNull File file) {}

			@Override
			public void onDirectoryChange(final @NonNull File file) {}

			private void track(final File file) {
				if (location.isDirectory() ? file.getName().endsWith(".class") : file.equals(location)) {
					this.change = file;
				}
			}

		});

		final FileAlterationMonitor monitor = new FileAlterationMonitor(500);
		monitor.setThreadFactory(ThreadUtils.daemonFactory("UI/" + location.getName() + "/monitor"));
		monitor.addObserver(observer);
		return monitor;
	}

	private static File locate(final Class<?> type) {
		final ProtectionDomain domain = type.getProtectionDomain();
		final CodeSource source = domain != null ? domain.getCodeSource() : null;
		final URL location = source != null ? source.getLocation() : null;
		if (location == null || !"file".equals(location.getProtocol())) {
			return null;
		}

		try {
			return new File(location.toURI());
		} catch (final URISyntaxException | IllegalArgumentException exception) {
			return null;
		}
	}

	@Getter
	private class MaskRegion {

		public final double x;
		public final double y;
		public final int value;
		public final double width;
		public final double height;

		public MaskRegion(final int value, final double x, final double y, final double width, final double height) {
			this.value = value;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

	}

}