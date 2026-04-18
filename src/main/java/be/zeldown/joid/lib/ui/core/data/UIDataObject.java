package be.zeldown.joid.lib.ui.core.data;

import java.lang.annotation.Annotation;
import java.util.Optional;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataObject implements UIData {

	/* [ Default Section ] */
	private boolean active          = true;
	private boolean visible         = true;
	private boolean pause           = true;
	private boolean closeable       = true;
	private boolean zoomable        = false;
	private boolean projection      = true;
	private boolean background      = true;
	private String  backgroundColor = "#101010C0";
	private double  zlevel          = 0D;
	private Align   anchorX         = Align.CENTER;
	private Align   anchorY         = Align.CENTER;

	/* [ Cache Section ] */
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

	public static @NonNull Optional<UIDataObject> get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIData data = currentClass.getAnnotation(UIData.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIData.class);
		}
		return data != null ? Optional.of(new UIDataObject(data)) : Optional.empty();
	}

	public static @NonNull UIDataObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		return UIDataObject.get(clazz).orElse(new UIDataObject());
	}

	/* [ Annotation Section ] */
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

	/* [ Setter Section ] */
	/**
	 * Define if the UI should be active
	 * default: true
	 * @param active
	 * @return this
	 */
	public final @NonNull UIDataObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	/**
	 * Define if the UI should be visible
	 * default: true
	 * @param visible
	 * @return this
	 */
	public final @NonNull UIDataObject setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	/**
	 * Define if the game should be paused when the UI is opened
	 * default: true
	 * @param pause
	 * @return this
	 */
	public final @NonNull UIDataObject setPause(final boolean pause) {
		this.pause = pause;
		return this;
	}

	public final @NonNull UIDataObject setCloseable(final boolean closeable) {
		this.closeable = closeable;
		return this;
	}

	/**
	 * Define if the UI should be zoomable default: false
	 *
	 * @param zoomable
	 * @return this
	 */
	public final @NonNull UIDataObject setZoomable(final boolean zoomable) {
		this.zoomable = zoomable;
		return this;
	}

	/**
	 * Define if the UI should be projected on the screen default: true
	 *
	 * @param projection
	 * @return this
	 */
	public final @NonNull UIDataObject setProjection(final boolean projection) {
		this.projection = projection;
		return this;
	}

	/**
	 * Define if a default gray background should be drawn
	 * default: true
	 * @param background
	 * @return this
	 */
	public final @NonNull UIDataObject setBackground(final boolean background) {
		this.background = background;
		return this;
	}

	/**
	 * Define the background color of the UI
	 * default: #101010c0
	 * @param color
	 * @return this
	 */
	public final @NonNull UIDataObject setBackgroundColor(final @NonNull String color) {
		this.backgroundColor = color;
		this.backgroundColorCache = Color.decode(color);
		return this;
	}

	/**
	 * Define the Z level of the UI default: 0
	 *
	 * @param zlevel
	 * @return this
	 */
	public final @NonNull UIDataObject setZlevel(final double zlevel) {
		this.zlevel = zlevel;
		return this;
	}

	/**
	 * Define the X anchor of the UI default: 1920 / 2
	 *
	 * @param anchorX
	 * @return this
	 */
	public final @NonNull UIDataObject setAnchorX(final @NonNull Align anchorX) {
		this.anchorX = anchorX;
		return this;
	}

	/**
	 * Define the Y anchor of the UI default: 1080 / 2
	 *
	 * @param anchorY
	 * @return this
	 */
	public final @NonNull UIDataObject setAnchorY(final @NonNull Align anchorY) {
		this.anchorY = anchorY;
		return this;
	}

	/* [ Getter Section ] */
	/**
	 * Get the cached background color of the UI
	 * @return the background color
	 */
	public final @NonNull Color getBackgroundColor() {
		return this.backgroundColorCache;
	}

}
