package be.zeldown.joid.demo.ui.font.markup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.zeldown.joid.demo.ui.font.effect.DemoUnderlineEffect;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextStyle;
import be.zeldown.joid.lib.font.dto.markup.ITextMarkup;
import lombok.NonNull;

public final class DemoMarkup implements ITextMarkup {

	private static final DemoMarkup INSTANCE = new DemoMarkup();

	private static final Pattern TAG = Pattern.compile("<(?:([biu])|w=(\\d{3})|c=([0-9a-fA-F]{6})|(/[biuwc]))>");

	private DemoMarkup() {}

	public static @NonNull DemoMarkup inst() {
		return DemoMarkup.INSTANCE;
	}

	@Override
	public int parse(final @NonNull String text, final int index, final @NonNull TextStyle style) {
		if (text.charAt(index) != '<') {
			return 0;
		}

		final Matcher matcher = DemoMarkup.TAG.matcher(text).region(index, text.length());
		if (!matcher.lookingAt()) {
			return 0;
		}

		final TextStyle base = style.getBase();
		switch (matcher.group(1) != null ? matcher.group(1) : matcher.group(2) != null ? "w" : matcher.group(3) != null ? "c" : matcher.group(4)) {
		case "b":
			style.weight(FontWeight.BOLD);
			break;
		case "i":
			style.italic(true);
			break;
		case "u":
			style.effect(DemoUnderlineEffect.inst());
			break;
		case "w":
			style.weight(FontWeight.of(Integer.parseInt(matcher.group(2))));
			break;
		case "c":
			style.color(Color.decode("#" + matcher.group(3)));
			break;
		case "/i":
			style.italic(base.isItalic());
			break;
		case "/u":
			style.removeEffect(DemoUnderlineEffect.inst());
			break;
		case "/c":
			style.color(base.getColor());
			break;
		default:
			style.weight(base.getWeight());
			break;
		}

		return matcher.end() - index;
	}

}