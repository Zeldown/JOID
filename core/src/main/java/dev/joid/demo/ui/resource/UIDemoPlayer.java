package dev.joid.demo.ui.resource;

import java.io.ByteArrayInputStream;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.internal.JOID;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.builder.Text;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.impl.design.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.design.shape.RectNode;
import dev.joid.lib.ui.node.impl.design.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.key.Key;
import dev.joid.lib.utils.signal.impl.primitive.FloatSignal;
import dev.joid.lib.utils.signal.impl.primitive.IntegerSignal;

public class UIDemoPlayer extends UIDemo {

	private static final Color INK = new Color(153, 153, 153);

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoPlayer.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 20, UIDemoPlayer.INK);
		final TextInfo button = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE);
		final IntegerSignal ends = IntegerSignal.of(0);
		final IntegerSignal plays = IntegerSignal.of(0);
		final IntegerSignal pauses = IntegerSignal.of(0);
		final IntegerSignal stops = IntegerSignal.of(0);
		final FloatSignal progress = FloatSignal.of(0F);

		final ResourcePlayerNode video = ResourcePlayerNode
				.create(80, 40, 320, 180)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4")))
				.loop(true)
				.attach(this);
		TextNode.create(240, 285).text(Text.create("Video", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(440, 40, 320, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm"))).loop(true).attach(this);
		TextNode.create(600, 285).text(Text.create("WebM", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(800, 40, 320, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp"))).loop(true).attach(this);
		TextNode.create(960, 285).text(Text.create("Animated image", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1160, 40, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.webp")))
		.loop(true)
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.resume();
			}
		})
		.attach(this);
		TextNode.create(1320, 285).text(Text.create("Click to pause", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 40, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
		.autoplay(false)
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> player.play())
		.attach(this);
		TextNode.create(1680, 285).text(Text.create("Autoplay off", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(80, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/textures/image/placeholder-animated.png")))
		.onEnd(player -> ends.increment())
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> player.restart())
		.attach(this);
		TextNode.create(80, 574).text(Text.create("Ends: " + ends.get(), label)).attach(this);
		TextNode.create(240, 625).text(Text.create("Play once", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(440, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
		.loop(true)
		.onPlay(player -> plays.increment())
		.onPause(player -> pauses.increment())
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> {
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.resume();
			}
		})
		.attach(this);
		TextNode.create(440, 574).text(Text.create("Play: " + plays.get() + "  Pause: " + pauses.get(), label)).attach(this);
		TextNode.create(600, 625).text(Text.create("Callbacks", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(800, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
		.loop(true)
		.onProgress((player, value, time) -> progress.set((float) value))
		.attach(this);
		ProgressNode.create(800, 574, 320, 12).background(Color.WHITE).foreground(UIDemoPlayer.INK).progress(progress).attach(this);
		TextNode.create(960, 625).text(Text.create("Progress", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		final ResourcePlayerNode seeked = ResourcePlayerNode
				.create(1160, 380, 320, 180)
				.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
				.loop(true)
				.attach(this);
		RectNode
		.create(1160, 570, 100, 36)
		.color(UIDemoPlayer.INK)
		.onClick((node, mouseX, mouseY, clickType) -> seeked.seek(1D))
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("+1 s", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		RectNode
		.create(1270, 570, 100, 36)
		.color(UIDemoPlayer.INK)
		.onClick((node, mouseX, mouseY, clickType) -> seeked.restart())
		.body(container -> {
			TextNode.create(container.dw(2), container.dh(2)).text(Text.create("Restart", button, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(container);
		})
		.attach(this);
		TextNode.create(1320, 625).text(Text.create("Seek and restart", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 380, 320, 180)
		.resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/placeholder.webm")))
		.loop(true)
		.onStop(player -> stops.increment())
		.<ResourcePlayerNode>onClick((player, mouseX, mouseY, clickType) -> player.stop())
		.attach(this);
		TextNode.create(1520, 574).text(Text.create("Stops: " + stops.get(), label)).attach(this);
		TextNode.create(1680, 625).text(Text.create("Stop", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(150, 720, 180, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4"))).loop(true).volume(0F).stretch(StretchType.COVER).attach(this);
		TextNode.create(240, 965).text(Text.create("Cover", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(510, 720, 180, 180).color(UIDemoPlayer.INK).attach(this);
		ResourcePlayerNode.create(510, 720, 180, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4"))).loop(true).volume(0F).stretch(StretchType.CONTAIN).attach(this);
		TextNode.create(600, 965).text(Text.create("Contain", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(870, 720, 180, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4"))).loop(true).volume(0F).stretch(StretchType.STRETCH).attach(this);
		TextNode.create(960, 965).text(Text.create("Stretch", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(1160, 720, 320, 180).resource(Resource.of(JOID.class.getResourceAsStream("/assets/demo/videos/video.mp4"))).loop(true).volume(0F).attach(this);
		TextNode.create(1320, 965).text(Text.create("Muted", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1520, 720, 320, 180).color(Color.WHITE).attach(this);
		ResourcePlayerNode.create(1522, 722, 316, 176).resource(Resource.of(new ByteArrayInputStream(new byte[] {0, 1, 2, 3}))).attach(this);
		TextNode.create(1680, 965).text(Text.create("Failed", caption, Align.CENTER)).anchorX(Align.CENTER).attach(this);

		this.keybind(() -> {
			if (video.isPlaying()) {
				video.pause();
			} else {
				video.resume();
			}
		}, Key.SPACE);
		this.keybind(() -> {
			if (video.getWidth() < this.getWidth()) {
				video.x(0).y(0).width(this.getWidth()).height(this.getHeight());
			} else {
				video.x(80).y(40).width(320).height(180);
			}
		}, Key.F);
	}

}