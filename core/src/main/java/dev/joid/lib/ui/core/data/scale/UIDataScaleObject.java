package dev.joid.lib.ui.core.data.scale;

import java.lang.annotation.Annotation;

import dev.joid.lib.ui.core.UI;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataScaleObject implements UIDataScale {

	private boolean active  = true;
	private boolean limited = false;
	private double  limit   = 1D;

	public UIDataScaleObject(final @NonNull UIDataScale data) {
		this.active  = data.active();
		this.limited = data.limited();
		this.limit   = data.limit();
	}

	public static @NonNull UIDataScaleObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		final UIDataScaleObject data = UIDataScaleObject.get(clazz);
		return data != null ? data : new UIDataScaleObject();
	}

	public static UIDataScaleObject get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIDataScale data = currentClass.getAnnotation(UIDataScale.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIDataScale.class);
		}
		return data != null ? new UIDataScaleObject(data) : null;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataScale.class;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public boolean limited() {
		return this.limited;
	}

	@Override
	public double limit() {
		return this.limit;
	}

	public double apply(final double interfaceScale) {
		return this.limited ? Math.min(interfaceScale, this.limit) : interfaceScale;
	}

	public @NonNull UIDataScaleObject setLimit(final double limit) {
		this.limit = limit;
		return this;
	}

	public @NonNull UIDataScaleObject setActive(final boolean active) {
		this.active = active;
		return this;
	}

	public @NonNull UIDataScaleObject setLimited(final boolean limited) {
		this.limited = limited;
		return this;
	}

	public @NonNull UIDataScaleObject update(final @NonNull UIDataScale previous, final @NonNull UIDataScale next) {
		if (previous.active() != next.active()) {
			this.setActive(next.active());
		}

		if (previous.limited() != next.limited()) {
			this.setLimited(next.limited());
		}

		if (previous.limit() != next.limit()) {
			this.setLimit(next.limit());
		}

		return this;
	}

}