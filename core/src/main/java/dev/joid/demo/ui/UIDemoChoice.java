package dev.joid.demo.ui;

import java.util.ArrayList;
import java.util.List;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.animation.UIDemoAnimation;
import dev.joid.demo.ui.animation.UIDemoTween;
import dev.joid.demo.ui.chart.UIDemoChart;
import dev.joid.demo.ui.control.UIDemoControl;
import dev.joid.demo.ui.core.transition.DemoPushTransition;
import dev.joid.demo.ui.cursor.UIDemoCursor;
import dev.joid.demo.ui.draggable.UIDemoDraggable;
import dev.joid.demo.ui.event.UIDemoEvent;
import dev.joid.demo.ui.event.UIDemoEventFlow;
import dev.joid.demo.ui.font.UIDemoFont;
import dev.joid.demo.ui.font.UIDemoMarkup;
import dev.joid.demo.ui.font.UIDemoText;
import dev.joid.demo.ui.layout.UIDemoLayout;
import dev.joid.demo.ui.model.UIDemoModel;
import dev.joid.demo.ui.option.UIDemoOption;
import dev.joid.demo.ui.overflow.UIDemoOverflow;
import dev.joid.demo.ui.popup.UIDemoOverlay;
import dev.joid.demo.ui.popup.UIDemoPopup;
import dev.joid.demo.ui.reorderable.UIDemoReorderable;
import dev.joid.demo.ui.resource.UIDemoAudio;
import dev.joid.demo.ui.resource.UIDemoPlayer;
import dev.joid.demo.ui.resource.UIDemoResource;
import dev.joid.demo.ui.selector.UIDemoSelector;
import dev.joid.demo.ui.shader.UIDemoEffect;
import dev.joid.demo.ui.shader.UIDemoRender;
import dev.joid.demo.ui.shader.UIDemoShader;
import dev.joid.demo.ui.signal.UIDemoSignal;
import dev.joid.demo.ui.signal.UIDemoWatch;
import dev.joid.demo.ui.simple.UIDemoSimple;
import dev.joid.demo.ui.slider.UIDemoSlider;
import dev.joid.demo.ui.store.UIDemoOtherStore;
import dev.joid.demo.ui.store.UIDemoStore;
import dev.joid.demo.ui.textfield.UIDemoTextField;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.property.OverflowProperty;
import dev.joid.lib.ui.shortcut.Shortcut;

@UIData(background = true, closeable = false)
public class UIDemoChoice extends UI {

	private static final Color INK     = new Color(153, 153, 153).lock();
	private static final Color ACTIVE  = new Color(102, 102, 102).lock();
	private static final Color HOVERED = new Color(128, 128, 128).lock();

	private static final List<DemoEntry> LIST = new ArrayList<>();

	static {
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoSimple.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoOption.class));
		UIDemoChoice.LIST.add(DemoEntry.create("UIDemoOverlay", () -> {
			if (JOID.isOpen(UIDemoOverlay.class)) {
				JOID.close(JOID.getUi(UIDemoOverlay.class));
				JOID.close(JOID.getUi(UIDemoOverlay.BelowOverlay.class));
			} else {
				JOID.open(new UIDemoOverlay());
				JOID.open(new UIDemoOverlay.BelowOverlay());
			}
		}).state(() -> JOID.isOpen(UIDemoOverlay.class)));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoLayout.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoOverflow.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoText.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoFont.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoMarkup.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoResource.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoPlayer.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoAudio.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoEvent.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoEventFlow.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoCursor.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoDraggable.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoReorderable.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoTextField.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoControl.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoSlider.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoSelector.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoSignal.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoWatch.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoStore.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoOtherStore.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoEffect.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoAnimation.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoTween.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoRender.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoShader.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoChart.class));
		UIDemoChoice.LIST.add(DemoEntry.create(UIDemoModel.class));
	}

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 26).color(Color.WHITE);

		super.transition(DemoPushTransition.create());

		GridNode
		.create(90, 40, 1740, 1000)
		.margin(20D)
		.overflow(OverflowProperty.SCROLL)
		.body(grid -> {
			for (final DemoEntry entry : UIDemoChoice.LIST) {
				RectNode
				.create(0, 0, 420, 70)
				.color(() -> entry.isActive() ? UIDemoChoice.ACTIVE : UIDemoChoice.INK)
				.hoveredColor(UIDemoChoice.HOVERED)
				.label(Text.create(entry::getText, label))
				.onClick(event -> entry.getAction().run())
				.hover(entry::getHover)
				.attach(grid);
			}
		})
		.attach(this);

		Shortcut.create(Key.K).ctrl(true).onTrigger(event -> JOID.open(new UIDemoPopup())).attach(this);
	}

}