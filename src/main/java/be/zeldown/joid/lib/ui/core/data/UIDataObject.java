package be.zeldown.joid.lib.ui.core.data;

import java.lang.annotation.Annotation;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopup;
import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopupObject;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataObject implements UIData {

	/* [ Default Section ] */
	private UIDataPopupObject popup = new UIDataPopupObject();
	private boolean active          = true;
	private boolean visible         = true;
	private boolean background      = false;
	private boolean hotreload       = true;
	private boolean profiler        = true;
	private String  backgroundColor = "#101010C0";
	private int     zindex          = 0;
	private double  zlevel          = 0D;
	private Align   anchorX         = Align.CENTER;
	private Align   anchorY         = Align.CENTER;

	/* [ Cache Section ] */
	private Color backgroundColorCache = Color.decode(this.backgroundColor);

	public UIDataObject(final @NonNull UIData data) {
		this.popup           = new UIDataPopupObject(data.popup());
		this.active          = data.active();
		this.visible         = data.visible();
		this.background      = data.background();
		this.backgroundColor = data.backgroundColor();
		this.hotreload       = data.hotreload();
		this.profiler        = data.profiler();
		this.zindex          = data.zindex();
		this.zlevel          = data.zlevel();
		this.anchorX         = data.anchorX();
		this.anchorY         = data.anchorY();

		this.backgroundColorCache = Color.decode(this.backgroundColor);
	}

	/* [ Annotation Section ] */
	@Override
	public Class<? extends Annotation> annotationType() {
		return UIData.class;
	}

	@Override
	public UIDataPopup popup() {
		return this.popup;
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
	public boolean background() {
		return this.background;
	}

	@Override
	public String backgroundColor() {
		return this.backgroundColor;
	}

	@Override
	public boolean hotreload() {
		return this.hotreload;
	}

	@Override
	public boolean profiler() {
		return this.profiler;
	}

	@Override
	public int zindex() {
		return this.zindex;
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
	 * Define if the UI should be opened as an popup
	 * @param popup
	 * @return
	 */
	public final @NonNull UIDataObject setPopup(final @NonNull UIDataPopupObject popup) {
		this.popup = popup;
		return this;
	}

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
	 * Define if a default gray background should be drawn
	 * default: false
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
	 * Define if the UI should be reloaded when the code is modified default: true
	 * default: true
	 * @param hotreload
	 * @return this
	 */
	public final @NonNull UIDataObject setHotreload(final boolean hotreload) {
		this.hotreload = hotreload;
		return this;
	}

	/**
	 * Define if the profiler should be activated in debug mode
	 * default: true
	 * @param profiler
	 * @return this
	 */
	public final @NonNull UIDataObject setProfiler(final boolean profiler) {
		this.profiler = profiler;
		return this;
	}

	/**
	 * Define the Z index of the UI default: 0
	 *
	 * @param zlevel
	 * @return this
	 */
	public final @NonNull UIDataObject setZindex(final int zindex) {
		this.zindex = zindex;
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
	public final @NonNull UIDataObject setAnchorX(final Align anchorX) {
		this.anchorX = anchorX;
		return this;
	}

	/**
	 * Define the Y anchor of the UI default: 1080 / 2
	 *
	 * @param anchorY
	 * @return this
	 */
	public final @NonNull UIDataObject setAnchorY(final Align anchorY) {
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
