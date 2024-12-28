<div align="center">

# JOID
## Java OpenGL Interface Developement

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-4.0.0 (91f23d4)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/push.yml/badge.svg">
</div>

<br>

Welcome to JOID, a powerful and flexible user interface toolkit designed for developer community.
<br><br>
Design your project uniquely by making the theme that best fits your preferences, as there is no default theme to limit your creativity.
<br>
Stand out in the global landscape with a personalized design that reflects your vision.
<br><br>


Create the ideal interface with a wide range of customizable components and impressive animations, allowing you to showcase your talent effortlessly.
<br><br>

[Demo](#demo)
[Credits](#credits)

</div>

## Demo
- [Watch all prebuilt components's video](https://github.com/Zeldown/JOID/blob/lwjgl-2/demo/components.mp4)
- [See awesome UIs made with JOID](https://github.com/Zeldown/JOID/blob/lwjgl-2/demo/showcase/)
<br>

```java
public class UIDemoChoice extends UI {

	public static final Set<Class<? extends UI>> LIST = new LinkedHashSet<>();

	static {
		UIDemoChoice.LIST.add(UIDemoChoice.class);
		UIDemoChoice.LIST.add(UIDemoSimple.class);
		UIDemoChoice.LIST.add(UIDemoOverflow.class);
		UIDemoChoice.LIST.add(UIDemoDraggable.class);
		UIDemoChoice.LIST.add(UIDemoFont.class);
		UIDemoChoice.LIST.add(UIDemoFlex.class);
		UIDemoChoice.LIST.add(UIDemoImage.class);
		UIDemoChoice.LIST.add(UIDemoWait.class);
		UIDemoChoice.LIST.add(UIDemoWatch.class);
		UIDemoChoice.LIST.add(UIDemoTextField.class);
		UIDemoChoice.LIST.add(UIDemoSelector.class);
		UIDemoChoice.LIST.add(UIDemoContainer.class);
		UIDemoChoice.LIST.add(UIDemoEntity.class);
		UIDemoChoice.LIST.add(UIDemoModel.class);
		UIDemoChoice.LIST.add(UIDemoGrid.class);
		UIDemoChoice.LIST.add(UIDemoSlider.class);
		UIDemoChoice.LIST.add(UIDemoCheckbox.class);
		UIDemoChoice.LIST.add(UIDemoToggle.class);
		UIDemoChoice.LIST.add(UIDemoSwitch.class);
		UIDemoChoice.LIST.add(UIDemoStore.class);
		UIDemoChoice.LIST.add(UIDemoOtherStore.class);
		UIDemoChoice.LIST.add(UIDemoChart.class);
	}

	public UIDemoChoice(final String name, final Boolean isServerSide) {
		System.out.println(name + " " + isServerSide);
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
							ZUI.open(ui);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}).hover(() -> clazz.getName()).attach(flex);
				}
			}).attach(rect);
		}).attach(this);
	}

}
```

## Credits

JOID is built upon the Universal Tween Engine, a powerful and versatile animation engine created by [Aurélien Ribon](https://github.com/AurelienRibon).<br>
The font rendering technology used in this project is helped by [msdfgen](https://github.com/Chlumsky/msdfgen) for the msdf generator.

## MSDF-Generator

```bash
msdf-atlas-gen.exe -font font.ttf -charset charset.txt -dimensions 2048 2048 -imageout font.png -json font.json -type msdf -pxrange 24 -coloringstrategy distance
```
