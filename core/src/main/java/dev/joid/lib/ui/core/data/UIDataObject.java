package dev.joid.lib.ui.core.data;

import java.lang.annotation.Annotation;
import java.util.Optional;

import dev.joid.lib.color.Color;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.utils.align.Align;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataObject implements UIData {

	private boolean pause           = true;
	private double  zlevel          = 0D;
	private Align   anchorX         = Align.CENTER;
	private Align   anchorY         = Align.CENTER;
	private boolean active          = true;
	private boolean visible         = true;
	private boolean zoomable        = true;
	private boolean closeable       = true;
	private boolean projection      = true;
	private boolean background      = true;
	private String  backgroundColor = "#101010C0";

	private Color backgroundColorCache = Color.decode(this.backgroundColor);

	public UIDataObject(final @NonNull UIData data) {
		this.active          = data.active();
		this.visible         = data.visible();
		this.pause           = data.pause();
		this.closeable       = data.closeable();
		this.zoomable        = data.zoomable();
		this.projection      = data.projection();
		this.background      = data.background();
		this.backgroundColor = data.backgroundColor();
		this.zlevel          = data.zlevel();
		this.anchorX         = data.anchorX();
		this.anchorY         = data.anchorY();

		this.backgroundColorCache = Color.decode(this.backgroundColor);
	}

	public static @NonNull UIDataObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		return UIDataObject.get(clazz).orElse(new UIDataObject());
	}

	public double getAnchorPositionX() {
		switch (this.anchorX) {
		case START:
			return 0D;
		case CENTER:
			return 1920D / 2D;
		case END:
			return 1920D;
		default:
			return 0D;
		}
	}

	public double getAnchorPositionY() {
		switch (this.anchorY) {
		case START:
			return 0D;
		case CENTER:
			return 1080D / 2D;
		case END:
			return 1080D;
		default:
			return 0D;
		}
	}

	public final @NonNull Color getBackgroundColor() {
		return this.backgroundColorCache;
	}

	public static @NonNull Optional<UIDataObject> get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIData data = currentClass.getAnnotation(UIData.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIData.class);
		}
		return data != null ? Optional.of(new UIDataObject(data)) : Optional.empty();
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIData.class;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public boolean visible() {
		return this.visible;
	}

	@Override
	public boolean pause() {
		return this.pause;
	}

	@Override
	public boolean closeable() {
		return this.closeable;
	}

	@Override
	public boolean projection() {
		return this.projection;
	}

	@Override
	public boolean background() {
		return this.background;
	}

	@Override
	public String backgroundColor() {
		return this.backgroundColor;
	}

	@Override
	public boolean zoomable() {
		return this.zoomable;
	}

	@Override
	public double zlevel() {
		return this.zlevel;
	}

	@Override
	public Align anchorX() {
		return this.anchorX;
	}

	@Override
	public Align anchorY() {
		return this.anchorY;
	}

	public final @NonNull UIDataObject setPause(final boolean pause) {
		this.pause = pause;
		return this;
	}

	public final @NonNull UIDataObject setZlevel(final double zlevel) {
		this.zlevel = zlevel;
		return this;
	}

	public final @NonNull UIDataObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	public final @NonNull UIDataObject setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	public final @NonNull UIDataObject setZoomable(final boolean zoomable) {
		this.zoomable = zoomable;
		return this;
	}

	public final @NonNull UIDataObject setCloseable(final boolean closeable) {
		this.closeable = closeable;
		return this;
	}

	public final @NonNull UIDataObject setBackground(final boolean background) {
		this.background = background;
		return this;
	}

	public final @NonNull UIDataObject setProjection(final boolean projection) {
		this.projection = projection;
		return this;
	}

	public final @NonNull UIDataObject setAnchorX(final @NonNull Align anchorX) {
		this.anchorX = anchorX;
		return this;
	}

	public final @NonNull UIDataObject setAnchorY(final @NonNull Align anchorY) {
		this.anchorY = anchorY;
		return this;
	}

	public final @NonNull UIDataObject setBackgroundColor(final @NonNull String color) {
		this.backgroundColor = color;
		this.backgroundColorCache = Color.decode(color);
		return this;
	}

}