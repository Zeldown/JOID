package dev.joid.lib.ui.node.impl.model;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.geom.Point;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ModelViewerNode extends BaseModelNode {

	private static final EventType<NodeChangeEvent.Pre<?, Double>>  BEFORE_ZOOM = EventType.create("beforeZoom");
	private static final EventType<NodeChangeEvent.Post<?, Double>> ZOOM        = EventType.create("onZoom");

	private static final EventType<NodeChangeEvent.Pre<?, Point>>  BEFORE_ROTATE = EventType.create("beforeRotate");
	private static final EventType<NodeChangeEvent.Post<?, Point>> ROTATE        = EventType.create("onRotate");

	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Double> zoomValue;
	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Point>  rotationValue;

	private double zoom;
	private double rotationYaw;
	private double rotationPitch;

	private double minZoom;
	private double maxZoom;

	private double minRotationYaw;
	private double maxRotationYaw;

	private double minRotationPitch;
	private double maxRotationPitch;

	@Getter(AccessLevel.NONE) private boolean dragged;
	@Getter(AccessLevel.NONE) private double  draggedMouseX;
	@Getter(AccessLevel.NONE) private double  draggedMouseY;

	private double drawnZoom;
	private double drawnRotationYaw;
	private double drawnRotationPitch;

	protected ModelViewerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.zoom          = 1D;
		this.rotationYaw   = 0D;
		this.rotationPitch = 0D;

		this.minZoom = 0.1D;
		this.maxZoom = 2D;

		this.minRotationYaw = -Double.MAX_VALUE;
		this.maxRotationYaw = Double.MAX_VALUE;

		this.minRotationPitch = -Double.MAX_VALUE;
		this.maxRotationPitch = Double.MAX_VALUE;

		this.zoomValue = InteractiveValue.create(super.getBindings(), "zoom", () -> this.zoom, (final Double zoom) -> this.zoom = zoom)
		.clamp(zoom -> Math.max(this.minZoom, Math.min(this.maxZoom, zoom)))
		.events(ModelViewerNode.BEFORE_ZOOM, (previous, zoom) -> NodeChangeEvent.Pre.create(this, previous, zoom), ModelViewerNode.ZOOM, (previous, zoom) -> NodeChangeEvent.Post.create(this, previous, zoom));
		this.rotationValue = InteractiveValue.create(super.getBindings(), "rotation", () -> Point.of(this.rotationYaw, this.rotationPitch), this::turn)
		.clamp(rotation -> Point.of(Math.max(this.minRotationYaw, Math.min(this.maxRotationYaw, rotation.getX())), Math.max(this.minRotationPitch, Math.min(this.maxRotationPitch, rotation.getY()))))
		.events(ModelViewerNode.BEFORE_ROTATE, (previous, rotation) -> NodeChangeEvent.Pre.create(this, previous, rotation), ModelViewerNode.ROTATE, (previous, rotation) -> NodeChangeEvent.Post.create(this, previous, rotation));

		this.settle();
	}

	public static @NonNull ModelViewerNode create(final double x, final double y, final double width, final double height) {
		return new ModelViewerNode(x, y, width, height);
	}

	@Override
	public void init(final @NonNull UI ui) {
		this.settle();
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final UI ui = super.getUi();
		this.drawnRotationYaw = ui.lerpByFramerate(this.drawnRotationYaw, this.rotationYaw, 1D / 30D, 0.1D);
		this.drawnRotationPitch = ui.lerpByFramerate(this.drawnRotationPitch, this.rotationPitch, 1D / 30D, 0.1D);
		this.drawnZoom = ui.lerpByFramerate(this.drawnZoom, this.zoom, 1D / 30D, 0.001D);
		super.drawModel(this.drawnZoom, this.drawnRotationYaw, this.drawnRotationPitch);
	}

	@Override
	public void mouseScroll(final @NonNull NodeMouseScrollEvent<?> event) {
		if (event.isConsumed() || event.getNotchesY() == 0D || !super.isHovered()) {
			return;
		}

		this.zoomValue.set(this.zoomValue.get() + event.getNotchesY() / 25D);
		event.consume();
	}

	@Override
	public void mousePressed(final @NonNull NodeMouseEvent<?> event) {
		if (event.isConsumed() || !event.getButton().isLeft() || !super.isHovered()) {
			return;
		}

		this.dragged = true;
		this.draggedMouseX = event.getMouseX();
		this.draggedMouseY = event.getMouseY();
		event.consume();
	}

	@Override
	public void mouseReleased(final @NonNull NodeMouseEvent<?> event) {
		if (event.getButton().isLeft()) {
			this.dragged = false;
		}
	}

	@Override
	public void mouseDragged(final @NonNull NodeMouseEvent<?> event) {
		if (!this.dragged) {
			return;
		}

		final double rotationYaw = this.rotationYaw + (event.getMouseX() - this.draggedMouseX) / 5D;
		final double rotationPitch = this.rotationPitch - (event.getMouseY() - this.draggedMouseY) / 5D;
		this.draggedMouseX = event.getMouseX();
		this.draggedMouseY = event.getMouseY();
		this.rotationValue.set(Point.of(rotationYaw, rotationPitch));
	}

	@Override
	public void detach() {
		this.dragged = false;
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final double zoom) {
		return this.zoomValue.start(zoom);
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final @NonNull Signal<Double> zoom) {
		return this.zoomValue.bind(zoom);
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final double rotationYaw) {
		return super.start("rotationYaw", rotationYaw, value -> this.aim(Point.of(value, this.rotationValue.getRequested().getY())));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final @NonNull Signal<Double> rotationYaw) {
		return super.bind("rotationYaw", rotationYaw, value -> this.aim(Point.of(value, this.rotationValue.getRequested().getY())), Point::getX);
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final double rotationPitch) {
		return super.start("rotationPitch", rotationPitch, value -> this.aim(Point.of(this.rotationValue.getRequested().getX(), value)));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final @NonNull Signal<Double> rotationPitch) {
		return super.bind("rotationPitch", rotationPitch, value -> this.aim(Point.of(this.rotationValue.getRequested().getX(), value)), Point::getY);
	}

	public final <T extends ModelViewerNode> @NonNull T zoomRange(final double min, final double max) {
		return this.zoomRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T zoomRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		return super.follow("zoomRange", () -> Point.of(min.get(), max.get()), range -> {
			if (range.getX() > range.getY()) {
				throw new IllegalArgumentException("The min " + range.getX() + " of the zoom range is above its max " + range.getY() + ", pass a min of at most " + range.getY());
			}

			this.minZoom = range.getX();
			this.maxZoom = range.getY();
			this.zoomValue.reapply();
		});
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final double min, final double max) {
		return this.rotationYawRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		return super.follow("rotationYawRange", () -> Point.of(min.get(), max.get()), range -> {
			if (range.getX() > range.getY()) {
				throw new IllegalArgumentException("The min " + range.getX() + " of the rotation yaw range is above its max " + range.getY() + ", pass a min of at most " + range.getY());
			}

			this.minRotationYaw = range.getX();
			this.maxRotationYaw = range.getY();
			this.rotationValue.reapply();
		});
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final double min, final double max) {
		return this.rotationPitchRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		return super.follow("rotationPitchRange", () -> Point.of(min.get(), max.get()), range -> {
			if (range.getX() > range.getY()) {
				throw new IllegalArgumentException("The min " + range.getX() + " of the rotation pitch range is above its max " + range.getY() + ", pass a min of at most " + range.getY());
			}

			this.minRotationPitch = range.getX();
			this.maxRotationPitch = range.getY();
			this.rotationValue.reapply();
		});
	}

	public final <T extends ModelViewerNode> @NonNull T zoomTo(final double zoom) {
		this.zoomValue.change(zoom);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotate(final double rotationYaw, final double rotationPitch) {
		this.rotationValue.change(Point.of(rotationYaw, rotationPitch));
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T beforeZoom(final @NonNull EventListener<NodeChangeEvent.Pre<T, Double>> listener) {
		return this.zoomValue.before(listener);
	}

	public final <T extends ModelViewerNode> @NonNull T onZoom(final @NonNull EventListener<NodeChangeEvent.Post<T, Double>> listener) {
		return this.zoomValue.on(listener);
	}

	public final <T extends ModelViewerNode> @NonNull T beforeRotate(final @NonNull EventListener<NodeChangeEvent.Pre<T, Point>> listener) {
		return this.rotationValue.before(listener);
	}

	public final <T extends ModelViewerNode> @NonNull T onRotate(final @NonNull EventListener<NodeChangeEvent.Post<T, Point>> listener) {
		return this.rotationValue.on(listener);
	}

	private void settle() {
		this.drawnZoom          = this.zoom;
		this.drawnRotationYaw   = this.rotationYaw;
		this.drawnRotationPitch = this.rotationPitch;
	}

	private void aim(final Point rotation) {
		this.rotationValue.update(rotation);
		this.turn(Point.of(this.rotationYaw, this.rotationPitch));
	}

	private void turn(final Point rotation) {
		this.rotationYaw   = rotation.getX();
		this.rotationPitch = rotation.getY();
		Signal.batch(() -> {
			super.sync("rotationYaw", rotation);
			super.sync("rotationPitch", rotation);
		});
	}

}