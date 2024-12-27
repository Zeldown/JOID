package be.zeldown.joid.lib.ui.core.data.popup;

import java.lang.annotation.Annotation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataPopupObject implements UIDataPopup {

	/* [ Default Section ] */
	private boolean         active     = false;
	private PopupTransition transition = PopupTransition.IN_OUT;

	public UIDataPopupObject(final @NonNull UIDataPopup data) {
		this.active     = data.active();
		this.transition = data.transition();
	}

	/* [ Annotation Section ] */
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

	/* [ Setter Section ] */
	/**
	 * Define the active state of the overlay
	 * default: false
	 * @param active
	 * @return this
	 */
	public @NonNull UIDataPopupObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	/**
	 * Define the active state of the transition
	 * default: PopupTransition.IN_OUT
	 * @param transition
	 * @return this
	 */
	public @NonNull UIDataPopupObject setTransition(final @NonNull PopupTransition transition) {
		this.transition = transition;
		return this;
	}

}