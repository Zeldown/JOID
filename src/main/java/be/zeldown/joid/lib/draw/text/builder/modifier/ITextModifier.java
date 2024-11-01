package be.zeldown.joid.lib.draw.text.builder.modifier;

import lombok.NonNull;

@FunctionalInterface
public interface ITextModifier {

	@NonNull String modify(final @NonNull String text);

}