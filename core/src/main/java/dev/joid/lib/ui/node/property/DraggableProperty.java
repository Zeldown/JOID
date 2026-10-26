package dev.joid.lib.ui.node.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.view.UIView;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.utils.geom.BoundingBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DraggableProperty {

	private final List<Node> snapNodes;

	private DraggableType     type;
	private Predicate<Node>   enabled;

	private Node              areaNode;
	private DraggableAreaType areaType;

	@Getter(AccessLevel.NONE)
	private BoundingBox area;

	private DraggableSnapType snapType;

	private DraggableProperty(final DraggableAreaType areaType, final BoundingBox area, final Node areaNode) {
		this.enabled   = node -> true;
		this.type      = DraggableType.MOVE;
		this.areaType  = areaType;
		this.area      = area;
		this.areaNode  = areaNode;
		this.snapType  = DraggableSnapType.NEAREST;
		this.snapNodes = new ArrayList<>();
	}

	public static @NonNull DraggableProperty free() { return new DraggableProperty(DraggableAreaType.FREE, null, null); }

	public static @NonNull DraggableProperty custom(final double x, final double y, final double width, final double height) { return new DraggableProperty(DraggableAreaType.CUSTOM, BoundingBox.create(x, y, width, height), null); }

	public static @NonNull DraggableProperty parent() { return new DraggableProperty(DraggableAreaType.PARENT, null, null); }

	public static @NonNull DraggableProperty node(final @NonNull Node node) { return new DraggableProperty(DraggableAreaType.NODE, null, node); }

	public static @NonNull DraggableProperty ui() { return new DraggableProperty(DraggableAreaType.UI, null, null); }

	public static @NonNull DraggableProperty screen() { return new DraggableProperty(DraggableAreaType.SCREEN, null, null); }

	public static @NonNull DraggableProperty disabled() { return DraggableProperty.free().enabled(node -> false); }

	public Node getSnapping(final @NonNull Node node) {
		final double x = node.getAbsoluteX();
		final double y = node.getAbsoluteY();
		final double width = node.getWidth();
		final double height = node.getHeight();

		if (this.snapType == DraggableSnapType.NEAREST) {
			Node nearest = null;
			double nearestDistance = Double.MAX_VALUE;

			for (final Node snapNode : this.snapNodes) {
				final double snapX = snapNode.getAbsoluteX() + snapNode.getWidth() / 2D;
				final double snapY = snapNode.getAbsoluteY() + snapNode.getHeight() / 2D;

				final double distance = Math.sqrt((snapX - (x + width / 2D)) * (snapX - (x + width / 2D)) + (snapY - (y + height / 2D)) * (snapY - (y + height / 2D)));
				if (distance < nearestDistance) {
					nearest = snapNode;
					nearestDistance = distance;
				}
			}

			return nearest;
		}

		if (this.snapType == DraggableSnapType.OVERLAP) {
			for (final Node snapNode : this.snapNodes) {
				final double snapX = snapNode.getAbsoluteX();
				final double snapY = snapNode.getAbsoluteY();
				final double snapWidth = snapNode.getWidth();
				final double snapHeight = snapNode.getHeight();

				if (x < snapX + snapWidth && x + width > snapX && y < snapY + snapHeight && y + height > snapY) {
					return snapNode;
				}
			}
		}

		return null;
	}

	public @NonNull List<@NonNull Node> getSnapNodes() {
		return Collections.unmodifiableList(this.snapNodes);
	}

	public @NonNull BoundingBox getBounds(final @NonNull Node node) {
		switch (this.areaType) {
		case CUSTOM:
			return this.area.copy();
		case PARENT:
			if (node.getParent() == null) {
				throw new IllegalStateException("The node " + JOIDLog.nameOf(node.getClass()) + " is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()");
			}
			return BoundingBox.create(node.getParent().getAbsoluteX(), node.getParent().getAbsoluteY(), node.getParent().getWidth(), node.getParent().getHeight());
		case NODE:
			return BoundingBox.create(this.areaNode.getAbsoluteX(), this.areaNode.getAbsoluteY(), this.areaNode.getWidth(), this.areaNode.getHeight());
		case UI:
			return BoundingBox.create(0D, 0D, UIView.DESIGN_WIDTH, UIView.DESIGN_HEIGHT);
		case SCREEN:
			final UI ui = node.getUi();
			final double x = ui.getView().toUiX(0D);
			final double y = ui.getView().toUiY(0D);
			return BoundingBox.create(x, y, ui.getView().toUiX(ui.getWindowWidth()) - x, ui.getView().toUiY(ui.getWindowHeight()) - y);
		default:
			throw new IllegalStateException("A free draggable area has no bounds, check getAreaType() before calling getBounds() or pick a bounded area such as DraggableProperty.parent()");
		}
	}

	public boolean hasSnapping() {
		return !this.snapNodes.isEmpty();
	}

	public boolean isEnabled(final @NonNull Node node) {
		return this.enabled.test(node);
	}

	public @NonNull DraggableProperty enabled(final @NonNull Predicate<@NonNull Node> enabled) {
		this.enabled = enabled;
		return this;
	}

	public @NonNull DraggableProperty type(final @NonNull DraggableType type) {
		this.type = type;
		return this;
	}

	public @NonNull DraggableProperty snap(final @NonNull Node @NonNull... snapNodes) {
		this.snapNodes.addAll(Arrays.asList(snapNodes));
		return this;
	}

	public @NonNull DraggableProperty snapType(final @NonNull DraggableSnapType snapType) {
		this.snapType = snapType;
		return this;
	}

	public @NonNull DraggableProperty copy() {
		final DraggableProperty copy = new DraggableProperty(this.areaType, this.area, this.areaNode).type(this.type).enabled(this.enabled).snapType(this.snapType);
		return copy.snap(this.snapNodes.toArray(new Node[0]));
	}

	public enum DraggableType {

		MOVE,
		COPY;

	}

	public enum DraggableAreaType {

		FREE,
		CUSTOM,
		PARENT,
		NODE,
		UI,
		SCREEN;

	}

	public enum DraggableSnapType {

		NEAREST,
		OVERLAP;

	}

}