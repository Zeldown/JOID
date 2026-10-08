package dev.joid.base.glfw.input;

import dev.joid.lib.utils.key.Key;
import lombok.NonNull;

@FunctionalInterface
public interface IKeyTypedListener {

	public void keyTyped(final char c, final @NonNull Key key);

}