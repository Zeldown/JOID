package be.zeldown.joid.lib.ui.node.impl.structure.reorderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode.FlexDirection;
import be.zeldown.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderCallback;
import be.zeldown.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderEndCallback;
import be.zeldown.joid.lib.ui.node.impl.structure.reorderable.callback.NodeReorderStartCallback;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.utils.align.Align;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class ReorderableFlexNode extends Node {

	public static final int CALLBACK_REORDER       = NodeCallbackRegistry.next(NodeReorderCallback.class);
	public static final int CALLBACK_REORDER_END   = NodeCallbackRegistry.next(NodeReorderEndCallback.class);
	public static final int CALLBACK_REORDER_START = NodeCallbackRegistry.next(NodeReorderStartCallback.class);

	private static final double SCROLL_HOT_ZONE      = 60D;
	private static final double SCROLL_SPEED_MAX     = 2D;
	private static final double SCROLL_ARM_THRESHOLD = 5D;
	private static final double LERP_SNAP            = 0.5D;
	private static final double LERP_SPEED           = 0.5D;

	private FlexDirection direction;
	private Align         align;
	private double        margin;
	private boolean       autoDrag = true;

	private boolean releasing;
	private boolean scrollArmed;
	private Node    draggedNode;
	private double  dragOffset;
	private double  draggedCurrent;
	private double  dragStartMouseX;
	private double  dragStartMouseY;
	private int     initialIndex;
	private int     currentIndex;

	private final Map<Node, Double> childCurrent = new HashMap<>();
	private final List<Node>        logicalOrder = new ArrayList<>();

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
		if (this.draggedNode != null) {
			this.tickDrag(mouseX, mouseY);
		}
		this.layout();
	}

	@Override
	public void drawSkeleton(final double mouseX, final double mouseY) {
		this.layout();
	}

	@Override
	public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (!this.autoDrag || !clickType.isLeft() || context.isCancelled() || this.draggedNode != null || !super.isEnabled()) {
			return;
		}

		for (final Node child : super.getChildren()) {
			if (child.isHovered(mouseX, mouseY)) {
				this.startDragInternal(child, mouseX, mouseY);
				context.cancel();
				return;
			}
		}
	}

	@Override
	public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {
		if (this.draggedNode != null && !this.releasing) {
			this.endDrag();
		}
	}

	public final @NonNull ReorderableFlexNode startDrag(final @NonNull Node child) {
		if (this.draggedNode != null) {
			return this;
		}

		this.getChildIndex(child);
		final UI ui = super.getUi();
		final double mouseX = ui != null ? ui.getMouseX() : 0D;
		final double mouseY = ui != null ? ui.getMouseY() : 0D;
		this.startDragInternal(child, mouseX, mouseY);
		return this;
	}

	public final @NonNull ReorderableFlexNode endDrag() {
		if (this.draggedNode == null || this.releasing) {
			return this;
		}

		this.releasing = true;
		return this;
	}

	public final @NonNull ReorderableFlexNode onReorderStart(final @NonNull NodeReorderStartCallback callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER_START, callback);
		return this;
	}

	public final @NonNull ReorderableFlexNode onReorder(final @NonNull NodeReorderCallback callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER, callback);
		return this;
	}

	public final @NonNull ReorderableFlexNode onReorderEnd(final @NonNull NodeReorderEndCallback callback) {
		super.registerCallback(ReorderableFlexNode.CALLBACK_REORDER_END, callback);
		return this;
	}

	public final @NonNull ReorderableFlexNode direction(final @NonNull FlexDirection direction) {
		this.direction = direction;
		return this;
	}

	public final @NonNull ReorderableFlexNode align(final Align align) {
		this.align = align;
		return this;
	}

	public final @NonNull ReorderableFlexNode margin(final double margin) {
		this.margin = margin;
		return this;
	}

	public final @NonNull ReorderableFlexNode auto(final boolean auto) {
		this.autoDrag = auto;
		return this;
	}

	public final int getChildIndex(final @NonNull Node child) {
		final int index = super.getChildren().ordered().indexOf(child);
		if (index == -1) {
			throw new IllegalArgumentException("Node is not a child of this ReorderableFlexNode");
		}
		return index;
	}

	public final boolean isDragging(final @NonNull Node child) {
		return this.draggedNode == child;
	}

	private void startDragInternal(final @NonNull Node child, final double mouseX, final double mouseY) {
		final int index = super.getChildren().ordered().indexOf(child);
		if (index == -1) {
			return;
		}

		this.draggedNode = child;
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

		child.zindex(Integer.MAX_VALUE);
		super.getChildren().remove(child);
		super.getChildren().add(child);

		child.fireDragStart(null);
		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER_START, InternalContext.create(), child);
	}

	private void tickDrag(final double mouseX, final double mouseY) {
		final Node dragged = this.draggedNode;
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

		this.draggedCurrent = super.getUi().lerpByFramerate(this.draggedCurrent, target, ReorderableFlexNode.LERP_SPEED, ReorderableFlexNode.LERP_SNAP, true);

		if (this.releasing) {
			if (this.draggedCurrent == target) {
				this.finishRelease();
			}
			return;
		}

		final double draggedCenter = this.draggedCurrent + draggedSize / 2D;
		int newIndex = 0;
		double off = 0D;
		for (final Node c : this.logicalOrder) {
			if (c == dragged) {
				continue;
			}

			final double cSize = this.mainSize(c);
			final double cCenter = off + cSize / 2D;
			if (cCenter < draggedCenter) {
				newIndex++;
			}
			if (c.isVisibleProperty()) {
				off += cSize + this.margin;
			}
		}

		if (newIndex != this.currentIndex) {
			this.logicalOrder.remove(dragged);
			this.logicalOrder.add(newIndex, dragged);
			this.currentIndex = newIndex;
		}

		this.autoScrollParent(mouseX, mouseY);

		dragged.fireDrag(null);
		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER, InternalContext.create(), dragged);
	}

	private double computeDraggedTarget() {
		double off = 0D;
		for (final Node c : this.logicalOrder) {
			if (c == this.draggedNode) {
				return off;
			}
			if (c.isVisibleProperty()) {
				off += this.mainSize(c) + this.margin;
			}
		}
		return off;
	}

	private void finishRelease() {
		final Node node = this.draggedNode;
		final int oldIndex = this.initialIndex;
		final int newIndex = this.currentIndex;

		this.applyReorder();
		node.fireDragEnd(null);

		this.draggedNode = null;
		this.releasing = false;
		this.logicalOrder.clear();
		this.childCurrent.clear();

		super.executeCallback(ReorderableFlexNode.CALLBACK_REORDER_END, InternalContext.create(), node, oldIndex, newIndex);
	}

	private void layout() {
		final boolean vertical = this.direction == FlexDirection.COLUMN;
		final boolean dragging = this.draggedNode != null;
		final List<Node> order = dragging ? this.logicalOrder : new ArrayList<>(super.getChildren().ordered());

		double off = 0D;
		for (final Node child : order) {
			final double size = this.mainSize(child);

			if (child == this.draggedNode) {
				this.applyMain(child, this.draggedCurrent);
				this.applyAlign(child);
				if (child.isVisibleProperty()) {
					off += size + this.margin;
				}
				continue;
			}

			if (dragging) {
				final double cur = this.childCurrent.getOrDefault(child, off);
				final double newPos = super.getUi().lerpByFramerate(cur, off, ReorderableFlexNode.LERP_SPEED, ReorderableFlexNode.LERP_SNAP, true);
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

	private void applyReorder() {
		for (int i = 0; i < this.logicalOrder.size(); i++) {
			this.logicalOrder.get(i).zindex(i);
		}

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
			if (Math.sqrt(dx * dx + dy * dy) < ReorderableFlexNode.SCROLL_ARM_THRESHOLD) {
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