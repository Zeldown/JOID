package dev.joid.demo.ui.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import dev.joid.demo.DemoFont;
import dev.joid.demo.asset.locator.impl.DemoAssetLocator;
import dev.joid.demo.resource.format.impl.DemoCheckerResourceFormat;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.asset.locator.AssetLocator;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.ResourceBuilder;
import dev.joid.lib.resource.format.ResourceFormat;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.StringSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.effect.impl.CircleNodeEffect;
import dev.joid.lib.ui.node.effect.impl.RoundedNodeEffect;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoResource extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	private static final ResourceBuilder PIXELATED = ResourceBuilder.create().nearest().textureCoords(160D, 165D, 35D, 35D);

	static {
		AssetLocator.register(DemoAssetLocator.inst());
		ResourceFormat.register(DemoCheckerResourceFormat.inst());
	}

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoResource.INK);
		final Resource vector = Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"));
		final Resource square = Resource.of("https://placehold.co/100x100.png");
		final Resource broken = Resource.of(new ByteArrayInputStream(new byte[] {0, 1, 2, 3})).uniqueId("broken-image").onError((resource, error) -> System.out.println("[UIDemoResource] failed resource: " + error.getMessage()));
		final BooleanSignal second = BooleanSignal.of(false);
		final StringSignal natural = StringSignal.of("");
		final BufferedImage disc = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 100; y++) {
				disc.setRGB(x, y, Math.hypot(x - 49.5D, y - 49.5D) < 40D ? 0xFF999999 : 0xFFDDDDDD);
			}
		}

		ResourceNode.create(105, 50).resource(Resource.of("https://placehold.co/100x100.png")).attach(this);
		TextNode.create(155, 175).text(Text.create("PNG", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(335, 50).resource(Resource.of("https://placehold.co/100x100.jpg")).attach(this);
		TextNode.create(385, 175).text(Text.create("JPEG", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(565, 50, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).attach(this);
		TextNode.create(615, 175).text(Text.create("WebP", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(795, 50, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"))).attach(this);
		TextNode.create(845, 175).text(Text.create("SVG", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1025, 50, 0, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/gif.gif")).linear()).attach(this);
		TextNode.create(1075, 175).text(Text.create("GIF", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1255, 50, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp"))).attach(this);
		TextNode.create(1305, 175).text(Text.create("Animated WebP", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1485, 50, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png"))).attach(this);
		TextNode.create(1535, 175).text(Text.create("APNG", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1713, 48, 104, 104).color(Color.WHITE).attach(this);
		ResourceNode.create(1715, 50, 100, 100).resource(broken).attach(this);
		TextNode.create(1765, 100).text(() -> Text.create(broken.isFailed() ? "isFailed" : "Loading", caption, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(this);
		TextNode.create(1765, 175).text(Text.create("Failed", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(105, 280).resource(Resource.of("https://placehold.co/100x100.png").onLoad(resource -> natural.set("onLoad " + resource.getWidth() + "x" + resource.getHeight()))).attach(this);
		TextNode.create(155, 405).text(Text.create("Natural size", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);
		TextNode.create(155, 440).text(() -> Text.create(natural.get(), caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(360, 270, 50, 0).resource(Resource.of("https://placehold.co/400x800.png")).attach(this);
		TextNode.create(385, 405).text(Text.create("Width only", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(515, 300, 0, 60).resource(Resource.of("https://placehold.co/800x400.png")).attach(this);
		TextNode.create(615, 405).text(Text.create("Height only", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(795, 280, 100, 100).resource(Resource.of("https://placehold.co/400x800.png")).stretch(StretchType.STRETCH).attach(this);
		TextNode.create(845, 405).text(Text.create("Stretch", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1025, 280, 100, 100).color(UIDemoResource.INK).attach(this);
		ResourceNode.create(1025, 280, 100, 100).resource(Resource.of("https://placehold.co/400x800.png")).stretch(StretchType.CONTAIN).attach(this);
		TextNode.create(1075, 405).text(Text.create("Contain", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1255, 280, 100, 100).resource(Resource.of("https://placehold.co/400x800.png")).stretch(StretchType.COVER).attach(this);
		TextNode.create(1305, 405).text(Text.create("Cover", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1523, 318, 24, 24).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"))).attach(this);
		TextNode.create(1535, 405).text(Text.create("SVG 24", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1740, 305, 50, 50).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/vector/placeholder.svg"))).attach(this);
		TextNode.create(1765, 405).text(Text.create("SVG 50", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(105, 510, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).color(Color.ORANGE).attach(this);
		TextNode.create(155, 635).text(Text.create("Tint", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(375, 500, 20, 120).color(Color.WHITE).attach(this);
		ResourceNode.create(335, 510, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).color(Color.WHITE.copyAlpha(0.4F)).attach(this);
		TextNode.create(385, 635).text(Text.create("Transparent", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(565, 510, 100, 100).resource(Resource.of("https://placehold.co/100x100.png")).hoveredResource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).attach(this);
		TextNode.create(615, 635).text(Text.create("Hovered image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(795, 510, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).hoveredColor(Color.ORANGE).attach(this);
		TextNode.create(845, 635).text(Text.create("Hovered tint", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1027, 512, 96, 96).resource(Resource.of("https://placehold.co/16x16.png").nearest()).attach(this);
		TextNode.create(1075, 635).text(Text.create("Nearest", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1257, 512, 96, 96).resource(Resource.of("https://placehold.co/16x16.png").linear()).attach(this);
		TextNode.create(1305, 635).text(Text.create("Linear", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1485, 510, 100, 100).resource(Resource.of("https://placehold.co/400x400.png")).effect(RoundedNodeEffect.create(10F)).attach(this);
		TextNode.create(1535, 635).text(Text.create("Rounded", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1715, 510, 100, 100).resource(Resource.of("https://placehold.co/400x400.png")).effect(CircleNodeEffect.create()).attach(this);
		TextNode.create(1765, 635).text(Text.create("Circle", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ProgressNode
		.create(55, 770, 200, 40)
		.backgroundResource(Resource.of("https://placehold.co/200x40.png"))
		.foregroundResource(Resource.of("https://placehold.co/200x40/999999/FFFFFF.png"))
		.progress(0.6F)
		.attach(this);
		TextNode.create(155, 865).text(Text.create("Progress image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode
		.create(335, 740, 100, 100)
		.resource(second.map(value -> value ? square : vector))
		.onClick(event -> second.toggle())
		.attach(this);
		TextNode.create(385, 865).text(Text.create("Switch on click", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(565, 740, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp")).textureCoords(40D, 60D, 280D, 280D)).attach(this);
		TextNode.create(615, 865).text(Text.create("Region", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(795, 740, 100, 100).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp")).mipmap(false)).attach(this);
		TextNode.create(845, 865).text(Text.create("Mipmap off", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1025, 740).resource(Resource.of(disc)).attach(this);
		TextNode.create(1075, 865).text(Text.create("BufferedImage", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1257, 742, 96, 96).resource(Resource.of(new ByteArrayInputStream("CHECKER 8".getBytes(StandardCharsets.US_ASCII))).nearest()).attach(this);
		TextNode.create(1305, 865).text(Text.create("Custom format", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1485, 740, 100, 100).resource(Resource.of("demo:textures/image/placeholder.webp")).attach(this);
		TextNode.create(1535, 865).text(Text.create("Custom locator", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourceNode.create(1690, 755, 70, 70).resource(UIDemoResource.PIXELATED.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder.webp"))).attach(this);
		ResourceNode.create(1770, 755, 70, 70).resource(UIDemoResource.PIXELATED.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png"))).attach(this);
		TextNode.create(1765, 865).text(Text.create("Builder", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);
	}

}