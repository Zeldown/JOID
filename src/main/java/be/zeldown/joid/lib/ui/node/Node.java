package be.zeldown.joid.lib.ui.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.animation.animator.TweenAnimator;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.opengl.context.GLContext;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.core.hook.store.UIStore;
import be.zeldown.joid.lib.ui.node.callback.NodeCallback;
import be.zeldown.joid.lib.ui.node.callback.NodeCallbackObject;
import be.zeldown.joid.lib.ui.node.callback.impl.draggable.NodeDragCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.draggable.NodeSnapCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.key.NodeKeyPressedCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.mouse.NodeMouseDraggedCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.mouse.NodeMousePressedCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.mouse.NodeMouseReleasedCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.mouse.NodeMouseScrollCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.scroll.NodeScrollEndCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.scroll.NodeScrollUpdateCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.signal.NodeMountCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.signal.NodeWatchCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.state.NodeAppendCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.state.NodeDrawCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.state.NodeInitCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.state.NodeReloadCallback;
import be.zeldown.joid.lib.ui.node.callback.impl.state.NodeUpdateCallback;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.hover.HoverSupplier;
import be.zeldown.joid.lib.ui.node.impl.structure.scrollbar.ScrollbarNode;
import be.zeldown.joid.lib.ui.node.layer.NodeLayer;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty;
import be.zeldown.joid.lib.ui.node.property.draggable.DraggableProperty.DraggableAreaType;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.ui.node.property.position.PositionProperty;
import be.zeldown.joid.lib.ui.node.property.watch.WatchProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.list.IndexedConcurrentList;
import be.zeldown.joid.lib.utils.list.IndexedLinkedList;
import be.zeldown.joid.lib.utils.signal.ISignal;
import be.zeldown.joid.lib.utils.signal.Signal;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class Node implements INode {

	private static final transient Gson GSON        = new GsonBuilder().create();
	private static final transient Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final int CALLBACK_CLICK          = NodeCallbackRegistry.next(NodeMousePressedCallback.class);

	private static final int CALLBACK_MOUSE_PRESSED  = NodeCallbackRegistry.next(NodeMousePressedCallback.class);
	private static final int CALLBACK_MOUSE_DRAGGED  = NodeCallbackRegistry.next(NodeMouseDraggedCallback.class);
	private static final int CALLBACK_MOUSE_RELEASED = NodeCallbackRegistry.next(NodeMouseReleasedCallback.class);
	private static final int CALLBACK_MOUSE_SCROLL   = NodeCallbackRegistry.next(NodeMouseScrollCallback.class);
	private static final int CALLBACK_KEY_PRESSED    = NodeCallbackRegistry.next(NodeKeyPressedCallback.class);

	private static final int CALLBACK_INIT           = NodeCallbackRegistry.next(NodeInitCallback.class);
	private static final int CALLBACK_DRAW           = NodeCallbackRegistry.next(NodeDrawCallback.class);
	private static final int CALLBACK_UPDATE         = NodeCallbackRegistry.next(NodeUpdateCallback.class);
	private static final int CALLBACK_RELOAD         = NodeCallbackRegistry.next(NodeReloadCallback.class);
	private static final int CALLBACK_APPEND         = NodeCallbackRegistry.next(NodeAppendCallback.class);

	private static final int CALLBACK_MOUNT          = NodeCallbackRegistry.next(NodeMountCallback.class);
	private static final int CALLBACK_WATCH          = NodeCallbackRegistry.next(NodeWatchCallback.class);

	private static final int CALLBACK_SCROLL_UPDATE  = NodeCallbackRegistry.next(NodeScrollUpdateCallback.class);
	private static final int CALLBACK_SCROLL_END     = NodeCallbackRegistry.next(NodeScrollEndCallback.class);

	private static final int CALLBACK_DRAG           = NodeCallbackRegistry.next(NodeDragCallback.class);
	private static final int CALLBACK_DRAG_START     = NodeCallbackRegistry.next(NodeDragCallback.class);
	private static final int CALLBACK_DRAG_END       = NodeCallbackRegistry.next(NodeDragCallback.class);
	private static final int CALLBACK_SNAP           = NodeCallbackRegistry.next(NodeSnapCallback.class);

	private final List<TweenAnimator> animators;
	private final TweenAnimator hoverAnimator;

	private final IndexedConcurrentList<Node>  children;
	private final List<NodeLayer>              layers;
	private final List<Supplier<List<String>>> hoverSupplierList;

	private final double defaultX;
	private final double defaultY;
	private final double defaultWidth;
	private final double defaultHeight;

	private transient UI   ui;
	private transient Node parent;

	private transient Node          overflowArea;
	private transient ScrollbarNode scrollbar;
	private transient Node          skeleton;

	private transient Consumer<Node>                            bodyConsumer;
	private transient List<Predicate<Node>>                     waitingList;
	private transient Map<Integer, List<NodeCallbackObject<?>>> callbackMap;

	private double x;
	private double y;
	private double width;
	private double height;

	private Predicate<Node> visible;
	private Predicate<Node> enabled;

	private PositionProperty position;
	private OverflowProperty overflow;
	private Align            anchorX;
	private Align            anchorY;

	private DraggableProperty draggable;
	private boolean           dragging;
	private boolean           dragged;
	private double            dragX;
	private double            dragY;
	private double            startDragX;
	private double            startDragY;
	private double            targetDragX;
	private double            targetDragY;

	private int    zindex;
	private double zlevel;

	private double[] stencilBox;
	private double   aspectRatio;

	private boolean mounted;

	private boolean hovered;
	private long    hoverDuration;

	private double scrollX;
	private double scrollY;
	private double targetScrollX;
	private double targetScrollY;
	private double maxScrollX;
	private double maxScrollY;
	private double scrollSpeed;

	private double lastWidth;
	private double lastHeight;

	private int  lastClickType;
	private long lastClickTime;

	private char lastKey;
	private int  lastKeyCode;
	private long lastKeyTime;

	private long lastUpdate;
	private long updateCount;
	private long renderTime;

	public Node(final double x, final double y) {
		this(x, y, 0, 0);
	}

	public Node(final double x, final double y, final double width, final double height) {
		this.animators     = new ArrayList<>();
		this.hoverAnimator = this.createAnimator();

		this.children          = new IndexedConcurrentList<>();
		this.layers            = new LinkedList<>();
		this.hoverSupplierList = new LinkedList<>();

		this.waitingList = new ArrayList<>();
		this.callbackMap = new HashMap<>();

		this.defaultX = this.x = x;
		this.defaultY = this.y = y;

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
		this.scrollSpeed = 1D;
	}

	/* [ Bridge Section ] */
	public final void load(final @NonNull UI ui) {
		this.executeCallback(Node.CALLBACK_INIT, InternalContext.create(), () -> {
			this.ui = ui;

			this.children.forEach(child -> child.load(this.ui));
			if (this.scrollbar != null) {
				this.scrollbar.load(this.ui);
			}

			if (this.skeleton != null) {
				this.skeleton.load(this.ui);
			}

			this.init(this.ui);
		});

		this.updateCount++;
		this.lastUpdate = System.currentTimeMillis();
	}

	public final void render(final double mouseX, final double mouseY) {
		final long now = System.nanoTime();
		GLContext.matrix(() -> {
			Color.reset();
			if (this.parent != null) {
				GL11.glTranslated(this.parent.x, this.parent.y, 0D);
				if (this.position == PositionProperty.ABSOLUTE) {
					GL11.glTranslated(-this.parent.getAbsoluteX(), -this.parent.getAbsoluteY(), 0D);
				}
			}

			GL11.glTranslated(0D, 0D, this.zlevel);

			if (this.isVisible()) {
				if (this.aspectRatio >= 0D) {
					if (this.width != 0) {
						this.height = this.width * this.aspectRatio;
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
					this.hoverAnimator.sequence(this.hoverDuration, 100F).start();
				}

				if (this.hovered && !this.isHovered(mouseX, mouseY)) {
					this.hoverAnimator.sequence(this.hoverDuration, 0F).start();
				}

				this.hovered = this.isHovered(mouseX, mouseY);
				this.animators.forEach(TweenAnimator::update);

				if (this.overflow == OverflowProperty.SCROLL) {
					if (!this.hasOverflowY()) {
						this.maxScrollX = 0;
						double scrollOffset = Double.MIN_VALUE;
						for (final Node child : this.children) {
							this.maxScrollX = Math.max(this.maxScrollX, child.defaultX + child.width - this.width);
							if (scrollOffset == Double.MIN_VALUE) {
								scrollOffset = child.defaultX;
							} else {
								scrollOffset = Math.min(scrollOffset, child.defaultX);
							}
						}

						if (this.hasOverflowX()) {
							this.maxScrollX += scrollOffset;
							this.children.forEach(child -> {
								child.x = child.defaultX + this.scrollX;
							});
						}
					}

					if (!this.hasOverflowX()) {
						this.maxScrollY = 0;
						double scrollOffset = Double.MIN_VALUE;
						for (final Node child : this.children) {
							this.maxScrollY = Math.max(this.maxScrollY, child.defaultY + child.height - this.height);
							if (scrollOffset == Double.MIN_VALUE) {
								scrollOffset = child.defaultY;
							} else {
								scrollOffset = Math.min(scrollOffset, child.defaultY);
							}
						}

						if (this.hasOverflowY()) {
							this.maxScrollY += scrollOffset;
							this.children.forEach(child -> {
								child.y = child.defaultY + this.scrollY;
							});
						}
					}
				}

				this.targetScrollX = Math.min(Math.max(this.targetScrollX, -this.maxScrollX), 0);
				this.targetScrollY = Math.min(Math.max(this.targetScrollY, -this.maxScrollY), 0);

				if (this.targetScrollX != this.scrollX) {
					final double speed = this.scrollbar != null && this.scrollbar.isDragging() ? 1D : 0.2D;
					this.scrollX = this.ui.lerpByFramerate(this.scrollX, this.targetScrollX, speed, speed, true);
				}

				if (this.targetScrollY != this.scrollY) {
					final double speed = this.scrollbar != null && this.scrollbar.isDragging() ? 1D : 0.2D;
					this.scrollY = this.ui.lerpByFramerate(this.scrollY, this.targetScrollY, speed, speed, true);
				}

				if (this.scrollbar != null) {
					if (this.hasOverflowX()) {
						final float percent = (float) Math.abs(this.scrollX / this.maxScrollX);
						this.scrollbar.x(this.scrollbar.getDefaultX() + this.scrollbar.getScrollWidth() * percent);
					}

					if (this.hasOverflowY()) {
						final float percent = (float) Math.abs(this.scrollY / this.maxScrollY);
						this.scrollbar.y(this.scrollbar.getDefaultY() + this.scrollbar.getScrollHeight() * percent);
					}
				}

				if (this.dragging) {
					this.executeCallback(Node.CALLBACK_DRAG, InternalContext.create(), () -> {
						this.targetDragX = mouseX - this.dragX;
						this.targetDragY = mouseY - this.dragY;
					});
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
						final double newAbsoluteX = this.draggable.lerp(this.ui.getFps(), this.getAbsoluteX(), this.targetDragX);
						final double newAbsoluteY = this.draggable.lerp(this.ui.getFps(), this.getAbsoluteY(), this.targetDragY);

						final double diffX = this.getAbsoluteX() - this.x;
						final double diffY = this.getAbsoluteY() - this.y;

						this.x = newAbsoluteX - diffX;
						this.y = newAbsoluteY - diffY;

						if (!this.dragging && this.getAbsoluteX() == this.targetDragX && this.getAbsoluteY() == this.targetDragY) {
							this.dragged = false;
						}
					}
				}

				final boolean wasMounted = this.mounted;
				this.mounted = this.isMounted();

				if (!wasMounted && this.mounted) {
					this.executeCallback(Node.CALLBACK_MOUNT, InternalContext.create());
				}

				final double stencilBoxX = this.stencilBox == null ? 0 : this.x + this.stencilBox[0];
				final double stencilBoxY = this.stencilBox == null ? 0 : this.y + this.stencilBox[1];
				final double stencilBoxW = this.stencilBox == null ? 0 : this.stencilBox[2];
				final double stencilBoxH = this.stencilBox == null ? 0 : this.stencilBox[3];
				this.ui.stencil(stencilBoxX, stencilBoxY, stencilBoxW, stencilBoxH, () -> {
					this.ui.stencil(this.x, this.y, this.width, this.height, () -> {
						if (this.overflow != OverflowProperty.NONE) {
							this.children.forEach(child -> child.overflowArea = this);
						} else if (this.overflowArea != null) {
							this.children.forEach(child -> child.overflowArea = this.overflowArea);
						}

						if (this.skeleton != null && !this.mounted) {
							this.executeCallback(Node.CALLBACK_DRAW, InternalContext.create(), () -> {
								this.skeleton.render(mouseX, mouseY);
							}, mouseX, mouseY);
							return;
						}

						this.children.ordered().stream().filter(child -> child.zindex < 0).forEach(child -> child.render(mouseX, mouseY));
						this.executeCallback(Node.CALLBACK_DRAW, InternalContext.create(), () -> {
							if (this.mounted) {
								this.draw(mouseX, mouseY);
							} else {
								this.drawSkeleton(mouseX, mouseY);
							}
						}, mouseX, mouseY);
						this.children.ordered().stream().filter(child -> child.zindex >= 0).forEach(child -> child.render(mouseX, mouseY));
						this.layers.forEach(layer -> layer.draw(mouseX, mouseY));
					}, this.overflow != OverflowProperty.NONE);
				}, this.stencilBox != null);

				if (this.overflow == OverflowProperty.SCROLL && this.scrollbar != null && (this.hasOverflowX() || this.hasOverflowY())) {
					this.scrollbar.render(mouseX, mouseY);
				}
			}
			Color.reset();
		});
		this.renderTime = System.nanoTime() - now;
	}

	public boolean renderHover(final double mouseX, final double mouseY) {
		final AtomicBoolean cancelled = new AtomicBoolean(false);
		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> {
			if (child.renderHover(mouseX, mouseY)) {
				cancelled.set(true);
			}
		});

		if (this.isHovered(mouseX, mouseY, false)) {
			if (this.hoverSupplierList != null && !this.hoverSupplierList.isEmpty()) {
				final List<String> hoverList = new LinkedList<>();
				for (final Supplier<List<String>> hoverSupplier : this.hoverSupplierList) {
					hoverList.addAll(hoverSupplier.get());
				}
				this.ui.drawHover(hoverList, mouseX, mouseY);
			}
			return true;
		}

		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> {
			if (child.renderHover(mouseX, mouseY)) {
				cancelled.set(true);
			}
		});

		return cancelled.get();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(
				this.x,
				this.y,
				this.width,
				this.height,
				Color.LOADING()
				);
	}

	public final void onUpdate() {
		this.executeCallback(Node.CALLBACK_UPDATE, InternalContext.create(), () -> {
			this.children.ordered().stream().forEach(Node::onUpdate);
			this.update();
		});
	}

	public final void onMousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		this.lastClickType = clickType;
		this.lastClickTime = System.currentTimeMillis();

		if (this.scrollbar != null) {
			this.scrollbar.onMousePressed(mouseX, mouseY, clickType, context);
		}

		if (this.skeleton != null) {
			this.skeleton.onMousePressed(mouseX, mouseY, clickType, context);
		}

		if (this.hasCallback(Node.CALLBACK_MOUSE_PRESSED)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_PRESSED, context, mouseX, mouseY, clickType);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMousePressed(mouseX, mouseY, clickType, context));
		if (this.isHovered(mouseX, mouseY) && this.hasCallback(Node.CALLBACK_CLICK)) {
			this.executeCallback(Node.CALLBACK_CLICK, context, mouseX, mouseY, clickType);
		}

		this.mousePressed(mouseX, mouseY, clickType, context);
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMousePressed(mouseX, mouseY, clickType, context));

		if (this.hasCallback(Node.CALLBACK_MOUSE_PRESSED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_PRESSED, context, mouseX, mouseY, clickType);
		}

		if (!context.isCancelled() && this.draggable != null && this.draggable.isEnabled(this) && clickType == 0 && this.isHovered(mouseX, mouseY)) {
			this.executeCallback(Node.CALLBACK_DRAG_START, InternalContext.create(), () -> {
				this.dragging = true;
				this.dragX = mouseX - this.getAbsoluteX();
				this.dragY = mouseY - this.getAbsoluteY();
				this.startDragX = this.getAbsoluteX();
				this.startDragY = this.getAbsoluteY();
				this.targetDragX = this.getAbsoluteX();
				this.targetDragY = this.getAbsoluteY();
				this.dragged = true;
			});
		}
	}

	public final void onMouseDragged(final double mouseX, final double mouseY, final int clickType, final long deltaTime, final @NonNull InternalContext context) {
		if (this.scrollbar != null) {
			this.scrollbar.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context);
		}

		if (this.skeleton != null) {
			this.skeleton.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context);
		}

		if (this.hasCallback(Node.CALLBACK_MOUSE_DRAGGED)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_DRAGGED, context, mouseX, mouseY, clickType, deltaTime);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context));
		this.mouseDragged(mouseX, mouseY, clickType, deltaTime, context);
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseDragged(mouseX, mouseY, clickType, deltaTime, context));

		if (this.hasCallback(Node.CALLBACK_MOUSE_DRAGGED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_DRAGGED, context, mouseX, mouseY, clickType, deltaTime);
		}
	}

	public final void onMouseReleased(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {
		if (this.scrollbar != null) {
			this.scrollbar.onMouseReleased(mouseX, mouseY, clickType, context);
		}

		if (this.skeleton != null) {
			this.skeleton.onMouseReleased(mouseX, mouseY, clickType, context);
		}

		if (this.dragging) {
			this.executeCallback(Node.CALLBACK_DRAG_END, InternalContext.create(), () -> {
				if (this.draggable != null && this.draggable.isEnabled(this)) {
					if (this.draggable.hasSnapping()) {
						final Node snapNode = this.draggable.getSnapping(this);
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
			});
		}

		if (this.hasCallback(Node.CALLBACK_MOUSE_RELEASED)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_RELEASED, context, mouseX, mouseY, clickType);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseReleased(mouseX, mouseY, clickType, context));
		this.mouseReleased(mouseX, mouseY, clickType, context);
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseReleased(mouseX, mouseY, clickType, context));

		if (this.hasCallback(Node.CALLBACK_MOUSE_RELEASED)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_RELEASED, context, mouseX, mouseY, clickType);
		}
	}

	public final void onMouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {
		if (this.scrollbar != null) {
			this.scrollbar.onMouseScroll(mouseX, mouseY, value, context);
		}

		if (this.skeleton != null) {
			this.skeleton.onMouseScroll(mouseX, mouseY, value, context);
		}

		if (this.hasCallback(Node.CALLBACK_MOUSE_SCROLL)) {
			this.executePreCallback(Node.CALLBACK_MOUSE_SCROLL, context, mouseX, mouseY, value);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onMouseScroll(mouseX, mouseY, value, context));
		if (!context.isCancelled() && this.isHovered(mouseX, mouseY) && value != 0) {
			final double mappedScrollSpeed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? this.scrollSpeed * 2 : this.scrollSpeed;
			if (this.hasOverflowX()) {
				this.scrollX(value > 0 ? 30 : -30, mappedScrollSpeed);
				context.cancel();
			}

			if (this.hasOverflowY()) {
				this.scrollY(value > 0 ? 30 : -30, mappedScrollSpeed);
				context.cancel();
			}
		}

		this.mouseScroll(mouseX, mouseY, value, context);
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onMouseScroll(mouseX, mouseY, value, context));

		if (this.hasCallback(Node.CALLBACK_MOUSE_SCROLL)) {
			this.executePostCallback(Node.CALLBACK_MOUSE_SCROLL, context, mouseX, mouseY, value);
		}
	}

	public final void onKeyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {
		this.lastKey = c;
		this.lastKeyCode = keyCode;
		this.lastKeyTime = System.currentTimeMillis();

		if (this.scrollbar != null) {
			this.scrollbar.onKeyPressed(c, keyCode, context);
		}

		if (this.skeleton != null) {
			this.skeleton.onKeyPressed(c, keyCode, context);
		}

		if (this.hasCallback(Node.CALLBACK_KEY_PRESSED)) {
			this.executePreCallback(Node.CALLBACK_KEY_PRESSED, context, c, keyCode);
		}

		this.children.reversed().stream().filter(child -> child.zindex >= 0).forEach(child -> child.onKeyPressed(c, keyCode, context));
		this.keyPressed(c, keyCode, context);
		this.children.reversed().stream().filter(child -> child.zindex < 0).forEach(child -> child.onKeyPressed(c, keyCode, context));

		if (this.hasCallback(Node.CALLBACK_KEY_PRESSED)) {
			this.executePostCallback(Node.CALLBACK_KEY_PRESSED, context, c, keyCode);
		}
	}

	/* [ Utility Section ] */
	/**
	 * Adds the specified animator to the node.
	 * @param <T> the type of the node
	 * @param animator the animator to add
	 * @return the created animator
	 */
	public final @NonNull TweenAnimator createAnimator() {
		final TweenAnimator animator = TweenAnimator.create();
		this.animators.add(animator);
		return animator;
	}

	/**
	 * Removes the specified animator from the node
	 * @param <T> the type of the node
	 * @param animator the animator to remove
	 * @return the modified node
	 */
	public final <T extends Node> @NonNull T removeAnimator(final @NonNull TweenAnimator animator) {
		this.animators.remove(animator);
		return (T) this;
	}

	/**
	 * Reloads the node and its children, loads the UI, and executes the reload callback.
	 * The method iterates through all the children nodes, calling their reload methods.
	 * Then, it loads the UI and executes the reload callback specified by the identifier {@link Node#CALLBACK_RELOAD}.
	 */
	public final void reload() {
		this.executeCallback(Node.CALLBACK_RELOAD, InternalContext.create(), () -> {
			this.children.forEach(Node::reload);
			this.load(this.ui);
		});
	}

	/**
	 * Appends the given nodes as children to this node.
	 *
	 * @param index The index to append.
	 * @param nodes The nodes to append.
	 * @param <T>   The type of the node.
	 * @return The modified node after appending the children.
	 * @throws NullPointerException If the nodes are {@code null}.
	 */
	public final <T extends Node> @NonNull T append(final @NonNull Node @NonNull ... nodes) {
		for (final Node node : nodes) {
			this.executeCallback(Node.CALLBACK_APPEND, InternalContext.create(), () -> {
				node.parent(this);
				if (this.ui != null) {
					node.load(this.ui);
				}
				this.children.add(node);
			}, node);
		}
		return (T) this;
	}

	/**
	 * Attaches the specified node as a child to this node.
	 *
	 * @param node The node to attach.
	 * @param <T>  The type of the node.
	 * @return The modified node after attaching the child.
	 * @throws NullPointerException If the node is {@code null}.
	 */
	public final <T extends Node> @NonNull T attach(final @NonNull Node node) {
		node.append(this);
		return (T) this;
	}

	/**
	 * Attaches this node to the specified UI.
	 *
	 * @param ui The UI to attach to.
	 * @param <T> The type of the node.
	 * @return The modified node after attaching to the UI.
	 * @throws NullPointerException If the UI is {@code null}.
	 */
	public final <T extends Node> @NonNull T attach(final @NonNull UI ui) {
		ui.add(this);
		return (T) this;
	}

	/**
	 * Executes the specified runnable as the body of this node.
	 *
	 * @param runnable The runnable to execute.
	 * @param <T>      The type of the node.
	 * @return The modified node after executing the runnable.
	 * @throws NullPointerException If the runnable is {@code null}.
	 */
	public final <T extends Node> @NonNull T body(final @NonNull Runnable runnable) {
		return this.body(n -> runnable.run());
	}

	/**
	 * Executes the specified consumer, passing this node as an argument.
	 *
	 * @param consumer The consumer to execute.
	 * @param <T>      The type of the node.
	 * @return The modified node after executing the consumer.
	 * @throws NullPointerException If the consumer is {@code null}.
	 */
	public final <T extends Node> @NonNull T body(final @NonNull Consumer<@NonNull T> consumer) {
		this.bodyConsumer = (Consumer<Node>) consumer;
		this.bodyConsumer.accept(this);
		return (T) this;
	}

	/**
	 * Scrolls this node horizontally based on the provided value and speed.
	 *
	 * @param value The value to scroll by.
	 * @param speed The speed of the scroll.
	 * @param <T>   The type of the node.
	 * @return The modified node after horizontal scrolling.
	 */
	public final <T extends Node> @NonNull T scrollX(final double value, final double speed) {
		this.setScrollX(this.targetScrollX + value * speed);
		return (T) this;
	}

	/**
	 * Scrolls this node vertically based on the provided value and speed.
	 *
	 * @param value The value to scroll by.
	 * @param speed The speed of the scroll.
	 * @param <T>   The type of the node.
	 * @return The modified node after vertical scrolling.
	 */
	public final <T extends Node> @NonNull T scrollY(final double value, final double speed) {
		this.setScrollY(this.targetScrollY + value * speed);
		return (T) this;
	}

	/**
	 * Sets the horizontal scroll percentage based on the provided value.
	 *
	 * @param percent The percentage value to set.
	 * @param <T>     The type of the node.
	 * @return The modified node after setting the horizontal scroll.
	 */
	public final <T extends Node> @NonNull T setScrollX(final float percent) {
		this.setScrollX(-this.maxScrollX * percent);
		return (T) this;
	}

	/**
	 * Sets the vertical scroll percentage based on the provided value.
	 *
	 * @param percent The percentage value to set.
	 * @param <T>     The type of the node.
	 * @return The modified node after setting the vertical scroll.
	 */
	public final <T extends Node> @NonNull T setScrollY(final float percent) {
		this.setScrollY(-this.maxScrollY * percent);
		return (T) this;
	}

	/**
	 * Sets the horizontal scroll value based on the provided value.
	 *
	 * @param value The value to set.
	 * @param <T>   The type of the node.
	 * @return The modified node after setting the horizontal scroll.
	 */
	public final <T extends Node> @NonNull T setScrollX(final double value) {
		this.executeCallback(Node.CALLBACK_SCROLL_UPDATE, InternalContext.create(), () -> {
			final double oldValue = this.targetScrollX;
			this.targetScrollX = Math.min(value, 0);
			if (this.targetScrollX <= -this.maxScrollX) {
				this.targetScrollX = -this.maxScrollX;
				if (oldValue != this.targetScrollX) {
					this.executeCallback(Node.CALLBACK_SCROLL_END, InternalContext.create(), this.scrollX, this.scrollY);
				}
			}
		}, value);
		return (T) this;
	}

	/**
	 * Sets the vertical scroll value based on the provided value.
	 *
	 * @param value The value to set.
	 * @param <T>   The type of the node.
	 * @return The modified node after setting the vertical scroll.
	 */
	public final <T extends Node> @NonNull T setScrollY(final double value) {
		this.executeCallback(Node.CALLBACK_SCROLL_UPDATE, InternalContext.create(), () -> {
			final double oldValue = this.targetScrollY;
			this.targetScrollY = Math.min(value, 0);
			if (this.targetScrollY <= -this.maxScrollY) {
				this.targetScrollY = -this.maxScrollY;
				if (oldValue != this.targetScrollY) {
					this.executeCallback(Node.CALLBACK_SCROLL_END, InternalContext.create(), this.scrollX, this.scrollY);
				}
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

	public final <T extends NodeCallback> void executeCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		this.executeCallback(type, context, null, args);
	}

	public final <T extends NodeCallback> void executePreCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList == null) {
			return;
		}

		for (final NodeCallbackObject<T> callback : callbackList) {
			callback.pre(this, context, args);
		}
	}

	public final <T extends NodeCallback> void executePostCallback(final int type, final @NonNull InternalContext context, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList == null) {
			return;
		}

		for (final NodeCallbackObject<T> callback : callbackList) {
			callback.post(this, context, args);
		}
	}

	public final <T extends NodeCallback> void executeCallback(final int type, final @NonNull InternalContext context, final Runnable runnable, final Object... args) {
		final List<NodeCallbackObject<T>> callbackList = this.getCallbackList(type);
		if (callbackList == null) {
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

		for (final NodeCallbackObject<T> callback : callbackList) {
			callback.post(this, context, args);
		}
	}

	protected final <T extends Node> @NonNull T registerCallback(final int type, final @NonNull NodeCallback callback) {
		final List<NodeCallbackObject<?>> callbackList = this.callbackMap.getOrDefault(type, new ArrayList<>());
		callbackList.add(new NodeCallbackObject<>(callback));
		this.callbackMap.put(type, callbackList);
		return (T) this;
	}

	/* [ Getter Section ] */
	/**
	 * Gets the absolute X-coordinate of the node, taking into account its parent's position.
	 *
	 * @return The absolute X-coordinate of the node.
	 */
	public final double getAbsoluteX() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.x;
		}

		return this.parent != null ? this.parent.getAbsoluteX() + this.x : this.x;
	}

	/**
	 * Gets the absolute Y-coordinate of the node, taking into account its parent's position.
	 *
	 * @return The absolute Y-coordinate of the node.
	 */
	public final double getAbsoluteY() {
		if (this.position == PositionProperty.ABSOLUTE) {
			return this.y;
		}

		return this.parent != null ? this.parent.getAbsoluteY() + this.y : this.y;
	}

	/**
	 * Gets the absolute default X-coordinate of the node, taking into account its parent's position.
	 *
	 * @return The absolute default X-coordinate of the node.
	 */
	public final double getAbsoluteDefaultX() {
		return this.parent != null ? this.parent.getAbsoluteX() + this.defaultX : this.defaultX;
	}

	/**
	 * Gets the absolute default Y-coordinate of the node, taking into account its parent's position.
	 *
	 * @return The absolute default Y-coordinate of the node.
	 */
	public final double getAbsoluteDefaultY() {
		return this.parent != null ? this.parent.getAbsoluteY() + this.defaultY : this.defaultY;
	}

	/**
	 * Clears all children nodes associated with this node.
	 *
	 * @param <T> The type of the node.
	 * @return The modified node after clearing its children.
	 */
	public final <T extends Node> @NonNull T clearChildren() {
		this.children.clear();
		return (T) this;
	}

	/**
	 * Gets a list of children nodes of a specific type associated with this node.
	 *
	 * @param clazz The class type of the children nodes.
	 * @param <T>   The type of the children nodes.
	 * @return A list of children nodes of the specified type.
	 * @throws NullPointerException If the class type is {@code null}.
	 */
	public final <T extends Node> IndexedLinkedList<T> getChildren(final @NonNull Class<T> clazz) {
		return new IndexedLinkedList<>(this.children.ordered().stream().filter(child -> clazz.isAssignableFrom(child.getClass())).map(child -> (T) child).collect(Collectors.toList()));
	}

	/**
	 * Gets a specific child node based on its index and type associated with this node.
	 *
	 * @param index The index of the child node.
	 * @param clazz The class type of the child node.
	 * @param <T>   The type of the child node.
	 * @return The specified child node or {@code null} if not found.
	 * @throws NullPointerException If the class type is {@code null}.
	 */
	public final <T extends Node> T getChild(final int index, final @NonNull Class<T> clazz) {
		int i = 0;
		for (final Node child : this.children) {
			if (child.getClass().equals(clazz)) {
				if (i == index) {
					return (T) child;
				}

				i++;
			}
		}

		return null;
	}

	/**
	 * Get the associated ui of this node.
	 *
	 * @param <T>
	 * @param clazz
	 * @return The associated ui of this node.
	 * @throws NullPointerException If the class type is {@code null}.
	 */
	public final <T extends UI> @NonNull T getUi() {
		return (T) this.ui;
	}

	/**
	 * Checks if the node is being hovered by the mouse at the specified coordinates.
	 *
	 * @param mouseX The X-coordinate of the mouse.
	 * @param mouseY The Y-coordinate of the mouse.
	 * @return {@code true} if the node is being hovered, {@code false} otherwise.
	 */
	public boolean isHovered(final double mouseX, final double mouseY) {
		return this.isHovered(mouseX, mouseY, true);
	}

	/**
	 * Checks if the node is being hovered by the mouse at the specified coordinates.
	 *
	 * @param mouseX The X-coordinate of the mouse.
	 * @param mouseY The Y-coordinate of the mouse.
	 * @param checkEnabled {@code true} if the node should be checked for being enabled, {@code false} otherwise.
	 * @return {@code true} if the node is being hovered, {@code false} otherwise.
	 */
	public boolean isHovered(final double mouseX, final double mouseY, final boolean checkEnabled) {
		if (this.ui == null) {
			return false;
		}

		return this.isVisible() && (!checkEnabled || this.isEnabled()) && this.ui.isOnTop() && (this.overflowArea != null ? this.overflowArea.isHovered(mouseX, mouseY) : true) && mouseX > this.getAbsoluteX() && mouseX <= this.getAbsoluteX() + this.width && mouseY > this.getAbsoluteY() && mouseY <= this.getAbsoluteY() + this.height;
	}

	/**
	 * Checks if the node should be rendered.
	 * @return {@code true} if the node should be rendered, {@code false} otherwise.
	 */
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

	/**
	 * Return the node's visible properties.
	 * @return {@code true} if the node should be rendered, {@code false} otherwise.
	 */
	public boolean isVisibleProperty() {
		return this.visible.test(this);
	}

	/**
	 * Return the node's enabled properties.
	 * @return {@code true} if the node is enabled, {@code false} otherwise.
	 */
	public boolean isEnabled() {
		return this.enabled.test(this);
	}

	/**
	 * Checks if the node is currently mounted in the hierarchy.
	 *
	 * @return {@code true} if the node is mounted, {@code false} otherwise.
	 */
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

	/**
	 * Adjusts a value based on the hover animation.
	 *
	 * @param value The original value to adjust.
	 * @return The adjusted value based on the hover animation.
	 */
	public final float hoverValue(final float value) {
		return value / 100F * this.hoverAnimator.getValue();
	}

	/**
	 * Calculates the ratio of the node's width to the specified value.
	 *
	 * @param value The value to divide the width by.
	 * @return The result of the division.
	 */
	public final double dw(final double value) {
		return this.width / value;
	}

	/**
	 * Calculates the ratio of the node's height to the specified value.
	 *
	 * @param value The value to divide the height by.
	 * @return The result of the division.
	 */
	public final double dh(final double value) {
		return this.height / value;
	}

	/**
	 * Calculates the result of multiplying the node's width by the specified value.
	 *
	 * @param value The value to multiply the width by.
	 * @return The result of the multiplication.
	 */
	public final double mw(final double value) {
		return this.width * value;
	}

	/**
	 * Calculates the result of multiplying the node's height by the specified value.
	 *
	 * @param value The value to multiply the height by.
	 * @return The result of the multiplication.
	 */
	public final double mh(final double value) {
		return this.height * value;
	}

	/**
	 * Calculates the result of adding the specified value to the node's width.
	 *
	 * @param value The value to add to the width.
	 * @return The result of the addition.
	 */
	public final double aw(final double value) {
		return this.width + value;
	}

	/**
	 * Calculates the result of adding the specified value to the node's height.
	 *
	 * @param value The value to add to the height.
	 * @return The result of the addition.
	 */
	public final double ah(final double value) {
		return this.height + value;
	}

	/**
	 * Calculates the result of adding the specified value to the node's X-coordinate.
	 *
	 * @param value The value to add to the X-coordinate.
	 * @return The result of the addition.
	 */
	public final double ax(final double value) {
		return this.x + value;
	}

	/**
	 * Calculates the result of adding the specified value to the node's Y-coordinate.
	 *
	 * @param value The value to add to the Y-coordinate.
	 * @return The result of the addition.
	 */
	public final double ay(final double value) {
		return this.y + value;
	}

	/**
	 * Checks if there is overflow in the X-direction, indicating the content extends beyond the visible area.
	 *
	 * @return {@code true} if there is overflow in the X-direction, {@code false} otherwise.
	 */
	public final boolean hasOverflowX() {
		return this.maxScrollX > 0;
	}

	/**
	 * Checks if there is overflow in the Y-direction, indicating the content extends beyond the visible area.
	 *
	 * @return {@code true} if there is overflow in the Y-direction, {@code false} otherwise.
	 */
	public final boolean hasOverflowY() {
		return this.maxScrollY > 0;
	}

	/**
	 * Retrieves the callback associated with the specified callback identifier.
	 *
	 * @param type The callback identifier.
	 * @param <T>      The type of the node.
	 * @return The callback associated with the identifier, or {@code null} if not found.
	 */
	public final <T extends NodeCallback> @NonNull List<@NonNull NodeCallbackObject<T>> getCallbackList(final int type) {
		if (this.callbackMap.isEmpty() || !this.callbackMap.containsKey(type)) {
			return null;
		}

		final List<NodeCallbackObject<?>> callbackList = this.callbackMap.get(type);
		if (callbackList.isEmpty()) {
			return null;
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

	public final boolean hasCallback(final int type) {
		return this.callbackMap.containsKey(type);
	}

	/**
	 * Gets the index of the node within its parent or UI hierarchy.
	 *
	 * @return The index of the node as a string.
	 */
	public final @NonNull String getMappedIndex() {
		if (this.ui == null) {
			return "N/A";
		}

		if (this.parent != null) {
			return this.parent.getMappedIndex() + "." + this.parent.children.ordered().indexOf(this);
		}

		return String.valueOf(this.ui.getNodeList().ordered().indexOf(this));
	}

	/**
	 * Gets the hierarchical representation of the node's position within its parent or UI hierarchy.
	 *
	 * @return The hierarchical representation of the node's position.
	 */
	public final @NonNull String getHierarchy() {
		if (this.parent == null) {
			return this.getClass().getSimpleName();
		}

		return this.parent.getHierarchy() + " - " + this.getClass().getSimpleName();
	}

	/* [ Hook Section ] */
	public final <T extends UIStore> T useStore(final @NonNull Class<T> clazz) {
		return this.ui.useStore(clazz);
	}

	/* [ Setter Section ] */
	public final <T extends Node> @NonNull T layer(final @NonNull NodeLayer layer) {
		this.layers.add(layer);
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

	public final <T extends Node> @NonNull T position(final @NonNull PositionProperty position) {
		this.position = position;
		return (T) this;
	}

	public final <T extends Node> @NonNull T overflow(final @NonNull OverflowProperty overflow) {
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

	public final <T extends Node> @NonNull T stencilBox(final double x, final double y, final double width, final double height) {
		this.stencilBox = new double[] { x, y, width, height };
		return (T) this;
	}

	public final <T extends Node> @NonNull T stencilBox(final double width, final double height) {
		this.stencilBox = new double[] { 0, 0, width, height };
		return (T) this;
	}

	public final <T extends Node> @NonNull T stencilBox() {
		this.stencilBox = new double[] { 0, 0, this.width, this.height };
		return (T) this;
	}

	public final <T extends Node> @NonNull T aspectRatio(final double aspectRatio) {
		this.aspectRatio = aspectRatio;
		return (T) this;
	}

	public final <T extends Node> @NonNull T position(final double x, final double y) {
		this.x = x;
		this.y = y;
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

	public final <T extends Node> @NonNull T wait(final @NonNull ISignal<?> watchable) {
		this.waitingList.add(node -> watchable.isPresent());
		return (T) this;
	}

	public final <T extends Node> @NonNull T wait(final @NonNull Predicate<@NonNull T> predicate) {
		this.waitingList.add((Predicate<Node>) predicate);
		return (T) this;
	}

	public final <T extends Node> @NonNull T wait(final long time, final @NonNull TimeUnit unit) {
		final long endTime = System.currentTimeMillis() + unit.toMillis(time);
		this.waitingList.add(node -> System.currentTimeMillis() >= endTime);
		return (T) this;
	}

	public final <T extends Node> @NonNull T watch(final Signal<?> signal) {
		return this.watch(signal, WatchProperty.RELOAD);
	}

	public final <T extends Node> @NonNull T watch(final Signal<?> signal, final @NonNull WatchProperty @NonNull... properties) {
		signal.subscribe(value -> {
			if (this.ui == null) {
				return true;
			}

			this.executeCallback(Node.CALLBACK_WATCH, InternalContext.create(), () -> {
				for (final WatchProperty property : properties) {
					property.apply(this);
				}
			}, signal, properties);

			return JOID.isOpen(this.ui.getClass());
		});
		return (T) this;
	}

	public final <T extends Node> @NonNull T hovered(final boolean hovered) {
		this.hovered = hovered;
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverDuration(final long hoverDuration) {
		this.hoverDuration = hoverDuration;
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
		return (T) this;
	}

	public final <T extends Node> @NonNull T zlevel(final double zlevel) {
		this.zlevel = zlevel;
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
		this.hoverSupplierList.clear();
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverLines(final @NonNull Supplier<@NonNull List<@NonNull String>> supplier) {
		this.hoverSupplierList.clear();
		this.hover(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hoverLines(final @NonNull HoverSupplier supplier) {
		this.hoverSupplierList.clear();
		this.hover(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hover(final @NonNull Supplier<@NonNull List<@NonNull String>> supplier) {
		this.hoverSupplierList.add(supplier);
		return (T) this;
	}

	public final <T extends Node> @NonNull T hover(final @NonNull HoverSupplier supplier) {
		this.hoverSupplierList.add(() -> Collections.singletonList(supplier.get()));
		return (T) this;
	}

	/* [ Callback Section ] */
	public final <T extends Node> @NonNull T onClick(final @NonNull NodeMousePressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_CLICK, callback);
	}

	public final <T extends Node> @NonNull T onMousePressed(final @NonNull NodeMousePressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_PRESSED, callback);
	}

	public final <T extends Node> @NonNull T onMouseDragged(final @NonNull NodeMouseDraggedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_DRAGGED, callback);
	}

	public final <T extends Node> @NonNull T onMouseReleased(final @NonNull NodeMouseReleasedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_RELEASED, callback);
	}

	public final <T extends Node> @NonNull T onMouseScroll(final @NonNull NodeMouseScrollCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_MOUSE_SCROLL, callback);
	}

	public final <T extends Node> @NonNull T onKeyPressed(final @NonNull NodeKeyPressedCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_KEY_PRESSED, callback);
	}

	public final <T extends Node> @NonNull T onInit(final @NonNull NodeInitCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_INIT, callback);
	}

	public final <T extends Node> @NonNull T onDraw(final @NonNull NodeDrawCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAW, callback);
	}

	public final <T extends Node> @NonNull T onUpdate(final @NonNull NodeUpdateCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_UPDATE, callback);
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

	public final <T extends Node> @NonNull T onScrollUpdate(final @NonNull NodeScrollUpdateCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SCROLL_UPDATE, callback);
	}

	public final <T extends Node> @NonNull T onScrollEnd(final @NonNull NodeScrollEndCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SCROLL_END, callback);
	}

	public final <T extends Node> @NonNull T onDrag(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG, callback);
	}

	public final <T extends Node> @NonNull T onDragStart(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG_START, callback);
	}

	public final <T extends Node> @NonNull T onDragEnd(final @NonNull NodeDragCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_DRAG_END, callback);
	}

	public final <T extends Node> @NonNull T onSnap(final @NonNull NodeSnapCallback<T> callback) {
		return this.registerCallback(Node.CALLBACK_SNAP, callback);
	}

	/* [ Abstract Section ] */
	@Override
	public int getIndex() {
		return this.zindex;
	}

	/* [ Java Section ] */
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

	@Override
	public @NonNull String toString() {
		final JsonObject json = this.toJson();
		if (JOID.inst().isDevMode()) {
			return Node.PRETTY_GSON.toJson(json);
		}

		return Node.GSON.toJson(json);
	}

}