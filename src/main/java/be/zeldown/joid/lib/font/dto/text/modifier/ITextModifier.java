package be.zeldown.joid.lib.font.dto.text.modifier;

import lombok.NonNull;

@FunctionalInterface
public interface ITextModifier {

	String modify(final @NonNull String text);

}