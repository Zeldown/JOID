package be.zeldown.joid.lib.ui.node.hover;

import lombok.NonNull;

@FunctionalInterface
public interface HoverSupplier {

	public @NonNull String get();

}