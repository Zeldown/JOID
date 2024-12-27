package be.zeldown.joid.demo.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.chart.UIDemoChart;
import be.zeldown.joid.demo.ui.checkbox.UIDemoCheckbox;
import be.zeldown.joid.demo.ui.draggable.UIDemoDraggable;
import be.zeldown.joid.demo.ui.flex.UIDemoFlex;
import be.zeldown.joid.demo.ui.font.UIDemoFont;
import be.zeldown.joid.demo.ui.grid.UIDemoGrid;
import be.zeldown.joid.demo.ui.overflow.UIDemoOverflow;
import be.zeldown.joid.demo.ui.popup.UIDemoPopup;
import be.zeldown.joid.demo.ui.resource.UIDemoResource;
import be.zeldown.joid.demo.ui.selector.UIDemoSelector;
import be.zeldown.joid.demo.ui.simple.UIDemoSimple;
import be.zeldown.joid.demo.ui.slider.UIDemoSlider;
import be.zeldown.joid.demo.ui.store.UIDemoOtherStore;
import be.zeldown.joid.demo.ui.store.UIDemoStore;
import be.zeldown.joid.demo.ui.sw.UIDemoSwitch;
import be.zeldown.joid.demo.ui.textfield.UIDemoTextField;
import be.zeldown.joid.demo.ui.toggle.UIDemoToggle;
import be.zeldown.joid.demo.ui.wait.UIDemoWait;
import be.zeldown.joid.demo.ui.watch.UIDemoWatch;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.ui.node.impl.design.shape.RectNode;
import be.zeldown.joid.lib.ui.node.impl.design.text.TextNode;
import be.zeldown.joid.lib.ui.node.impl.structure.flex.FlexNode;
import be.zeldown.joid.lib.ui.node.property.overflow.OverflowProperty;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UIDemoChoice extends UI {

	public static final Set<Class<? extends UI>> LIST = new LinkedHashSet<>();

	static {
		UIDemoChoice.LIST.add(UIDemoChoice.class);
		UIDemoChoice.LIST.add(UIDemoSimple.class);
		UIDemoChoice.LIST.add(UIDemoOverflow.class);
		UIDemoChoice.LIST.add(UIDemoDraggable.class);
		UIDemoChoice.LIST.add(UIDemoFont.class);
		UIDemoChoice.LIST.add(UIDemoFlex.class);
		UIDemoChoice.LIST.add(UIDemoResource.class);
		UIDemoChoice.LIST.add(UIDemoWait.class);
		UIDemoChoice.LIST.add(UIDemoWatch.class);
		UIDemoChoice.LIST.add(UIDemoTextField.class);
		UIDemoChoice.LIST.add(UIDemoSelector.class);
		UIDemoChoice.LIST.add(UIDemoGrid.class);
		UIDemoChoice.LIST.add(UIDemoSlider.class);
		UIDemoChoice.LIST.add(UIDemoCheckbox.class);
		UIDemoChoice.LIST.add(UIDemoToggle.class);
		UIDemoChoice.LIST.add(UIDemoSwitch.class);
		UIDemoChoice.LIST.add(UIDemoStore.class);
		UIDemoChoice.LIST.add(UIDemoOtherStore.class);
		UIDemoChoice.LIST.add(UIDemoChart.class);
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
							JOID.open(ui);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}).hover(() -> clazz.getName()).attach(flex);
				}
			}).attach(rect);
		}).attach(this);

		this.keybind(() -> {
			JOID.open(new UIDemoPopup());
		}, Keyboard.KEY_K, Keyboard.KEY_LCONTROL);
	}

}