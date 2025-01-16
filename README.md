<div align="center">

# JOID
## Java OpenGL Interface Developement

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-4.0.2 (a2edb64)-blue">
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


<img src="https://github.com/Zeldown/JOID/blob/lwjgl-2/demo/simple.png">
<details>
  <summary>Code</summary>
  
  ```java
  public class UIDemoSimple extends UIDemo {

	@Override
	public void init() {
		final ContainerNode container = ContainerNode.create(0D, 0D, 1920D, 1080D);

		/* append children */
		container.body(() -> {
			RectNode.create(
					1920D / 4D,
					1080D / 4D,
					1920D / 2D,
					1080D / 2D
					)
			.color(Color.RED, Color.GREEN)
			.border(Color.GREEN, Color.RED, 3D, true)
			.body(n -> {
				final double childWidth = n.dw(3D);
				final double childHeight = n.dh(2D);

				RectNode.create(
						0D,
						n.dh(2D) - childHeight/2,
						childWidth,
						childHeight
						)
				.color(Color.RED, Color.WHITE)
				.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
				.hover(() -> "hover1")
				.attach(n);

				RectNode.create(
						n.aw(-childWidth),
						n.dh(2D) - childHeight/2,
						childWidth,
						childHeight
						)
				.color(Color.RED, Color.WHITE)
				.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
				.hover(() -> Arrays.asList("hover1", "hover2"))
				.body(n1 -> {
					RectNode.create(
							n1.dw(4D),
							n1.dh(4D),
							n1.dw(2D),
							n1.dh(2D)
							)
					.color(Color.RED, Color.MAGENTA)
					.onClick((node, mouseX, mouseY, clickType) -> System.out.println(node))
					.hover(() -> Arrays.asList("hover1", "hover2", "hover3"))
					.attach(n1);
				})
				.attach(n);
			})
			.attach(container);
		});

		container.attach(this);
	}

	@Override
	public void preDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(0D, 0D, 1920D, 1080D, Color.BLUE.toGradient(Color.RED));
	}

	@Override
	public void postDraw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.BLUE, 10D);
	}

  }
  ```
</details>

## Credits

JOID is built upon the Universal Tween Engine, a powerful and versatile animation engine created by [Aurélien Ribon](https://github.com/AurelienRibon).<br>
The font rendering technology used in this project is helped by [msdfgen](https://github.com/Chlumsky/msdfgen) for the msdf generator.

## MSDF-Generator

```bash
msdf-atlas-gen.exe -font font.ttf -charset charset.txt -dimensions 2048 2048 -imageout font.png -json font.json -type msdf -pxrange 24 -coloringstrategy distance
```
