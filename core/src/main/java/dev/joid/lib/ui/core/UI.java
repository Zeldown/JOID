package dev.joid.lib.ui.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import dev.joid.internal.HotReload;
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
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.binding.NodeBindings;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.core.data.UIDataDebugObject;
import dev.joid.lib.ui.core.data.UIDataObject;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.core.data.UIDataOverlayObject;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.core.data.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.data.UIDataPopupObject;
import dev.joid.lib.ui.core.data.UIDataScale;
import dev.joid.lib.ui.core.data.UIDataScaleObject;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.transition.PopTransition;
import dev.joid.lib.ui.core.transition.Transition;
import dev.joid.lib.ui.core.transition.TransitionStep;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.ui.UIChangeEvent;
import dev.joid.lib.ui.event.impl.ui.UICharEvent;
import dev.joid.lib.ui.event.impl.ui.UICloseEvent;
import dev.joid.lib.ui.event.impl.ui.UIInputEvent;
import dev.joid.lib.ui.event.impl.ui.UIKeyEvent;
import dev.joid.lib.ui.event.impl.ui.UIMouseEvent;
import dev.joid.lib.ui.event.impl.ui.UIMouseScrollEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.dev.DevNode;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.annotation.AnnotationValues;
import dev.joid.lib.utils.list.IndexedElement;
import dev.joid.lib.utils.list.IndexedList;
import lombok.Getter;
import lombok.NonNull;

public abstract class UI implements IUI, IndexedElement {

	private static final EventType<UIKeyEvent<UI>>         HANDLE_KEY_PRESSED    = EventType.create("handleKeyPressed");
	private static final EventType<UIKeyEvent<UI>>         HANDLE_KEY_RELEASED   = EventType.create("handleKeyReleased");
	private static final EventType<UICharEvent<UI>>        HANDLE_CHAR_TYPED     = EventType.create("handleCharTyped");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_PRESSED  = EventType.create("handleMousePressed");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_DRAGGED  = EventType.create("handleMouseDragged");
	private static final EventType<UIMouseEvent<UI>>       HANDLE_MOUSE_RELEASED = EventType.create("handleMouseReleased");
	private static final EventType<UIMouseScrollEvent<UI>> HANDLE_MOUSE_SCROLL   = EventType.create("handleMouseScroll");

	private static final EventType<UICloseEvent.Post<UI>> CLOSE        = EventType.create("onClose");
	private static final EventType<UICloseEvent.Pre<UI>>  BEFORE_CLOSE = EventType.create("beforeClose");

	private static final EventType<UIChangeEvent.Post<UI, Double>> ZOOM        = EventType.create("onZoom");
	private static final EventType<UIChangeEvent.Pre<UI, Double>>  BEFORE_ZOOM = EventType.create("beforeZoom");

	@Getter
	private static UI current;

	private static long frameNumber;

	static {
		BridgeHandler.UI.onFrame(() -> UI.frameNumber++);
	}

	@NonNull private final EventBus                   eventBus;
	@NonNull private final IndexedList<@NonNull Node> nodeList;

	private final List<Task>               taskList;
	private final NodeBindings             bindings;
	private final List<Task>               keptTaskList;
	private final List<Shortcut>           shortcutList;
	private final List<Shortcut>           keptShortcutList;
	private final InteractiveValue<Double> zoomValue;

	@Getter
	private final UIView view;

	@Getter private final UIDataObject        data;
	@Getter private final UIDataDebugObject   debug;
	@Getter private final UIDataPopupObject   popup;
	@Getter private final UIDataScaleObject   scale;
	@Getter private final UIDataOverlayObject overlay;

	@Getter
	private Transition transition;

	private PopupTransition                              popupTransition;
	private Map<Class<? extends UIStore>, UIStore>       storeMap;
	private Map<Class<? extends Annotation>, Annotation> annotationMap;

	private double zoom;

	@Getter private boolean closed;
	@Getter private boolean initialized;

	private int              maskDepth;
	private boolean          loaded;
	private boolean          watched;
	private boolean          customTransition;
	private volatile boolean reloadPending;

	@Getter private double fps;
	@Getter private long   renderTime;
	@Getter private double frameTime;

	private long lastFrame;
	private long drawnFrame;
	private long fpsCounter;
	private long frameCount;
	private long lastFpsUpdate;

	private double mouseX;
	private double mouseY;

	@Getter private boolean onTop;
	@Getter private double  depthLevel;

	@Getter
	private Node devNode;

	private List<Node> hoveredPath;
	private double     lastHoveredX;
	private double     lastHoveredY;
	private long       lastHoveredFrame;

	protected UI() {
		this.annotationMap = UI.annotations(this.getClass());
		this.data          = AnnotationValues.read(this.annotationMap.get(UIData.class), new UIDataObject());
		this.debug         = AnnotationValues.read(this.annotationMap.get(UIDataDebug.class), new UIDataDebugObject());
		this.popup         = AnnotationValues.read(this.annotationMap.get(UIDataPopup.class), new UIDataPopupObject());
		this.scale         = AnnotationValues.read(this.annotationMap.get(UIDataScale.class), new UIDataScaleObject());
		this.overlay       = AnnotationValues.read(this.annotationMap.get(UIDataOverlay.class), new UIDataOverlayObject());

		if (this.popup.active() && this.overlay.active()) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be a popup and an overlay at the same time, keep only one of @UIDataPopup and @UIDataOverlay");
		}

		this.eventBus = EventBus.create(() -> JOIDLog.nameOf(this.getClass()));
		this.nodeList = IndexedList.concurrent();
		this.storeMap = new HashMap<>();

		this.refreshTransition();

		this.view = UIView.create(this.data.getAnchorPositionX(), this.data.getAnchorPositionY());

		this.bindings         = NodeBindings.create(this, this.eventBus, () -> JOIDLog.nameOf(this.getClass()), this::traced);
		this.taskList         = new CopyOnWriteArrayList<>();
		this.keptTaskList     = new ArrayList<>();
		this.shortcutList     = new CopyOnWriteArrayList<>();
		this.keptShortcutList = new ArrayList<>();
		this.zoomValue        = InteractiveValue.create(this.bindings, "zoom", () -> this.zoom, (final Double zoom) -> {
			this.zoom = zoom;
			this.view.zoom(zoom);
		}).clamp(zoom -> this.view.clampZoom(zoom)).events(UI.BEFORE_ZOOM, (previous, zoom) -> UIChangeEvent.Pre.create(this, previous, zoom), UI.ZOOM, (previous, zoom) -> UIChangeEvent.Post.create(this, previous, zoom));

		this.zoom = 1D;
		this.devNode = null;
		this.drawnFrame = Long.MIN_VALUE;
		this.lastHoveredFrame = -1L;
		this.hoveredPath = Collections.emptyList();
	}

	public final void load(final double finalWidth, final double finalHeight) {
		this.load(finalWidth, finalHeight, this.view.getZoom());
	}

	public final void load(final double finalWidth, final double finalHeight, final double zoomLevel) {
		final long start = System.nanoTime();

		this.bindings.subscribe(this.loaded);
		this.view.resize(finalWidth, finalHeight);
		this.zoomValue.update(zoomLevel);

		if (!this.initialized) {
			UIPropertyHook.load(this);

			final boolean devNodeEnabled = this.nodeList.contains(this.devNode);
			if (!this.loaded) {
				this.eventBus.keep();
				this.keptTaskList.addAll(this.taskList);
				this.keptShortcutList.addAll(this.shortcutList);
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
					this.taskList.forEach(Task::detach);
					this.shortcutList.forEach(Shortcut::detach);
				}
				this.keptTaskList.forEach(this::addTask);
				this.keptShortcutList.forEach(this::addShortcut);
				this.nodeList.clear();

				SignalReplay.reset();
				SignalReplay.run(this, null, this::init);
			});
			this.initialized = true;

			if (this.transition != null) {
				if (this.transition.getIn().isEnabled()) {
					this.transition.getIn().init(this);
					this.transition.getIn().start();
				}

				if (this.transition.getOut().isEnabled()) {
					this.transition.getOut().init(this);
				}
			}

			if (JOIDEnv.isDevMode()) {
				if (devNodeEnabled) {
					this.scoped(() -> this.devNode.attach(this));
				} else {
					this.devNode = DevNode.create(1900, 1060).visible(this::isOnTop).enabled(this::isOnTop).draggable(DraggableProperty.screen()).zindex(Integer.MAX_VALUE).zlevel(999);
				}
			}

			if (JOIDEnv.isDevMode() && this.debug.profiler()) {
				final long end = System.nanoTime();
				JOIDLog.info(this.getClass().getSimpleName() + " loaded in " + String.format(Locale.ROOT, "%.2f", (end - start) / 1000000F) + "ms");
			}
		}

		this.closed = false;
		this.refreshWatch();
	}

	public final <T extends UI> @NonNull T handleMouseScroll(final @NonNull EventListener<UIMouseScrollEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_SCROLL, listener);
	}

	public final boolean fireMouseScroll(final double notchesX, final double notchesY) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		final UIMouseScrollEvent<UI> event = UIMouseScrollEvent.create(this, mx, my, notchesX, notchesY);
		return this.fireInput(UI.HANDLE_MOUSE_SCROLL, "mouseScroll", event, node -> node.fireMouseScroll(mx, my, notchesX, notchesY, event), () -> {
			if (!event.isConsumed() && JOIDEnv.isDevMode() && Key.LEFT_ALT.isDown() && notchesY != 0D) {
				this.zoomValue.change(this.zoom + notchesY * (Key.LEFT_SHIFT.isDown() ? 0.12D : 0.012D));
				event.consume();
			}
		});
	}

	public final <T extends UI> @NonNull T handleMousePressed(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_PRESSED, listener);
	}

	public final boolean fireMousePressed(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, 0L);
		return this.fireInput(UI.HANDLE_MOUSE_PRESSED, "mousePressed", event, node -> node.fireMousePressed(mx, my, button, event), () -> {});
	}

	public final <T extends UI> @NonNull T handleMouseReleased(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_RELEASED, listener);
	}

	public final boolean fireMouseReleased(final @NonNull MouseButton button) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, 0L);
		return this.fireInput(UI.HANDLE_MOUSE_RELEASED, "mouseReleased", event, node -> node.fireMouseReleased(mx, my, button, event), () -> {});
	}

	public final <T extends UI> @NonNull T handleMouseDragged(final @NonNull EventListener<UIMouseEvent<T>> listener) {
		return this.listen(UI.HANDLE_MOUSE_DRAGGED, listener);
	}

	public final boolean fireMouseDragged(final @NonNull MouseButton button, final long deltaTime) {
		if (!this.initialized) {
			return false;
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();
		final UIMouseEvent<UI> event = UIMouseEvent.create(this, mx, my, button, deltaTime);
		return this.fireInput(UI.HANDLE_MOUSE_DRAGGED, "mouseDragged", event, node -> node.fireMouseDragged(mx, my, button, deltaTime, event), () -> {});
	}

	public final <T extends UI> @NonNull T handleCharTyped(final @NonNull EventListener<UICharEvent<T>> listener) {
		return this.listen(UI.HANDLE_CHAR_TYPED, listener);
	}

	public final boolean fireCharTyped(final int codepoint) {
		if (!this.initialized) {
			return false;
		}

		final UICharEvent<UI> event = UICharEvent.create(this, codepoint);
		return this.fireInput(UI.HANDLE_CHAR_TYPED, "charTyped", event, node -> node.fireCharTyped(codepoint, event), () -> {});
	}

	public final <T extends UI> @NonNull T handleKeyPressed(final @NonNull EventListener<UIKeyEvent<T>> listener) {
		return this.listen(UI.HANDLE_KEY_PRESSED, listener);
	}

	public final boolean fireKeyPressed(final @NonNull Key physicalKey, final @NonNull Key key) {
		if (!this.initialized) {
			return false;
		}

		final UIKeyEvent<UI> event = UIKeyEvent.create(this, physicalKey, key);
		this.fireShortcuts(key, true, event);
		return this.fireInput(UI.HANDLE_KEY_PRESSED, "keyPressed", event, node -> node.fireKeyPressed(physicalKey, key, event), () -> {
			this.fireShortcuts(key, false, event);
			if (this.data.zoomable() && !event.isConsumed()) {
				final double zoom = this.zoom;
				if ((key == Key.NUMPAD_ADD || key == Key.EQUAL) && (UI.isCtrlKeyDown() || UI.isAltKeyDown()) && this.zoomValue.change(zoom + 0.1D) && this.zoom != zoom) {
					event.consume();
				}

				if ((key == Key.NUMPAD_SUBTRACT || key == Key.MINUS) && (UI.isCtrlKeyDown() || UI.isAltKeyDown()) && this.zoomValue.change(zoom - 0.1D) && this.zoom != zoom) {
					event.consume();
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
		});
	}

	public final <T extends UI> @NonNull T handleKeyReleased(final @NonNull EventListener<UIKeyEvent<T>> listener) {
		return this.listen(UI.HANDLE_KEY_RELEASED, listener);
	}

	public final boolean fireKeyReleased(final @NonNull Key physicalKey, final @NonNull Key key) {
		if (!this.initialized) {
			return false;
		}

		final UIKeyEvent<UI> event = UIKeyEvent.create(this, physicalKey, key);
		return this.fireInput(UI.HANDLE_KEY_RELEASED, "keyReleased", event, node -> node.fireKeyReleased(physicalKey, key, event), () -> {});
	}

	public final void fireUpdate() {
		this.dispatch("update", () -> {
			this.nodeList.forEach(Node::fireUpdate);
			this.update();
		});
	}

	public final <T extends UI> @NonNull T beforeClose(final @NonNull EventListener<UICloseEvent.Pre<T>> listener) {
		return this.listen(UI.BEFORE_CLOSE, listener);
	}

	public final <T extends UI> @NonNull T onClose(final @NonNull EventListener<UICloseEvent.Post<T>> listener) {
		return this.listen(UI.CLOSE, listener);
	}

	public final boolean fireClose() {
		if (this.transition != null && this.transition.getOut().isRunning()) {
			return false;
		}

		final UICloseEvent.Pre<UI> event = this.fire(UI.BEFORE_CLOSE, () -> UICloseEvent.Pre.create(this));
		if (event != null && event.isCancelled()) {
			return false;
		}

		if (this.transition != null && this.transition.getOut().isEnabled() && this.data.visible() && this.drawnFrame + 1L >= UI.frameNumber) {
			final TransitionStep in = this.transition.getIn();
			final float progress = in.isRunning() ? Math.min(1F, Math.max(0F, 1F - in.getAnimator().peek())) : 0F;
			in.stop();
			this.transition.getOut().start(progress);
			this.transition.getOut().getAnimator().onComplete(tween -> {
				this.dispose();
				this.getBridge().close(this);
			});
			return false;
		}

		this.dispose();
		return true;
	}

	public final <T extends UI> @NonNull T zoom(final double zoom) {
		return this.zoomValue.start(zoom);
	}

	public final <T extends UI> @NonNull T zoom(final @NonNull Signal<Double> zoom) {
		return this.zoomValue.bind(zoom);
	}

	public final <T extends UI> @NonNull T zoomTo(final double zoom) {
		this.zoomValue.change(zoom);
		return (T) this;
	}

	public final <T extends UI> @NonNull T zoomBy(final double delta) {
		return this.zoomTo(this.zoom + delta);
	}

	public final <T extends UI> @NonNull T beforeZoom(final @NonNull EventListener<UIChangeEvent.Pre<T, Double>> listener) {
		return this.zoomValue.before(listener);
	}

	public final <T extends UI> @NonNull T onZoom(final @NonNull EventListener<UIChangeEvent.Post<T, Double>> listener) {
		return this.zoomValue.on(listener);
	}

	public final @NonNull List<@NonNull Task> getTaskList() {
		return Collections.unmodifiableList(this.taskList);
	}

	public final @NonNull List<@NonNull Node> getNodeList() {
		return this.nodeList.ordered();
	}

	public final @NonNull List<@NonNull Shortcut> getShortcutList() {
		return Collections.unmodifiableList(this.shortcutList);
	}

	@Override
	public int getIndex() {
		return this.data.zindex();
	}

	public final double getZoom() {
		return this.view.getZoom();
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
		if (this.transition != null) {
			this.transition.getIn().stop();
			this.transition.getOut().stop();
		}

		this.scoped(() -> this.nodeList.forEach(Node::fireDetach));
		this.taskList.forEach(Task::detach);
		this.shortcutList.forEach(Shortcut::detach);
		this.bindings.unsubscribe();
		this.closed = true;
		this.initialized = false;

		if (this.watched) {
			this.watched = false;
			HotReload.unwatch(this);
		}

		UIStoreHook.saveAll();
		this.storeMap.values().forEach(UIStoreHook::destroyStore);
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

		this.drawnFrame = UI.frameNumber;
		this.untraced(() -> this.drawFrame(mouseX, mouseY));
	}

	public void drawHover(final @NonNull Object content, final double mouseX, final double mouseY) {
		this.getBridge().drawHover(this, content, mouseX, mouseY);
	}

	public final void mask(final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Runnable drawing) {
		this.mask(() -> DrawUtils.SHAPE.drawRect(maskX, maskY, maskWidth, maskHeight, Color.RED), drawing);
	}

	public final void mask(final @NonNull Resource resource, final double maskX, final double maskY, final double maskWidth, final double maskHeight, final @NonNull Runnable drawing) {
		this.mask(() -> {
			BridgeHandler.RENDER.get().getState().alphaCutoff(0.5F);
			DrawUtils.RESOURCE.drawResource(maskX, maskY, maskWidth, maskHeight, resource);
		}, drawing);
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

			final Map<Class<? extends Annotation>, Annotation> annotations = UI.annotations(this.getClass());
			AnnotationValues.merge(this.data, this.annotationMap.get(UIData.class), annotations.get(UIData.class));
			AnnotationValues.merge(this.debug, this.annotationMap.get(UIDataDebug.class), annotations.get(UIDataDebug.class));
			AnnotationValues.merge(this.popup, this.annotationMap.get(UIDataPopup.class), annotations.get(UIDataPopup.class));
			AnnotationValues.merge(this.scale, this.annotationMap.get(UIDataScale.class), annotations.get(UIDataScale.class));
			AnnotationValues.merge(this.overlay, this.annotationMap.get(UIDataOverlay.class), annotations.get(UIDataOverlay.class));
			this.annotationMap = annotations;
			this.refreshTransition();

			this.initialized = false;
			this.load(this.view.getWidth(), this.view.getHeight(), this.view.getZoom());
		});
	}

	public final @NonNull UI renew() {
		final IUIBridge bridge = BridgeHandler.UI.get(this);
		if (bridge == null || !bridge.isOpen(this)) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " is not open, only an open UI can be renewed: open it before renew()");
		}

		final UI renewed;
		try {
			final Constructor<? extends UI> constructor = this.getClass().getDeclaredConstructor();
			constructor.setAccessible(true);
			renewed = constructor.newInstance();
		} catch (final NoSuchMethodException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead", exception);
		} catch (final ReflectiveOperationException | RuntimeException exception) {
			throw new IllegalStateException("The UI " + this.getClass().getName() + " cannot be renewed: its constructor without argument failed (" + exception + "), fix the constructor from this error", exception);
		}

		this.dispose();
		bridge.remove(this);
		bridge.add(renewed);
		return renewed;
	}

	public final double lerpByFramerate(final double value, final double target, final double speed, final double snapDiff) {
		final double diff = target - value;
		if (Math.abs(diff) <= snapDiff) {
			return target;
		}

		return value + diff * (1D - Math.pow(1D - Math.min(1D, speed), this.frameTime / (1000D / 60D)));
	}

	public final void add(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (node.getParent() != null) {
				node.getParent().remove(node);
			} else if (node.isAttached() && node.getUi() != this) {
				node.getUi().remove(node);
			}

			if (!node.isAttached()) {
				node.load(this);
			}
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

	public final <T extends UI> @NonNull T addTask(final @NonNull Task task) {
		task.detach();
		this.taskList.add(task.owner(this, null));
		return (T) this;
	}

	public final <T extends UI> @NonNull T removeTask(final @NonNull Task task) {
		if (this.taskList.remove(task)) {
			task.owner(null, null);
		}
		return (T) this;
	}

	public final <T extends UI> @NonNull T addShortcut(final @NonNull Shortcut shortcut) {
		shortcut.detach();
		this.shortcutList.add(shortcut.owner(this, null));
		return (T) this;
	}

	public final <T extends UI> @NonNull T removeShortcut(final @NonNull Shortcut shortcut) {
		if (this.shortcutList.remove(shortcut)) {
			shortcut.owner(null, null);
		}
		return (T) this;
	}

	public final <T extends UI> @NonNull T depthLevel(final double depthLevel) {
		this.depthLevel = depthLevel;
		return (T) this;
	}

	public final <T extends UI> @NonNull T transition(final Transition transition) {
		this.transition = transition;
		this.customTransition = true;
		return (T) this;
	}

	public final <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final @NonNull Supplier<T> factory) {
		if (this.storeMap.containsKey(clazz)) {
			return (T) this.storeMap.get(clazz);
		}

		final T store = UIStoreHook.useStore(clazz, factory);
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

	private void renderHoveredNode(final double mouseX, final double mouseY) {
		BridgeHandler.RENDER.get().withState(render -> render.getModelView().with(matrix -> {
			render.getState().depthTest(false).depthWrite(false);
			Node hovered = this.getHoveredNode();
			while (hovered != null && !hovered.renderHover(mouseX, mouseY)) {
				hovered = hovered.getParent();
			}
		}));
	}

	private void refreshTransition() {
		final PopupTransition popupTransition = this.popup.active() ? this.popup.transition() : PopupTransition.NONE;
		if (this.customTransition || popupTransition == this.popupTransition) {
			return;
		}

		this.popupTransition = popupTransition;
		this.transition = UI.createPopupTransition(popupTransition);
	}

	private void refreshWatch() {
		final boolean watch = JOIDEnv.isDevMode() && this.debug.hotreload();
		if (watch == this.watched) {
			return;
		}

		this.watched = watch;
		if (watch) {
			HotReload.watch(this, () -> this.reloadPending = true);
		} else {
			HotReload.unwatch(this);
		}
	}

	private void mask(final Runnable shape, final Runnable drawing) {
		try {
			this.startMask(shape);
			drawing.run();
		} finally {
			this.stopMask();
		}
	}

	private void startMask(final Runnable shape) {
		this.maskDepth++;
		BridgeHandler.RENDER.get().withState(render -> {
			if (this.maskDepth == 1) {
				render.clearStencil();
			}

			render.getState().stencil(StencilState.create(StencilFunction.EQUAL, this.maskDepth - 1, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.INCREMENT));
			render.getState().colorWrite(false);
			shape.run();
		});
		BridgeHandler.RENDER.get().getState().stencil(StencilState.create(StencilFunction.EQUAL, this.maskDepth, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
	}

	private void stopMask() {
		this.maskDepth--;
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (this.maskDepth == 0) {
			render.getState().stencil(StencilState.DISABLED);
			render.clearStencil();
		} else {
			render.getState().stencil(StencilState.create(StencilFunction.EQUAL, this.maskDepth, 0xFF, StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP));
		}
	}

	private void drawFrame(final double mouseX, final double mouseY) {
		final long start = BridgeHandler.CLOCK.get().nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();

		this.frameTime = this.lastFrame == 0L ? 1000D / 60D : (start - this.lastFrame) / 1_000_000D;
		this.lastFrame = start;
		this.frameCount++;

		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.view.anchorX(this.data.getAnchorPositionX()).anchorY(this.data.getAnchorPositionY());
		this.refreshTransition();
		if (!this.closed) {
			this.refreshWatch();
		}
		final IUIBridge bridge = this.getBridge();
		this.onTop = bridge != null && bridge.isOnTop(this);

		final List<Task> taskList = new ArrayList<>(this.taskList);
		this.nodeList.forEach(node -> UI.collectTasks(node, taskList));
		for (final Task task : taskList) {
			if (task.isDue()) {
				this.traced(task::fireRun);
			}
		}

		final double interfaceScale = bridge == null || !this.scale.active() ? 1D : this.scale.apply(bridge.getInterfaceScale(this));
		if (interfaceScale != this.view.getInterfaceScale()) {
			this.view.interfaceScale(interfaceScale);
			this.zoomValue.update(this.zoom);
		}

		final double mx = this.getMouseX();
		final double my = this.getMouseY();

		final Transition transition = this.transition;
		if (this.data.background()) {
			final float alpha = this.data.getBackgroundColor().getAlpha() / 255F;
			float opacity = alpha;
			if (transition != null) {
				if (transition.getIn().isRunning()) {
					opacity = Math.min(1F, Math.max(0F, transition.getIn().getAnimator().peek())) * alpha;
				}

				if (transition.getOut().isRunning()) {
					opacity = Math.min(1F, Math.max(0F, 1F - transition.getOut().getAnimator().peek())) * alpha;
				}
			}
			DrawUtils.SHAPE.drawRect(0D, 0D, this.view.getWidth(), this.view.getHeight(), this.data.getBackgroundColor().withAlpha(opacity));
		}

		render.getState().alphaCutoff(0F);
		final Runnable body = () -> this.view.render(render, this.data.projection(), () -> {
			this.depthLevel = 0D;
			double lastDepthLevel = this.depthLevel;

			for (final Node node : this.nodeList.ordered()) {
				render.getModelView().translate(0D, 0D, this.depthLevel - lastDepthLevel);
				lastDepthLevel = this.depthLevel;
				node.render(mx, my);
			}

			if (this.getHoveredNode() != null) {
				this.renderHoveredNode(mx, my);
			}
		});

		if (transition != null) {
			transition.getIn().render(this, () -> transition.getOut().render(this, body));
		} else {
			body.run();
		}

		final long end = BridgeHandler.CLOCK.get().nanoTime();
		this.renderTime = end - start;

		if (JOIDEnv.isDevMode() && this.debug.profiler()) {
			final float ms = this.renderTime / 1_000_000F;
			if (ms > 16.66F) {
				JOIDLog.warn(this.getClass().getSimpleName() + " took " + String.format(Locale.ROOT, "%.2f", ms) + "ms to draw a frame, more than the 16.66ms of 60 fps: move the heavy work out of draw() or spread it over several frames");
			}
		}

		if (this.lastFpsUpdate == 0L) {
			this.fps = 0D;
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
		shortcutList.addAll(this.shortcutList);
		for (final Shortcut shortcut : shortcutList) {
			if (shortcut.isOverrideFocus() == overrideFocus && shortcut.isTriggeredBy(key)) {
				this.traced(() -> shortcut.fireTrigger(key));
				event.consume();
				return;
			}
		}
	}

	private <T extends UI> T listen(final EventType<?> type, final EventListener<?> listener) {
		this.eventBus.listen(type, listener);
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

	private <E extends UIInputEvent<UI>> boolean fireInput(final EventType<?> type, final String input, final E event, final Consumer<Node> dispatch, final Runnable builtin) {
		if (!event.isConsumed()) {
			this.dispatch(input, () -> this.nodeList.reversed().forEach(dispatch));
		}
		this.fireListeners(type, event);
		builtin.run();
		this.fireMonitor(type, event);
		return event.isConsumed();
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
		SignalReplay.run(this, null, () -> this.scoped(runnable));
	}

	private void dispatch(final String input, final Runnable runnable) {
		try {
			this.untraced(runnable);
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("UI." + input + "." + System.identityHashCode(this) + "." + exception.getClass().getName(), "The " + input + " of " + JOIDLog.nameOf(this.getClass()) + " failed (" + exception + "), the next inputs still run: catch the exception inside the handler to handle it", exception);
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

	private static Map<Class<? extends Annotation>, Annotation> annotations(final Class<?> type) {
		final Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<>();
		for (final Annotation annotation : type.getAnnotations()) {
			annotationMap.put(annotation.annotationType(), annotation);
		}
		return Collections.unmodifiableMap(annotationMap);
	}

	private static Transition createPopupTransition(final PopupTransition popupTransition) {
		if (!popupTransition.isActive()) {
			return null;
		}

		final Transition transition = PopTransition.create();
		if (!popupTransition.isIn()) {
			transition.getIn().enabled(false);
		}

		if (!popupTransition.isOut()) {
			transition.getOut().enabled(false);
		}
		return transition;
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

}