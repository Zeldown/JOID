package com.example.state;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class SettingsUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	// #region property
	@UIProperty
	private int opened;
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(SettingsUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);
		// #region use
		final SettingsStore settings = super.useStore(SettingsStore.class, SettingsStore::new);
		// #endregion
		// #region opened
		this.opened++;
		// #endregion

		RectNode
		.create(560, 340, 800, 400)
		.color(Color.WHITE)
		.body(card -> {
			RectNode.create(60, 60, 120, 60).color(SettingsUI.INK).onClick(event -> settings.getVolume().decrement()).label(Text.create("-", label)).attach(card);
			// #region bind
			RectNode
			.create(200, 60, 120, 60)
			.color(SettingsUI.INK)
			.onClick(event -> settings.getVolume().increment())
			.label(Text.create("+", label))
			.attach(card);
			TextNode
			.create(360, 72)
			.text(Text.create(settings.getVolume().map(volume -> "Volume: " + volume), info))
			.attach(card);
			// #endregion
			RectNode.create(60, 160, 260, 60).color(SettingsUI.INK).onClick(event -> settings.getMuted().toggle()).label(Text.create("Mute", label)).attach(card);
			TextNode.create(360, 172).text(Text.create(settings.getMuted().map(muted -> muted ? "Muted" : "Sound on"), info)).attach(card);
			TextNode.create(60, 290).text(Text.create(() -> "Openings: " + this.opened, info)).attach(card);
		})
		.attach(this);
	}

}