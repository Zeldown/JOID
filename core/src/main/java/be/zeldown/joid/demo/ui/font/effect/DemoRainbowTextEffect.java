package be.zeldown.joid.demo.ui.font.effect;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoRainbowTextEffect implements ITextEffect {

	private static final DemoRainbowTextEffect INSTANCE = new DemoRainbowTextEffect();

	public static @NonNull DemoRainbowTextEffect inst() {
		return DemoRainbowTextEffect.INSTANCE;
	}

	@Override
	public void apply(final @NonNull ITextGlyph glyph) {
		glyph.color(Color.RAINBOW(BridgeHandler.CLOCK.get().currentTimeMillis() + glyph.getIndex() * 120L).copyAlpha(glyph.getColor().a));
	}

}