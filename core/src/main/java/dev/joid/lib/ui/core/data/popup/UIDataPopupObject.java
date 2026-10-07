package dev.joid.lib.ui.core.data.popup;

import java.lang.annotation.Annotation;

import dev.joid.lib.ui.core.UI;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataPopupObject implements UIDataPopup {

	private boolean         active     = false;
	private PopupTransition transition = PopupTransition.IN_OUT;

	public UIDataPopupObject(final @NonNull UIDataPopup data) {
		this.active     = data.active();
		this.transition = data.transition();
	}

	public static @NonNull UIDataPopupObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		final UIDataPopupObject data = UIDataPopupObject.get(clazz);
		return data != null ? data : new UIDataPopupObject();
	}

	public static UIDataPopupObject get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIDataPopup data = currentClass.getAnnotation(UIDataPopup.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIDataPopup.class);
		}
		return data != null ? new UIDataPopupObject(data) : null;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataPopup.class;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public PopupTransition transition() {
		return this.transition;
	}

	public @NonNull UIDataPopupObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	public @NonNull UIDataPopupObject setTransition(final @NonNull PopupTransition transition) {
		this.transition = transition;
		return this;
	}

	public @NonNull UIDataPopupObject update(final @NonNull UIDataPopup previous, final @NonNull UIDataPopup next) {
		if (previous.active() != next.active()) {
			this.setActive(next.active());
		}

		if (previous.transition() != next.transition()) {
			this.setTransition(next.transition());
		}

		return this;
	}

}