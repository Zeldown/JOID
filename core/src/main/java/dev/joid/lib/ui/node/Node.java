package dev.joid.lib.ui.node;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import dev.joid.internal.JOID;
import dev.joid.lib.animation.animator.TweenAnimator;
import dev.joid.lib.animation.tweenengine.TweenEquation;
import dev.joid.lib.animation.tweenengine.TweenEquations;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.shader.pipeline.ShaderPass;
import dev.joid.lib.shader.pipeline.ShaderPipeline;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.node.callback.NodeCallback;
import dev.joid.lib.ui.node.callback.NodeCallbackObject;
import dev.joid.lib.ui.node.callback.impl.animation.NodeAnimationCallback;
import dev.joid.lib.ui.node.callback.impl.draggable.NodeDragCallback;
import dev.joid.lib.ui.node.callback.impl.draggable.NodeSnapCallback;
import dev.joid.lib.ui.node.callback.impl.hover.NodeHoverCallback;
import dev.joid.lib.ui.node.callback.impl.hover.NodeHoverEndCallback;
import dev.joid.lib.ui.node.callback.impl.hover.NodeHoverStartCallback;
import dev.joid.lib.ui.node.callback.impl.key.NodeKeyPressedCallback;
import dev.joid.lib.ui.node.callback.impl.mouse.NodeMouseDraggedCallback;
import dev.joid.lib.ui.node.callback.impl.mouse.NodeMousePressedCallback;
import dev.joid.lib.ui.node.callback.impl.mouse.NodeMouseReleasedCallback;
import dev.joid.lib.ui.node.callback.impl.mouse.NodeMouseScrollCallback;
import dev.joid.lib.ui.node.callback.impl.scroll.NodeScrollEndCallback;
import dev.joid.lib.ui.node.callback.impl.scroll.NodeScrollEndingCallback;
import dev.joid.lib.ui.node.callback.impl.scroll.NodeScrollUpdateCallback;
import dev.joid.lib.ui.node.callback.impl.signal.NodeMountCallback;
import dev.joid.lib.ui.node.callback.impl.signal.NodeWatchCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeAppendCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeDetachCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeDrawCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeInitCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeReloadCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeRenderCallback;
import dev.joid.lib.ui.node.callback.impl.state.NodeUpdateCallback;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.effect.NodeEffect;
import dev.joid.lib.ui.node.effect.NodeEffect.NodeEffectScope;
import dev.joid.lib.ui.node.hover.HoverElement;
import dev.joid.lib.ui.node.hover.HoverSupplier;
import dev.joid.lib.ui.node.hover.impl.DefaultHoverElement;
import dev.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import dev.joid.lib.ui.node.layer.NodeLayer;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableAreaType;
import dev.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableType;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.ui.node.property.position.PositionProperty;
import dev.joid.lib.ui.node.property.watch.WatchProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.box.BoundingBox;
import dev.joid.lib.utils.click.ClickType;
import dev.joid.lib.utils.context.InternalContext;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.list.IndexedConcurrentList;
import dev.joid.lib.utils.list.IndexedLinkedList;
import dev.joid.lib.utils.signal.ISignal;
import dev.joid.lib.utils.signal.Signal;
import dev.joid.lib.utils.signal.SignalSubscriber;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@SuppressWarnings("unchecked")
public abstract class Node implements INode {

	private static final transient Gson GSON        = new GsonBuilder().create();
	private static final transient Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final int CALLBACK_CLICK          = NodeCallbackRegistry.next(NodeMousePressedCallback.class);

	private static final int CALLBACK_KEY_PRESSED    = NodeCallbackRegistry.next(NodeKeyPressedCallback.class);
	private static final int CALLBACK_MOUSE_SCROLL   = NodeCallbackRegistry.next(NodeMouseScrollCallback.class);
	private static final int CALLBACK_MOUSE_PRESSED  = NodeCallbackRegistry.next(NodeMousePressedCallback.class);
	private static final int CALLBACK_MOUSE_DRAGGED  = NodeCallbackRegistry.next(NodeMouseDraggedCallback.class);
	private static final int CALLBACK_MOUSE_RELEASED = NodeCallbackRegistry.next(NodeMouseReleasedCallback.class);

	private static final int CALLBACK_INIT           = NodeCallbackRegistry.next(NodeInitCallback.class);
	private static final int CALLBACK_DRAW           = NodeCallbackRegistry.next(NodeDrawCallback.class);
	private static final int CALLBACK_RENDER         = NodeCallbackRegistry.next(NodeRenderCallback.class);
	private static final int CALLBACK_UPDATE         = NodeCallbackRegistry.next(NodeUpdateCallback.class);
	private static final int CALLBACK_RELOAD         = NodeCallbackRegistry.next(NodeReloadCallback.class);
	private static final int CALLBACK_DETACH         = NodeCallbackRegistry.next(NodeDetachCallback.class);
	private static final int CALLBACK_APPEND         = NodeCallbackRegistry.next(NodeAppendCallback.class);

	private static final int CALLBACK_MOUNT          = NodeCallbackRegistry.next(NodeMountCallback.class);
	private static final int CALLBACK_WATCH          = NodeCallbackRegistry.next(NodeWatchCallback.class);
	private static final int CALLBACK_ANIMATION      = NodeCallbackRegistry.next(NodeAnimationCallback.class);

	private static final int CALLBACK_SCROLL_END     = NodeCallbackRegistry.next(NodeScrollEndCallback.class);
	private static final int CALLBACK_SCROLL_ENDING  = NodeCallbackRegistry.next(NodeScrollEndingCallback.class);
	private static final int CALLBACK_SCROLL_UPDATE  = NodeCallbackRegistry.next(NodeScrollUpdateCallback.class);

	private static final int CALLBACK_DRAG           = NodeCallbackRegistry.next(NodeDragCallback.class);
	private static final int CALLBACK_SNAP           = NodeCallbackRegistry.next(NodeSnapCallback.class);
	private static final int CALLBACK_DRAG_END       = NodeCallbackRegistry.next(NodeDragCallback.class);
	private static final int CALLBACK_DRAG_START     = NodeCallbackRegistry.next(NodeDragCallback.class);

	private static final int CALLBACK_HOVER          = NodeCallbackRegistry.next(NodeHoverCallback.class);
	private static final int CALLBACK_HOVER_END      = NodeCallbackRegistry.next(NodeHoverEndCallback.class);
	private static final int CALLBACK_HOVER_START    = NodeCallbackRegistry.next(NodeHoverStartCallback.class);

	private final transient List<Predicate<Node>>                     waitingList;
	private final transient List<SignalSubscriber<?>>                 subscriptionList;
	private final transient Map<Integer, List<NodeCallbackObject<?>>> callbackMap;

	private final transient TweenAnimator             hoverAnimator;
	private final transient Map<TweenAnimator, Float> animatorMap;

	private final LinkedList<NodeLayer>           layerList;
	private final IndexedConcurrentList<Node>     children;
	private final Map<Class<?>, NodeEffect<Node>> effectMap;

	private final List<HoverElement>           hoverElementList;
	private final List<Supplier<List<String>>> hoverSupplierList;

	private final double defaultX;
	private final double defaultY;
	private final double defaultWidth;
	private final double defaultHeight;

	private transient UI   ui;
	private transient Node parent;

	private transient Node           skeleton;
	private transient Node           overflowArea;
	private transient ScrollbarNode  scrollbar;
	private transient Consumer<Node> bodyConsumer;

	private double x;
	private double y;
	private double width;
	private double height;

	private Predicate<Node> visible;
	private Predicate<Node> enabled;

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
	private boolean subscribed;

	private boolean       hovered;
	private long          hoverDuration;
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

	private double lastWidth;
	private double lastHeight;

	private double  restX;
	private double  restY;
	private double  drawnX;
	private double  drawnY;
	private boolean moving;

	private long      lastClickTime;
	private ClickType lastClickType;

	private Key  lastKey;
	private long lastKeyTime;
	private char lastCharacter;

	private long lastUpdate;
	private long renderTime;
	private long updateCount;

	public Node(final double x, final double y) {
		this(x, y, 0, 0);
	}

	public Node(final double x, final double y, final double width, final double height) {
		this.waitingList = new ArrayList<>();
		this.subscriptionList = new ArrayList<>();
		this.callbackMap = new HashMap<>();

		this.animatorMap   = new HashMap<>();
		this.hoverAnimator = TweenAnimator.create();

		this.children  = new IndexedConcurrentList<>();
		this.layerList = new LinkedList<>();
		this.effectMap = new LinkedHashMap<>();

		this.hoverElementList  = new LinkedList<>();
		this.hoverSupplierList = new LinkedList<>();

		this.defaultX = this.x = this.drawnX = x;
		this.defaultY = this.y = this.drawnY = y;

		this.defaultWidth  = this.width  = this.lastWidth  = width;
		this.defaultHeight = this.height = this.lastHeight = height;

		this.visible = node -> true;
		this.enabled = node -> true;

		this.position = PositionProperty.RELATIVE;
		this.overflow = OverflowProperty.NONE;
		this.anchorX  = Align.START;
		this.anchorY  = Align.START;

		this.aspectRatio = -1D;
		this.hoverDuration = 200L;
		this.hoverEquation = TweenEquations.LINEAR;
		this.scrollSpeed = 1D;
		this.subscribed = true;
	}

	public final void load(final @NonNull UI ui) {
		this.load(ui, true);
	}

	private void load(final UI ui, final boolean children) {
		this.executeCallback(Node.CALLBACK_INIT, InternalContext.create(), () -> {
			this.ui = ui;

			if (children) {
				this.children.forEach(child -> child.load(this.ui));
			}

			if (this.scrollbar != null) {
				this.scrollbar.load(this.ui);
			}

			if (this.skeleton != null) {
				this.skeleton.load(this.ui);
			}

			this.getAppliedEffects().forEach(effect -> effect.init(this, this.ui));
			this.init(this.ui);
			this.subscribe();
		});

		this.updateCount++;
		this.lastUpdate = BridgeHandler.CLOCK.get().currentTimeMillis();
	}

	public final void render(final double mouseX, final double mouseY) {
		final long now = System.nanoTime();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		try {
			Color.reset();
			if (this.parent != null) {
				render.translate(this.parent.x, this.parent.y, 0D);
				if (this.position == PositionProperty.ABSOLUTE) {
					render.translate(-this.parent.getAbsoluteX(), -this.parent.getAbsoluteY(), 0D);
				}
			}

			render.translate(0D, 0D, this.zlevel);

			if (this.isVisible()) {
				if (this.aspectRatio > 0D) {
					if (this.width != 0) {
						this.height = this.width / this.aspectRatio;
					} else if (this.height != 0) {
						this.width = this.height * this.aspectRatio;
					}
				}

				if (this.width != this.lastWidth) {
					if (this.anchorX.isCenter()) {
						this.x += (this.lastWidth - this.width) / 2;
					} else if (this.anchorX.isEnd()) {
						this.x += this.lastWidth - this.width;
					}
					this.lastWidth = this.width;
				}

				if (this.height != this.lastHeight) {
					if (this.anchorY.isCenter()) {
						this.y += (this.lastHeight - this.height) / 2;
					} else if (this.anchorY.isEnd()) {
						this.y += this.lastHeight - this.height;
					}
					this.lastHeight = this.height;
				}

				if (!this.hovered && this.isHovered(mouseX, mouseY)) {
					this.hoverAnimator.sequence(this.hoverDuration, 1F, this.hoverEquation).start();
					this.executeCallback(Node.CALLBACK_HOVER_START, InternalContext.create(), mouseX, mouseY);
				}

				if (this.hovered && !this.isHovered(mouseX, mouseY)) {
					this.hoverAnimator.sequence(this.hoverDuration, 0F, this.hoverEquation).start();
					this.executeCallback(Node.CALLBACK_HOVER_END, InternalContext.create(), mouseX, mouseY);
				}

				this.hovered = this.isHovered(mouseX, mouseY);
				this.hoverAnimator.update();

				if (this.hovered) {
					this.executeCallback(Node.CALLBACK_HOVER, InternalContext.create(), mouseX, mouseY);
				}

				for (final Entry<TweenAnimator, Float> entry : this.animatorMap.entrySet()) {
					final TweenAnimator animator = entry.getKey().update();
					final float value = animator.getValue();
					if (value != entry.getValue()) {
						this.executeCallback(Node.CALLBACK_ANIMATION, InternalContext.create(), animator, value);
						entry.setValue(value);
					}
				}

				boolean scrollsX = false;
				boolean scrollsY = false;
				if (this.overflow == OverflowProperty.SCROLL) {
					this.maxScrollX = 0;
					double scrollOffsetX = Double.MIN_VALUE;
					for (final Node child : this.children) {
						this.maxScrollX = Math.max(this.maxScrollX, child.defaultX + child.width - this.width);
						if (scrollOffsetX == Double.MIN_VALUE) {
							scrollOffsetX = child.defaultX;
						} else {
							scrollOffsetX = Math.min(scrollOffsetX, child.defaultX);
						}
					}

					if (this.hasOverflowX()) {
						this.maxScrollX += scrollOffsetX;
						scrollsX = true;
					}

					this.maxScrollY = 0;
					double scrollOffsetY = Double.MIN_VALUE;
					for (final Node child : this.children) {
						this.maxScrollY = Math.max(this.maxScrollY, child.defaultY + child.height - this.height);
						if (scrollOffsetY == Double.MIN_VALUE) {
							scrollOffsetY = child.defaultY;
						} else {
							scrollOffsetY = Math.min(scrollOffsetY, child.defaultY);
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
					this.executeCallback(Node.CALLBACK_SCROLL_END, InternalContext.create(), this.scrollX, this.scrollY);
				}

				if (this.scrollEndY && this.scrollY == this.targetScrollY) {
					this.scrollEndY = false;
					this.executeCallback(Node.CALLBACK_SCROLL_END, InternalContext.create(), this.scrollX, this.scrollY);
				}

				if (scrollsX || scrollsY) {
					final PixelGrid grid = render.getPixelGrid();
					final double offsetX = grid.quantizeX(this.scrollX);
					final double offsetY = grid.quantizeY(this.scrollY);
					for (final Node child : this.children) {
						if (scrollsX) {
							child.x = child.defaultX + offsetX;
						}

						if (scrollsY) {
							child.y = child.defaultY + offsetY;
						}
					}
				}

				if (this.scrollbar != null) {
					if (this.scrollbar.isHorizontal() && this.hasOverflowX()) {
						final float percent = (float) Math.min(1, Math.max(0, Math.abs(this.scrollX / this.maxScrollX)));
						this.scrollbar.x(this.scrollbar.getDefaultX() + this.scrollbar.getScrollWidth() * percent);
					}

					if (!this.scrollbar.isHorizontal() && this.hasOverflowY()) {
						final float percent = (float) Math.min(1, Math.max(0, Math.abs(this.scrollY / this.maxScrollY)));
						this.scrollbar.y(this.scrollbar.getDefaultY() + this.scrollbar.getScrollHeight() * percent);
					}
				}

				if (this.dragging && BridgeHandler.WINDOW.get().isMouseGrabbed()) {
					this.stopDragging();
				}

				if (this.draggable != null && this.draggable.isEnabled(this)) {
					if (!this.dragging && this.draggable.getAreaType() != DraggableAreaType.FREE) {
						final double[] bounds = this.draggable.getBounds(this);
						final double boundX = bounds[0];
						final double boundY = bounds[1];
						final double boundWidth = bounds[2];
						final double boundHeight = bounds[3];

						final boolean inside = this.getAbsoluteX() >= boundX && this.getAbsoluteY() >= boundY && this.getAbsoluteX() + this.width <= boundX + boundWidth && this.getAbsoluteY() + this.height <= boundY + boundHeight;
						if (!inside) {
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

							this.dragged = true;
						}
					}

					if (this.dragged) {
						if (this.draggable.getType() == DraggableType.MOVE) {
							final double newAbsoluteX = this.draggable.lerp(this.getUi().getFrameTime(), this.getAbsoluteX(), this.targetDragX);
							final double newAbsoluteY = this.draggable.lerp(this.getUi().getFrameTime(), this.getAbsoluteY(), this.targetDragY);
							final double diffX = this.getAbsoluteX() - this.x;
							final double diffY = this.getAbsoluteY() - this.y;

							this.x = newAbsoluteX - diffX;
							this.y = newAbsoluteY - diffY;
						} else if (this.draggable.getType() == DraggableType.COPY && this.draggedNode != null) {
							final double newAbsoluteX = this.draggable.lerp(this.getUi().getFrameTime(), this.draggedNode.getAbsoluteX(), this.targetDragX);
							final double newAbsoluteY = this.draggable.lerp(this.getUi().getFrameTime(), this.draggedNode.getAbsoluteY(), this.targetDragY);
							this.draggedNode.x = newAbsoluteX;
							this.draggedNode.y = newAbsoluteY;
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
					this.executeCallback(Node.CALLBACK_MOUNT, InternalContext.create());
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
							this.children.forEach(child -> child.overflowArea = this);
						} else if (this.overflowArea != null) {
							this.children.forEach(child -> child.overflowArea = this.overflowArea);
						}

						if (this.skeleton != null && !this.mounted) {
							this.executeCallback(Node.CALLBACK_RENDER, InternalContext.create(), () -> {
								this.skeleton.render(mouseX, mouseY);
							}, mouseX, mouseY);
							return;
						}

						this.executeCallback(Node.CALLBACK_RENDER, InternalContext.create(), () -> {
							this.children.ordered().stream().filter(child -> child.zindex < 0).forEach(child -> child.render(mouseX, mouseY));
							this.executeCallback(Node.CALLBACK_DRAW, InternalContext.create(), () -> {
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
									final List<ShaderPass> selfPasses = selfShaderEffects.stream().flatMap(e -> e.toShaderPasses(this).stream()).collect(Collectors.toList());
									ShaderPipeline.render(this, selfPasses, selfDraw);
								}
							}, mouseX, mouseY);

							this.children.ordered().stream().filter(child -> child.zindex >= 0).forEach(child -> child.render(mouseX, mouseY));
							this.layerList.forEach(layer -> layer.draw(mouseX, mouseY));

							if (this.draggedNode != null) {
								render.pushState();
								render.pushMatrix();
								try {
									render.stencilTest(false);

									if (this.parent != null) {
										render.translate(-this.parent.x, -this.parent.y, 0D);
									}

									this.draggedNode.render(mouseX, mouseY);
								} finally {
									render.popMatrix();
									render.popState();
								}
							}
						}, mouseX, mouseY);
					}, this.overflow != OverflowProperty.NONE);
				};

				try {
					if (!subtreeShaderEffects.isEmpty()) {
						final List<ShaderPass> subtreePasses = subtreeShaderEffects.stream().flatMap(e -> e.toShaderPasses(this).stream()).collect(Collectors.toList());
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
			}
		} finally {
			Color.reset();
			render.popMatrix();
		}
		this.renderTime = System.nanoTime() - now;
	}

	public boolean renderHover(final double mouseX, final double mouseY) {
		return this.renderHover(mouseX, mouseY, new AtomicBoolean(false));
	}

	private boolean renderHover(final double mouseX, final double mouseY, final AtomicBoolean shown) {
		final AtomicBoolean cancelled = new AtomicBoolean(false);
		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> {
			if (child.renderHover(mouseX, mouseY, shown)) {
				cancelled.set(true);
			}
		});

		if (this.isHovered(mouseX, mouseY, false)) {
			if (shown.get()) {
				return true;
			}

			final List<HoverElement> hoverList = new LinkedList<>(this.hoverElementList);
			if (!this.hoverSupplierList.isEmpty()) {
				final List<String> lines = new LinkedList<>();
				for (final Supplier<List<String>> hoverSupplier : this.hoverSupplierList) {
					lines.addAll(hoverSupplier.get());
				}

				if (!lines.isEmpty()) {
					hoverList.add(new DefaultHoverElement(lines));
				}
			}

			if (!hoverList.isEmpty()) {
				hoverList.forEach(element -> element.render(this, mouseX, mouseY));
				shown.set(true);
			}

			return true;
		}

		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> {
			if (child.renderHover(mouseX, mouseY, shown)) {
				cancelled.set(true);
			}
		});

		return cancelled.get();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(this.x,this.y, this.width, this.height, Color.LOADING());
	}

	public final void onUpdate() {
		this.executeCallback(Node.CALLBACK_UPDATE, InternalContext.create(), () -> {
			this.children.ordered().stream().forEach(Node::onUpdate);
			this.update();
		});
	}

	public final <T extends Node> @NonNull T onUpdate(final @NonNull NodeUpdateCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_UPDATE, callback);
	}

	public final <T extends Node> @NonNull T onMouseScroll(final @NonNull NodeMouseScrollCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_SCROLL, callback);
	}

	public final void onMouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (!this.isVisible()) {
			return;
		}

		final boolean enabled = this.isEnabled();
		if (this.scrollbar != null) {
			this.scrollbar.onMouseScroll(mouseX, mouseY, value, context);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.onMouseScroll(mouseX, mouseY, value, context);
		}

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_SCROLL)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_SCROLL, context, mouseX, mouseY, value);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseScroll(mouseX, mouseY, value, context));
		if (!context.isCancelled() && this.isHovered(mouseX, mouseY) && value != 0) {
			final double mappedScrollSpeed = Key.LEFT_CONTROL.isDown() ? this.scrollSpeed * 2 : this.scrollSpeed;
			if (this.hasOverflowY()) {
				if (value > 0 ? this.targetScrollY < 0 : this.targetScrollY > -this.maxScrollY) {
					this.scrollY(value > 0 ? 30 : -30, mappedScrollSpeed);
					context.cancel();
				}
			} else if (this.hasOverflowX() && (value > 0 ? this.targetScrollX < 0 : this.targetScrollX > -this.maxScrollX)) {
				this.scrollX(value > 0 ? 30 : -30, mappedScrollSpeed);
				context.cancel();
			}
		}

		if (enabled) {
			this.mouseScroll(mouseX, mouseY, value, context);
		}
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseScroll(mouseX, mouseY, value, context));

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_SCROLL)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_SCROLL, context, mouseX, mouseY, value);
		}
	}

	public final <T extends Node> @NonNull T onMouseDragged(final @NonNull NodeMouseDraggedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_DRAGGED, callback);
	}

	public final void onMouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {
		final boolean visible = this.isVisible();
		final boolean enabled = visible && this.isEnabled();
		if (visible) {
			if (this.scrollbar != null) {
				this.scrollbar.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context);
			}

			if (this.skeleton != null && !this.mounted) {
				this.skeleton.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context);
			}

			if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_DRAGGED)) {
				this.executePreCallback(Node.CALLBACK_MOUSE_DRAGGED, context, mouseX, mouseY, clickType, deltaTime);
			}

			this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context));
			if (enabled) {
				this.mouseDragged(mouseX, mouseY, clickType, deltaTime, context);
			}
			this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context));
		}

		if (this.dragging) {
			this.fireDrag(() -> {
				this.targetDragX = mouseX - this.dragX;
				this.targetDragY = mouseY - this.dragY;
			});
		}

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_DRAGGED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_DRAGGED, context, mouseX, mouseY, clickType, deltaTime);
		}
	}

	public final <T extends Node> @NonNull T onMousePressed(final @NonNull NodeMousePressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_PRESSED, callback);
	}

	public final void onMousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (!this.isVisible()) {
			return;
		}

		final boolean enabled = this.isEnabled();
		this.lastClickType = clickType;
		this.lastClickTime = BridgeHandler.CLOCK.get().currentTimeMillis();

		if (this.scrollbar != null) {
			this.scrollbar.onMousePressed(mouseX, mouseY, clickType, context);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.onMousePressed(mouseX, mouseY, clickType, context);
		}

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_PRESSED)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_PRESSED, context, mouseX, mouseY, clickType);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMousePressed(mouseX, mouseY, clickType, context));
		final boolean pressed = context.isCancelled();
		if (!pressed && this.isHovered(mouseX, mouseY) && this.hasCallback(Node.CALLBACK_CLICK)) {
			this.executeCallback(Node.CALLBACK_CLICK, context, mouseX, mouseY, clickType);
			context.cancel();
		}
		final boolean clicked = !pressed && context.isCancelled();

		if (enabled) {
			this.mousePressed(mouseX, mouseY, clickType, context);
		}
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMousePressed(mouseX, mouseY, clickType, context));

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_PRESSED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_PRESSED, clicked ? InternalContext.create() : context, mouseX, mouseY, clickType);
		}

		if (!context.isCancelled() && this.draggable != null && this.draggable.isEnabled(this) && clickType.isLeft() && this.isHovered(mouseX, mouseY)) {
			this.startDragging(mouseX, mouseY);
		}
	}

	public final <T extends Node> @NonNull T onMouseReleased(final @NonNull NodeMouseReleasedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_RELEASED, callback);
	}

	public final void onMouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (this.scrollbar != null) {
			this.scrollbar.onMouseReleased(mouseX, mouseY, clickType, context);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.onMouseReleased(mouseX, mouseY, clickType, context);
		}

		if (this.dragging) {
			this.stopDragging();
		}

		if (!this.isVisible()) {
			return;
		}

		final boolean enabled = this.isEnabled();
		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_RELEASED)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_RELEASED, context, mouseX, mouseY, clickType);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseReleased(mouseX, mouseY, clickType, context));
		if (enabled) {
			this.mouseReleased(mouseX, mouseY, clickType, context);
		}
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseReleased(mouseX, mouseY, clickType, context));

		if (enabled && this.hasCallback(Node.CALLBACK_MOUSE_RELEASED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_RELEASED, context, mouseX, mouseY, clickType);
		}
	}

	public final <T extends Node> @NonNull T onKeyPressed(final @NonNull NodeKeyPressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_KEY_PRESSED, callback);
	}

	public final void onKeyPressed(final char c, final @NonNull Key key, final @NonNull InternalContext context) {
		if (!this.isVisible()) {
			return;
		}

		final boolean enabled = this.isEnabled();
		this.lastCharacter = c;
		this.lastKey = key;
		this.lastKeyTime = BridgeHandler.CLOCK.get().currentTimeMillis();

		if (this.scrollbar != null) {
			this.scrollbar.onKeyPressed(c, key, context);
		}

		if (this.skeleton != null && !this.mounted) {
			this.skeleton.onKeyPressed(c, key, context);
		}

		if (enabled && this.hasCallback(Node.CALLBACK_KEY_PRESSED)) {
			this.executePreCallback(Node.CALLBACK_KEY_PRESSED, context, c, key);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onKeyPressed(c, key, context));
		if (enabled) {
			this.keyPressed(c, key, context);
		}
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onKeyPressed(c, key, context));

		if (enabled && this.hasCallback(Node.CALLBACK_KEY_PRESSED)) {
			this.executePostCallback(Node.CALLBACK_KEY_PRESSED, context, c, key);
		}
	}

	public final void reload() {
		this.executeCallback(Node.CALLBACK_RELOAD, InternalContext.create(), () -> {
			this.children.forEach(Node::reload);
			this.load(this.ui, false);
		});
	}

	public final void onDetach() {
		this.executeCallback(Node.CALLBACK_DETACH, InternalContext.create(), () -> {
			this.children.forEach(Node::onDetach);
			this.unsubscribe();
			this.detach();
		});
	}

	public final <T extends Node> @NonNull T onDetach(final @NonNull NodeDetachCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DETACH, callback);
	}

	public final <T extends Node> @NonNull T append(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			this.executeCallback(Node.CALLBACK_APPEND, InternalContext.create(), () -> {
				if (node.parent != null && node.parent != this) {
					node.parent.remove(node);
				} else if (node.parent == null && node.ui != null && node.ui.getNodeList().contains(node)) {
					node.onDetach();
					node.ui.getNodeList().remove(node);
					node.clearOverflowArea(node.overflowArea);
				}

				node.parent(this);
				if (this.ui != null) {
					node.load(this.ui);
				}
				this.children.add(node);
			}, node);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T remove(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			if (this.children.contains(node)) {
				node.onDetach();
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
		this.bodyConsumer.accept(this);
		return (T) this;
	}

	public final <T extends Node> @NonNull T self(final @NonNull Consumer<@NonNull T> consumer) {
		consumer.accept((T) this);
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
		this.executeCallback(Node.CALLBACK_SCROLL_UPDATE, InternalContext.create(), () -> {
			final double oldValue = this.targetScrollX;
			this.targetScrollX = Math.min(value, 0);
			if (this.targetScrollX <= -this.maxScrollX) {
				this.targetScrollX = -this.maxScrollX;
				if (oldValue != this.targetScrollX) {
					this.scrollEndX = true;
					this.executeCallback(Node.CALLBACK_SCROLL_ENDING, InternalContext.create(), this.targetScrollX, this.targetScrollY);
				}
			} else {
				this.scrollEndX = false;
			}
		}, value);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollRatioY(final float ratio) {
		this.scrollOffsetY(-this.maxScrollY * ratio);
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollOffsetY(final double value) {
		this.executeCallback(Node.CALLBACK_SCROLL_UPDATE, InternalContext.create(), () -> {
			final double oldValue = this.targetScrollY;
			this.targetScrollY = Math.min(value, 0);
			if (this.targetScrollY <= -this.maxScrollY) {
				this.targetScrollY = -this.maxScrollY;
				if (oldValue != this.targetScrollY) {
					this.scrollEndY = true;
					this.executeCallback(Node.CALLBACK_SCROLL_ENDING, InternalContext.create(), this.targetScrollX, this.targetScrollY);
				}
			} else {
				this.scrollEndY = false;
			}
		}, value);
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
		this.fireDragEnd(() -> {
			if (this.draggable != null && this.draggable.isEnabled(this)) {
				if (this.draggable.hasSnapping()) {
					final Node snapNode = this.draggable.getSnapping(this.draggable.getType() == DraggableType.COPY && this.draggedNode != null ? this.draggedNode : this);
					if (snapNode != null) {
						this.executeCallback(Node.CALLBACK_SNAP, InternalContext.create(), () -> {
							this.targetDragX = snapNode.getAbsoluteX();
							this.targetDragY = snapNode.getAbsoluteY();
						}, snapNode);
					} else {
						this.targetDragX = this.startDragX;
						this.targetDragY = this.startDragY;
					}
				}
			}
			this.dragging = false;
			this.draggedNode = null;
		});
		return (T) this;
	}

	public final <T extends Node> @NonNull T startDragging(final double mouseX, final double mouseY) {
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
				this.draggedNode = this.copy();
				this.draggedNode.x = this.targetDragX;
				this.draggedNode.y = this.targetDragY;
				this.draggedNode.position(PositionProperty.ABSOLUTE);
			}
		});
		return (T) this;
	}

	public final void fireDrag(final Runnable runnable) {
		this.executeCallback(Node.CALLBACK_DRAG, InternalContext.create(), runnable);
	}

	public final void fireDragEnd(final Runnable runnable) {
		this.executeCallback(Node.CALLBACK_DRAG_END, InternalContext.create(), runnable);
	}

	public final void fireDragStart(final Runnable runnable) {
		this.executeCallback(Node.CALLBACK_DRAG_START, InternalContext.create(), runnable);
	}

	public final <T extends NodeCallback> void executeCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		this.executeCallback(type, context, null, args);
	}

	public final <T extends NodeCallback> void executeCallback(final int type, final @NonNull InternalContext context, final Runnable runnable, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList.isEmpty()) {
			if (runnable != null) {
				runnable.run();
			}
			return;
		}

		for (final NodeCallbackObject<T> callback : callbackList) {
			callback.pre(this, context, args);
		}

		if (context.isCancelled()) {
			return;
		}

		if (runnable != null) {
			runnable.run();
		}

		this.post(callbackList, context, args);
	}

	public final <T extends NodeCallback> void executePreCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList.isEmpty()) {
			return;
		}

		for (final NodeCallbackObject<T> callback : callbackList) {
			callback.pre(this, context, args);
		}
	}

	public final <T extends NodeCallback> void executePostCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList.isEmpty()) {
			return;
		}

		this.post(callbackList, context, args);
	}

	private <T extends NodeCallback> void post(final List<NodeCallbackObject<T>> callbackList, final InternalContext context, final Object... args) {
		final boolean cancelled = context.isCancelled();
		boolean handled = cancelled;
		for (final NodeCallbackObject<T> callback : callbackList) {
			if (!cancelled) {
				context.reset();
			}

			callback.post(this, context, args);
			handled |= context.isCancelled();
		}

		if (handled) {
			context.cancel();
		}
	}

	protected final <T extends Node> @NonNull T registerCallback(final int type, final @NonNull NodeCallback callback) {
		final List<NodeCallbackObject<?>> callbackList = this.callbackMap.getOrDefault(type, new ArrayList<>());
		callbackList.add(new NodeCallbackObject<>(callback));
		this.callbackMap.put(type, callbackList);
		return (T) this;
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

	public final boolean hasCallback(final int type) {
		return this.callbackMap.containsKey(type);
	}

	public final boolean hasEffect(final @NonNull Class<?> clazz) {
		return this.effectMap.containsKey(clazz);
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
			return this.parent.getMappedIndex() + "." + this.parent.children.ordered().indexOf(this);
		}

		return String.valueOf(this.ui.getNodeList().ordered().indexOf(this));
	}

	public final <T extends NodeEffect<?>> T getEffect(final @NonNull Class<T> clazz) {
		return clazz.cast(this.effectMap.get(clazz));
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

	public final <T extends Node> IndexedLinkedList<T> getChildren(final @NonNull Class<T> clazz) {
		return new IndexedLinkedList<>(this.children.ordered().stream().filter(child -> clazz.isAssignableFrom(child.getClass())).map(child -> (T) child).collect(Collectors.toList()));
	}

	public final <T extends NodeCallback> @NonNull List<@NonNull NodeCallbackObject<T>> getCallbackList(final int type) {
		if (this.callbackMap.isEmpty() || !this.callbackMap.containsKey(type)) {
			return Collections.emptyList();
		}

		final List<NodeCallbackObject<?>> callbackList = this.callbackMap.get(type);
		if (callbackList.isEmpty()) {
			return Collections.emptyList();
		}

		final List<NodeCallbackObject<T>> mappedCallbackList = new ArrayList<>();
		for (final NodeCallbackObject<?> callback : callbackList) {
			if (callback == null) {
				continue;
			}
			mappedCallbackList.add((NodeCallbackObject<T>) callback);
		}

		return mappedCallbackList;
	}

	public final <T extends Node> @NonNull T clearChildren() {
		this.children.forEach(child -> {
			child.onDetach();
			child.parent(null);
			child.clearOverflowArea(child.overflowArea);
		});
		this.children.clear();
		return (T) this;
	}

	public boolean isEnabled() {
		if (this.parent != null && !this.parent.isEnabled()) {
			return false;
		}

		return this.enabled.test(this);
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

	public boolean isHovered(final double mouseX, final double mouseY) {
		return this.isHovered(mouseX, mouseY, true);
	}

	public boolean isHovered(final double mouseX, final double mouseY, final boolean checkEnabled) {
		if (this.ui == null) {
			return false;
		}

		return this.isVisible() && (!checkEnabled || this.isEnabled()) && this.ui.isOnTop() && (this.overflowArea != null ? this.overflowArea.isHovered(mouseX, mouseY) : true) && mouseX > this.getAbsoluteX() && mouseX <= this.getAbsoluteX() + this.width && mouseY > this.getAbsoluteY() && mouseY <= this.getAbsoluteY() + this.height;
	}

	public final float hoverValue(final float value) {
		return value * this.hoverAnimator.getValue();
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

	public boolean shouldApplyEffect(final @NonNull NodeEffect<Node> effect) {
		return effect.shouldApply(this);
	}

	private @NonNull List<NodeEffect<Node>> getAppliedEffects() {
		return this.effectMap.values().stream().filter(this::shouldApplyEffect).sorted(Comparator.comparingInt(NodeEffect::getPriority)).collect(Collectors.toList());
	}

	public final <T extends Node> @NonNull T copy() {
		final Node copy = this.instantiate();
		copy.ui = this.ui;
		copy.parent = this.parent;
		copy.skeleton = this.skeleton;

		copy.getCallbackMap().clear();
		for (final Entry<Integer, List<NodeCallbackObject<?>>> entry : this.callbackMap.entrySet()) {
			copy.getCallbackMap().put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}

		copy.effectMap.putAll(this.effectMap);
		copy.layerList.addAll(this.layerList);
		copy.hoverElementList.addAll(this.hoverElementList);
		copy.hoverSupplierList.addAll(this.hoverSupplierList);

		copy.x = this.x;
		copy.y = this.y;
		copy.width = this.width;
		copy.height = this.height;

		copy.visible = this.visible;
		copy.enabled = this.enabled;
		copy.position = this.position;
		copy.overflow = this.overflow;
		copy.anchorX = this.anchorX;
		copy.anchorY = this.anchorY;

		copy.draggable = this.draggable;
		copy.zindex = this.zindex;
		copy.zlevel = this.zlevel;

		copy.aspectRatio = this.aspectRatio;

		copy.hoverDuration = this.hoverDuration;
		copy.hoverEquation = this.hoverEquation;
		copy.scrollSpeed = this.scrollSpeed;

		copy.waitingList.addAll(this.waitingList);
		copy.animatorMap.putAll(this.animatorMap);

		copy.mounted = this.mounted;

		copy.lastWidth = this.lastWidth;
		copy.lastHeight = this.lastHeight;

		for (final Node child : this.children) {
			final Node childCopy = child.copy();
			childCopy.parent(copy);
			copy.children.add(childCopy);
		}

		if (this.scrollbar != null) {
			copy.scrollbar(this.scrollbar.copy());
		}

		for (final Field field : this.getFields(this.getClass())) {
			if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.getDeclaringClass() == Node.class) {
				continue;
			}

			try {
				field.setAccessible(true);
				field.set(copy, field.get(this));
			} catch (final Exception e) {
				throw new RuntimeException("Failed to copy node: " + this.getClass().getSimpleName(), e);
			}
		}

		return (T) copy;
	}

	public final <T extends UIStore> T useStore(final @NonNull Class<T> clazz) {
		return this.getUi().useStore(clazz);
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
		this.effectMap.put(effect.getClass(), (NodeEffect<Node>) effect);
		return (T) this;
	}

	public final <T extends Node> @NonNull T removeEffect(final @NonNull Class<?> effect) {
		this.effectMap.remove(effect);
		return (T) this;
	}

	public final <T extends Node> @NonNull T clearEffects() {
		this.effectMap.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T animate(final @NonNull TweenAnimator animator) {
		this.animatorMap.put(animator, animator.getValue());
		return (T) this;
	}

	public final <T extends Node> @NonNull T x(final double x) {
		this.x = x;
		return (T) this;
	}

	public final <T extends Node> @NonNull T y(final double y) {
		this.y = y;
		return (T) this;
	}

	public final <T extends Node> @NonNull T width(final double width) {
		this.width = width;
		return (T) this;
	}

	public final <T extends Node> @NonNull T height(final double height) {
		this.height = height;
		return (T) this;
	}

	public final <T extends Node> @NonNull T position(final double x, final double y) {
		this.x = x;
		this.y = y;
		return (T) this;
	}

	public final <T extends Node> @NonNull T position(final @NonNull PositionProperty position) {
		this.position = position;
		return (T) this;
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull OverflowProperty overflow) {
		if (this.overflow == OverflowProperty.SCROLL && overflow != OverflowProperty.SCROLL) {
			for (final Node child : this.children) {
				if (this.hasOverflowX()) {
					child.x = child.defaultX;
				}

				if (this.hasOverflowY()) {
					child.y = child.defaultY;
				}
			}

			this.maxScrollX = this.maxScrollY = 0D;
			this.scrollX = this.targetScrollX = 0D;
			this.scrollY = this.targetScrollY = 0D;
			this.scrollEndX = this.scrollEndY = false;
		}

		this.overflow = overflow;
		return (T) this;
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Align anchor) {
		this.anchorX = anchor;
		this.anchorY = anchor;
		return (T) this;
	}

	public final <T extends Node> @NonNull T anchor(final @NonNull Align anchorX, final @NonNull Align anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		return (T) this;
	}

	public final <T extends Node> @NonNull T anchorX(final @NonNull Align anchorX) {
		this.anchorX = anchorX;
		return (T) this;
	}

	public final <T extends Node> @NonNull T anchorY(final @NonNull Align anchorY) {
		this.anchorY = anchorY;
		return (T) this;
	}

	public final <T extends Node> @NonNull T draggable(final @NonNull DraggableProperty draggable) {
		this.draggable   = draggable;
		this.dragging    = false;
		this.startDragX  = this.getAbsoluteX();
		this.startDragY  = this.getAbsoluteY();
		this.targetDragX = this.getAbsoluteX();
		this.targetDragY = this.getAbsoluteY();
		return (T) this;
	}

	public final <T extends Node> @NonNull T dragging(final boolean dragging, final double mouseX, final double mouseY) {
		if (!dragging) {
			this.dragging = false;
			this.draggedNode = null;
			return (T) this;
		}

		this.dragging = dragging;
		this.dragX = mouseX - this.getAbsoluteX();
		this.dragY = mouseY - this.getAbsoluteY();
		this.startDragX = this.getAbsoluteX();
		this.startDragY = this.getAbsoluteY();
		this.targetDragX = this.getAbsoluteX();
		this.targetDragY = this.getAbsoluteY();
		this.dragged = true;
		return (T) this;
	}

	public final <T extends Node> @NonNull T aspectRatio(final double aspectRatio) {
		this.aspectRatio = aspectRatio;
		return (T) this;
	}

	public final <T extends Node> @NonNull T size(final double width, final double height) {
		this.width = width;
		this.height = height;
		return (T) this;
	}

	public final <T extends Node> @NonNull T bounds(final double x, final double y, final double width, final double height) {
		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;
		return (T) this;
	}

	public final <T extends Node> @NonNull T ui(final @NonNull UI ui) {
		this.ui = ui;
		return (T) this;
	}

	public final <T extends Node> @NonNull T parent(final Node parent) {
		this.parent = parent;
		return (T) this;
	}

	public final <T extends Node> @NonNull T overflowArea(final Node overflowArea) {
		this.overflowArea = overflowArea;
		return (T) this;
	}

	private void clearOverflowArea(final Node area) {
		if (area == null || this.overflowArea != area) {
			return;
		}

		this.overflowArea = null;
		this.children.forEach(child -> child.clearOverflowArea(area));
	}

	public final <T extends Node> @NonNull T wait(final @NonNull ISignal<?> watchable) {
		this.waitingList.add(node -> watchable.isPresent());
		return (T) this;
	}

	public final <T extends Node> @NonNull T wait(final long time, final @NonNull TimeUnit unit) {
		final long endTime = BridgeHandler.CLOCK.get().currentTimeMillis() + unit.toMillis(time);
		this.waitingList.add(node -> BridgeHandler.CLOCK.get().currentTimeMillis() >= endTime);
		return (T) this;
	}

	public final <T extends Node> @NonNull T wait(final @NonNull Predicate<@NonNull T> predicate) {
		this.waitingList.add((Predicate<Node>) predicate);
		return (T) this;
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal) {
		return this.watch(signal, WatchProperty.RELOAD);
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		return this.watch(signal, () -> JOID.isOpen(this.ui), properties);
	}

	public final <T extends Node> @NonNull T watch(final @NonNull Signal<?> signal, final @NonNull Supplier<Boolean> condition, final @NonNull WatchProperty @NonNull... properties) {
		this.listen(signal, condition, false, value -> {
			if (this.ui == null) {
				return;
			}

			this.executeCallback(Node.CALLBACK_WATCH, InternalContext.create(), () -> {
				for (final WatchProperty property : properties) {
					try {
						property.apply(this);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}, signal, properties);
		});
		return (T) this;
	}

	protected final <V> void bind(final @NonNull Signal<V> signal, final @NonNull Consumer<@NonNull V> consumer) {
		for (final SignalSubscriber<?> subscriber : new ArrayList<>(this.subscriptionList)) {
			final NodeSubscription<?> subscription = (NodeSubscription<?>) subscriber;
			if (subscription.bound) {
				subscription.cancel();
			}
		}

		final V current = signal.getOrDefault();
		if (current != null) {
			consumer.accept(current);
		}

		this.listen(signal, () -> JOID.isOpen(this.ui), true, value -> {
			if (value != null) {
				consumer.accept(value);
			}
		});
	}

	protected final <V> void sync(final Signal<V> signal, final @NonNull V value) {
		if (signal != null && !value.equals(signal.getOrDefault())) {
			signal.set(value);
		}
	}

	private <V> void listen(final Signal<V> signal, final Supplier<Boolean> condition, final boolean bound, final Consumer<V> consumer) {
		final NodeSubscription<V> subscription = new NodeSubscription<>(signal, condition, bound, consumer);
		this.subscriptionList.add(subscription);
		signal.subscribe(subscription);
	}

	private void subscribe() {
		if (this.subscribed) {
			return;
		}

		this.subscribed = true;
		for (final SignalSubscriber<?> subscriber : new ArrayList<>(this.subscriptionList)) {
			((NodeSubscription<?>) subscriber).subscribe();
		}
	}

	private void unsubscribe() {
		if (!this.subscribed) {
			return;
		}

		this.subscribed = false;
		for (final SignalSubscriber<?> subscriber : this.subscriptionList) {
			((NodeSubscription<?>) subscriber).unsubscribe();
		}
	}

	public final <T extends Node> @NonNull T hovered(final boolean hovered) {
		this.hovered = hovered;
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverDuration(final long hoverDuration) {
		this.hoverDuration = hoverDuration;
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverEquation(final @NonNull TweenEquation equation) {
		this.hoverEquation = equation;
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollSpeed(final double scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
		return (T) this;
	}

	public final <T extends Node> @NonNull T scrollbar(final @NonNull ScrollbarNode scrollbar) {
		this.scrollbar = scrollbar;
		this.scrollbar.scrollNode(this);
		this.scrollbar.parent(this);
		if (this.ui != null) {
			this.scrollbar.load(this.ui);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T skeleton(final @NonNull Function<@NonNull T, Node> skeleton) {
		this.skeleton = skeleton.apply((T) this);
		if (this.skeleton != null) {
			this.skeleton.parent(this);
			if (this.ui != null) {
				this.skeleton.load(this.ui);
			}
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T zindex(final int zindex) {
		this.zindex = zindex;
		if (this.parent != null && this.parent.children.contains(this)) {
			this.parent.children.add(this);
		} else if (this.parent == null && this.ui != null && this.ui.getNodeList().contains(this)) {
			this.ui.getNodeList().add(this);
		}
		return (T) this;
	}

	public final <T extends Node> @NonNull T zlevel(final double zlevel) {
		this.zlevel = zlevel;
		return (T) this;
	}

	public final <T extends Node> @NonNull T visible(final @NonNull Signal<?>... signals) {
		this.visible = node -> {
			for (final Signal<?> signal : signals) {
				final Object value = signal.getOrDefault();
				if (value == null || Boolean.FALSE.equals(value)) {
					return false;
				}
			}
			return true;
		};
		return (T) this;
	}

	public final <T extends Node> @NonNull T visible(final @NonNull Predicate<@NonNull T> visibility) {
		this.visible = (Predicate<Node>) visibility;
		return (T) this;
	}

	public final <T extends Node> @NonNull T enabled(final @NonNull Predicate<@NonNull T> enabled) {
		this.enabled = (Predicate<Node>) enabled;
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

	public final <T extends Node> @NonNull T hover(final @NonNull HoverElement element) {
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

	public final <T extends Node> @NonNull T hoverElements(final @NonNull HoverElement element) {
		this.hoverElementList.clear();
		this.hover(element);
		return (T) this;
	}

	public final <T extends Node> @NonNull T onClick(final @NonNull NodeMousePressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_CLICK, callback);
	}

	public final <T extends Node> @NonNull T onInit(final @NonNull NodeInitCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_INIT, callback);
	}

	public final <T extends Node> @NonNull T onRender(final @NonNull NodeRenderCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_RENDER, callback);
	}

	public final <T extends Node> @NonNull T onDraw(final @NonNull NodeDrawCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAW, callback);
	}

	public final <T extends Node> @NonNull T onReload(final @NonNull NodeReloadCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_RELOAD, callback);
	}

	public final <T extends Node> @NonNull T onAppend(final @NonNull NodeAppendCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_APPEND, callback);
	}

	public final <T extends Node> @NonNull T onMount(final @NonNull NodeMountCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUNT, callback);
	}

	public final <T extends Node> @NonNull T onWatch(final @NonNull NodeWatchCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_WATCH, callback);
	}

	public final <T extends Node> @NonNull T onAnimate(final @NonNull NodeAnimationCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_ANIMATION, callback);
	}

	public final <T extends Node> @NonNull T onScrollEnd(final @NonNull NodeScrollEndCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SCROLL_END, callback);
	}

	public final <T extends Node> @NonNull T onScrollEnding(final @NonNull NodeScrollEndingCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SCROLL_ENDING, callback);
	}

	public final <T extends Node> @NonNull T onScrollUpdate(final @NonNull NodeScrollUpdateCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SCROLL_UPDATE, callback);
	}

	public final <T extends Node> @NonNull T onDrag(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG, callback);
	}

	public final <T extends Node> @NonNull T onDragEnd(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG_END, callback);
	}

	public final <T extends Node> @NonNull T onDragStart(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG_START, callback);
	}

	public final <T extends Node> @NonNull T onSnap(final @NonNull NodeSnapCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SNAP, callback);
	}

	public final <T extends Node> @NonNull T onHover(final @NonNull NodeHoverCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_HOVER, callback);
	}

	public final <T extends Node> @NonNull T onHoverEnd(final @NonNull NodeHoverEndCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_HOVER_END, callback);
	}

	public final <T extends Node> @NonNull T onHoverStart(final @NonNull NodeHoverStartCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_HOVER_START, callback);
	}

	public @NonNull JsonObject toJson() {
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

		if (JOID.inst().isDevMode()) {
			json.addProperty("scrollX", this.targetScrollX + " / " + this.maxScrollX + " [" + this.overflow.toString() + "]");
			json.addProperty("scrollY", this.targetScrollY + " / " + this.maxScrollY + " [" + this.overflow.toString() + "]");

			json.addProperty("visible", this.visible.test(this));
			json.addProperty("enabled", this.enabled.test(this));
			json.addProperty("hovered", this.hovered);

			json.addProperty("isChild", this.parent != null);
			json.addProperty("children", this.children.size());
			json.addProperty("hierarchy", this.getHierarchy() + " (this)");
		}

		return json;
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

		throw new RuntimeException("Failed to copy node: " + this.getClass().getSimpleName(), failure);
	}

	private @NonNull List<@NonNull Field> getFields(final @NonNull Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			Collections.addAll(fields, c.getDeclaredFields());
		}
		return fields;
	}

	@Override
	public @NonNull String toString() {
		final JsonObject json = this.toJson();
		if (JOID.inst().isDevMode()) {
			return Node.PRETTY_GSON.toJson(json);
		}

		return Node.GSON.toJson(json);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private final class NodeSubscription<V> implements SignalSubscriber<V> {

		private final Signal<V>         signal;
		private final Supplier<Boolean> condition;
		private final boolean           bound;
		private final Consumer<V>       consumer;

		private V value;

		@Override
		public boolean update(final V value) {
			if (!Node.this.subscribed) {
				return true;
			}

			this.consumer.accept(value);
			if (Node.this.ui == null ? UI.getCurrent() != null : this.condition.get()) {
				return true;
			}

			Node.this.subscriptionList.remove(this);
			return false;
		}

		private void subscribe() {
			this.signal.subscribe(this);
			final V current = this.signal.getOrDefault();
			if (!Objects.equals(this.value, current) && !this.update(current)) {
				this.signal.unsubscribe(this);
			}
		}

		private void unsubscribe() {
			this.value = this.signal.getOrDefault();
			this.signal.unsubscribe(this);
		}

		private void cancel() {
			this.signal.unsubscribe(this);
			Node.this.subscriptionList.remove(this);
		}

	}

}