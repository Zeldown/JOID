package com.example.describing.uis;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ReloadUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	private int builds;

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(ReloadUI.INK);
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 24F).color(Color.WHITE);

		// #region rebuild
		this.builds++;

		TextNode.create(760, 420).text(Text.create("Builds: " + this.builds, info)).attach(this);

		RectNode
		.create(760, 480, 190, 70)
		.color(ReloadUI.INK)
		.label(Text.create("Reload", label))
		.onClick(event -> super.reload())
		.attach(this);

		RectNode
		.create(970, 480, 190, 70)
		.color(ReloadUI.INK)
		.label(Text.create("Renew", label))
		.onClick(event -> super.renew())
		.attach(this);
		// #endregion
	}

}