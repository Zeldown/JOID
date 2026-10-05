package dev.joid.demo.ui.font.effect;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.dto.effect.ITextEffect;
import dev.joid.lib.font.dto.effect.ITextGlyph;
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