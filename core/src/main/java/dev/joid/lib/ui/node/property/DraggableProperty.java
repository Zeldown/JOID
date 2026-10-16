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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class DraggableProperty {

	private DraggableType     type;
	private Predicate<Node>   enabled;

	private Node              areaNode;
	private DraggableAreaType areaType;

	@Getter(AccessLevel.NONE)
	private double[] area;

	private List<Node>        snapNodes;
	private DraggableSnapType snapType;

	private DraggableProperty(final DraggableAreaType areaType, final double[] area, final Node areaNode) {
		this.enabled   = node -> true;
		this.type      = DraggableType.MOVE;
		this.areaType  = areaType;
		this.area      = area;
		this.areaNode  = areaNode;
		this.snapType  = DraggableSnapType.NEAREST;
		this.snapNodes = null;
	}

	public static @NonNull DraggableProperty free() { return new DraggableProperty(DraggableAreaType.FREE, null, null); }

	public static @NonNull DraggableProperty custom(final double x, final double y, final double width, final double height) { return new DraggableProperty(DraggableAreaType.CUSTOM, new double[] {x, y, width, height}, null); }

	public static @NonNull DraggableProperty parent() { return new DraggableProperty(DraggableAreaType.PARENT, null, null); }

	public static @NonNull DraggableProperty node(final @NonNull Node node) { return new DraggableProperty(DraggableAreaType.NODE, null, node); }

	public static @NonNull DraggableProperty ui() { return new DraggableProperty(DraggableAreaType.UI, null, null); }

	public static @NonNull DraggableProperty screen() { return new DraggableProperty(DraggableAreaType.SCREEN, null, null); }

	public static @NonNull DraggableProperty disabled() { return DraggableProperty.free().enabled(node -> false); }

	public Node getSnapping(final @NonNull Node node) {
		if (!this.hasSnapping()) {
			return null;
		}

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

	public double[] getBounds(final @NonNull Node node) {
		double boundX = 0D;
		double boundY = 0D;
		double boundWidth = 0D;
		double boundHeight = 0D;

		switch (this.areaType) {
		case CUSTOM:
			boundX = this.area[0];
			boundY = this.area[1];
			boundWidth = this.area[2];
			boundHeight = this.area[3];
			break;
		case PARENT:
			if (node.getParent() == null) {
				throw new IllegalStateException("The node " + JOIDLog.nameOf(node.getClass()) + " is dragged inside its parent but sits at the top of its UI, attach it to a node or pick another area such as DraggableProperty.ui()");
			}
			boundX = node.getParent().getAbsoluteX();
			boundY = node.getParent().getAbsoluteY();
			boundWidth = node.getParent().getWidth();
			boundHeight = node.getParent().getHeight();
			break;
		case NODE:
			boundX = this.areaNode.getAbsoluteX();
			boundY = this.areaNode.getAbsoluteY();
			boundWidth = this.areaNode.getWidth();
			boundHeight = this.areaNode.getHeight();
			break;
		case UI:
			boundX = 0D;
			boundY = 0D;
			boundWidth = UIView.DESIGN_WIDTH;
			boundHeight = UIView.DESIGN_HEIGHT;
			break;
		case SCREEN:
			final UI ui = node.getUi();
			boundX = ui.getView().toUiX(0D);
			boundY = ui.getView().toUiY(0D);
			boundWidth = ui.getView().toUiX(ui.getWindowWidth()) - boundX;
			boundHeight = ui.getView().toUiY(ui.getWindowHeight()) - boundY;
			break;
		case FREE:
			throw new IllegalStateException("A free draggable area has no bounds, check getAreaType() before calling getBounds() or pick a bounded area such as DraggableProperty.parent()");
		}

		return new double[] {boundX, boundY, boundWidth, boundHeight};
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
		if (this.snapNodes == null) {
			this.snapNodes = new ArrayList<>();
		}

		this.snapNodes.addAll(Arrays.asList(snapNodes));
		return this;
	}

	public @NonNull DraggableProperty snapType(final @NonNull DraggableSnapType snapType) {
		this.snapType = snapType;
		return this;
	}

	public boolean isEnabled(final @NonNull Node node) {
		return this.enabled.test(node);
	}

	public List<Node> getSnapNodes() {
		return this.snapNodes == null ? null : Collections.unmodifiableList(this.snapNodes);
	}

	public boolean hasSnapping() {
		return this.snapNodes != null && !this.snapNodes.isEmpty();
	}

	public @NonNull DraggableProperty copy() {
		final DraggableProperty copy = new DraggableProperty(this.areaType, this.area, this.areaNode).type(this.type).enabled(this.enabled).snapType(this.snapType);
		return copy.snap(this.snapNodes == null ? new Node[0] : this.snapNodes.toArray(new Node[0]));
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