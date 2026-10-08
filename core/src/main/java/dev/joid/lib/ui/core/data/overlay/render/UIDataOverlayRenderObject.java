package dev.joid.lib.ui.core.data.overlay.render;

import java.lang.annotation.Annotation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataOverlayRenderObject implements UIDataOverlayRender {

	private boolean always  = false;
	private boolean screens = false;
	private int     zindex  = 0;

	public UIDataOverlayRenderObject(final @NonNull UIDataOverlayRender data) {
		this.always  = data.always();
		this.screens = data.screens();
		this.zindex  = data.zindex();
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataOverlayRender.class;
	}

	@Override
	public boolean always() {
		return this.always;
	}

	@Override
	public boolean screens() {
		return this.screens;
	}

	@Override
	public int zindex() {
		return this.zindex;
	}

	public @NonNull UIDataOverlayRenderObject setZindex(final int zindex) {
		this.zindex = zindex;
		return this;
	}

	public @NonNull UIDataOverlayRenderObject setAlways(final boolean always) {
		this.always = always;
		return this;
	}

	public @NonNull UIDataOverlayRenderObject setScreens(final boolean screens) {
		this.screens = screens;
		return this;
	}

	public @NonNull UIDataOverlayRenderObject update(final @NonNull UIDataOverlayRender previous, final @NonNull UIDataOverlayRender next) {
		if (previous.always() != next.always()) {
			this.setAlways(next.always());
		}

		if (previous.screens() != next.screens()) {
			this.setScreens(next.screens());
		}

		if (previous.zindex() != next.zindex()) {
			this.setZindex(next.zindex());
		}

		return this;
	}

}