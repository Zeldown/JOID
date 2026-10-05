package be.zeldown.joid.demo.ui.font.effect;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.font.dto.effect.ITextEffect;
import be.zeldown.joid.lib.font.dto.effect.ITextGlyph;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DemoWaveTextEffect implements ITextEffect {

	private static final DemoWaveTextEffect INSTANCE = new DemoWaveTextEffect();

	public static @NonNull DemoWaveTextEffect inst() {
		return DemoWaveTextEffect.INSTANCE;
	}

	@Override
	public void apply(final @NonNull ITextGlyph glyph) {
		glyph.offset(0D, Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 150D + glyph.getIndex() * 0.6D) * glyph.getSize() / 8D);
	}

}