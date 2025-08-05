package be.zeldown.joid.lib.ui.core.data.debug;

import java.lang.annotation.Annotation;
import java.util.Optional;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopup;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@ToString
@NoArgsConstructor
public final class UIDataDebugObject implements UIDataDebug {

	/* [ Default Section ] */
	private boolean profiler = true;
	private boolean hotreload = true;

	public UIDataDebugObject(final @NonNull UIDataDebug data) {
		this.profiler = data.profiler();
		this.hotreload = data.hotreload();
	}

	public static @NonNull Optional<UIDataDebugObject> get(final @NonNull Class<? extends UI> clazz) {
		Class<?> currentClass = clazz;
		UIDataDebug data = currentClass.getAnnotation(UIDataDebug.class);
		while (data == null && currentClass.getSuperclass() != null) {
			currentClass = currentClass.getSuperclass();
			data = currentClass.getAnnotation(UIDataDebug.class);
		}
		return data != null ? Optional.of(new UIDataDebugObject(data)) : Optional.empty();
	}

	public static @NonNull UIDataDebugObject getOrDefault(final @NonNull Class<? extends UI> clazz) {
		return UIDataDebugObject.get(clazz).orElse(new UIDataDebugObject());
	}

	/* [ Annotation Section ] */
	@Override
	public Class<? extends Annotation> annotationType() {
		return UIDataPopup.class;
	}

	@Override
	public boolean profiler() {
		return this.profiler;
	}

	@Override
	public boolean hotreload() {
		return this.hotreload;
	}

	/* [ Setter Section ] */
	/**
	 * Define the profiler state of the debug
	 * @param profiler
	 * @return this
	 */
	public @NonNull UIDataDebugObject setProfiler(final boolean profiler) {
		this.profiler = profiler;
		return this;
	}

	/**
	 * Define the hotreload state of the debug
	 *
	 * @param hotreload
	 * @return this
	 */
	public @NonNull UIDataDebugObject setHotreload(final boolean hotreload) {
		this.hotreload = hotreload;
		return this;
	}

}