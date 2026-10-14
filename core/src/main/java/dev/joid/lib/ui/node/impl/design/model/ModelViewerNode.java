package dev.joid.lib.ui.node.impl.design.model;

import java.util.function.Supplier;

import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.impl.node.NodeChangeEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseEvent;
import dev.joid.lib.ui.event.impl.node.NodeMouseScrollEvent;
import dev.joid.lib.utils.point.Point;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ModelViewerNode extends BaseModelNode {

	private static final EventType<NodeChangeEvent.Post<?, Double>> CHANGE        = EventType.create("onChange");
	private static final EventType<NodeChangeEvent.Pre<?, Double>>  BEFORE_CHANGE = EventType.create("beforeChange");

	private static final EventType<NodeChangeEvent.Post<?, Point>> ROTATE        = EventType.create("onRotate");
	private static final EventType<NodeChangeEvent.Pre<?, Point>>  BEFORE_ROTATE = EventType.create("beforeRotate");

	private double zoom;
	private double rotationYaw;
	private double rotationPitch;

	private Supplier<Double> minZoom;
	private Supplier<Double> maxZoom;

	private Supplier<Double> minRotationYaw;
	private Supplier<Double> maxRotationYaw;

	private Supplier<Double> minRotationPitch;
	private Supplier<Double> maxRotationPitch;

	private boolean dragged;
	private double  draggedMouseX;
	private double  draggedMouseY;

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
			this.rotate(rotationYaw, rotationPitch, true);
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

		this.zoom(this.zoom + event.getNotchesY() / 25D, true);
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
		return super.start("zoom", zoom, value -> this.zoom(value, false));
	}

	public final <T extends ModelViewerNode> @NonNull T zoom(final @NonNull Signal<Double> zoom) {
		return super.bind("zoom", zoom, value -> this.zoom(value, false));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final double rotationYaw) {
		return super.start("rotationYaw", rotationYaw, value -> this.rotate(value, this.rotationPitch, false));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationYaw(final @NonNull Signal<Double> rotationYaw) {
		return super.bind("rotationYaw", rotationYaw, value -> this.rotate(value, this.rotationPitch, false));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final double rotationPitch) {
		return super.start("rotationPitch", rotationPitch, value -> this.rotate(this.rotationYaw, value, false));
	}

	public final <T extends ModelViewerNode> @NonNull T rotationPitch(final @NonNull Signal<Double> rotationPitch) {
		return super.bind("rotationPitch", rotationPitch, value -> this.rotate(this.rotationYaw, value, false));
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

	public final <T extends ModelViewerNode> @NonNull T change(final double zoom) {
		this.zoom(zoom, true);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T rotate(final double rotationYaw, final double rotationPitch) {
		this.rotate(rotationYaw, rotationPitch, true);
		return (T) this;
	}

	public final <T extends ModelViewerNode> @NonNull T beforeChange(final @NonNull EventListener<NodeChangeEvent.Pre<T, Double>> listener) {
		return super.listen(ModelViewerNode.BEFORE_CHANGE, listener);
	}

	public final <T extends ModelViewerNode> @NonNull T onChange(final @NonNull EventListener<NodeChangeEvent.Post<T, Double>> listener) {
		return super.listen(ModelViewerNode.CHANGE, listener);
	}

	public final <T extends ModelViewerNode> @NonNull T beforeRotate(final @NonNull EventListener<NodeChangeEvent.Pre<T, Point>> listener) {
		return super.listen(ModelViewerNode.BEFORE_ROTATE, listener);
	}

	public final <T extends ModelViewerNode> @NonNull T onRotate(final @NonNull EventListener<NodeChangeEvent.Post<T, Point>> listener) {
		return super.listen(ModelViewerNode.ROTATE, listener);
	}

	private void settle() {
		this.drawnZoom          = this.zoom;
		this.drawnRotationYaw   = this.rotationYaw;
		this.drawnRotationPitch = this.rotationPitch;
	}

	private void zoom(final double zoom, final boolean refusable) {
		final double value = Math.max(this.minZoom.get(), Math.min(this.maxZoom.get(), zoom));
		if (this.zoom == value) {
			super.sync("zoom", value);
			return;
		}

		super.change(ModelViewerNode.BEFORE_CHANGE, ModelViewerNode.CHANGE, this.zoom, value, refusable, () -> {
			this.zoom = value;
			super.sync("zoom", value);
		});
	}

	private void rotate(final double rotationYaw, final double rotationPitch, final boolean refusable) {
		final double yaw = Math.max(this.minRotationYaw.get(), Math.min(this.maxRotationYaw.get(), rotationYaw));
		final double pitch = Math.max(this.minRotationPitch.get(), Math.min(this.maxRotationPitch.get(), rotationPitch));
		if (this.rotationYaw == yaw && this.rotationPitch == pitch) {
			super.sync("rotationYaw", yaw);
			super.sync("rotationPitch", pitch);
			return;
		}

		super.change(ModelViewerNode.BEFORE_ROTATE, ModelViewerNode.ROTATE, Point.create(this.rotationYaw, this.rotationPitch), Point.create(yaw, pitch), refusable, () -> {
			this.rotationYaw   = yaw;
			this.rotationPitch = pitch;
			super.sync("rotationYaw", yaw);
			super.sync("rotationPitch", pitch);
		});
	}

}