package be.zeldown.joid.lib.font.dto.text.modifier;

@FunctionalInterface
public interface ITextModifier {

	String modify(final String text);

}