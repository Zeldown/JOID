package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.data.UIDataOverlay;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(zindex = 10)
@UIDataOverlay(active = true)
public class VolumeOverlay extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);
		// #region overlay
		final SettingsStore settings = super.useStore(SettingsStore.class, SettingsStore::new);

		RectNode
		.create(1560, 40, 320, 70)
		.color(VolumeOverlay.INK)
		.label(Text.create(settings.getVolume().map(volume -> "Volume " + volume), info))
		.attach(this);
		// #endregion
	}

}