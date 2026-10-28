package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class SettingsGuideUI extends UI {

	private static final Color TEXT = new Color(231, 233, 238).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(SettingsGuideUI.TEXT);
		// #region use
		final SettingsGuideStore store = super.useStore(SettingsGuideStore.class, SettingsGuideStore::new);
		// #endregion

		// #region sound
		TextNode.create(700, 400).text(Text.create("Sound", info)).anchorY(Align.CENTER).attach(this);

		PlainGuideCheckboxNode
		.create(1170, 375, 50, 50)
		.checked(store.getSound())
		.onChange(event -> store.save())
		.attach(this);
		// #endregion

		// #region volume
		TextNode
		.create(700, 500)
		.text(store.getVolume().map(volume -> Text.create("Volume " + volume, info)))
		.anchorY(Align.CENTER)
		.attach(this);

		PlainGuideSliderNode
		.create(920, 475, 300, 50)
		.range(0, 100)
		.value(store.getVolume())
		.onChange(event -> store.save())
		.enabled(store.getSound())
		.attach(this);
		// #endregion
	}

}