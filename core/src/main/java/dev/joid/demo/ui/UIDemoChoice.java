package dev.joid.demo.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

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
import lombok.Getter;
import lombok.NonNull;

@UIData(background = true, closeable = false)
public class UIDemoChoice extends UI {

	private static final Color INK     = new Color(153, 153, 153).lock();
	private static final Color ACTIVE  = new Color(102, 102, 102).lock();
	private static final Color HOVERED = new Color(128, 128, 128).lock();

	private static final List<DemoEntry> LIST = Arrays.asList(
		DemoEntry.create(UIDemoSimple.class),
		DemoEntry.create(UIDemoOption.class),
		DemoEntry.create("UIDemoOverlay", () -> {
			if (JOID.isOpen(UIDemoOverlay.class)) {
				JOID.close(JOID.getUi(UIDemoOverlay.class));
				JOID.close(JOID.getUi(UIDemoOverlay.BelowOverlay.class));
			} else {
				JOID.open(new UIDemoOverlay());
				JOID.open(new UIDemoOverlay.BelowOverlay());
			}
		}).state(() -> JOID.isOpen(UIDemoOverlay.class)),
		DemoEntry.create(UIDemoLayout.class),
		DemoEntry.create(UIDemoOverflow.class),
		DemoEntry.create(UIDemoText.class),
		DemoEntry.create(UIDemoFont.class),
		DemoEntry.create(UIDemoMarkup.class),
		DemoEntry.create(UIDemoResource.class),
		DemoEntry.create(UIDemoPlayer.class),
		DemoEntry.create(UIDemoAudio.class),
		DemoEntry.create(UIDemoEvent.class),
		DemoEntry.create(UIDemoEventFlow.class),
		DemoEntry.create(UIDemoCursor.class),
		DemoEntry.create(UIDemoDraggable.class),
		DemoEntry.create(UIDemoReorderable.class),
		DemoEntry.create(UIDemoTextField.class),
		DemoEntry.create(UIDemoControl.class),
		DemoEntry.create(UIDemoSlider.class),
		DemoEntry.create(UIDemoSelector.class),
		DemoEntry.create(UIDemoSignal.class),
		DemoEntry.create(UIDemoWatch.class),
		DemoEntry.create(UIDemoStore.class),
		DemoEntry.create(UIDemoOtherStore.class),
		DemoEntry.create(UIDemoEffect.class),
		DemoEntry.create(UIDemoAnimation.class),
		DemoEntry.create(UIDemoTween.class),
		DemoEntry.create(UIDemoRender.class),
		DemoEntry.create(UIDemoShader.class),
		DemoEntry.create(UIDemoChart.class),
		DemoEntry.create(UIDemoModel.class)
	);

	@Getter
	private static Supplier<UI> menu = UIDemoChoice::new;

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

	public static void menu(final @NonNull Supplier<UI> menu) {
		UIDemoChoice.menu = menu;
	}

	public static @NonNull List<@NonNull DemoEntry> getEntries() {
		return Collections.unmodifiableList(UIDemoChoice.LIST);
	}

}