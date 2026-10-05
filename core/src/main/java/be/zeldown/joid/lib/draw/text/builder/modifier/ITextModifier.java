package be.zeldown.joid.lib.draw.text.builder.modifier;

@FunctionalInterface
public interface ITextModifier {

	public String modify(final String text);

}