package dev.joid.lib.ui.node;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.draw.text.TextMode;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.binding.NodeBindings;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.IConsumableEvent;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.event.impl.node.NodeAnimateEvent;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeChildEvent;
import dev.joid.lib.ui.event.impl.node.NodeDragEvent;
import dev.joid.lib.ui.event.impl.node.NodeDrawEvent;
import dev.joid.lib.ui.event.impl.node.NodeHoverEvent;
import dev.joid.lib.ui.event.impl.node.NodeInputEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.event.impl.node.NodeScrollEvent;
import dev.joid.lib.ui.event.impl.node.NodeWatchEvent;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.impl.control.scrollbar.BaseScrollbarNode;
import dev.joid.lib.ui.node.layer.NodeLayer;
import dev.joid.lib.ui.node.property.DraggableProperty;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableAreaType;
import dev.joid.lib.ui.node.property.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.node.property.PositionProperty;
import dev.joid.lib.ui.node.property.WatchProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.align.Axis;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;
import dev.joid.lib.utils.list.IndexedList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Node implements INode {

	private static final Gson GSON        = new GsonBuilder().create();
	private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final EventType<NodeMouseEvent<Node>> CLICK = EventType.create("onClick");

	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_PRESSED  = EventType.create("handleMousePressed");
	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_RELEASED = EventType.create("handleMouseReleased");
	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_DRAGGED  = EventType.create("handleMouseDragged");
	private static final EventType<NodeMouseScrollEvent<Node>> HANDLE_MOUSE_SCROLL   = EventType.create("handleMouseScroll");
	private static final EventType<NodeCharEvent<Node>>        HANDLE_CHAR_TYPED     = EventType.create("handleCharTyped");
	private static final EventType<NodeKeyEvent<Node>>         HANDLE_KEY_PRESSED    = EventType.create("handleKeyPressed");
	private static final EventType<NodeKeyEvent<Node>>         HANDLE_KEY_RELEASED   = EventType.create("handleKeyReleased");

	private static final EventType<NodeEvent<Node>>      INIT         = EventType.create("onInit");
	private static final EventType<NodeEvent<Node>>      UPDATE       = EventType.create("onUpdate");
	private static final EventType<NodeDrawEvent<Node>>  RENDER       = EventType.create("onRender");
	private static final EventType<NodeDrawEvent<Node>>  DRAW         = EventType.create("onDraw");
	private static final EventType<NodeChildEvent<Node>> CHILD_APPEND = EventType.create("onChildAppend");
	private static final EventType<NodeEvent<Node>>      DETACH       = EventType.create("onDetach");

	private static final EventType<NodeEvent<Node>>        MOUNT   = EventType.create("onMount");
	private static final EventType<NodeWatchEvent<Node>>   WATCH   = EventType.create("onWatch");
	private static final EventType<NodeAnimateEvent<Node>> ANIMATE = EventType.create("onAnimate");

	private static final EventType<NodeScrollEvent.Pre<Node>>  BEFORE_SCROLL      = EventType.create("beforeScroll");
	private static final EventType<NodeScrollEvent.Post<Node>> SCROLL             = EventType.create("onScroll");
	private static final EventType<NodeScrollEvent.Post<Node>> SCROLL_TO_END      = EventType.create("onScrollToEnd");
	private static final EventType<NodeScrollEvent.Post<Node>> SCROLL_END_REACHED = EventType.create("onScrollEndReached");

	private static final EventType<NodeChangeEvent.Pre<Node, Point>>  BEFORE_MOVE = EventType.create("beforeMove");
	private static final EventType<NodeChangeEvent.Post<Node, Point>> MOVE        = EventType.create("onMove");

	private static final EventType<NodeDragEvent.Pre<Node>>  BEFORE_DRAG_START = EventType.create("beforeDragStart");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_START        = EventType.create("onDragStart");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG              = EventType.create("onDrag");
	private static final EventType<NodeDragEvent.Pre<Node>>  BEFORE_DRAG_DROP  = EventType.create("beforeDragDrop");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_DROP         = EventType.create("onDragDrop");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_SNAP         = EventType.create("onDragSnap");

	private static final EventType<NodeHoverEvent<Node>> HOVER_START = EventType.create("onHoverStart");
	private static final EventType<NodeHoverEvent<Node>> HOVER       = EventType.create("onHover");
	private static final EventType<NodeHoverEvent<Node>> HOVER_END   = EventType.create("onHoverEnd");

	private final transient EventBus      eventBus;
	private final transient TweenAnimator hoverAnimator;

	@Getter(AccessLevel.PROTECTED)
	private final transient NodeBindings bindings;

	@Getter(AccessLevel.NONE) private final transient List<Task>               taskList;
	@Getter(AccessLevel.NONE) private final transient List<Shortcut>           shortcutList;
	@Getter(AccessLevel.NONE) private final transient List<Runnable>           requirementList;
	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Point>  positionValue;
	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Double> scrollXValue;
	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Double> scrollYValue;

	@Getter(AccessLevel.NONE) private final transient List<Node>                      children;
	@Getter(AccessLevel.NONE) private final transient IndexedList<Node>               drawList;
	@Getter(AccessLevel.NONE) private final transient Map<Class<?>, NodeEffect<Node>> effectMap;

	@Getter(AccessLevel.NONE) private final LinkedList<NodeLayer>        layerList;
	@Getter(AccessLevel.NONE) private final List<Predicate<Node>>        waitingList;
	@Getter(AccessLevel.NONE) private final List<IHoverElement>          hoverElementList;
	@Getter(AccessLevel.NONE) private final Map<TweenAnimator, Float>    animatorMap;
	@Getter(AccessLevel.NONE) private final List<Supplier<List<String>>> hoverSupplierList;

	private double defaultWidth;
	private double defaultHeight;

	private transient UI   ui;
	private transient Node parent;

	private transient Node              skeleton;
	private transient Node              overflowArea;
	private transient Consumer<Node>    bodyConsumer;
	private transient BaseScrollbarNode scrollbar;

	private double defaultX;
	private double defaultY;

	private transient double layoutX;
	private transient double layoutY;

	private transient double shiftX;
	private transient double shiftY;

	private double x;
	private double y;
	private double width;
	private double height;

	private boolean visible;
	private boolean enabled;
	private boolean interactive;

	private Align            anchorX;
	private Align            anchorY;
	private PositionProperty position;
	private OverflowProperty overflow;

	private DraggableProperty draggable;

	private transient double  dragX;
	private transient double  dragY;
	private transient boolean dragged;
	private transient Node    dragSource;
	private transient Node    draggedNode;
	private transient boolean dragging;
	private transient boolean stepping;
	private transient double  startDragX;
	private transient double  startDragY;
	private transient double  targetDragX;
	private transient double  targetDragY;

	private int    zindex;
	private double zlevel;

	private double aspectRatio;

	private Text label;

	private transient boolean mounted;
	private transient boolean attached;

	@Getter(AccessLevel.NONE)
	private transient boolean pulled;

	private Cursor        cursor;
	private long          hoverDuration;
	private TweenEquation hoverEquation;

	private transient boolean hovered;

	@Getter(AccessLevel.NONE)
	private transient boolean hoverShown;

	private double scrollX;
	private double scrollY;
	private double scrollSpeed;
	private double targetScrollX;
	private double targetScrollY;

	private transient double  maxScrollX;
	private transient double  maxScrollY;
	private transient boolean scrollEndX;
	private transient boolean scrollEndY;

	@Getter(AccessLevel.NONE) private transient boolean scrollMeasured;
	@Getter(AccessLevel.NONE) private transient boolean scrollEndGuessX;
	@Getter(AccessLevel.NONE) private transient boolean scrollEndGuessY;

	@Getter(AccessLevel.NONE) private transient double  restX;
	@Getter(AccessLevel.NONE) private transient double  restY;
	@Getter(AccessLevel.NONE) private transient double  drawnX;
	@Getter(AccessLevel.NONE) private transient double  drawnY;
	@Getter(AccessLevel.NONE) private transient boolean moving;

	private transient long        lastClickTime;
	private transient MouseButton lastClickButton;

	private transient Key  lastKey;
	private transient long lastKeyTime;
	private transient int  lastCodepoint;

	private transient long loadCount;
	private transient long renderTime;

	protected Node(final double x, final double y) {
		this(x, y, 0D, 0D);
	}

	protected Node(final double x, final double y, final double width, final double height) {
		this.eventBus = EventBus.create(this::describe);
		this.bindings = new NodeBindings(this, this.eventBus, () -> JOIDLog.nameOf(this.getClass()), this::replay);

		this.taskList        = new CopyOnWriteArrayList<>();
		this.shortcutList    = new CopyOnWriteArrayList<>();
		this.waitingList     = new ArrayList<>();
		this.requirementList = new ArrayList<>();
		this.positionValue   = InteractiveValue.create(this.bindings, "move", () -> Point.of(this.defaultX, this.defaultY), this::locate).events(Node.BEFORE_MOVE, (previous, value) -> NodeChangeEvent.Pre.create(this, previous, value), Node.MOVE, (previous, value) -> NodeChangeEvent.Post.create(this, previous, value));
		this.scrollXValue    = this.createScrollValue(Axis.HORIZONTAL);
		this.scrollYValue    = this.createScrollValue(Axis.VERTICAL);
		this.animatorMap     = new HashMap<>();
		this.hoverAnimator   = TweenAnimator.create();

		this.children  = new CopyOnWriteArrayList<>();
		this.drawList  = IndexedList.concurrent();
		this.layerList = new LinkedList<>();
		this.effectMap = new LinkedHashMap<>();

		this.hoverElementList  = new LinkedList<>();
		this.hoverSupplierList = new LinkedList<>();

		this.defaultX = this.x = this.drawnX = x;
		this.defaultY = this.y = this.drawnY = y;

		this.defaultWidth  = this.width  = width;
		this.defaultHeight = this.height = height;

		this.visible = true;
		this.enabled = true;
		this.interactive = true;

		this.position = PositionProperty.RELATIVE;
		this.overflow = OverflowProperty.NONE;
		this.anchorX  = Align.START;
		this.anchorY  = Align.START;

		this.aspectRatio = -1D;
		this.hoverDuration = 200L;
		this.hoverEquation = TweenEquations.LINEAR;
		this.scrollSpeed = 1D;
	}

	public final void load(final @NonNull UI ui) {
		this.requirementList.forEach(Runnable::run);
		final SignalContext context = SignalContext.current();
		final List<Signal<?>> readList = context.takeReads();
		try {
			this.ui = ui;
			this.attached = true;
			this.taskList.forEach(task -> task.owner(null, this));
			this.drawList.forEach(child -> child.load(this.ui));

			if (this.scrollbar != null) {
				this.scrollbar.load(this.ui);
			}

			if (this.skeleton != null) {
				this.skeleton.load(this.ui);
			}

			this.effectMap.values().stream().sorted(Comparator.comparingInt(NodeEffect::getPriority)).forEach(effect -> effect.init(this, this.ui));
			this.bindings.subscribe(this.loadCount > 0);
			this.replay(() -> this.init(this.ui));
			this.fire(Node.INIT, () -> NodeEvent.create(this));
		} catch (final RuntimeException exception) {
			this.fireDetach();
			throw exception;
		} finally {
			context.clearReads();
			context.putBackReads(readList);
		}

		this.loadCount++;
	}

	public final void render(final double mouseX, final double mouseY) {
		final long now = BridgeHandler.CLOCK.get().nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		try {
			render.getModelView().with(matrix -> {
				Color.reset();
				if (!this.pulled) {
					this.bindings.pull();
				}

				this.pulled = false;
				this.drawList.forEach(child -> {
					child.bindings.pull();
					child.pulled = true;
				});

				if (this.parent != null) {
					matrix.translate(this.parent.x, this.parent.y, 0D);
					if (this.position == PositionProperty.ABSOLUTE) {
						matrix.translate(-this.parent.getAbsoluteX(), -this.parent.getAbsoluteY(), 0D);
					}
				}

				matrix.translate(0D, 0D, this.zlevel);

				if (this.isVisible()) {
					if (this.aspectRatio > 0D) {
						if (this.width != 0D) {
							this.height = this.width / this.aspectRatio;
						} else if (this.height != 0D) {
							this.width = this.height * this.aspectRatio;
						}
					}

					this.place();
					this.layout();
					this.updateHover(mouseX, mouseY);
					this.scrollContent(render);
					this.animateDrag();
					this.trackMovement(render);

					final boolean wasMounted = this.mounted;
					this.mounted = this.isMounted();

					if (!wasMounted && this.mounted) {
						this.fire(Node.MOUNT, () -> NodeEvent.create(this));
					}

					this.drawContent(matrix, mouseX, mouseY);
				} else {
					if (this.dragging || this.dragged) {
						this.animateDrag();
					}
					this.endHover(true);
				}
			});
		} finally {
			Color.reset();
		}
		this.renderTime = BridgeHandler.CLOCK.get().nanoTime() - now;
	}

	public boolean renderHover(final double mouseX, final double mouseY) {
		final List<IHoverElement> hoverList = new LinkedList<>(this.hoverElementList);
		if (!this.hoverSupplierList.isEmpty()) {
			final List<String> lines = new LinkedList<>();
			for (final Supplier<List<String>> hoverSupplier : this.hoverSupplierList) {
				final List<String> hoverLines = hoverSupplier.get();
				if (hoverLines != null) {
					lines.addAll(hoverLines);
				}
			}

			if (!lines.isEmpty()) {
				hoverList.add((node, x, y) -> node.getUi().drawHover(lines, x, y));
			}
		}

		hoverList.forEach(element -> element.render(this, mouseX, mouseY));
		return !hoverList.isEmpty();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(this.x, this.y, this.width, this.height, Color.LOADING());
	}

	public final void fireUpdate() {
		this.bindings.pull();
		this.fireAnimate();
		this.drawList.ordered().forEach(Node::fireUpdate);
		if (this.scrollbar != null) {
			this.scrollbar.fireUpdate();
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireUpdate();
		}

		try {
			this.update();
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("Node.update." + this.getClass().getName() + "." + exception.getClass().getName(), "The update of " + this.describe() + " failed (" + exception + "), the next updates still run: catch the exception inside update() to handle it", exception);
		}
		this.fire(Node.UPDATE, () -> NodeEvent.create(this));
	}

	public final <T extends Node> @NonNull T onUpdate(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.UPDATE, listener);
	}

	public final void fireDetach() {
		if (!this.attached) {
			return;
		}

		this.drawList.forEach(Node::fireDetach);

		if (this.scrollbar != null) {
			this.scrollbar.fireDetach();
		}

		if (this.skeleton != null) {
			this.skeleton.fireDetach();
		}

		this.attached = false;
		try {
			this.detach();
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("Node.detach." + this.getClass().getName() + "." + exception.getClass().getName(), "The detach of " + this.describe() + " failed (" + exception + "), the node is detached anyway: catch the exception inside detach() to handle it", exception);
		}
		this.endInteraction();
		this.bindings.unsubscribe();
		this.hoverElementList.forEach(IHoverElement::detach);
		this.effectMap.values().forEach(effect -> effect.detach(this));
		this.fire(Node.DETACH, () -> NodeEvent.create(this));
	}

	public final <T extends Node> @NonNull T onDetach(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.DETACH, listener);
	}

	public final <T extends Node> @NonNull T handleMousePressed(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_PRESSED, listener);
	}

	public final boolean fireMousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, null);
		this.fireMousePressed(mouseX, mouseY, button, input);
		return input.isConsumed();
	}

	public final void fireMousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull IConsumableEvent source) {
		if (!this.isShown() || !this.isInteractive()) {
			return;
		}

		this.lastClickButton = button;
		this.lastClickTime = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.dispatch(NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, source), this.reactsToInput(), Node.HANDLE_MOUSE_PRESSED, (node, event) -> node.fireMousePressed(mouseX, mouseY, button, event), event -> {
			final boolean click = !event.isConsumed() && button.isLeft() && this.isHovered() && this.hasListener(Node.CLICK);
			this.mousePressed(event);
			if (click) {
				this.fire(Node.CLICK, () -> NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, null));
				event.consume();
			}

			if (!event.isConsumed() && this.draggable != null && this.draggable.isEnabled(this) && button.isLeft() && this.isHovered()) {
				this.startDragging(mouseX, mouseY);
				if (this.dragging) {
					event.consume();
				}
			}
		});
	}

	public final <T extends Node> @NonNull T handleMouseReleased(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_RELEASED, listener);
	}

	public final boolean fireMouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, null);
		this.fireMouseReleased(mouseX, mouseY, button, deltaTime, input);
		return input.isConsumed();
	}

	public final void fireMouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull IConsumableEvent source) {
		if (this.dragging && button.isLeft()) {
			this.stopDragging();
		}

		this.dispatch(NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, source), true, Node.HANDLE_MOUSE_RELEASED, (node, event) -> node.fireMouseReleased(mouseX, mouseY, button, deltaTime, event), this::mouseReleased);
	}

	public final <T extends Node> @NonNull T handleMouseDragged(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_DRAGGED, listener);
	}

	public final boolean fireMouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, null);
		this.fireMouseDragged(mouseX, mouseY, button, deltaTime, input);
		return input.isConsumed();
	}

	public final void fireMouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull IConsumableEvent source) {
		if (this.isShown() && this.isInteractive()) {
			this.dispatch(NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, source), this.reactsToInput(), Node.HANDLE_MOUSE_DRAGGED, (node, event) -> node.fireMouseDragged(mouseX, mouseY, button, deltaTime, event), this::mouseDragged);
		}

		if (this.dragging) {
			this.fireDrag(() -> {
				this.targetDragX = mouseX - this.dragX;
				this.targetDragY = mouseY - this.dragY;
			});
		}
	}

	public final <T extends Node> @NonNull T handleMouseScroll(final @NonNull EventListener<NodeMouseScrollEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_SCROLL, listener);
	}

	public final boolean fireMouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
		final NodeMouseScrollEvent<Node> input = NodeMouseScrollEvent.create(this, mouseX, mouseY, notchesX, notchesY, null);
		this.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, input);
		return input.isConsumed();
	}

	public final void fireMouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull IConsumableEvent source) {
		if (!this.isShown() || !this.isInteractive()) {
			return;
		}

		this.dispatch(NodeMouseScrollEvent.create(this, mouseX, mouseY, notchesX, notchesY, source), this.reactsToInput(), Node.HANDLE_MOUSE_SCROLL, (node, event) -> node.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, event), event -> {
			if (!event.isConsumed() && this.isHovered()) {
				final double speed = UI.isCtrlKeyDown() ? this.scrollSpeed * 2D : this.scrollSpeed;
				if (notchesY != 0D && this.hasOverflowY()) {
					if (this.canScroll(this.scrollYValue, this.maxScrollY, notchesY)) {
						this.scrollYValue.set(Math.min(0D, this.scrollYValue.get() + notchesY * 30D * speed));
						event.consume();
					}
				} else if (notchesY != 0D && notchesX == 0D && this.hasOverflowX() && this.canScroll(this.scrollXValue, this.maxScrollX, notchesY)) {
					this.scrollXValue.set(Math.min(0D, this.scrollXValue.get() + notchesY * 30D * speed));
					event.consume();
				}

				if (notchesX != 0D && this.hasOverflowX() && this.canScroll(this.scrollXValue, this.maxScrollX, notchesX)) {
					this.scrollXValue.set(Math.min(0D, this.scrollXValue.get() + notchesX * 30D * speed));
					event.consume();
				}
			}

			this.mouseScroll(event);
		});
	}

	public final <T extends Node> @NonNull T handleCharTyped(final @NonNull EventListener<NodeCharEvent<T>> listener) {
		return this.listen(Node.HANDLE_CHAR_TYPED, listener);
	}

	public final boolean fireCharTyped(final int codepoint) {
		final NodeCharEvent<Node> input = NodeCharEvent.create(this, codepoint, null);
		this.fireCharTyped(codepoint, input);
		return input.isConsumed();
	}

	public final void fireCharTyped(final int codepoint, final @NonNull IConsumableEvent source) {
		if (!this.isShown()) {
			return;
		}

		this.lastCodepoint = codepoint;
		this.dispatch(NodeCharEvent.create(this, codepoint, source), this.reactsToInput(), Node.HANDLE_CHAR_TYPED, (node, event) -> node.fireCharTyped(codepoint, event), this::charTyped);
	}

	public final <T extends Node> @NonNull T handleKeyPressed(final @NonNull EventListener<NodeKeyEvent<T>> listener) {
		return this.listen(Node.HANDLE_KEY_PRESSED, listener);
	}

	public final boolean fireKeyPressed(final @NonNull Key physicalKey, final @NonNull Key key) {
		final NodeKeyEvent<Node> input = NodeKeyEvent.create(this, physicalKey, key, null);
		this.fireKeyPressed(physicalKey, key, input);
		return input.isConsumed();
	}

	public final void fireKeyPressed(final @NonNull Key physicalKey, final @NonNull Key key, final @NonNull IConsumableEvent source) {
		if (!this.isShown()) {
			return;
		}

		this.lastKey = key;
		this.lastKeyTime = BridgeHandler.CLOCK.get().currentTimeMillis();
		this.dispatch(NodeKeyEvent.create(this, physicalKey, key, source), this.reactsToInput(), Node.HANDLE_KEY_PRESSED, (node, event) -> node.fireKeyPressed(physicalKey, key, event), this::keyPressed);
	}

	public final <T extends Node> @NonNull T handleKeyReleased(final @NonNull EventListener<NodeKeyEvent<T>> listener) {
		return this.listen(Node.HANDLE_KEY_RELEASED, listener);
	}

	public final boolean fireKeyReleased(final @NonNull Key physicalKey, final @NonNull Key key) {
		final NodeKeyEvent<Node> input = NodeKeyEvent.create(this, physicalKey, key, null);
		this.fireKeyReleased(physicalKey, key, input);
		return input.isConsumed();
	}

	public final void fireKeyReleased(final @NonNull Key physicalKey, final @NonNull Key key, final @NonNull IConsumableEvent source) {
		this.dispatch(NodeKeyEvent.create(this, physicalKey, key, source), true, Node.HANDLE_KEY_RELEASED, (node, event) -> node.fireKeyReleased(physicalKey, key, event), this::keyReleased);
	}

	public final <T extends Node> @NonNull T append(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			final boolean moved = node.parent == this;
			if (node.parent != null && !moved) {
				node.parent.remove(node);
			} else if (node.parent == null && node.ui != null && node.ui.getNodeList().contains(node)) {
				node.ui.remove(node);
			}

			node.parent = this;
			if (this.attached && !node.attached) {
				try {
					node.load(this.ui);
				} catch (final RuntimeException exception) {
					node.parent = null;
					throw exception;
				}
			}
			this.drawList.remove(node);
			this.drawList.add(node);
			this.children.remove(node);
			this.children.add(node);
			if (!moved) {
				this.fire(Node.CHILD_APPEND, () -> NodeChildEvent.create(this, node));
			}
		}

		this.scrollMeasured = false;
		return (T) this;
	}

	public final <T extends Node> @NonNull T remove(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (this.drawList.contains(node)) {
				node.fireDetach();
				this.drawList.remove(node);
				this.children.remove(node);
				node.parent = null;
				node.layoutX = node.layoutY = node.shiftX = node.shiftY = 0D;
				node.clearOverflowArea(node.overflowArea);
			}
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T attach(final @NonNull UI ui) {
		ui.add(this);
		return (T) this;
	}

	public final <T extends Node> @NonNull T attach(final @NonNull Node node) {
		node.append(this);
		return (T) this;
	}

	public final <T extends Node> @NonNull T body(final @NonNull Runnable runnable) {
		return this.body(n -> runnable.run());
	}

	public final <T extends Node> @NonNull T body(final @NonNull Consumer<@NonNull T> consumer) {
		this.bodyConsumer = (Consumer<Node>) consumer;
		this.replay(() -> this.bodyConsumer.accept(this));
		return (T) this;
	}

	public final <T extends Node> @NonNull T self(final @NonNull Consumer<@NonNull T> consumer) {
		this.replay(() -> consumer.accept((T) this));
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearChildren() {
		return this.remove(this.children.toArray(new Node[0]));
	}

	public final <T extends Node> @NonNull T copy() {
		return (T) this.duplicate(false);
	}

	public final <T extends UIStore> @NonNull T useStore(final @NonNull Class<T> clazz, final @NonNull Supplier<T> factory) {
		return this.getUi().useStore(clazz, factory);
	}

	public final <T extends Node> @NonNull T addTask(final @NonNull Task task) {
		task.detach();
		this.taskList.add(task.owner(null, this));
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeTask(final @NonNull Task task) {
		if (this.taskList.remove(task)) {
			task.owner(null, null);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T addShortcut(final @NonNull Shortcut shortcut) {
		shortcut.detach();
		this.shortcutList.add(shortcut.owner(null, this));
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeShortcut(final @NonNull Shortcut shortcut) {
		if (this.shortcutList.remove(shortcut)) {
			shortcut.owner(null, null);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T layoutPosition(final double layoutX, final double layoutY) {
		this.layoutX = layoutX;
		this.layoutY = layoutY;
		this.place();
		return (T) this;
	}

	public final <T extends Node> @NonNull T layoutSize(final double width, final double height) {
		this.width = width;
		this.height = height;
		return (T) this;
	}

	public final <T extends Node> @NonNull T fit(final double contentWidth, final double contentHeight) {
		return this.layoutSize(this.defaultWidth == 0D ? contentWidth : this.width, this.defaultHeight == 0D ? contentHeight : this.height);
	}

	public final <T extends Node> @NonNull T x(final double x) {
		return this.x(Signal.from(x));
	}

	public final <T extends Node> @NonNull T x(final @NonNull Signal<Double> x) {
		if (x.isWritable()) {
			this.bind("x", x, value -> this.positionValue.update(Point.of(value, this.defaultY)), Point::getX);
		} else {
			this.follow("x", x, value -> this.positionValue.update(Point.of(value, this.defaultY)));
		}
		this.checkMovable(this.draggable);
		return (T) this;
	}

	public final <T extends Node> @NonNull T x(final @NonNull Supplier<Double> x) {
		this.follow("x", x, value -> this.positionValue.update(Point.of(value, this.defaultY)));
		this.checkMovable(this.draggable);
		return (T) this;
	}

	public final <T extends Node> @NonNull T y(final double y) {
		return this.y(Signal.from(y));
	}

	public final <T extends Node> @NonNull T y(final @NonNull Signal<Double> y) {
		if (y.isWritable()) {
			this.bind("y", y, value -> this.positionValue.update(Point.of(this.defaultX, value)), Point::getY);
		} else {
			this.follow("y", y, value -> this.positionValue.update(Point.of(this.defaultX, value)));
		}
		this.checkMovable(this.draggable);
		return (T) this;
	}

	public final <T extends Node> @NonNull T y(final @NonNull Supplier<Double> y) {
		this.follow("y", y, value -> this.positionValue.update(Point.of(this.defaultX, value)));
		this.checkMovable(this.draggable);
		return (T) this;
	}

	public final <T extends Node> @NonNull T move(final double x, final double y) {
		this.positionValue.change(Point.of(x, y));
		return (T) this;
	}

	public final <T extends Node> @NonNull T width(final double width) {
		return this.width(Signal.from(width));
	}

	public final <T extends Node> @NonNull T width(final @NonNull Supplier<Double> width) {
		return this.follow("width", width, value -> this.defaultWidth = this.width = value);
	}

	public final <T extends Node> @NonNull T height(final double height) {
		return this.height(Signal.from(height));
	}

	public final <T extends Node> @NonNull T height(final @NonNull Supplier<Double> height) {
		return this.follow("height", height, value -> this.defaultHeight = this.height = value);
	}

	public final <T extends Node> @NonNull T position(final @NonNull PositionProperty position) {
		return this.position(Signal.from(position));
	}

	public final <T extends Node> @NonNull T position(final @NonNull Supplier<@NonNull PositionProperty> position) {
		return this.follow("position", position, value -> {
			this.position = value;
			if (value == PositionProperty.ABSOLUTE) {
				this.layoutX = this.layoutY = this.shiftX = this.shiftY = 0D;
			}
		});
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull OverflowProperty overflow) {
		return this.overflow(Signal.from(overflow));
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull Supplier<@NonNull OverflowProperty> overflow) {
		return this.follow("overflow", overflow, this::changeOverflow);
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Align anchor) {
		return this.anchor(Signal.from(anchor));
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Supplier<@NonNull Align> anchor) {
		this.anchorX(anchor);
		return this.anchorY(anchor);
	}

	public final <T extends Node> @NonNull T anchorX(final @NonNull Align anchorX) {
		return this.anchorX(Signal.from(anchorX));
	}

	public final <T extends Node> @NonNull T anchorX(final @NonNull Supplier<@NonNull Align> anchorX) {
		return this.follow("anchorX", anchorX, value -> {
			this.anchorX = value;
			this.place();
		});
	}

	public final <T extends Node> @NonNull T anchorY(final @NonNull Align anchorY) {
		return this.anchorY(Signal.from(anchorY));
	}

	public final <T extends Node> @NonNull T anchorY(final @NonNull Supplier<@NonNull Align> anchorY) {
		return this.follow("anchorY", anchorY, value -> {
			this.anchorY = value;
			this.place();
		});
	}

	public final <T extends Node> @NonNull T draggable(final @NonNull DraggableProperty draggable) {
		return this.draggable(Signal.from(draggable));
	}

	public final <T extends Node> @NonNull T draggable(final @NonNull Supplier<@NonNull DraggableProperty> draggable) {
		return this.follow("draggable", draggable, value -> {
			this.checkMovable(value);
			final DraggableProperty previous = this.draggable;
			this.draggable = value;
			if (previous == null || previous.getType() != value.getType() || !value.isEnabled(this)) {
				this.dragging  = false;
				this.dragged   = false;
				this.releaseDraggedNode();
			}
		});
	}

	public final <T extends Node> @NonNull T startDragging(final double mouseX, final double mouseY) {
		this.checkDraggable();
		this.checkDragArea();
		this.fireDragStart(() -> {
			this.dragging = true;
			this.dragX = mouseX - this.getAbsoluteX();
			this.dragY = mouseY - this.getAbsoluteY();
			this.startDragX = this.getAbsoluteX();
			this.startDragY = this.getAbsoluteY();
			this.targetDragX = this.getAbsoluteX();
			this.targetDragY = this.getAbsoluteY();
			this.dragged = true;

			if (this.draggable.getType() == DraggableType.COPY) {
				this.draggedNode = this.duplicate(true);
				this.draggedNode.parent = this.parent;
				this.draggedNode.position(PositionProperty.ABSOLUTE);
				this.draggedNode.moveTo(this.targetDragX, this.targetDragY);
			}
		});
		return (T) this;
	}

	public final <T extends Node> @NonNull T stopDragging() {
		if (this.dragging) {
			final boolean snapping = this.draggable.hasSnapping();
			final Node target = snapping ? this.draggable.getSnapping(this.draggable.getType() == DraggableType.COPY && this.draggedNode != null ? this.draggedNode : this) : null;
			this.dragging = false;
			if (target != null) {
				this.targetDragX = target.getAbsoluteX() + (target.width - this.width) / 2D;
				this.targetDragY = target.getAbsoluteY() + (target.height - this.height) / 2D;
			} else if (snapping) {
				this.targetDragX = this.startDragX;
				this.targetDragY = this.startDragY;
			}

			if (!this.fireDragDrop(target, () -> {
				if (target != null) {
					this.fire(Node.DRAG_SNAP, () -> NodeDragEvent.Post.create(this, target));
				}
			})) {
				this.targetDragX = this.startDragX;
				this.targetDragY = this.startDragY;
			}
		}

		if (this.draggedNode != null) {
			this.releaseDraggedNode();
			this.dragged = false;
		}
		return (T) this;
	}

	public final boolean fireDragStart(final @NonNull Runnable start) {
		final NodeDragEvent.Pre<Node> event = this.fire(Node.BEFORE_DRAG_START, () -> NodeDragEvent.Pre.create(this, null));
		if (event != null && event.isCancelled()) {
			return false;
		}

		start.run();
		this.fire(Node.DRAG_START, () -> NodeDragEvent.Post.create(this, null));
		return true;
	}

	public final void fireDrag(final @NonNull Runnable drag) {
		drag.run();
		this.fire(Node.DRAG, () -> NodeDragEvent.Post.create(this, null));
	}

	public final boolean fireDragDrop(final Node target, final @NonNull Runnable drop) {
		final NodeDragEvent.Pre<Node> event = this.fire(Node.BEFORE_DRAG_DROP, () -> NodeDragEvent.Pre.create(this, target));
		if (event != null && event.isCancelled()) {
			return false;
		}

		drop.run();
		this.fire(Node.DRAG_DROP, () -> NodeDragEvent.Post.create(this, target));
		return true;
	}

	public final <T extends Node> @NonNull T aspectRatio(final double aspectRatio) {
		return this.aspectRatio(Signal.from(aspectRatio));
	}

	public final <T extends Node> @NonNull T aspectRatio(final @NonNull Supplier<Double> aspectRatio) {
		return this.follow("aspectRatio", aspectRatio, value -> this.aspectRatio = value);
	}

	public final <T extends Node> @NonNull T scrollX(final double scrollX) {
		return this.scrollXValue.start(scrollX);
	}

	public final <T extends Node> @NonNull T scrollX(final @NonNull Signal<Double> scrollX) {
		return this.scrollXValue.bind(scrollX);
	}

	public final <T extends Node> @NonNull T scrollY(final double scrollY) {
		return this.scrollYValue.start(scrollY);
	}

	public final <T extends Node> @NonNull T scrollY(final @NonNull Signal<Double> scrollY) {
		return this.scrollYValue.bind(scrollY);
	}

	public final <T extends Node> @NonNull T scrollTo(final double x, final double y) {
		this.scrollXValue.change(x);
		this.scrollYValue.change(y);
		return (T) this;
	}

	public final <T extends Node> @NonNull T updateScroll() {
		this.updateScrollX();
		this.updateScrollY();
		return (T) this;
	}

	public final <T extends Node> @NonNull T updateScrollX() {
		this.scrollX = this.targetScrollX;
		return (T) this;
	}

	public final <T extends Node> @NonNull T updateScrollY() {
		this.scrollY = this.targetScrollY;
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollSpeed(final double scrollSpeed) {
		return this.scrollSpeed(Signal.from(scrollSpeed));
	}

	public final <T extends Node> @NonNull T scrollSpeed(final @NonNull Supplier<Double> scrollSpeed) {
		return this.follow("scrollSpeed", scrollSpeed, value -> this.scrollSpeed = value);
	}

	public final <T extends Node> @NonNull T scrollbar(final @NonNull BaseScrollbarNode scrollbar) {
		if (this.scrollbar != null && this.scrollbar != scrollbar) {
			this.scrollbar.fireDetach();
		}

		this.scrollbar = scrollbar;
		this.scrollbar.scrollNode(this);
		((Node) this.scrollbar).parent = this;
		if (this.attached && !this.scrollbar.isAttached()) {
			this.scrollbar.load(this.ui);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T cursor(final @NonNull Cursor cursor) {
		return this.cursor(Signal.from(cursor));
	}

	public final <T extends Node> @NonNull T cursor(final @NonNull Supplier<Cursor> cursor) {
		return this.follow("cursor", cursor, value -> this.cursor = value);
	}

	public final <T extends Node> @NonNull T hoverDuration(final long hoverDuration) {
		return this.hoverDuration(Signal.from(hoverDuration));
	}

	public final <T extends Node> @NonNull T hoverDuration(final @NonNull Supplier<Long> hoverDuration) {
		return this.follow("hoverDuration", hoverDuration, value -> this.hoverDuration = value);
	}

	public final <T extends Node> @NonNull T hoverEquation(final @NonNull TweenEquation hoverEquation) {
		return this.hoverEquation(Signal.from(hoverEquation));
	}

	public final <T extends Node> @NonNull T hoverEquation(final @NonNull Supplier<@NonNull TweenEquation> hoverEquation) {
		return this.follow("hoverEquation", hoverEquation, value -> this.hoverEquation = value);
	}

	public final <T extends Node> @NonNull T skeleton(final @NonNull Function<@NonNull T, Node> skeleton) {
		final Node previous = this.skeleton;
		this.skeleton = skeleton.apply((T) this);
		if (previous != null && previous != this.skeleton) {
			previous.fireDetach();
		}

		if (this.skeleton != null) {
			this.skeleton.parent = this;
			if (this.attached && !this.skeleton.attached) {
				this.skeleton.load(this.ui);
			}
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T zindex(final int zindex) {
		return this.zindex(Signal.from(zindex));
	}

	public final <T extends Node> @NonNull T zindex(final @NonNull Supplier<Integer> zindex) {
		return this.follow("zindex", zindex, value -> this.zindex = value);
	}

	public final <T extends Node> @NonNull T zlevel(final double zlevel) {
		return this.zlevel(Signal.from(zlevel));
	}

	public final <T extends Node> @NonNull T zlevel(final @NonNull Supplier<Double> zlevel) {
		return this.follow("zlevel", zlevel, value -> this.zlevel = value);
	}

	public final <T extends Node> @NonNull T visible(final boolean visible) {
		return this.visible(Signal.from(visible));
	}

	public final <T extends Node> @NonNull T visible(final @NonNull Supplier<Boolean> visible) {
		return this.follow("visible", visible, value -> this.visible = Boolean.TRUE.equals(value));
	}

	public final <T extends Node> @NonNull T enabled(final boolean enabled) {
		return this.enabled(Signal.from(enabled));
	}

	public final <T extends Node> @NonNull T enabled(final @NonNull Supplier<Boolean> enabled) {
		return this.follow("enabled", enabled, value -> this.enabled = Boolean.TRUE.equals(value));
	}

	public final <T extends Node> @NonNull T interactive(final boolean interactive) {
		return this.interactive(Signal.from(interactive));
	}

	public final <T extends Node> @NonNull T interactive(final @NonNull Supplier<Boolean> interactive) {
		return this.follow("interactive", interactive, value -> this.interactive = Boolean.TRUE.equals(value));
	}

	public final <T extends Node> @NonNull T label(final Text label) {
		return this.label(Signal.from(label));
	}

	public final <T extends Node> @NonNull T label(final @NonNull Supplier<Text> label) {
		return this.follow("label", label, value -> this.label = value);
	}

	public final <T extends Node> @NonNull T layer(final @NonNull NodeLayer layer) {
		this.layerList.add(layer);
		return (T) this;
	}

	public final <T extends Node> @NonNull T layer(final int index, final @NonNull NodeLayer layer) {
		this.layerList.add(index, layer);
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearLayers() {
		this.layerList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T effect(final @NonNull NodeEffect<? super T> effect) {
		final NodeEffect<Node> previous = this.effectMap.put(effect.getClass(), (NodeEffect<Node>) effect);
		if (this.attached && previous != effect) {
			if (previous != null) {
				previous.detach(this);
			}

			((NodeEffect<Node>) effect).init(this, this.ui);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeEffect(final @NonNull Class<?> effect) {
		final List<NodeEffect<Node>> removed = this.effectMap.values().stream().filter(effect::isInstance).collect(Collectors.toList());
		this.effectMap.values().removeAll(removed);
		if (this.attached) {
			removed.forEach(previous -> previous.detach(this));
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearEffects() {
		return this.removeEffect(NodeEffect.class);
	}

	public final <T extends Node> @NonNull T animate(final @NonNull TweenAnimator animator) {
		this.animatorMap.put(animator, animator.peek());
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeAnimator(final @NonNull TweenAnimator animator) {
		this.animatorMap.remove(animator);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hover(final @NonNull IHoverElement element) {
		this.hoverElementList.add(element);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hover(final @NonNull HoverSupplier supplier) {
		this.hoverSupplierList.add(() -> {
			final String line = supplier.get();
			return line == null ? Collections.emptyList() : Collections.singletonList(line);
		});
		return (T) this;
	}

	public final <T extends Node> @NonNull T hover(final @NonNull Supplier<List<@NonNull String>> supplier) {
		this.hoverSupplierList.add(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeHover(final @NonNull IHoverElement element) {
		if (this.hoverElementList.remove(element)) {
			element.detach();
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearHover() {
		this.hoverElementList.forEach(IHoverElement::detach);
		this.hoverElementList.clear();
		this.hoverSupplierList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T wait(final @NonNull Predicate<@NonNull T> predicate) {
		this.waitingList.add((Predicate<Node>) predicate);
		return (T) this;
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		return this.bindings.watch(signal, () -> true, () -> this.applyWatch(signal, properties));
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull Supplier<Boolean> condition, final @NonNull WatchProperty @NonNull... properties) {
		return this.bindings.watch(signal, condition::get, () -> this.applyWatch(signal, properties));
	}

	public final <T extends Node> @NonNull T onClick(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.CLICK, listener);
	}

	public final <T extends Node> @NonNull T onInit(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.INIT, listener);
	}

	public final <T extends Node> @NonNull T onRender(final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.listen(Node.RENDER, listener);
	}

	public final <T extends Node> @NonNull T onDraw(final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.listen(Node.DRAW, listener);
	}

	public final <T extends Node> @NonNull T onChildAppend(final @NonNull EventListener<NodeChildEvent<T>> listener) {
		return this.listen(Node.CHILD_APPEND, listener);
	}

	public final <T extends Node> @NonNull T onMount(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.MOUNT, listener);
	}

	public final <T extends Node> @NonNull T onWatch(final @NonNull EventListener<NodeWatchEvent<T>> listener) {
		return this.listen(Node.WATCH, listener);
	}

	public final <T extends Node> @NonNull T onAnimate(final @NonNull EventListener<NodeAnimateEvent<T>> listener) {
		return this.listen(Node.ANIMATE, listener);
	}

	public final <T extends Node> @NonNull T beforeScroll(final @NonNull EventListener<NodeScrollEvent.Pre<T>> listener) {
		return this.listen(Node.BEFORE_SCROLL, listener);
	}

	public final <T extends Node> @NonNull T onScroll(final @NonNull EventListener<NodeScrollEvent.Post<T>> listener) {
		return this.listen(Node.SCROLL, listener);
	}

	public final <T extends Node> @NonNull T onScrollToEnd(final @NonNull EventListener<NodeScrollEvent.Post<T>> listener) {
		return this.listen(Node.SCROLL_TO_END, listener);
	}

	public final <T extends Node> @NonNull T onScrollEndReached(final @NonNull EventListener<NodeScrollEvent.Post<T>> listener) {
		return this.listen(Node.SCROLL_END_REACHED, listener);
	}

	public final <T extends Node> @NonNull T beforeMove(final @NonNull EventListener<NodeChangeEvent.Pre<T, Point>> listener) {
		return this.listen(Node.BEFORE_MOVE, listener);
	}

	public final <T extends Node> @NonNull T onMove(final @NonNull EventListener<NodeChangeEvent.Post<T, Point>> listener) {
		return this.listen(Node.MOVE, listener);
	}

	public final <T extends Node> @NonNull T beforeDragStart(final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.listen(Node.BEFORE_DRAG_START, listener);
	}

	public final <T extends Node> @NonNull T onDragStart(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_START, listener);
	}

	public final <T extends Node> @NonNull T onDrag(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG, listener);
	}

	public final <T extends Node> @NonNull T beforeDragDrop(final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.listen(Node.BEFORE_DRAG_DROP, listener);
	}

	public final <T extends Node> @NonNull T onDragDrop(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_DROP, listener);
	}

	public final <T extends Node> @NonNull T onDragSnap(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_SNAP, listener);
	}

	public final <T extends Node> @NonNull T onHoverStart(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER_START, listener);
	}

	public final <T extends Node> @NonNull T onHover(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER, listener);
	}

	public final <T extends Node> @NonNull T onHoverEnd(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER_END, listener);
	}

	public final boolean hasOverflowX() {
		return this.maxScrollX > 0D;
	}

	public final boolean hasOverflowY() {
		return this.maxScrollY > 0D;
	}

	public final boolean hasListener(final @NonNull EventType<?> type) {
		return this.eventBus.has(type);
	}

	public final boolean hasEffect(final @NonNull Class<?> clazz) {
		return this.effectMap.values().stream().anyMatch(clazz::isInstance);
	}

	public boolean isEnabled() {
		if (this.parent != null && !this.parent.isEnabled()) {
			return false;
		}

		return this.enabled;
	}

	public boolean isInteractive() {
		if (this.parent != null && !this.parent.isInteractive()) {
			return false;
		}

		return this.interactive;
	}

	public boolean isVisible() {
		if (this.parent != null && !this.parent.isVisible()) {
			return false;
		}

		if (this.overflowArea != null && this.overflowArea.overflow != OverflowProperty.NONE) {
			if (this.getAbsoluteX() + this.getContentWidth() < this.overflowArea.getAbsoluteX() || this.getAbsoluteX() > this.overflowArea.getAbsoluteX() + this.overflowArea.width || this.getAbsoluteY() + this.getContentHeight() < this.overflowArea.getAbsoluteY() || this.getAbsoluteY() > this.overflowArea.getAbsoluteY() + this.overflowArea.height) {
				return false;
			}
		}

		return this.isVisibleProperty();
	}

	public boolean isVisibleProperty() {
		return this.visible;
	}

	public boolean isDragging() {
		return this.dragging || this.parent != null && this.parent.isDraggingChild(this);
	}

	public final boolean isMounted() {
		if (this.parent != null && !this.parent.isMounted() && !this.equals(this.parent.skeleton)) {
			return false;
		}

		return this.waitingList.stream().allMatch(predicate -> predicate.test(this));
	}

	@Override
	public int getIndex() {
		final boolean raised = this.parent != null ? this.parent.isDraggingChild(this) : this.dragging && this.draggable.getType() == DraggableType.MOVE;
		return raised ? Integer.MAX_VALUE : this.zindex;
	}

	public final <T extends UI> T getUi() {
		return (T) (this.ui != null ? this.ui : UI.getCurrent());
	}

	public final double getAbsoluteX() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.x;
		}

		return this.parent != null ? this.parent.getAbsoluteX() + this.x : this.x;
	}

	public final double getAbsoluteY() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.y;
		}

		return this.parent != null ? this.parent.getAbsoluteY() + this.y : this.y;
	}

	public final double getAbsoluteDefaultX() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.defaultX;
		}

		return this.parent != null ? this.parent.getAbsoluteX() + this.defaultX : this.defaultX;
	}

	public final double getAbsoluteDefaultY() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.defaultY;
		}

		return this.parent != null ? this.parent.getAbsoluteY() + this.defaultY : this.defaultY;
	}

	public final double toDrawX(final double x) {
		return this.x + x - this.getAbsoluteX();
	}

	public final double toDrawY(final double y) {
		return this.y + y - this.getAbsoluteY();
	}

	public final @NonNull String getHierarchy() {
		if (this.parent == null) {
			return JOIDLog.nameOf(this.getClass());
		}

		return this.parent.getHierarchy() + " - " + JOIDLog.nameOf(this.getClass());
	}

	public final @NonNull String getMappedIndex() {
		if (this.ui == null) {
			return "N/A";
		}

		if (this.parent != null) {
			return this.parent.getMappedIndex() + "." + this.parent.drawList.ordered().indexOf(this);
		}

		return String.valueOf(this.ui.getNodeList().indexOf(this));
	}

	public final <T extends NodeEffect<?>> T getEffect(final @NonNull Class<T> clazz) {
		for (final NodeEffect<Node> effect : this.effectMap.values()) {
			if (clazz.isInstance(effect)) {
				return clazz.cast(effect);
			}
		}

		return null;
	}

	public final <T extends Node> T getChild(final int index, final @NonNull Class<T> clazz) {
		final List<T> children = this.getChildren(clazz);
		return index >= 0 && index < children.size() ? children.get(index) : null;
	}

	public final @NonNull List<@NonNull Node> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public final <T extends Node> @NonNull List<@NonNull T> getChildren(final @NonNull Class<T> clazz) {
		return Collections.unmodifiableList(this.children.stream().filter(child -> clazz.isAssignableFrom(child.getClass())).map(child -> (T) child).collect(Collectors.toList()));
	}

	public final @NonNull List<@NonNull Task> getTaskList() {
		return Collections.unmodifiableList(this.taskList);
	}

	public final @NonNull List<@NonNull Shortcut> getShortcutList() {
		return Collections.unmodifiableList(this.shortcutList);
	}

	public final @NonNull Cursor getResolvedCursor() {
		if (this.cursor != null) {
			return this.cursor;
		}

		return this.parent != null ? this.parent.getResolvedCursor() : Cursor.DEFAULT;
	}

	public final @NonNull List<@NonNull Node> getNodeListAt(final double x, final double y) {
		final List<Node> nodeList = new ArrayList<>();
		this.collectNodesAt(x, y, nodeList);
		return nodeList;
	}

	public final float hoverValue(final float value) {
		return value * Math.min(1F, Math.max(0F, this.hoverAnimator.get()));
	}

	public final @NonNull Color hoverColor(final @NonNull Color color, final Color hoveredColor) {
		return hoveredColor != null ? color.to(hoveredColor, this.hoverValue(1F)) : color;
	}

	public final double w() {
		return this.width;
	}

	public final double h() {
		return this.height;
	}

	public final double dw(final double value) {
		return this.width / value;
	}

	public final double dh(final double value) {
		return this.height / value;
	}

	public final double mw(final double value) {
		return this.width * value;
	}

	public final double mh(final double value) {
		return this.height * value;
	}

	public final double aw(final double value) {
		return this.width + value;
	}

	public final double ah(final double value) {
		return this.height + value;
	}

	public final double ax(final double value) {
		return this.x + value;
	}

	public final double ay(final double value) {
		return this.y + value;
	}

	public @NonNull String toJson() {
		return Node.GSON.toJson(this.json());
	}

	protected final <T extends Node> @NonNull T listen(final @NonNull EventType<?> type, final @NonNull EventListener<?> listener) {
		return this.bindings.listen(type, listener);
	}

	protected final <E extends Event> E fire(final @NonNull EventType<? super E> type, final @NonNull Supplier<@NonNull E> event) {
		return this.bindings.fire(type, event);
	}

	protected final <T extends Node, V> @NonNull T start(final @NonNull String property, final V value, final @NonNull Consumer<V> applier) {
		return this.bindings.start(property, value, applier);
	}

	protected final <T extends Node, V> @NonNull T follow(final @NonNull String property, final @NonNull Supplier<V> supplier, final @NonNull Consumer<V> applier) {
		return this.bindings.follow(property, supplier, applier);
	}

	protected final <T extends Node, V> @NonNull T bind(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull Consumer<@NonNull V> applier) {
		return this.bindings.bind(property, signal, applier, (final V value) -> value);
	}

	protected final <T extends Node, V, M> @NonNull T bind(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull Consumer<@NonNull V> applier, final @NonNull Function<@NonNull M, V> writer) {
		return this.bindings.bind(property, signal, applier, writer);
	}

	protected final <T extends Node, V, M> @NonNull T bindNullable(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull Consumer<V> applier, final @NonNull Function<@NonNull M, V> writer) {
		return this.bindings.bindNullable(property, signal, applier, writer);
	}

	protected final void sync(final @NonNull String property, final @NonNull Object value) {
		this.bindings.sync(property, value);
	}

	protected final <N extends Node> void require(final @NonNull String setting, final @NonNull String advice, final @NonNull Predicate<@NonNull N> present) {
		this.requirementList.add(() -> {
			if (!present.test((N) this)) {
				throw new IllegalStateException("The " + JOIDLog.nameOf(this.getClass()) + " has no " + setting + ", " + advice);
			}
		});
	}

	protected final <N extends Node> @NonNull N part(final Node previous, final @NonNull N next) {
		if (previous == next) {
			return next;
		}

		final int index = this.children.indexOf(previous);
		if (previous != null) {
			this.remove(previous);
		}

		this.append(next);
		if (index >= 0) {
			final List<Node> order = new ArrayList<>(this.children);
			order.remove(next);
			order.add(Math.min(index, order.size()), next);
			this.reorderChildren(order);
		}
		return next;
	}

	protected final void reorderChildren(final @NonNull List<@NonNull Node> order) {
		this.children.clear();
		this.children.addAll(order);
		this.drawList.clear();
		order.forEach(this.drawList::add);
	}

	protected @NonNull Node instantiate() {
		final Class<?>[][] types = {{double.class, double.class, double.class, double.class}, {double.class, double.class}, {}};
		final Object[][] arguments = {{this.x, this.y, this.width, this.height}, {this.x, this.y}, {}};
		for (int i = 0; i < types.length; i++) {
			final Constructor<? extends Node> constructor;
			try {
				constructor = this.getClass().getDeclaredConstructor(types[i]);
			} catch (final NoSuchMethodException exception) {
				continue;
			}

			try {
				constructor.setAccessible(true);
				return constructor.newInstance(arguments[i]);
			} catch (final ReflectiveOperationException exception) {
				final Throwable cause = exception instanceof InvocationTargetException ? exception.getCause() : exception;
				throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " cannot be copied: its constructor " + constructor + " failed (" + cause + ")", cause);
			}
		}
		throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " cannot be copied: declare a constructor (x, y, width, height) or override instantiate()");
	}

	protected boolean holdsInput() {
		return false;
	}

	protected boolean isDraggingChild(final @NonNull Node child) {
		return child.dragging && child.draggable.getType() == DraggableType.MOVE;
	}

	private void updateHover(final double mouseX, final double mouseY) {
		final boolean wasHovered = this.hovered;
		this.hovered = this.ui != null && this.ui.isOnTop() && this.ui.getHoveredPath().contains(this);
		final boolean hoverShown = this.hovered && this.isEnabled();
		if (hoverShown != this.hoverShown) {
			this.hoverShown = hoverShown;
			this.hoverAnimator.sequence(this.hoverDuration, hoverShown ? 1F : 0F, this.hoverEquation).start();
		}

		if (!wasHovered && this.hovered) {
			this.fire(Node.HOVER_START, () -> NodeHoverEvent.create(this, mouseX, mouseY));
		}

		if (wasHovered && !this.hovered) {
			this.fire(Node.HOVER_END, () -> NodeHoverEvent.create(this, mouseX, mouseY));
		}

		if (this.hovered) {
			this.fire(Node.HOVER, () -> NodeHoverEvent.create(this, mouseX, mouseY));
		}
	}

	private void scrollContent(final IRenderBridge render) {
		boolean scrollsX = false;
		boolean scrollsY = false;
		if (this.overflow == OverflowProperty.SCROLL) {
			double offsetX = Double.POSITIVE_INFINITY;
			double offsetY = Double.POSITIVE_INFINITY;
			this.maxScrollX = this.maxScrollY = 0D;
			for (final Node child : this.drawList) {
				if (child.position != PositionProperty.ABSOLUTE) {
					offsetX = Math.min(offsetX, child.getPlacedX());
					offsetY = Math.min(offsetY, child.getPlacedY());
					this.maxScrollX = Math.max(this.maxScrollX, child.getPlacedX() + child.getContentWidth() - this.width);
					this.maxScrollY = Math.max(this.maxScrollY, child.getPlacedY() + child.getContentHeight() - this.height);
				}
			}

			if (this.hasOverflowX()) {
				this.maxScrollX += Math.max(0D, offsetX);
				scrollsX = true;
			}

			if (this.hasOverflowY()) {
				this.maxScrollY += Math.max(0D, offsetY);
				scrollsY = true;
			}

			this.scrollMeasured = true;
		}

		final double targetScrollX = this.targetScrollX;
		final double targetScrollY = this.targetScrollY;
		this.scrollXValue.update(targetScrollX);
		this.scrollYValue.update(targetScrollY);
		this.scrollEndX = this.scrollEndX && this.targetScrollX == -this.maxScrollX;
		this.scrollEndY = this.scrollEndY && this.targetScrollY == -this.maxScrollY;
		if (this.scrollMeasured && this.scrollEndGuessX && this.scrollEndX && this.targetScrollX == targetScrollX) {
			this.fire(Node.SCROLL_TO_END, () -> NodeScrollEvent.Post.create(this, Axis.HORIZONTAL, targetScrollX, targetScrollX));
		}

		if (this.scrollMeasured && this.scrollEndGuessY && this.scrollEndY && this.targetScrollY == targetScrollY) {
			this.fire(Node.SCROLL_TO_END, () -> NodeScrollEvent.Post.create(this, Axis.VERTICAL, targetScrollY, targetScrollY));
		}

		if (this.scrollMeasured) {
			this.scrollEndGuessX = this.scrollEndGuessY = false;
		}

		final boolean thumbDragged = this.scrollbar != null && this.scrollbar.isDragging();
		if (this.targetScrollX != this.scrollX) {
			this.scrollX = this.ui.lerpByFramerate(this.scrollX, this.targetScrollX, thumbDragged ? 1D / 3D : 0.2D / 3D, thumbDragged ? 1D : 0.2D);
		}

		if (this.targetScrollY != this.scrollY) {
			this.scrollY = this.ui.lerpByFramerate(this.scrollY, this.targetScrollY, thumbDragged ? 1D / 3D : 0.2D / 3D, thumbDragged ? 1D : 0.2D);
		}

		if (this.scrollEndX && this.scrollX == this.targetScrollX) {
			this.scrollEndX = false;
			this.fire(Node.SCROLL_END_REACHED, () -> NodeScrollEvent.Post.create(this, Axis.HORIZONTAL, this.scrollX, this.scrollX));
		}

		if (this.scrollEndY && this.scrollY == this.targetScrollY) {
			this.scrollEndY = false;
			this.fire(Node.SCROLL_END_REACHED, () -> NodeScrollEvent.Post.create(this, Axis.VERTICAL, this.scrollY, this.scrollY));
		}

		if (this.overflow == OverflowProperty.SCROLL) {
			final PixelGrid grid = render.getPixelGrid();
			final double offsetX = scrollsX ? grid.quantizeX(this.scrollX) : 0D;
			final double offsetY = scrollsY ? grid.quantizeY(this.scrollY) : 0D;
			for (final Node child : this.drawList) {
				if (child.position != PositionProperty.ABSOLUTE) {
					child.shiftX = offsetX;
					child.shiftY = offsetY;
					child.place();
				}
			}
		}
	}

	private void animateDrag() {
		if (this.dragging && BridgeHandler.WINDOW.get().isMouseGrabbed()) {
			this.stopDragging();
		}

		if (this.draggable != null && (this.dragging || this.dragged || this.draggable.isEnabled(this))) {
			if (!this.dragging && !this.dragged) {
				this.targetDragX = this.getAbsoluteX();
				this.targetDragY = this.getAbsoluteY();
			}

			if (this.draggable.getAreaType() != DraggableAreaType.FREE && (this.draggable.getAreaType() != DraggableAreaType.PARENT || this.parent != null) && (this.dragging || this.draggable.getType() == DraggableType.MOVE)) {
				final BoundingBox bounds = this.draggable.getBounds(this);
				final double targetX = this.targetDragX;
				final double targetY = this.targetDragY;
				this.targetDragX = Math.max(bounds.getMinX(), Math.min(this.targetDragX, bounds.getMaxX() - this.width));
				this.targetDragY = Math.max(bounds.getMinY(), Math.min(this.targetDragY, bounds.getMaxY() - this.height));

				if (this.targetDragX != targetX || this.targetDragY != targetY) {
					this.dragged = true;
				}
			}

			if (this.dragged) {
				if (this.draggable.getType() == DraggableType.MOVE) {
					final double newAbsoluteX = this.ui.lerpByFramerate(this.getAbsoluteX(), this.targetDragX, 1D / 6D, 0.5D);
					final double newAbsoluteY = this.ui.lerpByFramerate(this.getAbsoluteY(), this.targetDragY, 1D / 6D, 0.5D);
					final double diffX = this.getAbsoluteX() - this.x;
					final double diffY = this.getAbsoluteY() - this.y;

					this.stepping = true;
					final boolean moved = this.moveTo(newAbsoluteX - diffX, newAbsoluteY - diffY);
					this.stepping = false;
					if (!moved) {
						this.targetDragX = this.getAbsoluteX();
						this.targetDragY = this.getAbsoluteY();
					}

					if (!this.dragging && newAbsoluteX == this.targetDragX && newAbsoluteY == this.targetDragY) {
						this.dragged = false;
					}
				} else if (this.draggable.getType() == DraggableType.COPY && this.draggedNode != null) {
					final double newAbsoluteX = this.ui.lerpByFramerate(this.draggedNode.getAbsoluteX(), this.targetDragX, 1D / 6D, 0.5D);
					final double newAbsoluteY = this.ui.lerpByFramerate(this.draggedNode.getAbsoluteY(), this.targetDragY, 1D / 6D, 0.5D);
					this.draggedNode.moveTo(newAbsoluteX, newAbsoluteY);
				}
			}
		}
	}

	private void trackMovement(final IRenderBridge render) {
		if (this.x != this.drawnX || this.y != this.drawnY) {
			if (!this.moving) {
				this.restX = this.drawnX;
				this.restY = this.drawnY;
				this.moving = true;
			}
		} else {
			this.moving = false;
		}

		this.drawnX = this.x;
		this.drawnY = this.y;
		if (this.moving) {
			render.quantize(this.x - this.restX, this.y - this.restY);
		}
	}

	private void drawContent(final MatrixStack matrix, final double mouseX, final double mouseY) {
		final List<NodeEffect<Node>> effects = this.getAppliedEffects();
		final Map<Boolean, List<NodeEffect<Node>>> scopes = effects.stream().collect(Collectors.partitioningBy(effect -> effect.getScope() == NodeEffectScope.SELF));

		final Runnable content = () -> {
			final Node area = this.overflow != OverflowProperty.NONE ? this : this.overflowArea;
			this.drawList.forEach(child -> child.overflowArea = area);

			if (this.skeleton != null && !this.mounted) {
				this.skeleton.render(mouseX, mouseY);
				this.fire(Node.RENDER, () -> NodeDrawEvent.create(this, mouseX, mouseY));
				return;
			}

			this.drawList.ordered().stream().filter(child -> child.zindex < 0).forEach(child -> child.render(mouseX, mouseY));
			final Runnable selfDraw = () -> {
				if (this.mounted) {
					this.draw(mouseX, mouseY);
				} else {
					this.drawSkeleton(mouseX, mouseY);
				}
			};
			ShaderPipeline.render(this.x, this.y, this.width, this.height, selfDraw, this.toShaderPasses(scopes.get(true)));
			this.fire(Node.DRAW, () -> NodeDrawEvent.create(this, mouseX, mouseY));
			if (this.mounted && this.label != null && !this.label.isEmpty()) {
				DrawUtils.TEXT.drawText(this.x + (this.width - this.label.getWidth()) / 2D, this.y + (this.height - this.label.getHeight()) / 2D, this.label.getWidth(), this.label.getHeight(), this.label, TextMode.NORMAL);
			}

			this.drawList.ordered().stream().filter(child -> child.zindex >= 0).forEach(child -> child.render(mouseX, mouseY));
			if (!this.layerList.isEmpty()) {
				final double localX = mouseX - this.getAbsoluteX();
				final double localY = mouseY - this.getAbsoluteY();
				matrix.with(layerMatrix -> {
					layerMatrix.translate(this.x, this.y, 0D);
					this.layerList.forEach(layer -> layer.draw(this, localX, localY));
				});
			}
			this.fire(Node.RENDER, () -> NodeDrawEvent.create(this, mouseX, mouseY));
		};
		final Runnable maskDraw = () -> {
			if (this.overflow != OverflowProperty.NONE) {
				this.ui.mask(this.x, this.y, this.width, this.height, content);
			} else {
				content.run();
			}
		};

		Runnable effectDraw = () -> {
			ShaderPipeline.render(this.x, this.y, this.width, this.height, maskDraw, this.toShaderPasses(scopes.get(false)));

			if (this.isScrollbarShown()) {
				this.scrollbar.render(mouseX, mouseY);
			} else if (this.scrollbar != null) {
				((Node) this.scrollbar).endHover(true);
			}
		};
		for (int i = effects.size() - 1; i >= 0; i--) {
			final NodeEffect<Node> effect = effects.get(i);
			final Runnable body = effectDraw;
			effectDraw = () -> effect.draw(this, body);
		}
		effectDraw.run();

		if (this.draggedNode != null) {
			this.ui.drawGhost(this.draggedNode);
		}
	}

	private @NonNull List<IShaderPass> toShaderPasses(final List<NodeEffect<Node>> effects) {
		return effects.stream().flatMap(effect -> effect.toShaderPasses(this).stream()).collect(Collectors.toList());
	}

	private double getContentWidth() {
		double width = this.width;
		if (this.overflow == OverflowProperty.NONE) {
			for (final Node child : this.drawList) {
				if (child.position != PositionProperty.ABSOLUTE) {
					width = Math.max(width, child.x + child.getContentWidth());
				}
			}
		}
		return width;
	}

	private double getContentHeight() {
		double height = this.height;
		if (this.overflow == OverflowProperty.NONE) {
			for (final Node child : this.drawList) {
				if (child.position != PositionProperty.ABSOLUTE) {
					height = Math.max(height, child.y + child.getContentHeight());
				}
			}
		}
		return height;
	}

	private boolean canScroll(final InteractiveValue<Double> scroll, final double maxScroll, final double notches) {
		return notches > 0D ? scroll.get() < 0D : scroll.get() > -maxScroll;
	}

	private boolean isShown() {
		return this.isVisibleProperty() && (this.parent == null || this.parent.isShown());
	}

	private boolean reactsToInput() {
		return this.isEnabled() && (this.isVisible() || this.holdsInput());
	}

	private boolean isScrollbarShown() {
		return this.overflow == OverflowProperty.SCROLL && this.scrollbar != null && this.scrollbar.isOverflowing();
	}

	private boolean isAt(final double x, final double y) {
		return this.isVisible() && (this.overflowArea == null || this.overflowArea.isAt(x, y)) && x >= this.getAbsoluteX() && x < this.getAbsoluteX() + this.width && y >= this.getAbsoluteY() && y < this.getAbsoluteY() + this.height;
	}

	private void collectNodesAt(final double x, final double y, final List<Node> nodeList) {
		if (!this.isVisible()) {
			return;
		}

		if (this.isScrollbarShown()) {
			nodeList.addAll(this.scrollbar.getNodeListAt(x, y));
		}

		final boolean skeleton = this.skeleton != null && !this.mounted;
		if (skeleton) {
			this.skeleton.collectNodesAt(x, y, nodeList);
		} else {
			this.collectChildrenAt(x, y, nodeList, true);
		}

		if (this.isAt(x, y)) {
			nodeList.add(this);
		}

		if (!skeleton) {
			this.collectChildrenAt(x, y, nodeList, false);
		}
	}

	private void collectChildrenAt(final double x, final double y, final List<Node> nodeList, final boolean front) {
		for (final Node child : this.drawList.reversed()) {
			if (child.zindex >= 0 == front) {
				child.collectNodesAt(x, y, nodeList);
			}
		}
	}

	private @NonNull List<NodeEffect<Node>> getAppliedEffects() {
		return this.effectMap.values().stream().filter(effect -> effect.shouldApply(this)).sorted(Comparator.comparingInt(NodeEffect::getPriority)).collect(Collectors.toList());
	}

	private void changeOverflow(final OverflowProperty overflow) {
		if (this.overflow == OverflowProperty.SCROLL && overflow != OverflowProperty.SCROLL) {
			for (final Node child : this.drawList) {
				child.shiftX = child.shiftY = 0D;
				child.place();
			}

			this.maxScrollX = this.maxScrollY = 0D;
			this.scrollXValue.update(0D);
			this.scrollYValue.update(0D);
			this.scrollX = this.scrollY = 0D;
			this.scrollEndX = this.scrollEndY = false;
			this.scrollEndGuessX = this.scrollEndGuessY = false;
			this.scrollMeasured = false;
		}

		this.overflow = overflow;
	}

	private <E extends NodeInputEvent<Node>> void dispatch(final E event, final boolean enabled, final EventType<?> type, final BiConsumer<Node, E> forward, final Consumer<E> handler) {
		if (this.scrollbar != null && (this.isScrollbarShown() || this.scrollbar.isDragging())) {
			forward.accept(this.scrollbar, event);
		}

		if (this.skeleton != null && !this.mounted) {
			forward.accept(this.skeleton, event);
			if (type != Node.HANDLE_MOUSE_RELEASED && type != Node.HANDLE_KEY_RELEASED) {
				return;
			}
		}

		this.dispatchChildren(event, forward, true);
		if (enabled) {
			this.bindings.mark();
			try {
				this.fireListeners(type, event);
				handler.accept(event);
			} finally {
				this.bindings.commit();
			}
		}

		this.dispatchChildren(event, forward, false);
		if (enabled) {
			this.fireMonitor(type, event);
		}
	}

	private <E extends NodeInputEvent<Node>> void dispatchChildren(final E event, final BiConsumer<Node, E> forward, final boolean front) {
		for (final Node child : this.drawList.reversed()) {
			if (child.zindex >= 0 == front) {
				forward.accept(child, event);
			}
		}
	}

	private void fireListeners(final EventType<?> type, final Event event) {
		if (this.eventBus.has(type)) {
			this.replay(() -> this.eventBus.fireListeners(type, event));
		}
	}

	private void fireMonitor(final EventType<?> type, final Event event) {
		if (this.eventBus.has(type)) {
			this.replay(() -> this.eventBus.fireMonitor(type, event));
		}
	}

	private void replay(final Runnable runnable) {
		SignalReplay.run(this, this.getUi(), runnable);
	}

	private String describe() {
		final UI ui = this.getUi();
		return ui == null ? JOIDLog.nameOf(this.getClass()) : JOIDLog.nameOf(this.getClass()) + " in " + JOIDLog.nameOf(ui.getClass());
	}

	private void fireAnimate() {
		for (final TweenAnimator animator : new ArrayList<>(this.animatorMap.keySet())) {
			final Float previous = this.animatorMap.get(animator);
			if (previous == null) {
				continue;
			}

			final float value = animator.peek();
			if (value != previous) {
				this.animatorMap.put(animator, value);
				this.fire(Node.ANIMATE, () -> NodeAnimateEvent.create(this, animator, value));
			}
		}
	}

	private void endInteraction() {
		this.stopDragging();

		if (this.dragged && this.draggable.getType() == DraggableType.MOVE) {
			this.moveTo(this.x + (this.targetDragX - this.getAbsoluteX()), this.y + (this.targetDragY - this.getAbsoluteY()));
		}

		this.dragged = false;
		this.releaseDraggedNode();
		this.endHover(false);
		this.hoverAnimator.clear();
		this.mounted = false;
	}

	private void releaseDraggedNode() {
		final Node node = this.draggedNode;
		this.draggedNode = null;
		if (node != null) {
			node.fireDetach();
		}
	}

	private void endHover(final boolean recursive) {
		if (!this.hovered && !this.hoverShown) {
			return;
		}

		final boolean wasHovered = this.hovered;
		this.hovered = false;
		if (wasHovered) {
			this.fire(Node.HOVER_END, () -> NodeHoverEvent.create(this, this.ui.getMouseX(), this.ui.getMouseY()));
		}

		this.hoverAnimator.clear();
		this.hoverShown = false;
		if (recursive) {
			this.drawList.forEach(child -> child.endHover(true));
			if (this.scrollbar != null) {
				((Node) this.scrollbar).endHover(true);
			}
		}
	}

	private boolean moveTo(final double x, final double y) {
		return this.positionValue.change(Point.of(x - this.layoutX - this.shiftX + this.getPivotX(), y - this.layoutY - this.shiftY + this.getPivotY()));
	}

	private void locate(final Point position) {
		final boolean stepping = this.stepping;
		final boolean movedX = position.getX() != this.defaultX;
		final boolean movedY = position.getY() != this.defaultY;
		this.stepping = false;
		this.defaultX = position.getX();
		this.defaultY = position.getY();
		this.place();
		if (!stepping && !this.dragging && this.dragged) {
			if (movedX) {
				this.targetDragX = this.getAbsoluteX();
			}
			if (movedY) {
				this.targetDragY = this.getAbsoluteY();
			}
		}
		Signal.batch(() -> {
			this.bindings.sync("x", position);
			this.bindings.sync("y", position);
		});
	}

	private void scroll(final Axis axis, final double offset) {
		final boolean horizontal = axis.isHorizontal();
		final double max = horizontal ? this.maxScrollX : this.maxScrollY;
		final boolean end = this.scrollMeasured ? max > 0D && offset <= -max : offset < 0D;
		if (horizontal) {
			this.targetScrollX = offset;
			this.scrollEndX = end;
			this.scrollEndGuessX = !this.scrollMeasured && end;
		} else {
			this.targetScrollY = offset;
			this.scrollEndY = end;
			this.scrollEndGuessY = !this.scrollMeasured && end;
		}
	}

	private double clampScroll(final Axis axis, final double offset) {
		final String name = axis.isHorizontal() ? "scrollX" : "scrollY";
		if (offset > 0D) {
			JOIDLog.devWarnOnce("Node." + name + System.identityHashCode(this), () -> name + "(" + offset + ") on " + JOIDLog.nameOf(this.getClass()) + " is brought back to 0: the scroll offsets are negative, from 0 down to minus the overflow, call " + name + "(" + -offset + ") instead");
		}

		final double max = axis.isHorizontal() ? this.maxScrollX : this.maxScrollY;
		return this.scrollMeasured ? Math.max(-max, Math.min(offset, 0D)) : Math.min(offset, 0D);
	}

	private InteractiveValue<Double> createScrollValue(final Axis axis) {
		final boolean horizontal = axis.isHorizontal();
		return InteractiveValue.create(this.bindings, horizontal ? "scrollX" : "scrollY", () -> horizontal ? this.targetScrollX : this.targetScrollY, (final Double offset) -> this.scroll(axis, offset))
		.clamp(offset -> this.clampScroll(axis, offset))
		.events(Node.BEFORE_SCROLL, (previous, offset) -> NodeScrollEvent.Pre.create(this, axis, previous, offset), Node.SCROLL, (previous, offset) -> NodeScrollEvent.Post.create(this, axis, previous, offset))
		.then((previous, offset) -> {
			if ((horizontal ? this.scrollEndX : this.scrollEndY) && this.scrollMeasured) {
				this.bindings.fireSubscribed(Node.SCROLL_TO_END, () -> NodeScrollEvent.Post.create(this, axis, previous, offset));
			}
		});
	}

	private void place() {
		this.x = this.getPlacedX() + this.shiftX;
		this.y = this.getPlacedY() + this.shiftY;
	}

	private double getPlacedX() {
		return this.defaultX + this.layoutX - this.getPivotX();
	}

	private double getPlacedY() {
		return this.defaultY + this.layoutY - this.getPivotY();
	}

	private double getPivotX() {
		return this.anchorX.isCenter() ? this.width / 2D : this.anchorX.isEnd() ? this.width : 0D;
	}

	private double getPivotY() {
		return this.anchorY.isCenter() ? this.height / 2D : this.anchorY.isEnd() ? this.height : 0D;
	}

	private void checkDraggable() {
		if (this.draggable == null) {
			throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " has no DraggableProperty, call draggable(...) first");
		}

		if (!this.draggable.isEnabled(this)) {
			throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " has a disabled DraggableProperty, enable it with enabled(...) first");
		}
	}

	private void checkDragArea() {
		if (this.draggable.getAreaType() == DraggableAreaType.PARENT && this.parent == null) {
			throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()");
		}
	}

	private void clearOverflowArea(final Node area) {
		if (area == null || this.overflowArea != area) {
			return;
		}

		this.overflowArea = null;
		this.drawList.forEach(child -> child.clearOverflowArea(area));
	}

	private void checkMovable(final DraggableProperty draggable) {
		if (draggable == null || draggable.getType() != DraggableType.MOVE) {
			return;
		}

		for (final String axis : new String[] {"x", "y"}) {
			if (this.bindings.isFollowed(axis)) {
				throw new IllegalStateException("The " + JOIDLog.nameOf(this.getClass()) + " is dragged with DraggableType.MOVE but its " + axis + " is computed (a lambda or a signal that cannot be written), so a drag cannot move it: give " + axis + "(...) a value or a writable Signal, or drag a copy with DraggableType.COPY");
			}
		}
	}

	private void applyWatch(final Signal<?> signal, final WatchProperty[] properties) {
		this.replay(() -> {
			for (final WatchProperty property : properties) {
				try {
					property.apply(this, signal);
				} catch (final Exception exception) {
					JOIDLog.warnOnce("Node.watch." + this.getClass().getName() + "." + exception.getClass().getName(), "A watch of " + JOIDLog.nameOf(this.getClass()) + " cannot apply its property when its signal changes (" + exception + "), the remaining properties of this watch still apply: check the value the signal gives or the body it replays", exception);
				}
			}
		});
		this.fire(Node.WATCH, () -> NodeWatchEvent.create(this, signal.peek(), properties));
	}

	private @NonNull JsonObject json() {
		final JsonObject json = new JsonObject();
		json.addProperty("index", this.getIndex());
		json.addProperty("mappedIndex", this.getMappedIndex());
		json.addProperty("name", JOIDLog.nameOf(this.getClass()));
		json.addProperty("class", this.getClass().getName());

		json.addProperty("defaultX", this.defaultX);
		json.addProperty("defaultY", this.defaultY);

		final JsonObject boundingJson = new JsonObject();
		boundingJson.addProperty("x", this.x + " / " + this.getAbsoluteX());
		boundingJson.addProperty("y", this.y + " / " + this.getAbsoluteY());
		boundingJson.addProperty("width", this.width);
		boundingJson.addProperty("height", this.height);
		json.add("bounding", boundingJson);

		if (JOIDEnv.isDevMode()) {
			json.addProperty("scrollX", this.targetScrollX + " / " + this.maxScrollX + " [" + this.overflow.toString() + "]");
			json.addProperty("scrollY", this.targetScrollY + " / " + this.maxScrollY + " [" + this.overflow.toString() + "]");

			json.addProperty("visible", this.visible);
			json.addProperty("enabled", this.enabled);
			json.addProperty("interactive", this.interactive);
			json.addProperty("hovered", this.hovered);

			json.addProperty("isChild", this.parent != null);
			json.addProperty("children", this.drawList.size());
			json.addProperty("hierarchy", this.getHierarchy() + " (this)");
		}

		return json;
	}

	private @NonNull Node duplicate(final boolean ghost) {
		final Map<Object, Object> copies = new IdentityHashMap<>();
		final Node copy = this.duplicate(ghost, copies);
		copies.forEach((original, duplicate) -> ((Node) original).copyFields((Node) duplicate, copies));
		return copy;
	}

	private @NonNull Node duplicate(final boolean ghost, final Map<Object, Object> copies) {
		final Node copy = this.instantiate();
		copies.put(this, copy);
		copy.ui = this.ui;
		copy.bindings.releaseFollows();

		copy.eventBus.clear();
		if (ghost) {
			copy.attached = true;
			copy.dragSource = this;
		} else {
			copy.eventBus.addAll(this.eventBus);
		}

		copy.effectMap.clear();
		this.effectMap.forEach((type, effect) -> copy.effectMap.put(type, effect.copy()));

		copy.drawList.clear();
		copy.children.clear();
		for (final Node child : this.children) {
			final Node childCopy = child.duplicate(ghost, copies);
			childCopy.parent = copy;
			copy.drawList.add(childCopy);
			copy.children.add(childCopy);
		}

		if (this.scrollbar != null) {
			final Node scrollbar = ((Node) this.scrollbar).duplicate(ghost, copies);
			scrollbar.parent = copy;
			copy.scrollbar = ((BaseScrollbarNode) scrollbar).scrollNode(copy);
		}

		if (this.skeleton != null) {
			copy.skeleton = this.skeleton.duplicate(ghost, copies);
			copy.skeleton.parent = copy;
		}

		return copy;
	}

	private void copyFields(final Node copy, final Map<Object, Object> copies) {
		for (final Field field : this.getFields(this.getClass())) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
				continue;
			}

			try {
				field.setAccessible(true);
				final Object original = field.get(this);
				if (!Modifier.isFinal(field.getModifiers()) || copies.containsKey(original)) {
					field.set(copy, copies.getOrDefault(original, original));
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					final Collection<Object> collection = (Collection<Object>) field.get(copy);
					collection.clear();
					((Collection<Object>) original).forEach(element -> collection.add(copies.getOrDefault(element, element)));
				} else if (Map.class.isAssignableFrom(field.getType())) {
					final Map<Object, Object> map = (Map<Object, Object>) field.get(copy);
					map.clear();
					((Map<Object, Object>) original).forEach((key, value) -> map.put(copies.getOrDefault(key, key), copies.getOrDefault(value, value)));
				}
			} catch (final Exception exception) {
				throw new IllegalStateException("The node " + JOIDLog.nameOf(this.getClass()) + " cannot be copied: its field " + field.getName() + " cannot be set (" + exception + "), mark the field transient to leave it out of copy()", exception);
			}
		}
	}

	private @NonNull List<@NonNull Field> getFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Collections.addAll(fields, c.getDeclaredFields());
		}
		return fields;
	}

	@Override
	public @NonNull String toString() {
		if (JOIDEnv.isDevMode()) {
			return Node.PRETTY_GSON.toJson(this.json());
		}

		return this.toJson();
	}

}