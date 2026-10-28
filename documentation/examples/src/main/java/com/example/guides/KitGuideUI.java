package com.example.guides;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.WatchProperty;

public class KitGuideUI extends UI {

	private static final Color TEXT    = new Color(231, 233, 238).lock();
	private static final Color ELEMENT = new Color(38, 43, 53).lock();

	// #region state
	private final IntegerSignal     volume  = new IntegerSignal(60);
	private final BooleanSignal     checked = new BooleanSignal(true);
	private final Signal<IGuideKit> kit     = new Signal<>(new PlainGuideKit());
	// #endregion

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 26F).color(KitGuideUI.TEXT);

		// #region screen
		RectNode
		.create(600, 330, 720, 340)
		.color(this.kit.map(IGuideKit::getSurface))
		.watch(this.kit, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
		.body(card -> {
			final IGuideKit current = this.kit.peek();
			current.checkbox(60, 60).checked(this.checked).attach(card);
			current.slider(60, 200, 600).range(0, 100).value(this.volume).attach(card);
		})
		.attach(this);
		// #endregion

		// #region switch
		RectNode
		.create(600, 710, 340, 70)
		.color(KitGuideUI.ELEMENT)
		.label(Text.create("Plain kit", info))
		.onClick(event -> this.kit.set(new PlainGuideKit()))
		.attach(this);

		RectNode
		.create(980, 710, 340, 70)
		.color(KitGuideUI.ELEMENT)
		.label(Text.create("Round kit", info))
		.onClick(event -> this.kit.set(new RoundGuideKit()))
		.attach(this);
		// #endregion
	}

}