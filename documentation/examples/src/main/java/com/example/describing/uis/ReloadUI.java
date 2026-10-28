package com.example.describing.uis;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

public class ReloadUI extends UI {

	private static final Color TEXT      = new Color(231, 233, 238).lock();
	private static final Color ACCENT    = new Color(91, 140, 255).lock();
	private static final Color ON_ACCENT = new Color(14, 16, 20).lock();

	private int builds;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(ReloadUI.TEXT);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(ReloadUI.ON_ACCENT);

		// #region rebuild
		this.builds++;

		TextNode.create(760, 420).text(Text.create("Builds: " + this.builds, info)).attach(this);

		RectNode
		.create(760, 480, 190, 70)
		.color(ReloadUI.ACCENT)
		.label(Text.create("Reload", label))
		.onClick(event -> super.reload())
		.attach(this);

		RectNode
		.create(970, 480, 190, 70)
		.color(ReloadUI.ACCENT)
		.label(Text.create("Renew", label))
		.onClick(event -> super.renew())
		.attach(this);
		// #endregion
	}

}