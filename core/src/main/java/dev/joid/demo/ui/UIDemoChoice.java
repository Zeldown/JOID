package dev.joid.demo.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.animation.UIDemoAnimation;
import dev.joid.demo.ui.chart.UIDemoChart;
import dev.joid.demo.ui.checkbox.UIDemoCheckbox;
import dev.joid.demo.ui.draggable.UIDemoDraggable;
import dev.joid.demo.ui.flex.UIDemoFlex;
import dev.joid.demo.ui.font.UIDemoFont;
import dev.joid.demo.ui.grid.UIDemoGrid;
import dev.joid.demo.ui.overflow.UIDemoOverflow;
import dev.joid.demo.ui.popup.UIDemoPopup;
import dev.joid.demo.ui.reorderable.UIDemoReorderable;
import dev.joid.demo.ui.resource.UIDemoResource;
import dev.joid.demo.ui.selector.UIDemoSelector;
import dev.joid.demo.ui.shader.UIDemoShader;
import dev.joid.demo.ui.signal.UIDemoSignal;
import dev.joid.demo.ui.simple.UIDemoSimple;
import dev.joid.demo.ui.slider.UIDemoSlider;
import dev.joid.demo.ui.store.UIDemoOtherStore;
import dev.joid.demo.ui.store.UIDemoStore;
import dev.joid.demo.ui.sw.UIDemoSwitch;
import dev.joid.demo.ui.textfield.UIDemoTextField;
import dev.joid.demo.ui.toggle.UIDemoToggle;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.ui.node.impl.structure.flex.FlexNode;
import dev.joid.lib.ui.node.property.overflow.OverflowProperty;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.key.Key;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UIDemoChoice extends UI {

	public static final Set<Class<? extends UI>> LIST = new LinkedHashSet<>();

	static {
		UIDemoChoice.LIST.add(UIDemoChoice.class);
		UIDemoChoice.LIST.add(UIDemoAnimation.class);
		UIDemoChoice.LIST.add(UIDemoSimple.class);
		UIDemoChoice.LIST.add(UIDemoOverflow.class);
		UIDemoChoice.LIST.add(UIDemoDraggable.class);
		UIDemoChoice.LIST.add(UIDemoFont.class);
		UIDemoChoice.LIST.add(UIDemoFlex.class);
		UIDemoChoice.LIST.add(UIDemoReorderable.class);
		UIDemoChoice.LIST.add(UIDemoResource.class);
		UIDemoChoice.LIST.add(UIDemoShader.class);
		UIDemoChoice.LIST.add(UIDemoSignal.class);
		UIDemoChoice.LIST.add(UIDemoTextField.class);
		UIDemoChoice.LIST.add(UIDemoSelector.class);
		UIDemoChoice.LIST.add(UIDemoGrid.class);
		UIDemoChoice.LIST.add(UIDemoSlider.class);
		UIDemoChoice.LIST.add(UIDemoCheckbox.class);
		UIDemoChoice.LIST.add(UIDemoToggle.class);
		UIDemoChoice.LIST.add(UIDemoSwitch.class);
		UIDemoChoice.LIST.add(UIDemoChart.class);
		UIDemoChoice.LIST.add(UIDemoStore.class);
		UIDemoChoice.LIST.add(UIDemoOtherStore.class);
	}

	@Override
	public void init() {
		super.setTransition(new DemoPushTransition());

		RectNode
		.create(0, 0, 1920, 1080)
		.color(Color.BLACK)
		.overflow(OverflowProperty.SCROLL)
		.body(rect -> {
			FlexNode
			.vertical(960 - 200, 10, 400)
			.margin(10D)
			.body(flex -> {
				for (final Class<? extends UI> clazz : UIDemoChoice.LIST) {
					RectNode
					.create(0, 0, 400, 60)
					.color(Color.WHITE)
					.body(container -> {
						TextNode
						.create(container.dw(2), container.dh(2))
						.text(Text.create(clazz.getSimpleName(), TextInfo.create(DemoFont.MONTSERRAT, 30), Align.CENTER, Align.CENTER))
						.anchor(Align.CENTER)
						.attach(container);
					}).onClick((node, mouseX, mouseY, clickType) -> {
						try {
							final UI ui = clazz.newInstance();
							ui.setTransition(new DemoPushTransition());
							JOID.open(ui, false);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}).hover(() -> clazz.getName()).attach(flex);
				}
			}).attach(rect);
		}).attach(this);

		this.keybind(() -> {
			JOID.open(new UIDemoPopup());
		}, Key.K, Key.LEFT_CONTROL);
	}

}