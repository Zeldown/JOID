package dev.joid.demo.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.animation.UIDemoAnimation;
import dev.joid.demo.ui.chart.UIDemoChart;
import dev.joid.demo.ui.control.UIDemoControl;
import dev.joid.demo.ui.cursor.UIDemoCursor;
import dev.joid.demo.ui.draggable.UIDemoDraggable;
import dev.joid.demo.ui.font.UIDemoFont;
import dev.joid.demo.ui.font.UIDemoMarkup;
import dev.joid.demo.ui.font.UIDemoText;
import dev.joid.demo.ui.layout.UIDemoLayout;
import dev.joid.demo.ui.model.UIDemoModel;
import dev.joid.demo.ui.overflow.UIDemoOverflow;
import dev.joid.demo.ui.popup.UIDemoOverlay;
import dev.joid.demo.ui.popup.UIDemoPopup;
import dev.joid.demo.ui.reorderable.UIDemoReorderable;
import dev.joid.demo.ui.resource.UIDemoPlayer;
import dev.joid.demo.ui.resource.UIDemoResource;
import dev.joid.demo.ui.selector.UIDemoSelector;
import dev.joid.demo.ui.shader.UIDemoEffect;
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
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.grid.GridNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.key.Key;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UIDemoChoice extends UI {

	private static final Color INK     = new Color(153, 153, 153);
	private static final Color HOVERED = new Color(128, 128, 128);

	public static final Set<Class<? extends UI>> LIST = new LinkedHashSet<>();

	static {
		UIDemoChoice.LIST.add(UIDemoChoice.class);
		UIDemoChoice.LIST.add(UIDemoSimple.class);
		UIDemoChoice.LIST.add(UIDemoAnimation.class);
		UIDemoChoice.LIST.add(UIDemoLayout.class);
		UIDemoChoice.LIST.add(UIDemoOverflow.class);
		UIDemoChoice.LIST.add(UIDemoDraggable.class);
		UIDemoChoice.LIST.add(UIDemoCursor.class);
		UIDemoChoice.LIST.add(UIDemoReorderable.class);
		UIDemoChoice.LIST.add(UIDemoFont.class);
		UIDemoChoice.LIST.add(UIDemoText.class);
		UIDemoChoice.LIST.add(UIDemoMarkup.class);
		UIDemoChoice.LIST.add(UIDemoTextField.class);
		UIDemoChoice.LIST.add(UIDemoControl.class);
		UIDemoChoice.LIST.add(UIDemoSlider.class);
		UIDemoChoice.LIST.add(UIDemoSelector.class);
		UIDemoChoice.LIST.add(UIDemoShader.class);
		UIDemoChoice.LIST.add(UIDemoEffect.class);
		UIDemoChoice.LIST.add(UIDemoResource.class);
		UIDemoChoice.LIST.add(UIDemoPlayer.class);
		UIDemoChoice.LIST.add(UIDemoModel.class);
		UIDemoChoice.LIST.add(UIDemoChart.class);
		UIDemoChoice.LIST.add(UIDemoSignal.class);
		UIDemoChoice.LIST.add(UIDemoWatch.class);
		UIDemoChoice.LIST.add(UIDemoStore.class);
		UIDemoChoice.LIST.add(UIDemoOtherStore.class);
	}

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 26, Color.WHITE);
		super.setTransition(new DemoPushTransition());

		GridNode
		.create(90, 40, 1740, 1000)
		.margin(20D)
		.overflow(OverflowProperty.SCROLL)
		.body(grid -> {
			for (final Class<? extends UI> clazz : UIDemoChoice.LIST) {
				RectNode
				.create(0, 0, 420, 70)
				.color(UIDemoChoice.INK)
				.hoveredColor(UIDemoChoice.HOVERED)
				.body(container -> {
					TextNode.create(container.dw(2), container.dh(2)).text(Text.create(clazz.getSimpleName(), label, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
				})
				.onClick((node, mouseX, mouseY, clickType) -> {
					try {
						final UI ui = clazz.newInstance();
						ui.setTransition(new DemoPushTransition());
						JOID.open(ui, false);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				})
				.hover(() -> clazz.getName())
				.attach(grid);
			}
		})
		.attach(this);

		this.keybind(() -> {
			JOID.open(new UIDemoPopup());
		}, Key.K, Key.LEFT_CONTROL);

		this.keybind(() -> {
			final UIDemoOverlay overlay = JOID.getUI(UIDemoOverlay.class);
			if (overlay != null) {
				JOID.close(overlay);
			} else {
				JOID.open(new UIDemoOverlay());
			}
		}, Key.O, Key.LEFT_CONTROL);
	}

}