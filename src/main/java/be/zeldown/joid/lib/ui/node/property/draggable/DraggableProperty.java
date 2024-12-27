package be.zeldown.joid.lib.ui.node.property.draggable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class DraggableProperty {

	private Predicate<Node>   enabled;

	private DraggableAreaType areaType;
	private Object            areaObject;

	private DraggableSnapType snapType;
	private List<Node>        snapNodes;

	private DraggableProperty() {
		this.enabled    = node -> true;
		this.areaType   = DraggableAreaType.FREE;
		this.areaObject = null;
		this.snapType   = DraggableSnapType.NEAREST;
		this.snapNodes  = null;
	}

	public static final DraggableProperty free() { return new DraggableProperty(); }
	public static final DraggableProperty custom(final double x, final double y, final double width, final double height) { return new DraggableProperty().area(DraggableAreaType.CUSTOM, new double[] {x, y, width, height}); }
	public static final DraggableProperty parent() { return new DraggableProperty().area(DraggableAreaType.PARENT); }
	public static final DraggableProperty node(final @NonNull Node node) { return new DraggableProperty().area(DraggableAreaType.NODE, node); }
	public static final DraggableProperty ui() { return new DraggableProperty().area(DraggableAreaType.UI); }
	public static final DraggableProperty screen() { return new DraggableProperty().area(DraggableAreaType.SCREEN); }
	public static final DraggableProperty disabled() { return new DraggableProperty().enabled(node -> false); }

	/* [ Worker Section ] */
	public final double[] getBounds(final @NonNull Node node) {
		double boundX = 0;
		double boundY = 0;
		double boundWidth = 0;
		double boundHeight = 0;

		switch (this.areaType) {
		case CUSTOM:
			final double[] area = (double[]) this.areaObject;
			boundX = area[0];
			boundY = area[1];
			boundWidth = area[2];
			boundHeight = area[3];
			break;
		case PARENT:
			boundX = node.getParent().getAbsoluteX();
			boundY = node.getParent().getAbsoluteY();
			boundWidth = node.getParent().getWidth();
			boundHeight = node.getParent().getHeight();
			break;
		case NODE:
			final Node areaNode = (Node) this.areaObject;
			boundX = areaNode.getAbsoluteX();
			boundY = areaNode.getAbsoluteY();
			boundWidth = areaNode.getWidth();
			boundHeight = areaNode.getHeight();
			break;
		case UI:
			boundX = 0;
			boundY = 0;
			boundWidth = 1920;
			boundHeight = 1080;
			break;
		case SCREEN:
			final UI ui = node.getUi();
			boundX = ui.getRelativeX(0);
			boundY = ui.getRelativeY(0);
			boundWidth = ui.getRelativeX(ui.getWidth() * (ui.getViewportWidth() / ui.getWidth())) - ui.getRelativeX(0);
			boundHeight = ui.getRelativeY(ui.getHeight() * (ui.getViewportHeight() / ui.getHeight())) - ui.getRelativeY(0);
			break;
		default:
			throw new IllegalArgumentException("Invalid DraggableAreaType: " + this.areaType);
		}

		return new double[] {boundX, boundY, boundWidth, boundHeight};
	}

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

	public double lerp(final double fps, double value, final double target) {
		final double diff = target - value;
		final double absDiff = Math.abs(diff);
		final double offset = 0.5D / ((fps == 0D ? 1D : fps) / 60D) * absDiff / 3D;
		if (absDiff > 0.5D) {
			value += diff > 0.0D ? offset : -offset;
		} else {
			value = target;
		}
		return value;
	}

	/* [ Setter Section ] */
	public final @NonNull DraggableProperty enabled(final @NonNull Predicate<@NonNull Node> enabled) {
		this.enabled = enabled;
		return this;
	}

	public final @NonNull DraggableProperty area(final @NonNull DraggableAreaType areaType) {
		this.areaType = areaType;
		return this;
	}

	public final @NonNull DraggableProperty area(final @NonNull DraggableAreaType areaType, final Object area) {
		this.areaType = areaType;
		this.areaObject = area;

		if (areaType == DraggableAreaType.NODE && !(area instanceof Node)) {
			throw new IllegalArgumentException("Invalid area object for DraggableAreaType.NODE: " + area);
		}

		if (areaType == DraggableAreaType.CUSTOM && !(area instanceof double[])) {
			throw new IllegalArgumentException("Invalid area object for DraggableAreaType.CUSTOM: " + area);
		}

		return this;
	}

	public final @NonNull DraggableProperty snap(final @NonNull DraggableSnapType snapType, final Node... snapNodes) {
		this.snapType = snapType;
		if (snapNodes != null && snapNodes.length > 0) {
			this.snapNodes = new ArrayList<>(Arrays.asList(snapNodes));
		} else {
			this.snapNodes = null;
		}
		return this;
	}

	public final @NonNull DraggableProperty snap(final @NonNull Node @NonNull... snapNodes) {
		if (this.snapNodes == null) {
			this.snapNodes = new ArrayList<>(Arrays.asList(snapNodes));
		} else {
			this.snapNodes.addAll(new ArrayList<>(Arrays.asList(snapNodes)));
		}
		return this;
	}

	/* [ Getter Section ] */
	public final boolean isEnabled(final @NonNull Node node) {
		return this.enabled.test(node);
	}

	public final boolean hasSnapping() {
		return this.snapNodes != null && !this.snapNodes.isEmpty();
	}

	public final @NonNull DraggableProperty copy() {
		return new DraggableProperty().enabled(this.enabled).area(this.areaType, this.areaObject).snap(this.snapType, this.snapNodes == null ? null : this.snapNodes.toArray(new Node[0]));
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