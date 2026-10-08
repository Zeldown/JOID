package dev.joid.lib.ui.core.data.overlay;

import java.lang.annotation.Annotation;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.overlay.interaction.UIDataOverlayInteractionObject;
import dev.joid.lib.ui.core.data.overlay.render.UIDataOverlayRenderObject;
import lombok.NonNull;
import lombok.ToString;

@ToString
public final class UIDataOverlayObject implements UIDataOverlay {

	private final UIDataOverlayRenderObject      render;
	private final UIDataOverlayInteractionObject interaction;

	private boolean active;

	public UIDataOverlayObject() {
		this.render      = new UIDataOverlayRenderObject();
		this.interaction = new UIDataOverlayInteractionObject();
		this.active      = false;
	}

	public UIDataOverlayObject(final @NonNull UIDataOverlay data) {
		this.render      = new UIDataOverlayRenderObject(data.render());
		this.interaction = new UIDataOverlayInteractionObject(data.interaction());
		this.active      = data.active();
	}

	public static @NonNull UIDataOverlayObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		final UIDataOverlayObject data = UIDataOverlayObject.get(clazz);
		return data != null ? data : new UIDataOverlayObject();
	}

	public static UIDataOverlayObject get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIDataOverlay data = currentClass.getAnnotation(UIDataOverlay.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIDataOverlay.class);
		}
		return data != null ? new UIDataOverlayObject(data) : null;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataOverlay.class;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public @NonNull UIDataOverlayRenderObject render() {
		return this.render;
	}

	@Override
	public @NonNull UIDataOverlayInteractionObject interaction() {
		return this.interaction;
	}

	public @NonNull UIDataOverlayObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	public @NonNull UIDataOverlayObject update(final @NonNull UIDataOverlay previous, final @NonNull UIDataOverlay next) {
		if (previous.active() != next.active()) {
			this.setActive(next.active());
		}

		this.render.update(previous.render(), next.render());
		this.interaction.update(previous.interaction(), next.interaction());
		return this;
	}

}