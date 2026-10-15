package dev.joid.lib.ui.node.impl.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeReorderEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Axis;
import lombok.Getter;
import lombok.NonNull;

public class ReorderableFlexNode extends FlexNode {

	private static final EventType<NodeReorderEvent.Post<?>> REORDER        = EventType.create("onReorder");
	private static final EventType<NodeReorderEvent.Post<?>> REORDER_END    = EventType.create("onReorderEnd");
	private static final EventType<NodeReorderEvent.Pre<?>>  BEFORE_REORDER = EventType.create("beforeReorder");
	private static final EventType<NodeReorderEvent.Post<?>> REORDER_START  = EventType.create("onReorderStart");

	private final transient Set<Node>         pinnedNodes;
	private final transient List<Node>        logicalOrder;
	private final transient Map<Node, Double> childCurrent;

	@Getter private boolean autoDrag;

	@Getter private Node reorderedNode;

	private int     initialIndex;
	private int     currentIndex;
	private boolean releasing;
	private double  dragOffset;
	private boolean scrollArmed;
	private double  draggedCurrent;
	private double  dragStartMouseX;
	private double  dragStartMouseY;

	protected ReorderableFlexNode(final double x, final double y, final double width, final double height, final @NonNull Axis direction) {
		super(x, y, width, height, direction);

		this.pinnedNodes  = new HashSet<>();
		this.logicalOrder = new ArrayList<>();
		this.childCurrent = new HashMap<>();
		this.autoDrag     = true;
	}

	public static @NonNull ReorderableFlexNode vertical(final double x, final double y, final double width) {
		return new ReorderableFlexNode(x, y, width, 0D, Axis.VERTICAL);
	}

	public static @NonNull ReorderableFlexNode horizontal(final double x, final double y, final double height) {
		return new ReorderableFlexNode(x, y, 0D, height, Axis.HORIZONTAL);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.reorderedNode != null && super.getChildren().contains(this.reorderedNode)) {
			this.tickDrag(mouseX, mouseY);
		}
		super.draw(mouseX, mouseY);
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (!this.autoDrag || !event.getButton().isLeft() || event.isConsumed() || this.reorderedNode != null) {
			return;
		}

		for (final Node child : super.getChildren()) {
			if (child.isHovered()) {
				if (this.pinnedNodes.contains(child)) {
					return;
				}

				this.startDragInternal(child, event.getMouseX(), event.getMouseY());
				if (this.reorderedNode == child) {
					event.consume();
				}
				return;
			}
		}
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (this.reorderedNode != null && !this.releasing) {
			this.endDrag();
		}
	}

	@Override
	public void detach() {
		if (this.reorderedNode == null) {
			return;
		}

		this.finishReorder(super.getChildren().contains(this.reorderedNode));
	}

	@Override
	public @NonNull Node instantiate() {
		return new ReorderableFlexNode(super.getX(), super.getY(), super.getWidth(), super.getHeight(), super.getDirection());
	}

	@Override
	protected boolean isDraggingChild(final @NonNull Node child) {
		return this.isDragging(child);
	}

	@Override
	protected @NonNull List<@NonNull Node> getLayoutOrder() {
		if (this.reorderedNode != null && !super.getChildren().contains(this.reorderedNode)) {
			this.finishReorder(false);
		}

		if (this.reorderedNode == null) {
			return super.getLayoutOrder();
		}

		this.syncOrder();
		return this.logicalOrder;
	}

	@Override
	protected void layoutChild(final @NonNull Node child, final double offset) {
		if (this.reorderedNode == null) {
			super.layoutChild(child, offset);
			return;
		}

		if (child == this.reorderedNode) {
			super.layoutChild(child, this.draggedCurrent);
			return;
		}

		final double current = super.getUi().lerpByFramerate(this.childCurrent.getOrDefault(child, offset), offset, 0.5D, 0.5D, true);
		this.childCurrent.put(child, current);
		super.layoutChild(child, current);
	}

	public final <T extends ReorderableFlexNode> @NonNull T startDrag(final @NonNull Node child) {
		if (this.reorderedNode != null) {
			return (T) this;
		}

		this.getChildIndex(child);
		if (this.pinnedNodes.contains(child)) {
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

		final Node dragged = this.reorderedNode;
		if (!dragged.fireDragDrop(null, () -> this.releasing = true)) {
			final int previous = this.currentIndex;
			this.move(dragged, this.initialIndex);
			this.currentIndex = this.initialIndex;
			this.releasing = true;
			if (previous != this.initialIndex) {
				super.fire(ReorderableFlexNode.REORDER, () -> NodeReorderEvent.Post.create(this, dragged, previous, this.initialIndex));
			}
		}
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T pin(final @NonNull Node @NonNull... children) {
		for (final Node child : children) {
			this.pinnedNodes.add(child);
		}
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T unpin(final @NonNull Node @NonNull... children) {
		for (final Node child : children) {
			this.pinnedNodes.remove(child);
		}
		return (T) this;
	}

	public final <T extends ReorderableFlexNode> @NonNull T beforeReorder(final @NonNull EventListener<NodeReorderEvent.Pre<T>> listener) {
		return super.listen(ReorderableFlexNode.BEFORE_REORDER, listener);
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorder(final @NonNull EventListener<NodeReorderEvent.Post<T>> listener) {
		return super.listen(ReorderableFlexNode.REORDER, listener);
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorderStart(final @NonNull EventListener<NodeReorderEvent.Post<T>> listener) {
		return super.listen(ReorderableFlexNode.REORDER_START, listener);
	}

	public final <T extends ReorderableFlexNode> @NonNull T onReorderEnd(final @NonNull EventListener<NodeReorderEvent.Post<T>> listener) {
		return super.listen(ReorderableFlexNode.REORDER_END, listener);
	}

	public final <T extends ReorderableFlexNode> @NonNull T autoDrag(final boolean autoDrag) {
		return this.autoDrag(Signal.from(autoDrag));
	}

	public final <T extends ReorderableFlexNode> @NonNull T autoDrag(final @NonNull Supplier<Boolean> autoDrag) {
		return super.follow("autoDrag", autoDrag, value -> this.autoDrag = value);
	}

	public final @NonNull Set<Node> getPinnedNodes() {
		return Collections.unmodifiableSet(this.pinnedNodes);
	}

	public final @NonNull List<Node> getLogicalOrder() {
		return Collections.unmodifiableList(this.logicalOrder);
	}

	public final @NonNull Map<Node, Double> getChildCurrent() {
		return Collections.unmodifiableMap(this.childCurrent);
	}

	public final int getChildIndex(final @NonNull Node child) {
		final int index = (this.reorderedNode != null ? this.logicalOrder : super.getChildren()).indexOf(child);
		if (index == -1) {
			throw new IllegalArgumentException("The node " + child.getClass().getSimpleName() + " is not a child of this " + this.getClass().getSimpleName() + ", attach it to the flex first");
		}
		return index;
	}

	public final boolean isPinned(final @NonNull Node child) {
		return this.pinnedNodes.contains(child);
	}

	public final boolean isDragging(final @NonNull Node child) {
		return this.reorderedNode == child;
	}

	private void startDragInternal(final @NonNull Node child, final double mouseX, final double mouseY) {
		final int index = super.getChildren().indexOf(child);
		final boolean started = child.fireDragStart(() -> {
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
					offset += this.mainSize(c) + super.getMargin();
				}
			}

			this.draggedCurrent = this.childCurrent.get(child);
			if (super.getDirection() == Axis.VERTICAL) {
				this.dragOffset = mouseY - super.getAbsoluteY() - this.draggedCurrent;
			} else {
				this.dragOffset = mouseX - super.getAbsoluteX() - this.draggedCurrent;
			}
		});

		if (started) {
			super.fire(ReorderableFlexNode.REORDER_START, () -> NodeReorderEvent.Post.create(this, child, index, index));
		}
	}

	private void tickDrag(final double mouseX, final double mouseY) {
		final Node dragged = this.reorderedNode;
		final boolean vertical = super.getDirection() == Axis.VERTICAL;
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
				this.finishReorder(true);
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
			final double cMiddle = c.isVisibleProperty() ? off + (cSize + super.getMargin()) / 2D : off;
			if (cMiddle <= this.draggedCurrent) {
				newIndex++;
			}
			if (c.isVisibleProperty()) {
				off += cSize + super.getMargin();
			}
		}

		dragged.fireDrag(() -> this.autoScrollParent(mouseX, mouseY));
		final int index = this.freeSlot(newIndex);
		if (index == this.currentIndex) {
			return;
		}

		final int previous = this.currentIndex;
		final NodeReorderEvent.Pre<ReorderableFlexNode> event = super.fire(ReorderableFlexNode.BEFORE_REORDER, () -> NodeReorderEvent.Pre.create(this, dragged, previous, index));
		if (event != null && event.isCancelled()) {
			return;
		}

		this.move(dragged, index);
		this.currentIndex = index;
		super.fire(ReorderableFlexNode.REORDER, () -> NodeReorderEvent.Post.create(this, dragged, previous, index));
	}

	private int freeSlot(final int index) {
		final int step = index < this.currentIndex ? 1 : -1;
		int slot = index;
		while (slot != this.currentIndex && this.pinnedNodes.contains(this.logicalOrder.get(slot))) {
			slot += step;
		}
		return slot;
	}

	private void move(final @NonNull Node dragged, final int index) {
		final List<Node> free = new ArrayList<>();
		int position = 0;
		for (int i = 0; i < this.logicalOrder.size(); i++) {
			final Node child = this.logicalOrder.get(i);
			if (this.pinnedNodes.contains(child)) {
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
			if (!this.pinnedNodes.contains(this.logicalOrder.get(i))) {
				this.logicalOrder.set(i, free.get(next++));
			}
		}
	}

	private double computeDraggedTarget() {
		double off = 0D;
		for (final Node c : this.logicalOrder.subList(0, this.logicalOrder.indexOf(this.reorderedNode))) {
			if (c.isVisibleProperty()) {
				off += this.mainSize(c) + super.getMargin();
			}
		}
		return off;
	}

	private void finishReorder(final boolean kept) {
		final Node node = this.reorderedNode;
		final int oldIndex = this.initialIndex;
		final int newIndex = this.currentIndex;
		final boolean releasing = this.releasing;
		if (kept) {
			this.applyReorder();
		}

		this.reorderedNode = null;
		this.releasing = false;
		this.logicalOrder.clear();
		this.childCurrent.clear();

		if (!releasing) {
			node.fireDragDrop(null, () -> {});
		}
		super.fire(ReorderableFlexNode.REORDER_END, () -> NodeReorderEvent.Post.create(this, node, oldIndex, newIndex));
	}

	private double computeFullExtent() {
		double off = 0D;
		for (final Node child : super.getChildren()) {
			if (child.isVisibleProperty()) {
				off += this.mainSize(child) + super.getMargin();
			}
		}
		return Math.max(0D, off - super.getMargin());
	}

	private double mainSize(final @NonNull Node child) {
		return super.getDirection() == Axis.VERTICAL ? child.getHeight() : child.getWidth();
	}

	private void syncOrder() {
		this.logicalOrder.removeIf(child -> child != this.reorderedNode && !super.getChildren().contains(child));
		for (final Node child : super.getChildren()) {
			if (!this.logicalOrder.contains(child)) {
				this.logicalOrder.add(child);
			}
		}
		this.currentIndex = this.logicalOrder.indexOf(this.reorderedNode);
	}

	private void applyReorder() {
		this.syncOrder();

		super.reorderChildren(new ArrayList<>(this.logicalOrder));
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

		final double zone = 60D;
		final double speed = 2D;
		Node parent = super.getParent();
		while (parent != null) {
			if (parent.getOverflow() == OverflowProperty.SCROLL && (parent.hasOverflowX() || parent.hasOverflowY())) {
				if (super.getDirection() == Axis.VERTICAL && parent.hasOverflowY()) {
					final double parentTop = parent.getAbsoluteY();
					final double parentBottom = parentTop + parent.getHeight();
					if (mouseY < parentTop + zone) {
						final double depth = Math.min(1D, (parentTop + zone - mouseY) / zone);
						parent.scrollTo(parent.getTargetScrollX(), parent.getTargetScrollY() + speed * depth * depth);
					} else if (mouseY > parentBottom - zone) {
						final double depth = Math.min(1D, (mouseY - (parentBottom - zone)) / zone);
						parent.scrollTo(parent.getTargetScrollX(), parent.getTargetScrollY() - speed * depth * depth);
					}
				} else if (super.getDirection() == Axis.HORIZONTAL && parent.hasOverflowX()) {
					final double parentLeft = parent.getAbsoluteX();
					final double parentRight = parentLeft + parent.getWidth();
					if (mouseX < parentLeft + zone) {
						final double depth = Math.min(1D, (parentLeft + zone - mouseX) / zone);
						parent.scrollTo(parent.getTargetScrollX() + speed * depth * depth, parent.getTargetScrollY());
					} else if (mouseX > parentRight - zone) {
						final double depth = Math.min(1D, (mouseX - (parentRight - zone)) / zone);
						parent.scrollTo(parent.getTargetScrollX() - speed * depth * depth, parent.getTargetScrollY());
					}
				}
				return;
			}
			parent = parent.getParent();
		}
	}

}