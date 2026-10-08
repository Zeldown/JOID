package dev.joid.lib.ui.core.data.overlay.interaction;

import java.lang.annotation.Annotation;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataOverlayInteractionObject implements UIDataOverlayInteraction {

	private boolean active         = false;
	private boolean cancelClick    = true;
	private boolean cancelScroll   = true;
	private boolean cancelKeyboard = true;

	public UIDataOverlayInteractionObject(final @NonNull UIDataOverlayInteraction data) {
		this.active         = data.active();
		this.cancelClick    = data.cancelClick();
		this.cancelScroll   = data.cancelScroll();
		this.cancelKeyboard = data.cancelKeyboard();
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataOverlayInteraction.class;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public boolean cancelClick() {
		return this.cancelClick;
	}

	@Override
	public boolean cancelScroll() {
		return this.cancelScroll;
	}

	@Override
	public boolean cancelKeyboard() {
		return this.cancelKeyboard;
	}

	public @NonNull UIDataOverlayInteractionObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	public @NonNull UIDataOverlayInteractionObject setCancelClick(final boolean cancelClick) {
		this.cancelClick = cancelClick;
		return this;
	}

	public @NonNull UIDataOverlayInteractionObject setCancelScroll(final boolean cancelScroll) {
		this.cancelScroll = cancelScroll;
		return this;
	}

	public @NonNull UIDataOverlayInteractionObject setCancelKeyboard(final boolean cancelKeyboard) {
		this.cancelKeyboard = cancelKeyboard;
		return this;
	}

	public @NonNull UIDataOverlayInteractionObject update(final @NonNull UIDataOverlayInteraction previous, final @NonNull UIDataOverlayInteraction next) {
		if (previous.active() != next.active()) {
			this.setActive(next.active());
		}

		if (previous.cancelClick() != next.cancelClick()) {
			this.setCancelClick(next.cancelClick());
		}

		if (previous.cancelScroll() != next.cancelScroll()) {
			this.setCancelScroll(next.cancelScroll());
		}

		if (previous.cancelKeyboard() != next.cancelKeyboard()) {
			this.setCancelKeyboard(next.cancelKeyboard());
		}

		return this;
	}

}