package be.zeldown.joid.lib.font.dto.text.modifier;

import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextCamelCaseModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextCapitalizeModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextLowerCaseModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextSnakeCaseModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextUpperCamelCaseModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextUpperCaseModifier;
import be.zeldown.joid.lib.font.dto.text.modifier.impl.TextWordCapitalizeModifier;

public class TextModifier {

	public static final ITextModifier LOWER_CASE       = new TextLowerCaseModifier();
	public static final ITextModifier UPPER_CASE       = new TextUpperCaseModifier();
	public static final ITextModifier SNAKE_CASE       = new TextSnakeCaseModifier();
	public static final ITextModifier CAMEL_CASE       = new TextCamelCaseModifier();
	public static final ITextModifier UPPER_CAMEL_CASE = new TextUpperCamelCaseModifier();

	public static final ITextModifier CAPITALIZE       = new TextCapitalizeModifier();
	public static final ITextModifier WORD_CAPITALIZE  = new TextWordCapitalizeModifier();

}