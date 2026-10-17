package dev.joid.lib.ui.core.transition;

import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

@FunctionalInterface
public interface TransitionDrawing {

	public void draw(final @NonNull UI ui, final float progress, final @NonNull Runnable body);

}