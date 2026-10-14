package dev.joid.lib.ui.node;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tween.TweenEquation;
import dev.joid.lib.animation.tween.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.input.cursor.Cursor;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.shader.pipeline.IShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.signal.ComputedSignal;
import dev.joid.lib.signal.ISignalSubscriber;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.SignalContext;
import dev.joid.lib.signal.replay.SignalReplay;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.event.Event;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.event.impl.node.NodeAnimateEvent;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeCharEvent;
import dev.joid.lib.ui.event.impl.node.NodeChildEvent;
import dev.joid.lib.ui.event.impl.node.NodeDragEvent;
import dev.joid.lib.ui.event.impl.node.NodeDrawEvent;
import dev.joid.lib.ui.event.impl.node.NodeHoverEvent;
import dev.joid.lib.ui.event.impl.node.NodeKeyEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.ui.event.impl.node.NodeScrollEvent;
import dev.joid.lib.ui.event.impl.node.NodeScrollEvent.ScrollAxis;
import dev.joid.lib.ui.event.impl.node.NodeWatchEvent;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.hover.IHoverElement;
import dev.joid.lib.ui.node.hover.impl.TextHoverElement;
import dev.joid.lib.ui.node.impl.structure.reorderable.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.layer.INodeLayer;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableAreaType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.ui.node.property.position.PositionProperty;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.ui.task.Task;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.list.IndexedConcurrentList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@SuppressWarnings("unchecked")
public abstract class Node implements INode {

	private static final transient Gson GSON        = new GsonBuilder().create();
	private static final transient Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final Set<Class<?>> CHECKED_APPLIER_SET = ConcurrentHashMap.newKeySet();

	private static final EventType<NodeMouseEvent<Node>> CLICK = EventType.create("onClick");

	private static final EventType<NodeKeyEvent<Node>>         HANDLE_KEY_PRESSED    = EventType.create("handleKeyPressed");
	private static final EventType<NodeCharEvent<Node>>        HANDLE_CHAR_TYPED     = EventType.create("handleCharTyped");
	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_PRESSED  = EventType.create("handleMousePressed");
	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_DRAGGED  = EventType.create("handleMouseDragged");
	private static final EventType<NodeMouseEvent<Node>>       HANDLE_MOUSE_RELEASED = EventType.create("handleMouseReleased");
	private static final EventType<NodeMouseScrollEvent<Node>> HANDLE_MOUSE_SCROLL   = EventType.create("handleMouseScroll");

	private static final EventType<NodeEvent<Node>>      INIT         = EventType.create("onInit");
	private static final EventType<NodeEvent<Node>>      UPDATE       = EventType.create("onUpdate");
	private static final EventType<NodeEvent<Node>>      DETACH       = EventType.create("onDetach");
	private static final EventType<NodeDrawEvent<Node>>  DRAW         = EventType.create("onDraw");
	private static final EventType<NodeDrawEvent<Node>>  RENDER       = EventType.create("onRender");
	private static final EventType<NodeChildEvent<Node>> CHILD_APPEND = EventType.create("onChildAppend");

	private static final EventType<NodeEvent<Node>>        MOUNT   = EventType.create("onMount");
	private static final EventType<NodeWatchEvent<Node>>   WATCH   = EventType.create("onWatch");
	private static final EventType<NodeAnimateEvent<Node>> ANIMATE = EventType.create("onAnimate");

	private static final EventType<NodeScrollEvent<Node>> SCROLL        = EventType.create("onScroll");
	private static final EventType<NodeScrollEvent<Node>> SCROLL_AT_END = EventType.create("onScrollAtEnd");
	private static final EventType<NodeScrollEvent<Node>> SCROLL_TO_END = EventType.create("onScrollToEnd");

	private static final EventType<NodeDragEvent.Post<Node>> DRAG              = EventType.create("onDrag");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_DROP         = EventType.create("onDragDrop");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_SNAP         = EventType.create("onDragSnap");
	private static final EventType<NodeDragEvent.Post<Node>> DRAG_START        = EventType.create("onDragStart");
	private static final EventType<NodeDragEvent.Pre<Node>>  BEFORE_DRAG_DROP  = EventType.create("beforeDragDrop");
	private static final EventType<NodeDragEvent.Pre<Node>>  BEFORE_DRAG_START = EventType.create("beforeDragStart");

	private static final EventType<NodeHoverEvent<Node>> HOVER       = EventType.create("onHover");
	private static final EventType<NodeHoverEvent<Node>> HOVER_END   = EventType.create("onHoverEnd");
	private static final EventType<NodeHoverEvent<Node>> HOVER_START = EventType.create("onHoverStart");

	private final transient EventBus                   eventBus;
	private final transient List<NodeLink<?>>          linkList;
	private final transient List<Predicate<Node>>      waitingList;
	private final transient Map<String, NodeSource<?>> sourceMap;
	private final transient List<NodeRequirement>      requirementList;

	private final transient TweenAnimator             hoverAnimator;
	private final transient Map<TweenAnimator, Float> animatorMap;

	private final List<Node>                      children;
	private final LinkedList<INodeLayer>          layerList;
	private final IndexedConcurrentList<Node>     drawList;
	private final Map<Class<?>, NodeEffect<Node>> effectMap;

	private final List<IHoverElement>          hoverElementList;
	private final List<Supplier<List<String>>> hoverSupplierList;

	private final double defaultWidth;
	private final double defaultHeight;

	private transient UI   ui;
	private transient Node parent;

	private transient Node           skeleton;
	private transient Node           overflowArea;
	private transient ScrollbarNode  scrollbar;
	private transient Consumer<Node> bodyConsumer;

	private double defaultX;
	private double defaultY;

	private double layoutX;
	private double layoutY;

	private double shiftX;
	private double shiftY;

	private double x;
	private double y;
	private double width;
	private double height;

	private Predicate<Node> visible;
	private Predicate<Node> enabled;
	private Predicate<Node> interactive;

	private Align            anchorX;
	private Align            anchorY;
	private PositionProperty position;
	private OverflowProperty overflow;

	private double            dragX;
	private double            dragY;
	private boolean           dragged;
	private Node              draggedNode;
	private boolean           dragging;
	private double            startDragX;
	private double            startDragY;
	private double            targetDragX;
	private double            targetDragY;
	private DraggableProperty draggable;

	private int    zindex;
	private double zlevel;

	private double aspectRatio;

	private boolean mounted;
	private boolean attached;
	private boolean subscribed;

	private Cursor        cursor;
	private boolean       hovered;
	private long          hoverDuration;
	private boolean       hoverShown;
	private TweenEquation hoverEquation;

	private double scrollX;
	private double scrollY;
	private double maxScrollX;
	private double maxScrollY;
	private double scrollSpeed;
	private double targetScrollX;
	private double targetScrollY;

	private boolean scrollEndX;
	private boolean scrollEndY;

	private double  restX;
	private double  restY;
	private double  drawnX;
	private double  drawnY;
	private boolean moving;

	private long        lastClickTime;
	private MouseButton lastClickButton;

	private Key  lastKey;
	private long lastKeyTime;
	private int  lastCodepoint;

	private long renderTime;
	private long updateCount;

	protected Node(final double x, final double y) {
		this(x, y, 0, 0);
	}

	protected Node(final double x, final double y, final double width, final double height) {
		this.linkList = new ArrayList<>();
		this.waitingList = new ArrayList<>();
		this.sourceMap = new LinkedHashMap<>();
		this.requirementList = new ArrayList<>();
		this.eventBus = EventBus.create(this::describe);

		this.animatorMap   = new HashMap<>();
		this.hoverAnimator = TweenAnimator.create();

		this.children  = new CopyOnWriteArrayList<>();
		this.drawList  = new IndexedConcurrentList<>();
		this.layerList = new LinkedList<>();
		this.effectMap = new LinkedHashMap<>();

		this.hoverElementList  = new LinkedList<>();
		this.hoverSupplierList = new LinkedList<>();

		this.defaultX = this.x = this.drawnX = x;
		this.defaultY = this.y = this.drawnY = y;

		this.defaultWidth  = this.width  = width;
		this.defaultHeight = this.height = height;

		this.visible = node -> true;
		this.enabled = node -> true;
		this.interactive = node -> true;

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
		this.checkRequirements();
		final SignalContext context = SignalContext.current();
		final List<Signal<?>> readList = context.takeReads();
		try {
			this.ui = ui;
			this.attached = true;
			this.drawList.forEach(child -> child.load(this.ui));

			if (this.scrollbar != null) {
				this.scrollbar.load(this.ui);
			}

			if (this.skeleton != null) {
				this.skeleton.load(this.ui);
			}

			this.effectMap.values().stream().sorted(Comparator.comparingInt(NodeEffect::getPriority)).forEach(effect -> effect.init(this, this.ui));
			SignalReplay.enter(this, this.ui);
			try {
				this.init(this.ui);
			} finally {
				SignalReplay.exit();
			}
			this.subscribe();
			this.fire(Node.INIT, () -> NodeEvent.create(this));
		} finally {
			context.clearReads();
			context.putBackReads(readList);
		}

		this.updateCount++;
	}

	public final void render(final double mouseX, final double mouseY) {
		final long now = BridgeHandler.CLOCK.get().nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.getModelView().push();
		try {
			Color.reset();
			this.pull();
			if (this.parent != null) {
				render.getModelView().translate(this.parent.x, this.parent.y, 0D);
				if (this.position == PositionProperty.ABSOLUTE) {
					render.getModelView().translate(-this.parent.getAbsoluteX(), -this.parent.getAbsoluteY(), 0D);
				}
			}

			render.getModelView().translate(0D, 0D, this.zlevel);

			if (this.isVisible()) {
				if (this.aspectRatio > 0D) {
					if (this.width != 0) {
						this.height = this.width / this.aspectRatio;
					} else if (this.height != 0) {
						this.width = this.height * this.aspectRatio;
					}
				}

				this.place();

				final boolean wasHovered = this.hovered;
				this.hovered = this.computeHovered();
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

				boolean scrollsX = false;
				boolean scrollsY = false;
				if (this.overflow == OverflowProperty.SCROLL) {
					this.maxScrollX = 0;
					double scrollOffsetX = Double.MIN_VALUE;
					for (final Node child : this.drawList) {
						if (child.position == PositionProperty.ABSOLUTE) {
							continue;
						}

						this.maxScrollX = Math.max(this.maxScrollX, child.getPlacedX() + child.getContentWidth() - this.width);
						if (scrollOffsetX == Double.MIN_VALUE) {
							scrollOffsetX = child.getPlacedX();
						} else {
							scrollOffsetX = Math.min(scrollOffsetX, child.getPlacedX());
						}
					}

					if (this.hasOverflowX()) {
						this.maxScrollX += scrollOffsetX;
						scrollsX = true;
					}

					this.maxScrollY = 0;
					double scrollOffsetY = Double.MIN_VALUE;
					for (final Node child : this.drawList) {
						if (child.position == PositionProperty.ABSOLUTE) {
							continue;
						}

						this.maxScrollY = Math.max(this.maxScrollY, child.getPlacedY() + child.getContentHeight() - this.height);
						if (scrollOffsetY == Double.MIN_VALUE) {
							scrollOffsetY = child.getPlacedY();
						} else {
							scrollOffsetY = Math.min(scrollOffsetY, child.getPlacedY());
						}
					}

					if (this.hasOverflowY()) {
						this.maxScrollY += scrollOffsetY;
						scrollsY = true;
					}
				}

				this.targetScrollX = Math.min(Math.max(this.targetScrollX, -this.maxScrollX), 0);
				this.targetScrollY = Math.min(Math.max(this.targetScrollY, -this.maxScrollY), 0);
				this.scrollEndX = this.scrollEndX && this.targetScrollX == -this.maxScrollX;
				this.scrollEndY = this.scrollEndY && this.targetScrollY == -this.maxScrollY;

				if (this.targetScrollX != this.scrollX) {
					final double speed = this.scrollbar != null && this.scrollbar.isDragging() ? 1D : 0.2D;
					this.scrollX = this.ui.lerpByFramerate(this.scrollX, this.targetScrollX, speed, speed, true);
				}

				if (this.targetScrollY != this.scrollY) {
					final double speed = this.scrollbar != null && this.scrollbar.isDragging() ? 1D : 0.2D;
					this.scrollY = this.ui.lerpByFramerate(this.scrollY, this.targetScrollY, speed, speed, true);
				}

				if (this.scrollEndX && this.scrollX == this.targetScrollX) {
					this.scrollEndX = false;
					this.fire(Node.SCROLL_AT_END, () -> NodeScrollEvent.create(this, ScrollAxis.HORIZONTAL, this.scrollX));
				}

				if (this.scrollEndY && this.scrollY == this.targetScrollY) {
					this.scrollEndY = false;
					this.fire(Node.SCROLL_AT_END, () -> NodeScrollEvent.create(this, ScrollAxis.VERTICAL, this.scrollY));
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

				if (this.scrollbar != null) {
					if (this.scrollbar.isHorizontal() && this.hasOverflowX()) {
						final float percent = (float) Math.min(1, Math.max(0, Math.abs(this.scrollX / this.maxScrollX)));
						this.layoutPosition(this.scrollbar, this.scrollbar.getScrollWidth() * percent, 0D);
					}

					if (!this.scrollbar.isHorizontal() && this.hasOverflowY()) {
						final float percent = (float) Math.min(1, Math.max(0, Math.abs(this.scrollY / this.maxScrollY)));
						this.layoutPosition(this.scrollbar, 0D, this.scrollbar.getScrollHeight() * percent);
					}
				}

				if (this.dragging && BridgeHandler.WINDOW.get().isMouseGrabbed()) {
					this.stopDragging();
				}

				if (this.draggable != null && this.draggable.isEnabled(this)) {
					if (!this.dragging && !this.dragged) {
						this.targetDragX = this.getAbsoluteX();
						this.targetDragY = this.getAbsoluteY();
					}

					if (this.draggable.getAreaType() != DraggableAreaType.FREE && (this.draggable.getAreaType() != DraggableAreaType.PARENT || this.parent != null) && (this.dragging || this.draggable.getType() == DraggableType.MOVE)) {
						final double[] bounds = this.draggable.getBounds(this);
						final double boundX = bounds[0];
						final double boundY = bounds[1];
						final double boundWidth = bounds[2];
						final double boundHeight = bounds[3];

						final double targetX = this.targetDragX;
						final double targetY = this.targetDragY;
						if (this.targetDragX < boundX) {
							this.targetDragX = boundX;
						} else if (this.targetDragX + this.width > boundX + boundWidth) {
							this.targetDragX = boundX + boundWidth - this.width;
						}

						if (this.targetDragY < boundY) {
							this.targetDragY = boundY;
						} else if (this.targetDragY + this.height > boundY + boundHeight) {
							this.targetDragY = boundY + boundHeight - this.height;
						}

						if (this.targetDragX != targetX || this.targetDragY != targetY) {
							this.dragged = true;
						}
					}

					if (this.dragged) {
						if (this.draggable.getType() == DraggableType.MOVE) {
							final double newAbsoluteX = this.draggable.lerp(this.getUi().getFrameTime(), this.getAbsoluteX(), this.targetDragX);
							final double newAbsoluteY = this.draggable.lerp(this.getUi().getFrameTime(), this.getAbsoluteY(), this.targetDragY);
							final double diffX = this.getAbsoluteX() - this.x;
							final double diffY = this.getAbsoluteY() - this.y;

							this.moveTo(newAbsoluteX - diffX, newAbsoluteY - diffY);
						} else if (this.draggable.getType() == DraggableType.COPY && this.draggedNode != null) {
							final double newAbsoluteX = this.draggable.lerp(this.getUi().getFrameTime(), this.draggedNode.getAbsoluteX(), this.targetDragX);
							final double newAbsoluteY = this.draggable.lerp(this.getUi().getFrameTime(), this.draggedNode.getAbsoluteY(), this.targetDragY);
							this.draggedNode.moveTo(newAbsoluteX, newAbsoluteY);
						}

						if (!this.dragging && this.getAbsoluteX() == this.targetDragX && this.getAbsoluteY() == this.targetDragY) {
							this.dragged = false;
						}
					}
				}

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

				final boolean wasMounted = this.mounted;
				this.mounted = this.isMounted();

				if (!wasMounted && this.mounted) {
					this.fire(Node.MOUNT, () -> NodeEvent.create(this));
				}

				final List<NodeEffect<Node>> effects = this.getAppliedEffects();
				final List<NodeEffect<Node>> shaderEffects = effects.stream().filter(NodeEffect::isShaderEffect).collect(Collectors.toList());
				final List<NodeEffect<Node>> otherEffects = effects.stream().filter(e -> !e.isShaderEffect()).collect(Collectors.toList());

				final List<NodeEffect<Node>> selfShaderEffects = shaderEffects.stream().filter(e -> e.getScope() == NodeEffectScope.SELF).collect(Collectors.toList());
				final List<NodeEffect<Node>> subtreeShaderEffects = shaderEffects.stream().filter(e -> e.getScope() == NodeEffectScope.CHILDREN).collect(Collectors.toList());

				otherEffects.forEach(effect -> effect.pre(this, mouseX, mouseY));

				final Runnable maskDraw = () -> {
					this.ui.mask(this.x, this.y, this.width, this.height, () -> {
						if (this.overflow != OverflowProperty.NONE) {
							this.drawList.forEach(child -> child.overflowArea = this);
						} else if (this.overflowArea != null) {
							this.drawList.forEach(child -> child.overflowArea = this.overflowArea);
						}

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
						if (selfShaderEffects.isEmpty()) {
							selfDraw.run();
						} else {
							final List<IShaderPass> selfPasses = selfShaderEffects.stream().flatMap(e -> e.toShaderPasses(this).stream()).collect(Collectors.toList());
							ShaderPipeline.render(this, selfPasses, selfDraw);
						}
						this.fire(Node.DRAW, () -> NodeDrawEvent.create(this, mouseX, mouseY));

						this.drawList.ordered().stream().filter(child -> child.zindex >= 0).forEach(child -> child.render(mouseX, mouseY));
						this.layerList.forEach(layer -> layer.draw(mouseX, mouseY));

						if (this.draggedNode != null) {
							render.pushState();
							render.getModelView().push();
							try {
								render.getState().stencil(StencilState.DISABLED);

								if (this.parent != null) {
									render.getModelView().translate(-this.parent.x, -this.parent.y, 0D);
								}

								this.draggedNode.render(mouseX, mouseY);
							} finally {
								render.getModelView().pop();
								render.popState();
							}
						}
						this.fire(Node.RENDER, () -> NodeDrawEvent.create(this, mouseX, mouseY));
					}, this.overflow != OverflowProperty.NONE);
				};

				try {
					if (!subtreeShaderEffects.isEmpty()) {
						final List<IShaderPass> subtreePasses = subtreeShaderEffects.stream().flatMap(e -> e.toShaderPasses(this).stream()).collect(Collectors.toList());
						ShaderPipeline.render(this, subtreePasses, maskDraw);
					} else {
						maskDraw.run();
					}
				} finally {
					for (int i = otherEffects.size() - 1; i >= 0; i--) {
						otherEffects.get(i).post(this, mouseX, mouseY);
					}
				}

				if (this.overflow == OverflowProperty.SCROLL && this.scrollbar != null && (this.scrollbar.isHorizontal() ? this.hasOverflowX() : this.hasOverflowY())) {
					this.scrollbar.render(mouseX, mouseY);
				}
			} else {
				this.endHover(true);
			}
		} finally {
			Color.reset();
			render.getModelView().pop();
		}
		this.renderTime = BridgeHandler.CLOCK.get().nanoTime() - now;
	}

	public boolean renderHover(final double mouseX, final double mouseY) {
		final List<IHoverElement> hoverList = new LinkedList<>(this.hoverElementList);
		if (!this.hoverSupplierList.isEmpty()) {
			final List<String> lines = new LinkedList<>();
			for (final Supplier<List<String>> hoverSupplier : this.hoverSupplierList) {
				lines.addAll(hoverSupplier.get());
			}

			if (!lines.isEmpty()) {
				hoverList.add(new TextHoverElement(lines));
			}
		}

		hoverList.forEach(element -> element.render(this, mouseX, mouseY));
		return !hoverList.isEmpty();
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

	private boolean canScrollX(final double notches) {
		return this.hasOverflowX() && (notches > 0D ? this.targetScrollX < 0 : this.targetScrollX > -this.maxScrollX);
	}

	private boolean canScrollY(final double notches) {
		return this.hasOverflowY() && (notches > 0D ? this.targetScrollY < 0 : this.targetScrollY > -this.maxScrollY);
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(this.x, this.y, this.width, this.height, Color.LOADING());
	}

	public final void fireUpdate() {
		this.fireAnimate();
		this.drawList.ordered().stream().forEach(Node::fireUpdate);
		try {
			this.update();
		} catch (final RuntimeException exception) {
			JOIDLog.warnOnce("Node.update." + System.identityHashCode(this) + exception, "The update of " + this.describe() + " failed (" + exception + "), the next updates still run: catch the exception inside update() to handle it", exception);
		}
		this.fire(Node.UPDATE, () -> NodeEvent.create(this));
	}

	public final <T extends Node> @NonNull T onUpdate(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.onUpdate(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onUpdate(final @NonNull EventPriority priority, final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.UPDATE, priority, listener);
	}

	public final <T extends Node> @NonNull T handleMouseScroll(final @NonNull EventListener<NodeMouseScrollEvent<T>> listener) {
		return this.handleMouseScroll(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleMouseScroll(final @NonNull EventPriority priority, final @NonNull EventListener<NodeMouseScrollEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_SCROLL, priority, listener);
	}

	public final boolean fireMouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
		final NodeMouseScrollEvent<Node> input = NodeMouseScrollEvent.create(this, mouseX, mouseY, notchesX, notchesY, null);
		this.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, input);
		return input.isConsumed();
	}

	public final void fireMouseScroll(final double mouseX, final double mouseY, final double notchesX, final double notchesY, final @NonNull ConsumableEvent source) {
		if (!this.isVisible() || !this.isInteractive()) {
			return;
		}

		final NodeMouseScrollEvent<Node> event = NodeMouseScrollEvent.create(this, mouseX, mouseY, notchesX, notchesY, source);
		if (this.scrollbar != null) {
			this.scrollbar.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, event);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, event);
			return;
		}

		final boolean enabled = this.isEnabled();
		this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, event));
		if (enabled) {
			this.fireListeners(Node.HANDLE_MOUSE_SCROLL, event);
			if (!event.isConsumed() && this.isHovered()) {
				final double mappedScrollSpeed = Key.LEFT_CONTROL.isDown() ? this.scrollSpeed * 2 : this.scrollSpeed;
				if (notchesY != 0D && this.hasOverflowY()) {
					if (this.canScrollY(notchesY)) {
						this.scrollY(notchesY > 0D ? 30 : -30, mappedScrollSpeed);
						event.consume();
					}
				} else if (notchesY != 0D && this.canScrollX(notchesY)) {
					this.scrollX(notchesY > 0D ? 30 : -30, mappedScrollSpeed);
					event.consume();
				}

				if (notchesX != 0D && this.canScrollX(notchesX)) {
					this.scrollX(notchesX > 0D ? 30 : -30, mappedScrollSpeed);
					event.consume();
				}
			}

			this.mouseScroll(event);
		}
		this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireMouseScroll(mouseX, mouseY, notchesX, notchesY, event));

		if (enabled) {
			this.fireMonitor(Node.HANDLE_MOUSE_SCROLL, event);
		}
	}

	public final <T extends Node> @NonNull T handleMouseDragged(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.handleMouseDragged(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleMouseDragged(final @NonNull EventPriority priority, final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_DRAGGED, priority, listener);
	}

	public final boolean fireMouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, null);
		this.fireMouseDragged(mouseX, mouseY, button, deltaTime, input);
		return input.isConsumed();
	}

	public final void fireMouseDragged(final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime, final @NonNull ConsumableEvent source) {
		final boolean reached = this.isVisible() && this.isInteractive();
		final boolean waiting = this.skeleton != null && !this.mounted;
		final boolean enabled = reached && !waiting && this.isEnabled();
		final NodeMouseEvent<Node> event = NodeMouseEvent.create(this, mouseX, mouseY, button, deltaTime, source);
		if (reached) {
			if (this.scrollbar != null) {
				this.scrollbar.fireMouseDragged(mouseX, mouseY, button, deltaTime, event);
			}

			if (waiting) {
				this.skeleton.fireMouseDragged(mouseX, mouseY, button, deltaTime, event);
			} else {
				this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireMouseDragged(mouseX, mouseY, button, deltaTime, event));
			}
		}

		if (enabled) {
			this.fireListeners(Node.HANDLE_MOUSE_DRAGGED, event);
			this.mouseDragged(event);
		}

		if (this.dragging) {
			this.fireDrag(() -> {
				this.targetDragX = mouseX - this.dragX;
				this.targetDragY = mouseY - this.dragY;
			});
		}

		if (reached && !waiting) {
			this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireMouseDragged(mouseX, mouseY, button, deltaTime, event));
		}

		if (enabled) {
			this.fireMonitor(Node.HANDLE_MOUSE_DRAGGED, event);
		}
	}

	public final <T extends Node> @NonNull T handleMousePressed(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.handleMousePressed(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleMousePressed(final @NonNull EventPriority priority, final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_PRESSED, priority, listener);
	}

	public final boolean fireMousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, null);
		this.fireMousePressed(mouseX, mouseY, button, input);
		return input.isConsumed();
	}

	public final void fireMousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull ConsumableEvent source) {
		if (!this.isVisible() || !this.isInteractive()) {
			return;
		}

		this.lastClickButton = button;
		this.lastClickTime = BridgeHandler.CLOCK.get().currentTimeMillis();

		final NodeMouseEvent<Node> event = NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, source);
		if (this.scrollbar != null) {
			this.scrollbar.fireMousePressed(mouseX, mouseY, button, event);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireMousePressed(mouseX, mouseY, button, event);
			return;
		}

		final boolean enabled = this.isEnabled();
		this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireMousePressed(mouseX, mouseY, button, event));
		if (enabled) {
			this.fireListeners(Node.HANDLE_MOUSE_PRESSED, event);
			if (!event.isConsumed() && button.isLeft() && this.isHovered() && this.hasListener(Node.CLICK)) {
				this.fire(Node.CLICK, () -> event);
				event.consume();
			}

			this.mousePressed(event);
			if (!event.isConsumed() && this.draggable != null && this.draggable.isEnabled(this) && button.isLeft() && this.isHovered()) {
				this.startDragging(mouseX, mouseY);
				if (this.dragging) {
					event.consume();
				}
			}
		}
		this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireMousePressed(mouseX, mouseY, button, event));

		if (enabled) {
			this.fireMonitor(Node.HANDLE_MOUSE_PRESSED, event);
		}
	}

	public final <T extends Node> @NonNull T handleMouseReleased(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.handleMouseReleased(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleMouseReleased(final @NonNull EventPriority priority, final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.HANDLE_MOUSE_RELEASED, priority, listener);
	}

	public final boolean fireMouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button) {
		final NodeMouseEvent<Node> input = NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, null);
		this.fireMouseReleased(mouseX, mouseY, button, input);
		return input.isConsumed();
	}

	public final void fireMouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull ConsumableEvent source) {
		final NodeMouseEvent<Node> event = NodeMouseEvent.create(this, mouseX, mouseY, button, 0L, source);
		if (this.scrollbar != null) {
			this.scrollbar.fireMouseReleased(mouseX, mouseY, button, event);
		}

		if (this.dragging) {
			this.stopDragging();
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireMouseReleased(mouseX, mouseY, button, event);
			return;
		}

		if (!this.isVisible() || !this.isInteractive()) {
			return;
		}

		final boolean enabled = this.isEnabled();
		this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireMouseReleased(mouseX, mouseY, button, event));
		if (enabled) {
			this.fireListeners(Node.HANDLE_MOUSE_RELEASED, event);
			this.mouseReleased(event);
		}
		this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireMouseReleased(mouseX, mouseY, button, event));

		if (enabled) {
			this.fireMonitor(Node.HANDLE_MOUSE_RELEASED, event);
		}
	}

	public final <T extends Node> @NonNull T handleCharTyped(final @NonNull EventListener<NodeCharEvent<T>> listener) {
		return this.handleCharTyped(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleCharTyped(final @NonNull EventPriority priority, final @NonNull EventListener<NodeCharEvent<T>> listener) {
		return this.listen(Node.HANDLE_CHAR_TYPED, priority, listener);
	}

	public final boolean fireCharTyped(final int codepoint) {
		final NodeCharEvent<Node> input = NodeCharEvent.create(this, codepoint, null);
		this.fireCharTyped(codepoint, input);
		return input.isConsumed();
	}

	public final void fireCharTyped(final int codepoint, final @NonNull ConsumableEvent source) {
		if (!this.isVisible()) {
			return;
		}

		this.lastCodepoint = codepoint;

		final NodeCharEvent<Node> event = NodeCharEvent.create(this, codepoint, source);
		if (this.scrollbar != null) {
			this.scrollbar.fireCharTyped(codepoint, event);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireCharTyped(codepoint, event);
			return;
		}

		final boolean enabled = this.isEnabled();
		this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireCharTyped(codepoint, event));
		if (enabled) {
			this.fireListeners(Node.HANDLE_CHAR_TYPED, event);
			this.charTyped(event);
		}
		this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireCharTyped(codepoint, event));

		if (enabled) {
			this.fireMonitor(Node.HANDLE_CHAR_TYPED, event);
		}
	}

	public final <T extends Node> @NonNull T handleKeyPressed(final @NonNull EventListener<NodeKeyEvent<T>> listener) {
		return this.handleKeyPressed(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T handleKeyPressed(final @NonNull EventPriority priority, final @NonNull EventListener<NodeKeyEvent<T>> listener) {
		return this.listen(Node.HANDLE_KEY_PRESSED, priority, listener);
	}

	public final boolean fireKeyPressed(final @NonNull Key key) {
		final NodeKeyEvent<Node> input = NodeKeyEvent.create(this, key, null);
		this.fireKeyPressed(key, input);
		return input.isConsumed();
	}

	public final void fireKeyPressed(final @NonNull Key key, final @NonNull ConsumableEvent source) {
		if (!this.isVisible()) {
			return;
		}

		this.lastKey = key;
		this.lastKeyTime = BridgeHandler.CLOCK.get().currentTimeMillis();

		final NodeKeyEvent<Node> event = NodeKeyEvent.create(this, key, source);
		if (this.scrollbar != null) {
			this.scrollbar.fireKeyPressed(key, event);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.fireKeyPressed(key, event);
			return;
		}

		final boolean enabled = this.isEnabled();
		this.drawList.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.fireKeyPressed(key, event));
		if (enabled) {
			this.fireListeners(Node.HANDLE_KEY_PRESSED, event);
			this.keyPressed(event);
		}
		this.drawList.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.fireKeyPressed(key, event));

		if (enabled) {
			this.fireMonitor(Node.HANDLE_KEY_PRESSED, event);
		}
	}

	public final void fireDetach() {
		this.drawList.forEach(Node::fireDetach);

		if (this.scrollbar != null) {
			this.scrollbar.fireDetach();
		}

		if (this.skeleton != null) {
			this.skeleton.fireDetach();
		}

		final boolean attached = this.attached;
		this.attached = false;
		this.unsubscribe();
		this.getTaskList().forEach(Task::detach);
		this.endInteraction();
		if (attached) {
			this.effectMap.values().forEach(effect -> effect.detach(this));
		}
		this.detach();
		this.fire(Node.DETACH, () -> NodeEvent.create(this));
	}

	public final <T extends Node> @NonNull T onDetach(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.onDetach(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDetach(final @NonNull EventPriority priority, final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.DETACH, priority, listener);
	}

	public final <T extends Node> @NonNull T append(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (node.parent != null && node.parent != this) {
				node.parent.remove(node);
			} else if (node.parent == null && node.ui != null && node.ui.getNodeList().contains(node)) {
				node.ui.remove(node);
				node.clearOverflowArea(node.overflowArea);
			}

			node.parent(this);
			if (this.ui != null) {
				node.load(this.ui);
			}
			this.drawList.add(node);
			this.children.remove(node);
			this.children.add(node);
			this.fire(Node.CHILD_APPEND, () -> NodeChildEvent.create(this, node));
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T remove(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (this.drawList.contains(node)) {
				node.fireDetach();
				this.drawList.remove(node);
				this.children.remove(node);
				node.parent(null);
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
		SignalReplay.enter(this, this.ui);
		try {
			this.bodyConsumer.accept(this);
		} finally {
			SignalReplay.exit();
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T self(final @NonNull Consumer<@NonNull T> consumer) {
		SignalReplay.enter(this, this.ui);
		try {
			consumer.accept((T) this);
		} finally {
			SignalReplay.exit();
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollX(final double value, final double speed) {
		this.scrollOffsetX(this.targetScrollX + value * speed);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollY(final double value, final double speed) {
		this.scrollOffsetY(this.targetScrollY + value * speed);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollRatioX(final float ratio) {
		this.scrollOffsetX(-this.maxScrollX * ratio);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollOffsetX(final double value) {
		final double oldValue = this.targetScrollX;
		this.targetScrollX = Math.min(value, 0);
		final boolean end = this.targetScrollX <= -this.maxScrollX;
		if (end) {
			this.targetScrollX = -this.maxScrollX;
		}

		this.scrollEndX = end && (oldValue != this.targetScrollX || this.scrollEndX);
		if (oldValue == this.targetScrollX) {
			return (T) this;
		}

		this.fire(Node.SCROLL, () -> NodeScrollEvent.create(this, ScrollAxis.HORIZONTAL, this.targetScrollX));
		if (end) {
			this.fire(Node.SCROLL_TO_END, () -> NodeScrollEvent.create(this, ScrollAxis.HORIZONTAL, this.targetScrollX));
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollRatioY(final float ratio) {
		this.scrollOffsetY(-this.maxScrollY * ratio);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollOffsetY(final double value) {
		final double oldValue = this.targetScrollY;
		this.targetScrollY = Math.min(value, 0);
		final boolean end = this.targetScrollY <= -this.maxScrollY;
		if (end) {
			this.targetScrollY = -this.maxScrollY;
		}

		this.scrollEndY = end && (oldValue != this.targetScrollY || this.scrollEndY);
		if (oldValue == this.targetScrollY) {
			return (T) this;
		}

		this.fire(Node.SCROLL, () -> NodeScrollEvent.create(this, ScrollAxis.VERTICAL, this.targetScrollY));
		if (end) {
			this.fire(Node.SCROLL_TO_END, () -> NodeScrollEvent.create(this, ScrollAxis.VERTICAL, this.targetScrollY));
		}
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

	public final <T extends Node> @NonNull T stopDragging() {
		if (this.dragging) {
			final boolean snapping = this.draggable != null && this.draggable.isEnabled(this) && this.draggable.hasSnapping();
			final Node target = snapping ? this.draggable.getSnapping(this.draggable.getType() == DraggableType.COPY && this.draggedNode != null ? this.draggedNode : this) : null;
			final boolean dropped = this.fireDragDrop(target, () -> {
				if (target != null) {
					this.targetDragX = target.getAbsoluteX() + (target.width - this.width) / 2D;
					this.targetDragY = target.getAbsoluteY() + (target.height - this.height) / 2D;
					this.fire(Node.DRAG_SNAP, () -> NodeDragEvent.Post.create(this, target));
				} else if (snapping) {
					this.targetDragX = this.startDragX;
					this.targetDragY = this.startDragY;
				}
				this.dragging = false;
			});

			if (!dropped) {
				this.dragging = false;
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
				this.releaseDraggedNode();
				this.draggedNode = this.duplicate(false);
				this.draggedNode.parent = this.parent;
				this.draggedNode.position(PositionProperty.ABSOLUTE);
				this.draggedNode.layoutX = this.draggedNode.layoutY = 0D;
				this.draggedNode.shiftX = this.draggedNode.shiftY = 0D;
				this.draggedNode.moveTo(this.targetDragX, this.targetDragY);
				this.draggedNode.link(true);
			}
		});
		return (T) this;
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

	public final boolean fireDragStart(final @NonNull Runnable start) {
		final NodeDragEvent.Pre<Node> event = this.fire(Node.BEFORE_DRAG_START, () -> NodeDragEvent.Pre.create(this, null));
		if (event != null && event.isCancelled()) {
			return false;
		}

		start.run();
		this.fire(Node.DRAG_START, () -> NodeDragEvent.Post.create(this, null));
		return true;
	}

	protected final <T extends Node> @NonNull T listen(final @NonNull EventType<?> type, final @NonNull EventPriority priority, final @NonNull EventListener<?> listener) {
		this.eventBus.listen(type, priority, listener);
		return (T) this;
	}

	protected final <E extends Event> E fire(final @NonNull EventType<? super E> type, final @NonNull Supplier<@NonNull E> event) {
		if (!this.eventBus.has(type)) {
			return null;
		}

		final E fired = event.get();
		this.replay(() -> this.eventBus.fire(type, fired));
		return fired;
	}

	protected final <N extends Node, V> boolean change(final @NonNull EventType<? super NodeChangeEvent.Pre<N, V>> before, final @NonNull EventType<? super NodeChangeEvent.Post<N, V>> after, final V previous, final V value, final boolean refusable, final @NonNull Runnable apply) {
		if (this.ui == null) {
			apply.run();
			return true;
		}

		if (refusable) {
			final NodeChangeEvent.Pre<N, V> event = this.fire(before, () -> NodeChangeEvent.Pre.create((N) this, previous, value));
			if (event != null && event.isCancelled()) {
				return false;
			}
		}

		apply.run();
		this.fire(after, () -> NodeChangeEvent.Post.create((N) this, previous, value));
		return true;
	}

	public final boolean hasUi() {
		return this.ui != null;
	}

	public final boolean hasOverflowX() {
		return this.maxScrollX > 0;
	}

	public final boolean hasOverflowY() {
		return this.maxScrollY > 0;
	}

	public final boolean hasListener(final @NonNull EventType<?> type) {
		return this.eventBus.has(type);
	}

	public final boolean hasEffect(final @NonNull Class<?> clazz) {
		return this.effectMap.containsKey(clazz);
	}

	public final @NonNull List<Predicate<Node>> getWaitingList() {
		return Collections.unmodifiableList(this.waitingList);
	}

	public final @NonNull Map<String, NodeSource<?>> getSourceMap() {
		return Collections.unmodifiableMap(this.sourceMap);
	}

	public final @NonNull List<NodeRequirement> getRequirementList() {
		return Collections.unmodifiableList(this.requirementList);
	}

	public final @NonNull List<INodeLayer> getLayerList() {
		return Collections.unmodifiableList(this.layerList);
	}

	public final @NonNull Map<Class<?>, NodeEffect<Node>> getEffectMap() {
		return Collections.unmodifiableMap(this.effectMap);
	}

	public final @NonNull List<IHoverElement> getHoverElementList() {
		return Collections.unmodifiableList(this.hoverElementList);
	}

	public final @NonNull List<Supplier<List<String>>> getHoverSupplierList() {
		return Collections.unmodifiableList(this.hoverSupplierList);
	}

	@Override
	public int getIndex() {
		return this.zindex;
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

	public final <T extends UI> T getUi() {
		return (T) (this.ui != null ? this.ui : UI.getCurrent());
	}

	public final double toDrawX(final double x) {
		return this.x + x - this.getAbsoluteX();
	}

	public final double toDrawY(final double y) {
		return this.y + y - this.getAbsoluteY();
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

	public final @NonNull String getHierarchy() {
		if (this.parent == null) {
			return this.getClass().getSimpleName();
		}

		return this.parent.getHierarchy() + " - " + this.getClass().getSimpleName();
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

	public final @NonNull Map<@NonNull TweenAnimator, @NonNull Float> getAnimatorMap() {
		return Collections.unmodifiableMap(this.animatorMap);
	}

	public final <T extends NodeEffect<?>> T getEffect(final @NonNull Class<T> clazz) {
		return clazz.cast(this.effectMap.get(clazz));
	}

	public final @NonNull List<@NonNull Node> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public final @NonNull List<@NonNull Node> getDrawList() {
		return this.drawList.ordered();
	}

	public final @NonNull List<@NonNull ISignalSubscriber<?>> getLinkList() {
		return Collections.unmodifiableList(this.linkList);
	}

	public final @NonNull List<@NonNull Task> getTaskList() {
		return Task.getTaskList(this);
	}

	public final @NonNull List<@NonNull Shortcut> getShortcutList() {
		return Shortcut.getShortcutList(this);
	}

	public final <T extends Node> T getChild(final int index, final @NonNull Class<T> clazz) {
		int i = 0;
		for (final Node child : this.children) {
			if (clazz.isAssignableFrom(child.getClass())) {
				if (i == index) {
					return (T) child;
				}

				i++;
			}
		}

		return null;
	}

	public final <T extends Node> @NonNull List<@NonNull T> getChildren(final @NonNull Class<T> clazz) {
		return Collections.unmodifiableList(this.children.stream().filter(child -> clazz.isAssignableFrom(child.getClass())).map(child -> (T) child).collect(Collectors.toList()));
	}

	public final <T extends Node> @NonNull T clearChildren() {
		this.drawList.forEach(child -> {
			child.fireDetach();
			child.parent(null);
			child.clearOverflowArea(child.overflowArea);
		});
		this.drawList.clear();
		this.children.clear();
		return (T) this;
	}

	protected final void reorderChildren(final @NonNull List<@NonNull Node> order) {
		if (order.size() != this.children.size() || !order.containsAll(this.children)) {
			throw new IllegalArgumentException("The new order of " + this.getClass().getSimpleName() + " must hold its " + this.children.size() + " children exactly once, but it holds " + order.size() + " nodes");
		}

		this.children.clear();
		this.children.addAll(order);
		this.drawList.clear();
		order.forEach(this.drawList::add);
	}

	protected final void layoutPosition(final @NonNull Node node, final double layoutX, final double layoutY) {
		node.layoutX = layoutX;
		node.layoutY = layoutY;
		node.place();
	}

	public boolean isEnabled() {
		if (this.parent != null && !this.parent.isEnabled()) {
			return false;
		}

		return this.enabled.test(this);
	}

	public boolean isInteractive() {
		if (this.parent != null && !this.parent.isInteractive()) {
			return false;
		}

		return this.interactive.test(this);
	}

	public boolean isVisible() {
		if (this.parent != null && !this.parent.isVisible()) {
			return false;
		}

		if (this.overflowArea != null && this.overflowArea.overflow != OverflowProperty.NONE) {
			if (this.getAbsoluteX() + this.width < this.overflowArea.getAbsoluteX() || this.getAbsoluteX() > this.overflowArea.getAbsoluteX() + this.overflowArea.width || this.getAbsoluteY() + this.height < this.overflowArea.getAbsoluteY() || this.getAbsoluteY() > this.overflowArea.getAbsoluteY() + this.overflowArea.height) {
				return false;
			}
		}

		return this.isVisibleProperty();
	}

	public boolean isDragging() {
		return this.dragging || this.parent instanceof ReorderableFlexNode && ((ReorderableFlexNode) this.parent).isDragging(this);
	}

	public final boolean isMounted() {
		if (this.parent != null && !this.parent.isMounted() && !this.equals(this.parent.skeleton)) {
			return false;
		}

		boolean mounted = this.waitingList.isEmpty();
		if (!mounted) {
			mounted = true;
			for (final Predicate<Node> predicate : this.waitingList) {
				if (!predicate.test(this)) {
					mounted = false;
					break;
				}
			}
		}

		return mounted;
	}

	public boolean isVisibleProperty() {
		return this.visible.test(this);
	}

	protected boolean computeHovered() {
		return this.ui != null && this.ui.isOnTop() && this.ui.getHoveredPath().contains(this);
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

	private boolean isAt(final double x, final double y) {
		return this.isVisible() && (this.overflowArea == null || this.overflowArea.isAt(x, y)) && x > this.getAbsoluteX() && x <= this.getAbsoluteX() + this.width && y > this.getAbsoluteY() && y <= this.getAbsoluteY() + this.height;
	}

	private void collectNodesAt(final double x, final double y, final List<Node> nodeList) {
		if (!this.isVisible()) {
			return;
		}

		if (this.overflow == OverflowProperty.SCROLL && this.scrollbar != null && (this.scrollbar.isHorizontal() ? this.hasOverflowX() : this.hasOverflowY())) {
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

	public final float hoverValue(final float value) {
		return value * Math.min(1F, Math.max(0F, this.hoverAnimator.get()));
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

	protected boolean shouldApplyEffect(final @NonNull NodeEffect<Node> effect) {
		return effect.shouldApply(this);
	}

	private @NonNull List<NodeEffect<Node>> getAppliedEffects() {
		return this.effectMap.values().stream().filter(this::shouldApplyEffect).sorted(Comparator.comparingInt(NodeEffect::getPriority)).collect(Collectors.toList());
	}

	public final <T extends Node> @NonNull T copy() {
		return (T) this.duplicate(true);
	}

	public final <T extends UIStore> T useStore(final @NonNull Class<T> clazz) {
		return this.getUi().useStore(clazz);
	}

	public final <T extends Node> @NonNull T layer(final @NonNull INodeLayer layer) {
		this.layerList.add(layer);
		return (T) this;
	}

	public final <T extends Node> @NonNull T layer(final int index, final @NonNull INodeLayer layer) {
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
		final NodeEffect<Node> removed = this.effectMap.remove(effect);
		if (this.attached && removed != null) {
			removed.detach(this);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearEffects() {
		final List<NodeEffect<Node>> removed = new ArrayList<>(this.effectMap.values());
		this.effectMap.clear();
		if (this.attached) {
			removed.forEach(effect -> effect.detach(this));
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T animate(final @NonNull TweenAnimator animator) {
		this.animatorMap.put(animator, animator.peek());
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeAnimator(final @NonNull TweenAnimator animator) {
		this.animatorMap.remove(animator);
		return (T) this;
	}

	public final <T extends Node> @NonNull T x(final double x) {
		return this.x(Signal.from(x));
	}

	public final <T extends Node> @NonNull T x(final @NonNull Supplier<Double> x) {
		return this.follow("x", x, (node, value) -> {
			node.defaultX = value;
			node.place();
		});
	}

	public final <T extends Node> @NonNull T y(final double y) {
		return this.y(Signal.from(y));
	}

	public final <T extends Node> @NonNull T y(final @NonNull Supplier<Double> y) {
		return this.follow("y", y, (node, value) -> {
			node.defaultY = value;
			node.place();
		});
	}

	public final <T extends Node> @NonNull T width(final double width) {
		return this.width(Signal.from(width));
	}

	public final <T extends Node> @NonNull T width(final @NonNull Supplier<Double> width) {
		return this.follow("width", width, (node, value) -> node.width = value);
	}

	public final <T extends Node> @NonNull T height(final double height) {
		return this.height(Signal.from(height));
	}

	public final <T extends Node> @NonNull T height(final @NonNull Supplier<Double> height) {
		return this.follow("height", height, (node, value) -> node.height = value);
	}

	public final <T extends Node> @NonNull T position(final @NonNull PositionProperty position) {
		return this.position(Signal.from(position));
	}

	public final <T extends Node> @NonNull T position(final @NonNull Supplier<@NonNull PositionProperty> position) {
		return this.follow("position", position, (node, value) -> node.position = value);
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull OverflowProperty overflow) {
		return this.overflow(Signal.from(overflow));
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull Supplier<@NonNull OverflowProperty> overflow) {
		return this.follow("overflow", overflow, Node::changeOverflow);
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Align anchor) {
		return this.anchor(Signal.from(anchor));
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Supplier<@NonNull Align> anchor) {
		return this.follow("anchor", anchor, (node, value) -> {
			node.anchorX = value;
			node.anchorY = value;
			node.place();
		});
	}

	public final <T extends Node> @NonNull T anchorX(final @NonNull Align anchorX) {
		return this.anchorX(Signal.from(anchorX));
	}

	public final <T extends Node> @NonNull T anchorX(final @NonNull Supplier<@NonNull Align> anchorX) {
		return this.follow("anchorX", anchorX, (node, value) -> {
			node.anchorX = value;
			node.place();
		});
	}

	public final <T extends Node> @NonNull T anchorY(final @NonNull Align anchorY) {
		return this.anchorY(Signal.from(anchorY));
	}

	public final <T extends Node> @NonNull T anchorY(final @NonNull Supplier<@NonNull Align> anchorY) {
		return this.follow("anchorY", anchorY, (node, value) -> {
			node.anchorY = value;
			node.place();
		});
	}

	public final <T extends Node> @NonNull T draggable(final @NonNull DraggableProperty draggable) {
		return this.draggable(Signal.from(draggable));
	}

	public final <T extends Node> @NonNull T draggable(final @NonNull Supplier<@NonNull DraggableProperty> draggable) {
		return this.follow("draggable", draggable, (node, value) -> {
			node.draggable = value;
			node.dragging  = false;
			node.dragged   = false;
			node.releaseDraggedNode();
		});
	}

	public final <T extends Node> @NonNull T aspectRatio(final double aspectRatio) {
		return this.aspectRatio(Signal.from(aspectRatio));
	}

	public final <T extends Node> @NonNull T aspectRatio(final @NonNull Supplier<Double> aspectRatio) {
		return this.follow("aspectRatio", aspectRatio, (node, value) -> node.aspectRatio = value);
	}

	public final <T extends Node> @NonNull T ui(final @NonNull UI ui) {
		this.ui = ui;
		return (T) this;
	}

	private <T extends Node> @NonNull T parent(final Node parent) {
		this.parent = parent;
		return (T) this;
	}

	public final <T extends Node> @NonNull T overflowArea(final Node overflowArea) {
		this.overflowArea = overflowArea;
		return (T) this;
	}

	private void changeOverflow(final OverflowProperty overflow) {
		if (this.overflow == OverflowProperty.SCROLL && overflow != OverflowProperty.SCROLL) {
			for (final Node child : this.drawList) {
				child.shiftX = child.shiftY = 0D;
				child.place();
			}

			this.maxScrollX = this.maxScrollY = 0D;
			this.scrollX = this.targetScrollX = 0D;
			this.scrollY = this.targetScrollY = 0D;
			this.scrollEndX = this.scrollEndY = false;
		}

		this.overflow = overflow;
	}

	private void pull() {
		if (this.sourceMap.isEmpty()) {
			return;
		}

		for (final Entry<String, NodeSource<?>> source : new ArrayList<>(this.sourceMap.entrySet())) {
			try {
				source.getValue().pull(this);
			} catch (final RuntimeException exception) {
				JOIDLog.warnOnce("Node.pull." + this.getClass().getName() + "." + source.getKey() + "." + exception.getClass().getName(), "The " + source.getKey() + " of " + this.getClass().getSimpleName() + " cannot take its new value (" + exception + "), it keeps the previous one: check the value its signal gives", exception);
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
		SignalReplay.enter(this, this.getUi());
		try {
			runnable.run();
		} finally {
			SignalReplay.exit();
		}
	}

	private String describe() {
		final UI ui = this.getUi();
		return ui == null ? Node.nameOf(this.getClass()) : Node.nameOf(this.getClass()) + " in " + Node.nameOf(ui.getClass());
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
		if (this.dragging) {
			this.stopDragging();
		}

		if (this.dragged && this.draggable != null && this.draggable.getType() == DraggableType.MOVE) {
			this.moveTo(this.x + (this.targetDragX - this.getAbsoluteX()), this.y + (this.targetDragY - this.getAbsoluteY()));
		}

		this.dragged = false;
		this.releaseDraggedNode();
		this.endHover(false);
		this.hoverAnimator.clear();
		this.mounted = false;
	}

	private void endHover(final boolean recursive) {
		if (!this.hovered && !this.hoverShown) {
			return;
		}

		final boolean wasHovered = this.hovered;
		this.hovered = false;
		if (wasHovered && this.ui != null) {
			this.fire(Node.HOVER_END, () -> NodeHoverEvent.create(this, this.ui.getMouseX(), this.ui.getMouseY()));
		}

		this.hoverAnimator.clear();
		this.hoverShown = false;
		if (recursive) {
			this.drawList.forEach(child -> child.endHover(true));
		}
	}

	private void releaseDraggedNode() {
		if (this.draggedNode != null) {
			this.draggedNode.link(false);
			this.draggedNode = null;
		}
	}

	private void moveTo(final double x, final double y) {
		this.defaultX = x - this.layoutX - this.shiftX + this.getPivotX();
		this.defaultY = y - this.layoutY - this.shiftY + this.getPivotY();
		this.place();
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
			throw new IllegalStateException("The node " + this.getClass().getSimpleName() + " has no DraggableProperty, call draggable(...) first");
		}
	}

	private void checkDragArea() {
		if (this.draggable.getAreaType() == DraggableAreaType.PARENT && this.parent == null) {
			throw new IllegalStateException("The node " + this.getClass().getSimpleName() + " is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()");
		}
	}

	private void clearOverflowArea(final Node area) {
		if (area == null || this.overflowArea != area) {
			return;
		}

		this.overflowArea = null;
		this.drawList.forEach(child -> child.clearOverflowArea(area));
	}

	public final <T extends Node> @NonNull T wait(final @NonNull Predicate<@NonNull T> predicate) {
		this.waitingList.add((Predicate<Node>) predicate);
		return (T) this;
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal) {
		return this.watch(signal, new WatchProperty[0]);
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		return this.addWatch(signal, node -> !node.ui.isClosed(), properties);
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull Supplier<Boolean> condition, final @NonNull WatchProperty @NonNull... properties) {
		return this.addWatch(signal, node -> condition.get(), properties);
	}

	protected final <T extends Node, N extends Node, V> @NonNull T bind(final @NonNull String property, final @NonNull Signal<V> signal, final @NonNull BiConsumer<@NonNull N, @NonNull V> applier) {
		Node.checkApplier(this, property, applier);
		final V current = signal.peek();
		if (current != null) {
			applier.accept((N) this, current);
		}

		this.listen(new NodeLink<>(this, property, (Signal<Object>) signal, node -> !node.ui.isClosed(), true, (node, value) -> {
			if (value != null) {
				applier.accept((N) node, (V) value);
			}
		}));
		return (T) this;
	}

	protected final <V> @NonNull Signal<V> writable(final @NonNull Signal<V> signal) {
		if (signal instanceof ComputedSignal) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + ".signal(...) needs a writable signal: a ComputedSignal is read-only, pass it to a setter instead");
		}
		return signal;
	}

	protected final <V> void sync(final Signal<V> signal, final @NonNull V value) {
		if (signal != null && !value.equals(signal.peek())) {
			signal.set(value);
		}
	}

	protected final <T extends Node, N extends Node, V> @NonNull T follow(final @NonNull String property, final @NonNull Supplier<V> supplier, final @NonNull BiConsumer<@NonNull N, V> applier) {
		Node.checkApplier(this, property, applier);
		final V value = SignalContext.current().untracked(supplier);
		if (supplier instanceof ComputedSignal && ((ComputedSignal<V>) supplier).isConstant()) {
			this.sourceMap.remove(property);
		} else {
			this.sourceMap.put(property, new NodeSource<>(supplier, (BiConsumer<Node, V>) applier, value));
		}

		applier.accept((N) this, value);
		return (T) this;
	}

	protected final <N extends Node> void require(final @NonNull String setting, final @NonNull String advice, final @NonNull Predicate<@NonNull N> present) {
		this.requirementList.add(new NodeRequirement(advice, setting, (Predicate<Node>) present));
	}

	private void checkRequirements() {
		for (final NodeRequirement requirement : this.requirementList) {
			if (!requirement.present.test(this)) {
				throw new IllegalStateException("The " + Node.nameOf(this.getClass()) + " has no " + requirement.setting + ", " + requirement.advice);
			}
		}
	}

	private <T extends Node> T addWatch(final Signal<?> signal, final Predicate<Node> condition, final WatchProperty[] properties) {
		this.listen(new NodeLink<>(this, null, (Signal<Object>) signal, condition, false, (node, value) -> node.applyWatch(signal, properties)));
		return (T) this;
	}

	private void applyWatch(final Signal<?> signal, final WatchProperty[] properties) {
		if (this.ui == null) {
			return;
		}

		SignalReplay.enter(this, this.ui);
		try {
			for (final WatchProperty property : properties) {
				try {
					property.apply(this, signal);
				} catch (final Exception e) {
					JOIDLog.warnOnce("Node.watch." + this.getClass().getName() + "." + e.getClass().getName(), "A watch of " + this.getClass().getSimpleName() + " cannot apply its property when its signal changes (" + e + "), the property keeps its previous value: check the value the signal gives", e);
				}
			}
		} finally {
			SignalReplay.exit();
		}
		this.fire(Node.WATCH, () -> NodeWatchEvent.create(this, signal, properties));
	}

	private <V> void listen(final NodeLink<V> link) {
		if (link.property != null) {
			for (final NodeLink<?> current : new ArrayList<>(this.linkList)) {
				if (link.property.equals(current.property)) {
					current.cancel();
				}
			}
		}

		this.linkList.add(link);
		if (this.subscribed) {
			link.signal.subscribe(link);
		} else {
			link.keep();
		}
	}

	private void link(final boolean active) {
		if (active) {
			this.subscribe();
		} else {
			this.unsubscribe();
		}

		this.drawList.forEach(child -> child.link(active));
		if (this.scrollbar != null) {
			((Node) this.scrollbar).link(active);
		}

		if (this.skeleton != null) {
			this.skeleton.link(active);
		}
	}

	private void subscribe() {
		if (this.subscribed) {
			return;
		}

		this.subscribed = true;
		final boolean loaded = this.updateCount > 0;
		for (final NodeLink<?> link : new ArrayList<>(this.linkList)) {
			link.subscribe(loaded);
		}
	}

	private void unsubscribe() {
		if (!this.subscribed) {
			return;
		}

		this.subscribed = false;
		for (final NodeLink<?> link : this.linkList) {
			link.unsubscribe();
		}
	}

	public final <T extends Node> @NonNull T cursor(final @NonNull Cursor cursor) {
		return this.cursor(Signal.from(cursor));
	}

	public final <T extends Node> @NonNull T cursor(final @NonNull Supplier<Cursor> cursor) {
		return this.follow("cursor", cursor, (node, value) -> node.cursor = value);
	}

	public final <T extends Node> @NonNull T hoverDuration(final long hoverDuration) {
		return this.hoverDuration(Signal.from(hoverDuration));
	}

	public final <T extends Node> @NonNull T hoverDuration(final @NonNull Supplier<Long> hoverDuration) {
		return this.follow("hoverDuration", hoverDuration, (node, value) -> node.hoverDuration = value);
	}

	public final <T extends Node> @NonNull T hoverEquation(final @NonNull TweenEquation hoverEquation) {
		return this.hoverEquation(Signal.from(hoverEquation));
	}

	public final <T extends Node> @NonNull T hoverEquation(final @NonNull Supplier<@NonNull TweenEquation> hoverEquation) {
		return this.follow("hoverEquation", hoverEquation, (node, value) -> node.hoverEquation = value);
	}

	public final <T extends Node> @NonNull T scrollSpeed(final double scrollSpeed) {
		return this.scrollSpeed(Signal.from(scrollSpeed));
	}

	public final <T extends Node> @NonNull T scrollSpeed(final @NonNull Supplier<Double> scrollSpeed) {
		return this.follow("scrollSpeed", scrollSpeed, (node, value) -> node.scrollSpeed = value);
	}

	public final <T extends Node> @NonNull T scrollbar(final @NonNull ScrollbarNode scrollbar) {
		if (this.scrollbar != null && this.scrollbar != scrollbar && this.scrollbar.isAttached()) {
			this.scrollbar.fireDetach();
		}

		this.scrollbar = scrollbar;
		this.scrollbar.scrollNode(this);
		((Node) this.scrollbar).parent(this);
		if (this.ui != null) {
			this.scrollbar.load(this.ui);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T skeleton(final @NonNull Function<@NonNull T, Node> skeleton) {
		final Node previous = this.skeleton;
		this.skeleton = skeleton.apply((T) this);
		if (previous != null && previous != this.skeleton && previous.attached) {
			previous.fireDetach();
		}

		if (this.skeleton != null) {
			this.skeleton.parent(this);
			if (this.ui != null) {
				this.skeleton.load(this.ui);
			}
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T zindex(final int zindex) {
		return this.zindex(Signal.from(zindex));
	}

	public final <T extends Node> @NonNull T zindex(final @NonNull Supplier<Integer> zindex) {
		return this.follow("zindex", zindex, (node, value) -> node.zindex = value);
	}

	public final <T extends Node> @NonNull T zlevel(final double zlevel) {
		return this.zlevel(Signal.from(zlevel));
	}

	public final <T extends Node> @NonNull T zlevel(final @NonNull Supplier<Double> zlevel) {
		return this.follow("zlevel", zlevel, (node, value) -> node.zlevel = value);
	}

	public final <T extends Node> @NonNull T visible(final boolean visible) {
		return this.visible(Signal.from(visible));
	}

	public final <T extends Node> @NonNull T visible(final @NonNull Supplier<Boolean> visible) {
		this.visible = node -> Boolean.TRUE.equals(visible.get());
		return (T) this;
	}

	public final <T extends Node> @NonNull T visible(final @NonNull Predicate<@NonNull T> visible) {
		this.visible = (Predicate<Node>) visible;
		return (T) this;
	}

	public final <T extends Node> @NonNull T enabled(final boolean enabled) {
		return this.enabled(Signal.from(enabled));
	}

	public final <T extends Node> @NonNull T enabled(final @NonNull Supplier<Boolean> enabled) {
		this.enabled = node -> Boolean.TRUE.equals(enabled.get());
		return (T) this;
	}

	public final <T extends Node> @NonNull T enabled(final @NonNull Predicate<@NonNull T> enabled) {
		this.enabled = (Predicate<Node>) enabled;
		return (T) this;
	}

	public final <T extends Node> @NonNull T interactive(final boolean interactive) {
		return this.interactive(Signal.from(interactive));
	}

	public final <T extends Node> @NonNull T interactive(final @NonNull Supplier<Boolean> interactive) {
		this.interactive = node -> Boolean.TRUE.equals(interactive.get());
		return (T) this;
	}

	public final <T extends Node> @NonNull T interactive(final @NonNull Predicate<@NonNull T> interactive) {
		this.interactive = (Predicate<Node>) interactive;
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearHover() {
		this.hoverElementList.clear();
		this.hoverSupplierList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearHoverLines() {
		this.hoverSupplierList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearHoverElements() {
		this.hoverElementList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverLines(final @NonNull HoverSupplier supplier) {
		this.hoverSupplierList.clear();
		this.hover(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverLines(final @NonNull Supplier<@NonNull List<@NonNull String>> supplier) {
		this.hoverSupplierList.clear();
		this.hover(supplier);
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

	public final <T extends Node> @NonNull T hover(final @NonNull Supplier<@NonNull List<@NonNull String>> supplier) {
		this.hoverSupplierList.add(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeHover(final @NonNull IHoverElement element) {
		this.hoverElementList.remove(element);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverElements(final @NonNull IHoverElement element) {
		this.hoverElementList.clear();
		this.hover(element);
		return (T) this;
	}

	public final <T extends Node> @NonNull T onClick(final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.onClick(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onClick(final @NonNull EventPriority priority, final @NonNull EventListener<NodeMouseEvent<T>> listener) {
		return this.listen(Node.CLICK, priority, listener);
	}

	public final <T extends Node> @NonNull T onInit(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.onInit(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onInit(final @NonNull EventPriority priority, final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.INIT, priority, listener);
	}

	public final <T extends Node> @NonNull T onRender(final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.onRender(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onRender(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.listen(Node.RENDER, priority, listener);
	}

	public final <T extends Node> @NonNull T onDraw(final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.onDraw(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDraw(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDrawEvent<T>> listener) {
		return this.listen(Node.DRAW, priority, listener);
	}

	public final <T extends Node> @NonNull T onChildAppend(final @NonNull EventListener<NodeChildEvent<T>> listener) {
		return this.onChildAppend(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onChildAppend(final @NonNull EventPriority priority, final @NonNull EventListener<NodeChildEvent<T>> listener) {
		return this.listen(Node.CHILD_APPEND, priority, listener);
	}

	public final <T extends Node> @NonNull T onMount(final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.onMount(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onMount(final @NonNull EventPriority priority, final @NonNull EventListener<NodeEvent<T>> listener) {
		return this.listen(Node.MOUNT, priority, listener);
	}

	public final <T extends Node> @NonNull T onWatch(final @NonNull EventListener<NodeWatchEvent<T>> listener) {
		return this.onWatch(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onWatch(final @NonNull EventPriority priority, final @NonNull EventListener<NodeWatchEvent<T>> listener) {
		return this.listen(Node.WATCH, priority, listener);
	}

	public final <T extends Node> @NonNull T onAnimate(final @NonNull EventListener<NodeAnimateEvent<T>> listener) {
		return this.onAnimate(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onAnimate(final @NonNull EventPriority priority, final @NonNull EventListener<NodeAnimateEvent<T>> listener) {
		return this.listen(Node.ANIMATE, priority, listener);
	}

	public final <T extends Node> @NonNull T onScroll(final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.onScroll(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onScroll(final @NonNull EventPriority priority, final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.listen(Node.SCROLL, priority, listener);
	}

	public final <T extends Node> @NonNull T onScrollToEnd(final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.onScrollToEnd(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onScrollToEnd(final @NonNull EventPriority priority, final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.listen(Node.SCROLL_TO_END, priority, listener);
	}

	public final <T extends Node> @NonNull T onScrollAtEnd(final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.onScrollAtEnd(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onScrollAtEnd(final @NonNull EventPriority priority, final @NonNull EventListener<NodeScrollEvent<T>> listener) {
		return this.listen(Node.SCROLL_AT_END, priority, listener);
	}

	public final <T extends Node> @NonNull T beforeDragStart(final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.beforeDragStart(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T beforeDragStart(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.listen(Node.BEFORE_DRAG_START, priority, listener);
	}

	public final <T extends Node> @NonNull T onDragStart(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.onDragStart(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDragStart(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_START, priority, listener);
	}

	public final <T extends Node> @NonNull T onDrag(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.onDrag(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDrag(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG, priority, listener);
	}

	public final <T extends Node> @NonNull T beforeDragDrop(final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.beforeDragDrop(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T beforeDragDrop(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Pre<T>> listener) {
		return this.listen(Node.BEFORE_DRAG_DROP, priority, listener);
	}

	public final <T extends Node> @NonNull T onDragDrop(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.onDragDrop(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDragDrop(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_DROP, priority, listener);
	}

	public final <T extends Node> @NonNull T onDragSnap(final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.onDragSnap(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onDragSnap(final @NonNull EventPriority priority, final @NonNull EventListener<NodeDragEvent.Post<T>> listener) {
		return this.listen(Node.DRAG_SNAP, priority, listener);
	}

	public final <T extends Node> @NonNull T onHover(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.onHover(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onHover(final @NonNull EventPriority priority, final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER, priority, listener);
	}

	public final <T extends Node> @NonNull T onHoverStart(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.onHoverStart(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onHoverStart(final @NonNull EventPriority priority, final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER_START, priority, listener);
	}

	public final <T extends Node> @NonNull T onHoverEnd(final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.onHoverEnd(EventPriority.NORMAL, listener);
	}

	public final <T extends Node> @NonNull T onHoverEnd(final @NonNull EventPriority priority, final @NonNull EventListener<NodeHoverEvent<T>> listener) {
		return this.listen(Node.HOVER_END, priority, listener);
	}

	public @NonNull String toJson() {
		return Node.GSON.toJson(this.json());
	}

	private @NonNull JsonObject json() {
		final JsonObject json = new JsonObject();
		json.addProperty("index", this.getIndex());
		json.addProperty("mappedIndex", this.getMappedIndex());
		json.addProperty("name", this.getClass().getSimpleName());
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

			json.addProperty("visible", this.visible.test(this));
			json.addProperty("enabled", this.enabled.test(this));
			json.addProperty("interactive", this.interactive.test(this));
			json.addProperty("hovered", this.hovered);

			json.addProperty("isChild", this.parent != null);
			json.addProperty("children", this.drawList.size());
			json.addProperty("hierarchy", this.getHierarchy() + " (this)");
		}

		return json;
	}

	private @NonNull Node duplicate(final boolean listeners) {
		final Node copy = this.instantiate();
		copy.ui = this.ui;

		copy.eventBus.clear();
		if (listeners) {
			copy.eventBus.addAll(this.eventBus);
		}

		this.effectMap.forEach((type, effect) -> copy.effectMap.put(type, effect.copy()));
		copy.layerList.addAll(this.layerList);
		copy.hoverElementList.addAll(this.hoverElementList);
		copy.hoverSupplierList.addAll(this.hoverSupplierList);

		copy.defaultX = this.defaultX;
		copy.defaultY = this.defaultY;
		copy.layoutX = this.layoutX;
		copy.layoutY = this.layoutY;
		copy.shiftX = this.shiftX;
		copy.shiftY = this.shiftY;

		copy.x = this.x;
		copy.y = this.y;
		copy.width = this.width;
		copy.height = this.height;

		copy.visible = this.visible;
		copy.enabled = this.enabled;
		copy.interactive = this.interactive;
		copy.position = this.position;
		copy.overflow = this.overflow;
		copy.anchorX = this.anchorX;
		copy.anchorY = this.anchorY;

		copy.draggable = this.draggable;
		copy.zindex = this.zindex;
		copy.zlevel = this.zlevel;

		copy.aspectRatio = this.aspectRatio;

		copy.cursor = this.cursor;
		copy.hoverDuration = this.hoverDuration;
		copy.hoverEquation = this.hoverEquation;
		copy.scrollSpeed = this.scrollSpeed;

		copy.waitingList.clear();
		copy.waitingList.addAll(this.waitingList);
		copy.animatorMap.putAll(this.animatorMap);

		copy.mounted = this.mounted;

		copy.sourceMap.clear();
		this.sourceMap.forEach((property, source) -> copy.sourceMap.put(property, source.copy()));
		copy.linkList.clear();
		this.linkList.forEach(link -> copy.listen(link.copy(copy)));

		for (final Node child : this.children) {
			final Node childCopy = child.duplicate(listeners);
			childCopy.parent = copy;
			copy.drawList.add(childCopy);
			copy.children.add(childCopy);
		}

		if (this.scrollbar != null) {
			final Node scrollbar = ((Node) this.scrollbar).duplicate(listeners);
			scrollbar.parent = copy;
			copy.scrollbar = ((ScrollbarNode) scrollbar).scrollNode(copy);
		}

		if (this.skeleton != null) {
			copy.skeleton = this.skeleton.duplicate(listeners);
			copy.skeleton.parent = copy;
		}

		for (final Field field : this.getFields(this.getClass())) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.getDeclaringClass() == Node.class) {
				continue;
			}

			try {
				field.setAccessible(true);
				if (!Modifier.isFinal(field.getModifiers())) {
					field.set(copy, field.get(this));
				} else if (Collection.class.isAssignableFrom(field.getType())) {
					final Collection<Object> collection = (Collection<Object>) field.get(copy);
					collection.clear();
					collection.addAll((Collection<Object>) field.get(this));
				} else if (Map.class.isAssignableFrom(field.getType())) {
					final Map<Object, Object> map = (Map<Object, Object>) field.get(copy);
					map.clear();
					map.putAll((Map<Object, Object>) field.get(this));
				}
			} catch (final Exception e) {
				throw new IllegalStateException("The node " + this.getClass().getSimpleName() + " cannot be copied: its field " + field.getName() + " cannot be set (" + e + "), mark the field transient to leave it out of copy()", e);
			}
		}

		return copy;
	}

	private @NonNull Node instantiate() {
		final Map<Class<?>[], Object[]> constructors = new LinkedHashMap<>();
		if (this instanceof ScrollbarNode) {
			constructors.put(new Class<?>[] {double.class, double.class, double.class, double.class, BoundingBox.class}, new Object[] {this.x, this.y, this.width, this.height, ((ScrollbarNode) this).getScroll()});
		}
		constructors.put(new Class<?>[] {double.class, double.class, double.class, double.class}, new Object[] {this.x, this.y, this.width, this.height});
		constructors.put(new Class<?>[] {double.class, double.class}, new Object[] {this.x, this.y});
		constructors.put(new Class<?>[0], new Object[0]);

		Exception failure = null;
		for (final Entry<Class<?>[], Object[]> entry : constructors.entrySet()) {
			try {
				final Constructor<? extends Node> constructor = this.getClass().getDeclaredConstructor(entry.getKey());
				constructor.setAccessible(true);
				return constructor.newInstance(entry.getValue());
			} catch (final Exception e) {
				failure = e;
			}
		}

		for (final Constructor<?> constructor : this.getClass().getDeclaredConstructors()) {
			final Object[] arguments = Node.getDefaultArguments(constructor.getParameterTypes());
			if (arguments == null) {
				continue;
			}

			try {
				constructor.setAccessible(true);
				return (Node) constructor.newInstance(arguments);
			} catch (final Exception e) {
				failure = e;
			}
		}

		throw new IllegalStateException("The node " + this.getClass().getSimpleName() + " cannot be copied: no constructor with numbers, booleans or enums only can be called (" + failure + "), declare a constructor (x, y, width, height)", failure);
	}

	private @NonNull List<@NonNull Field> getFields(final @NonNull Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Collections.addAll(fields, c.getDeclaredFields());
		}
		return fields;
	}

	private static @NonNull String nameOf(final @NonNull Class<?> type) {
		return type.getSimpleName().isEmpty() ? type.getName() : type.getSimpleName();
	}

	private static Object[] getDefaultArguments(final @NonNull Class<?>[] types) {
		final Object[] arguments = new Object[types.length];
		for (int i = 0; i < types.length; i++) {
			if (types[i].isPrimitive()) {
				arguments[i] = Array.get(Array.newInstance(types[i], 1), 0);
			} else if (types[i].isEnum() && types[i].getEnumConstants().length > 0) {
				arguments[i] = types[i].getEnumConstants()[0];
			} else {
				return null;
			}
		}
		return arguments;
	}

	private static void checkApplier(final @NonNull Node node, final @NonNull String property, final @NonNull Object applier) {
		if (!JOIDEnv.isDevMode() || !Node.CHECKED_APPLIER_SET.add(applier.getClass())) {
			return;
		}

		for (final Field field : applier.getClass().getDeclaredFields()) {
			if (Node.class.isAssignableFrom(field.getType())) {
				JOIDLog.devWarnOnce("Node.applier." + applier.getClass().getName(), () -> "The " + property + " applier of " + Node.nameOf(node.getClass()) + " reads the node it is written in, so copy() and the ghost of a COPY drag change the original node: write into the node the applier receives, as in (node, value) -> node.field = value");
				return;
			}
		}
	}

	@Override
	public @NonNull String toString() {
		if (JOIDEnv.isDevMode()) {
			return Node.PRETTY_GSON.toJson(this.json());
		}

		return this.toJson();
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class NodeRequirement {

		private final String          advice;
		private final String          setting;
		private final Predicate<Node> present;

	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class NodeSource<V> {

		private final Supplier<V>         supplier;
		private final BiConsumer<Node, V> applier;

		private V value;

		private void pull(final Node node) {
			final V next = this.supplier.get();
			if (Objects.equals(this.value, next)) {
				return;
			}

			this.value = next;
			this.applier.accept(node, next);
		}

		private NodeSource<V> copy() {
			return new NodeSource<>(this.supplier, this.applier, this.value);
		}

	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class NodeLink<V> implements ISignalSubscriber<V> {

		private final Node                node;
		private final String              property;
		private final Signal<V>           signal;
		private final Predicate<Node>     condition;
		private final boolean             bound;
		private final BiConsumer<Node, V> applier;

		private V value;

		@Override
		public boolean update(final V value) {
			if (!this.node.subscribed) {
				return true;
			}

			if (!this.condition.test(this.node)) {
				this.node.linkList.remove(this);
				return false;
			}

			this.applier.accept(this.node, value);
			return true;
		}

		private NodeLink<V> copy(final Node node) {
			return new NodeLink<>(node, this.property, this.signal, this.condition, this.bound, this.applier);
		}

		private void keep() {
			this.value = this.signal.peek();
		}

		private void subscribe(final boolean loaded) {
			this.signal.subscribe(this);
			final V current = this.signal.peek();
			if ((loaded || this.bound) && !Objects.equals(this.value, current) && !this.update(current)) {
				this.signal.unsubscribe(this);
			}
		}

		private void unsubscribe() {
			this.keep();
			this.signal.unsubscribe(this);
		}

		private void cancel() {
			this.signal.unsubscribe(this);
			this.node.linkList.remove(this);
		}

	}

}