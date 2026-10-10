package dev.joid.lib.ui.node.impl.structure.reorderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.DispatchContext;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import dev.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderCallback;
import dev.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderEndCallback;
import dev.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderStartCallback;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ReorderableFlexNode extends Node {

	public static final int CALLBACK_REORDER       = NodeCallbackRegistry.next(NodeReorderCallback.class);
	public static final int CALLBACK_REORDER_END   = NodeCallbackRegistry.next(NodeReorderEndCallback.class);
	public static final int CALLBACK_REORDER_START = NodeCallbackRegistry.next(NodeReorderStartCallback.class);

	private static final double SCROLL_HOT_ZONE  = 60D;
	private static final double SCROLL_SPEED_MAX = 2D;

	private final Set<Node>         lockedNodes  = new HashSet<>();
	private final List<Node>        logicalOrder = new ArrayList<>();
	private final Map<Node, Double> childCurrent = new HashMap<>();

	private Align         align;
	private double        margin;
	private boolean       autoDrag = true;
	private FlexDirection direction;

	private int     initialIndex;
	private int     currentIndex;
	private int     draggedZindex;
	private boolean releasing;
	private double  dragOffset;
	private Node    reorderedNode;
	private boolean scrollArmed;
	private double  draggedCurrent;
	private double  dragStartMouseX;
	private double  dragStartMouseY;

	protected ReorderableFlexNode(final double x, final double y, final double width, final double height, final @NonNull FlexDirection direction) {
		super(x, y, width, height);
		this.direction = direction;
	}

	public static @NonNull ReorderableFlexNode vertical(final double x, final double y, final double width) {
		return new ReorderableFlexNode(x, y, width, 0D, FlexDirection.COLUMN);
	}

	public static @NonNull ReorderableFlexNode horizontal(final double x, final double y, final double height) {
		return new ReorderableFlexNode(x, y, 0D, height, FlexDirection.ROW);
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.layout();
	}

	@Override
	public void update() {
		this.layout();
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.reorderedNode != null && super.getChildren().contains(this.reorderedNode)) {
			this.tickDrag(mouseX, mouseY);
		}
		this.layout();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.layout();
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (!this.autoDrag || !button.isLeft() || context.isCancelled() || this.reorderedNode != null || !super.isEnabled()) {
			return;
		}

		for (final Node child : super.getChildren()) {
			if (child.isHovered()) {
				if (this.lockedNodes.contains(child)) {
					return;
				}

				this.startDragInternal(child, mouseX, mouseY);
				if (this.reorderedNode == child) {
					context.cancel();
				}
				return;
			}
		}
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull MouseButton button, final @NonNull DispatchContext context) {
		if (this.reorderedNode != null && !this.releasing) {
			this.endDrag();
		}
	}

	@Override
	public void detach() {
		if (this.reorderedNode == null) {
			return;
		}

		if (super.getChildren().contains(this.reorderedNode)) {
			this.finishRelease();
		} else {
			this.abandonReorder();
		}
	}

	public final <T extends ReorderableFlexNode> @NonNull T startDrag(final @NonNull Node child) {
		if (this.reorderedNode != null) {
			return (T) this;
		}

		this.getChildIndex(child);
		if (this.lockedNodes.contains(child)) {
			return (T) this;
		}

		final UI ui = super.getUi();
		final double mouseX = ui != null ? ui.getMouseX() : 0D;
		final double mouseY = ui != null ? ui.getMouseY() : 0D;
		this.startDragInternal(child, mouseX, mouseY);
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T endDrag() {
		if (this.reorderedNode == null || this.releasing) {
			return (T) this;
		}

		this.releasing = true;
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T lock(final @NonNull Node @NonNull... children) {
		for (final Node child : children) {
			this.lockedNodes.add(child);
		}
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T unlock(final @NonNull Node @NonNull... children) {
		for (final Node child : children) {
			this.lockedNodes.remove(child);
		}
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorder(final @NonNull NodeReorderCallback<T> callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER, callback);
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorderEnd(final @NonNull NodeReorderEndCallback<T> callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER_END, callback);
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorderStart(final @NonNull NodeReorderStartCallback<T> callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER_START, callback);
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T direction(final @NonNull FlexDirection direction) {
		return this.direction(Signal.from(direction));
	}

	public final <T extends ReorderableFlexNode> @NonNull T direction(final @NonNull Supplier<@NonNull FlexDirection> direction) {
		return super.follow("direction", direction, this::changeDirection);
	}

	public final <T extends ReorderableFlexNode> @NonNull T align(final Align align) {
		return this.align(Signal.from(align));
	}

	public final <T extends ReorderableFlexNode> @NonNull T align(final @NonNull Supplier<Align> align) {
		return super.follow("align", align, value -> this.align = value);
	}

	public final <T extends ReorderableFlexNode> @NonNull T margin(final double margin) {
		return this.margin(Signal.from(margin));
	}

	public final <T extends ReorderableFlexNode> @NonNull T margin(final @NonNull Supplier<Double> margin) {
		return super.follow("margin", margin, value -> this.margin = value);
	}

	public final <T extends ReorderableFlexNode> @NonNull T autoDrag(final boolean autoDrag) {
		return this.autoDrag(Signal.from(autoDrag));
	}

	public final <T extends ReorderableFlexNode> @NonNull T autoDrag(final @NonNull Supplier<Boolean> autoDrag) {
		return super.follow("autoDrag", autoDrag, value -> this.autoDrag = value);
	}

	public final int getChildIndex(final @NonNull Node child) {
		final int index = super.getChildren().ordered().indexOf(child);
		if (index == -1) {
			throw new IllegalArgumentException("Node is not a child of this ReorderableFlexNode");
		}
		return index;
	}

	public final boolean isLocked(final @NonNull Node child) {
		return this.lockedNodes.contains(child);
	}

	public final boolean isDragging(final @NonNull Node child) {
		return this.reorderedNode == child;
	}

	private void changeDirection(final FlexDirection direction) {
		if (this.direction != direction) {
			for (final Node child : super.getChildren()) {
				child.x(child.getDefaultX());
				child.y(child.getDefaultY());
			}
		}

		this.direction = direction;
	}

	private void startDragInternal(final @NonNull Node child, final double mouseX, final double mouseY) {
		final int index = super.getChildren().ordered().indexOf(child);
		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER_START, DispatchContext.create(), () -> {
			this.reorderedNode = child;
			this.initialIndex = index;
			this.currentIndex = index;
			this.scrollArmed = false;
			this.dragStartMouseX = mouseX;
			this.dragStartMouseY = mouseY;

			this.logicalOrder.clear();
			for (final Node c : super.getChildren()) {
				this.logicalOrder.add(c);
			}

			this.childCurrent.clear();
			double offset = 0D;
			for (final Node c : super.getChildren()) {
				this.childCurrent.put(c, offset);
				if (c.isVisibleProperty()) {
					offset += this.mainSize(c) + this.margin;
				}
			}

			this.draggedCurrent = this.childCurrent.get(child);
			if (this.direction == FlexDirection.COLUMN) {
				this.dragOffset = mouseY - super.getAbsoluteY() - this.draggedCurrent;
			} else {
				this.dragOffset = mouseX - super.getAbsoluteX() - this.draggedCurrent;
			}

			this.draggedZindex = child.getZindex();
			child.zindex(Integer.MAX_VALUE);
			super.getChildren().remove(child);
			super.getChildren().add(child);

			child.fireDragStart(null);
		}, child);
	}

	private void tickDrag(final double mouseX, final double mouseY) {
		final Node dragged = this.reorderedNode;
		final boolean vertical = this.direction == FlexDirection.COLUMN;
		final double draggedSize = this.mainSize(dragged);

		final double target;
		if (this.releasing) {
			target = this.computeDraggedTarget();
		} else {
			final double localMouse = vertical ? mouseY - super.getAbsoluteY() : mouseX - super.getAbsoluteX();
			final double extent = this.computeFullExtent();
			target = Math.max(0D, Math.min(Math.max(0D, extent - draggedSize), localMouse - this.dragOffset));
		}

		this.draggedCurrent = super.getUi().lerpByFramerate(this.draggedCurrent, target, 0.5D, 0.5D, true);

		if (this.releasing) {
			if (this.draggedCurrent == target) {
				this.finishRelease();
			}
			return;
		}

		int newIndex = 0;
		double off = 0D;
		for (final Node c : this.logicalOrder) {
			if (c == dragged) {
				continue;
			}

			final double cSize = this.mainSize(c);
			final double cMiddle = c.isVisibleProperty() ? off + (cSize + this.margin) / 2D : off;
			if (cMiddle <= this.draggedCurrent) {
				newIndex++;
			}
			if (c.isVisibleProperty()) {
				off += cSize + this.margin;
			}
		}

		this.autoScrollParent(mouseX, mouseY);

		dragged.fireDrag(null);
		final int index = this.freeSlot(newIndex);
		if (index == this.currentIndex) {
			return;
		}

		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER, DispatchContext.create(), () -> {
			this.move(dragged, index);
			this.currentIndex = index;
		}, dragged);
	}

	private int freeSlot(final int index) {
		final int step = index < this.currentIndex ? 1 : -1;
		int slot = index;
		while (slot != this.currentIndex && this.lockedNodes.contains(this.logicalOrder.get(slot))) {
			slot += step;
		}
		return slot;
	}

	private void move(final @NonNull Node dragged, final int index) {
		final List<Node> free = new ArrayList<>();
		int position = 0;
		for (int i = 0; i < this.logicalOrder.size(); i++) {
			final Node child = this.logicalOrder.get(i);
			if (this.lockedNodes.contains(child)) {
				continue;
			}

			if (i < index) {
				position++;
			}
			if (child != dragged) {
				free.add(child);
			}
		}
		free.add(position, dragged);

		int next = 0;
		for (int i = 0; i < this.logicalOrder.size(); i++) {
			if (!this.lockedNodes.contains(this.logicalOrder.get(i))) {
				this.logicalOrder.set(i, free.get(next++));
			}
		}
	}

	private double computeDraggedTarget() {
		double off = 0D;
		for (final Node c : this.logicalOrder.subList(0, this.logicalOrder.indexOf(this.reorderedNode))) {
			if (c.isVisibleProperty()) {
				off += this.mainSize(c) + this.margin;
			}
		}
		return off;
	}

	private void finishRelease() {
		final Node node = this.reorderedNode;
		final int oldIndex = this.initialIndex;
		final int newIndex = this.currentIndex;

		this.applyReorder();
		node.fireDragEnd(null);

		this.reorderedNode = null;
		this.releasing = false;
		this.logicalOrder.clear();
		this.childCurrent.clear();

		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER_END, DispatchContext.create(), node, oldIndex, newIndex);
	}

	private void abandonReorder() {
		final Node node = this.reorderedNode;
		final int oldIndex = this.initialIndex;

		node.zindex(this.draggedZindex);
		node.fireDragEnd(null);

		this.reorderedNode = null;
		this.releasing = false;
		this.logicalOrder.clear();
		this.childCurrent.clear();

		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER_END, DispatchContext.create(), node, oldIndex, -1);
	}

	private void layout() {
		if (this.reorderedNode != null && !super.getChildren().contains(this.reorderedNode)) {
			this.abandonReorder();
		}

		final boolean vertical = this.direction == FlexDirection.COLUMN;
		final boolean dragging = this.reorderedNode != null;
		if (dragging) {
			this.syncOrder();
		}

		final List<Node> order = dragging ? this.logicalOrder : new ArrayList<>(super.getChildren().ordered());

		double off = 0D;
		for (final Node child : order) {
			final double size = this.mainSize(child);

			if (child == this.reorderedNode) {
				this.applyMain(child, this.draggedCurrent);
				this.applyAlign(child);
				if (child.isVisibleProperty()) {
					off += size + this.margin;
				}
				continue;
			}

			if (dragging) {
				final double cur = this.childCurrent.getOrDefault(child, off);
				final double newPos = super.getUi().lerpByFramerate(cur, off, 0.5D, 0.5D, true);
				this.childCurrent.put(child, newPos);
				this.applyMain(child, newPos);
			} else {
				this.applyMain(child, this.mainDefault(child) + off);
			}
			this.applyAlign(child);

			if (child.isVisibleProperty()) {
				off += size + this.margin;
			}
		}

		final double total = Math.max(0D, off - this.margin);
		if (vertical) {
			super.height(total);
		} else {
			super.width(total);
		}
	}

	private double computeFullExtent() {
		double off = 0D;
		for (final Node child : super.getChildren()) {
			if (child.isVisibleProperty()) {
				off += this.mainSize(child) + this.margin;
			}
		}
		return Math.max(0D, off - this.margin);
	}

	private double mainSize(final @NonNull Node child) {
		return this.direction == FlexDirection.COLUMN ? child.getHeight() : child.getWidth();
	}

	private double mainDefault(final @NonNull Node child) {
		return this.direction == FlexDirection.COLUMN ? child.getDefaultY() : child.getDefaultX();
	}

	private void applyMain(final @NonNull Node child, final double value) {
		if (this.direction == FlexDirection.COLUMN) {
			child.y(value);
		} else {
			child.x(value);
		}
	}

	private void applyAlign(final @NonNull Node child) {
		if (this.align == null) {
			return;
		}

		if (this.direction == FlexDirection.COLUMN) {
			if (this.align.isStart()) {
				child.x(0D);
			} else if (this.align.isCenter()) {
				child.x(super.dw(2D) - child.dw(2D));
			} else if (this.align.isEnd()) {
				child.x(super.aw(-child.getWidth()));
			}
		} else if (this.align.isStart()) {
			child.y(0D);
		} else if (this.align.isCenter()) {
			child.y(super.dh(2D) - child.dh(2D));
		} else if (this.align.isEnd()) {
			child.y(super.ah(-child.getHeight()));
		}
	}

	private void syncOrder() {
		this.logicalOrder.removeIf(child -> child != this.reorderedNode && !super.getChildren().contains(child));
		for (final Node child : super.getChildren().ordered()) {
			if (!this.logicalOrder.contains(child)) {
				this.logicalOrder.add(child);
			}
		}
		this.currentIndex = this.logicalOrder.indexOf(this.reorderedNode);
	}

	private void applyReorder() {
		this.syncOrder();
		this.reorderedNode.zindex(this.draggedZindex);

		final List<Node> snapshot = new ArrayList<>(this.logicalOrder);
		super.getChildren().clear();
		for (final Node child : snapshot) {
			super.getChildren().add(child);
		}
	}

	private void autoScrollParent(final double mouseX, final double mouseY) {
		if (!this.scrollArmed) {
			final double dx = mouseX - this.dragStartMouseX;
			final double dy = mouseY - this.dragStartMouseY;
			if (Math.sqrt(dx * dx + dy * dy) < 5D) {
				return;
			}
			this.scrollArmed = true;
		}

		Node parent = super.getParent();
		while (parent != null) {
			if (parent.getOverflow() == OverflowProperty.SCROLL && (parent.hasOverflowX() || parent.hasOverflowY())) {
				if (this.direction == FlexDirection.COLUMN && parent.hasOverflowY()) {
					final double parentTop = parent.getAbsoluteY();
					final double parentBottom = parentTop + parent.getHeight();
					if (mouseY < parentTop + ReorderableFlexNode.SCROLL_HOT_ZONE) {
						final double depth = Math.min(1D, (parentTop + ReorderableFlexNode.SCROLL_HOT_ZONE - mouseY) / ReorderableFlexNode.SCROLL_HOT_ZONE);
						parent.scrollY(ReorderableFlexNode.SCROLL_SPEED_MAX * depth * depth, 1D);
					} else if (mouseY > parentBottom - ReorderableFlexNode.SCROLL_HOT_ZONE) {
						final double depth = Math.min(1D, (mouseY - (parentBottom - ReorderableFlexNode.SCROLL_HOT_ZONE)) / ReorderableFlexNode.SCROLL_HOT_ZONE);
						parent.scrollY(-ReorderableFlexNode.SCROLL_SPEED_MAX * depth * depth, 1D);
					}
				} else if (this.direction == FlexDirection.ROW && parent.hasOverflowX()) {
					final double parentLeft = parent.getAbsoluteX();
					final double parentRight = parentLeft + parent.getWidth();
					if (mouseX < parentLeft + ReorderableFlexNode.SCROLL_HOT_ZONE) {
						final double depth = Math.min(1D, (parentLeft + ReorderableFlexNode.SCROLL_HOT_ZONE - mouseX) / ReorderableFlexNode.SCROLL_HOT_ZONE);
						parent.scrollX(ReorderableFlexNode.SCROLL_SPEED_MAX * depth * depth, 1D);
					} else if (mouseX > parentRight - ReorderableFlexNode.SCROLL_HOT_ZONE) {
						final double depth = Math.min(1D, (mouseX - (parentRight - ReorderableFlexNode.SCROLL_HOT_ZONE)) / ReorderableFlexNode.SCROLL_HOT_ZONE);
						parent.scrollX(-ReorderableFlexNode.SCROLL_SPEED_MAX * depth * depth, 1D);
					}
				}
				return;
			}
			parent = parent.getParent();
		}
	}

}