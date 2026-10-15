package dev.joid.lib.ui.node.impl.design.model;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.binding.InteractiveValue;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.point.Point;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ModelViewerNode extends BaseModelNode {

	private static final EventType<NodeChangeEvent.Post<?, Double>> ZOOM        = EventType.create("onZoom");
	private static final EventType<NodeChangeEvent.Pre<?, Double>>  BEFORE_ZOOM = EventType.create("beforeZoom");

	private static final EventType<NodeChangeEvent.Post<?, Point>> ROTATE        = EventType.create("onRotate");
	private static final EventType<NodeChangeEvent.Pre<?, Point>>  BEFORE_ROTATE = EventType.create("beforeRotate");

	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Double> zoomValue;
	@Getter(AccessLevel.NONE) private final transient InteractiveValue<Point>  rotationValue;

	private double zoom;
	private double rotationYaw;
	private double rotationPitch;

	private Supplier<Double> minZoom;
	private Supplier<Double> maxZoom;

	private Supplier<Double> minRotationYaw;
	private Supplier<Double> maxRotationYaw;

	private Supplier<Double> minRotationPitch;
	private Supplier<Double> maxRotationPitch;

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

		this.minZoom = () -> 0.1D;
		this.maxZoom = () -> 2D;

		this.minRotationYaw = () -> -Double.MAX_VALUE;
		this.maxRotationYaw = () -> Double.MAX_VALUE;

		this.minRotationPitch = () -> -Double.MAX_VALUE;
		this.maxRotationPitch = () -> Double.MAX_VALUE;

		this.zoomValue = InteractiveValue.create(super.getBindings(), "zoom", () -> this.zoom, (final Double zoom) -> this.zoom = zoom)
		.clamp(zoom -> Math.max(this.minZoom.get(), Math.min(this.maxZoom.get(), zoom)))
		.events(ModelViewerNode.BEFORE_ZOOM, (previous, zoom) -> NodeChangeEvent.Pre.create(this, previous, zoom), ModelViewerNode.ZOOM, (previous, zoom) -> NodeChangeEvent.Post.create(this, previous, zoom));
		this.rotationValue = InteractiveValue.create(super.getBindings(), "rotation", () -> Point.create(this.rotationYaw, this.rotationPitch), this::turn)
		.clamp(rotation -> Point.create(Math.max(this.minRotationYaw.get(), Math.min(this.maxRotationYaw.get(), rotation.getX())), Math.max(this.minRotationPitch.get(), Math.min(this.maxRotationPitch.get(), rotation.getY()))))
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
		if (this.dragged) {
			final double rotationYaw = this.rotationYaw + (mouseX - this.draggedMouseX) / 5F;
			final double rotationPitch = this.rotationPitch - (mouseY - this.draggedMouseY) / 5F;
			this.draggedMouseX = mouseX;
			this.draggedMouseY = mouseY;
			this.rotationValue.change(Point.create(rotationYaw, rotationPitch));
		}

		final UI ui = super.getUi();
		this.drawnRotationYaw = this.drawnRotationYaw != this.rotationYaw ? ui.lerpByFramerate(this.drawnRotationYaw, this.rotationYaw, 0.1D, 0.1D, true) : this.drawnRotationYaw;
		this.drawnRotationPitch = this.drawnRotationPitch != this.rotationPitch ? ui.lerpByFramerate(this.drawnRotationPitch, this.rotationPitch, 0.1D, 0.1D, true) : this.drawnRotationPitch;
		this.drawnZoom = this.drawnZoom != this.zoom ? ui.lerpByFramerate(this.drawnZoom, this.zoom, 0.1D, 0D, true) : this.drawnZoom;
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
	public void detach() {
		this.dragged = false;
	}

	public final double getMinZoom() {
		return this.minZoom.get();
	}

	public final double getMaxZoom() {
		return this.maxZoom.get();
	}

	public final double getMinRotationYaw() {
		return this.minRotationYaw.get();
	}

	public final double getMaxRotationYaw() {
		return this.maxRotationYaw.get();
	}

	public final double getMinRotationPitch() {
		return this.minRotationPitch.get();
	}

	public final double getMaxRotationPitch() {
		return this.maxRotationPitch.get();
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final double zoom) {
		return this.zoomValue.start(zoom);
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final @NonNull Signal<Double> zoom) {
		return this.zoomValue.bind(zoom);
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final double rotationYaw) {
		return super.start("rotationYaw", rotationYaw, value -> this.rotationValue.update(Point.create(value, this.rotationPitch)));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final @NonNull Signal<Double> rotationYaw) {
		return super.bind("rotationYaw", rotationYaw, value -> this.rotationValue.update(Point.create(value, this.rotationPitch)), Point::getX);
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final double rotationPitch) {
		return super.start("rotationPitch", rotationPitch, value -> this.rotationValue.update(Point.create(this.rotationYaw, value)));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final @NonNull Signal<Double> rotationPitch) {
		return super.bind("rotationPitch", rotationPitch, value -> this.rotationValue.update(Point.create(this.rotationYaw, value)), Point::getY);
	}

	public final <T extends ModelViewerNode> @NonNull T zoomRange(final double min, final double max) {
		return this.zoomRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T zoomRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		this.minZoom = min;
		this.maxZoom = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final double min, final double max) {
		return this.rotationYawRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYawRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		this.minRotationYaw = min;
		this.maxRotationYaw = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final double min, final double max) {
		return this.rotationPitchRange(Signal.from(min), Signal.from(max));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitchRange(final @NonNull Supplier<Double> min, final @NonNull Supplier<Double> max) {
		this.minRotationPitch = min;
		this.maxRotationPitch = max;
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T zoomTo(final double zoom) {
		this.zoomValue.change(zoom);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotate(final double rotationYaw, final double rotationPitch) {
		this.rotationValue.change(Point.create(rotationYaw, rotationPitch));
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

	private void turn(final @NonNull Point rotation) {
		this.rotationYaw   = rotation.getX();
		this.rotationPitch = rotation.getY();
		super.sync("rotationYaw", rotation);
		super.sync("rotationPitch", rotation);
	}

}