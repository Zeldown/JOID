package dev.joid.lib.ui.core.data.overlay.render;

import java.lang.annotation.Annotation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataOverlayRenderObject implements UIDataOverlayRender {

	private boolean always       = false;
	private boolean aboveScreens = false;

	public UIDataOverlayRenderObject(final @NonNull UIDataOverlayRender data) {
		this.always       = data.always();
		this.aboveScreens = data.aboveScreens();
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
	public boolean aboveScreens() {
		return this.aboveScreens;
	}

	public @NonNull UIDataOverlayRenderObject setAlways(final boolean always) {
		this.always = always;
		return this;
	}

	public @NonNull UIDataOverlayRenderObject setAboveScreens(final boolean aboveScreens) {
		this.aboveScreens = aboveScreens;
		return this;
	}

	public @NonNull UIDataOverlayRenderObject update(final @NonNull UIDataOverlayRender previous, final @NonNull UIDataOverlayRender next) {
		if (previous.always() != next.always()) {
			this.setAlways(next.always());
		}

		if (previous.aboveScreens() != next.aboveScreens()) {
			this.setAboveScreens(next.aboveScreens());
		}

		return this;
	}

}