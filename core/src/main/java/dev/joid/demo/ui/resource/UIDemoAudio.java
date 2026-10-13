package dev.joid.demo.ui.resource;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.FloatSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.point.Point;
import dev.joid.lib.video.VideoAudioPlayer;

@UIData(background = true)
public class UIDemoAudio extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoAudio.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 20, UIDemoAudio.INK);
		final TextInfo button = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE);
		final FloatSignal volume = FloatSignal.of(1F);
		final BooleanSignal far = BooleanSignal.of(false);
		final BooleanSignal away = BooleanSignal.of(false);
		VideoAudioPlayer.setAudioListenerPosition(() -> Point.create(0D, 0D, away.get() ? 30D : 0D));

		ResourcePlayerNode
		.create(80, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
		.loop(true)
		.autoplay(false)
		.volume(volume)
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		})
		.attach(this);
		RectNode
		.create(80, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> volume.set(Math.max(0F, volume.get() - 0.25F)))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("-", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		RectNode
		.create(190, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> volume.set(Math.min(1F, volume.get() + 0.25F)))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		TextNode.create(300, 576).text(() -> Text.create(Math.round(volume.get() * 100F) + " %", label)).attach(this);
		TextNode.create(240, 625).text(Text.create("Volume", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(440, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
		.loop(true)
		.autoplay(false)
		.location(() -> Point.create(0D, 0D, far.get() ? 30D : 0D))
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		})
		.attach(this);
		RectNode
		.create(440, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> far.set(false))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Near", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		RectNode
		.create(550, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> far.set(true))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Far", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		TextNode.create(660, 576).text(() -> Text.create("z: " + (far.get() ? 30 : 0), label)).attach(this);
		TextNode.create(600, 625).text(Text.create("Distance", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(800, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 30D))
		.referenceDistance(40F)
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		})
		.attach(this);
		TextNode.create(800, 576).text(Text.create("z: 30, full up to 40", label)).attach(this);
		TextNode.create(960, 625).text(Text.create("Reference distance", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1160, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 30D))
		.maxDistance(20F)
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		})
		.attach(this);
		TextNode.create(1160, 576).text(Text.create("z: 30, silent from 20", label)).attach(this);
		TextNode.create(1320, 625).text(Text.create("Max distance", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 0D))
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play();
			}
		})
		.attach(this);
		RectNode
		.create(1520, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> away.set(false))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Here", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		RectNode
		.create(1630, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick((node, mouseX, mouseY, clickType) -> away.set(true))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Away", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		TextNode.create(1740, 576).text(() -> Text.create("z: " + (away.get() ? 30 : 0), label)).attach(this);
		TextNode.create(1680, 625).text(Text.create("Listener", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);
	}

}