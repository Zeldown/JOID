package dev.joid.demo.ui.resource;

import java.io.ByteArrayInputStream;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.FloatSignal;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.ui.shortcut.Shortcut;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.video.VideoNatives;

@UIData(background = true)
public class UIDemoPlayer extends UIDemo {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoPlayer.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 20).color(UIDemoPlayer.INK);
		final TextInfo button = TextInfo.create(DemoFont.MONTSERRAT, 20).color(Color.WHITE);
		final IntegerSignal ends = new IntegerSignal(0);
		final IntegerSignal plays = new IntegerSignal(0);
		final IntegerSignal pauses = new IntegerSignal(0);
		final IntegerSignal stops = new IntegerSignal(0);
		final FloatSignal progress = new FloatSignal(0F);

		final ResourcePlayerNode video = ResourcePlayerNode
		.create(80, 40, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.attach(this);
		TextNode.create(240, 285).text(Text.create("Video", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(440, 40, 320, 180).resource(Resource.of("assets/demo/videos/placeholder.webm")).loop(true).attach(this);
		TextNode.create(600, 285).text(Text.create("WebM", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(800, 40, 320, 180).resource(Resource.of("assets/demo/textures/image/placeholder-animated.webp")).loop(true).attach(this);
		TextNode.create(960, 285).text(Text.create("Animated image", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1160, 40, 320, 180)
		.resource(Resource.of("assets/demo/textures/image/placeholder-animated.webp"))
		.loop(true)
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		TextNode.create(1320, 285).text(Text.create("Click to pause", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 40, 320, 180)
		.resource(Resource.of("assets/demo/videos/placeholder.webm"))
		.autoplay(false)
		.<ResourcePlayerNode>onClick(event -> event.getNode().play())
		.attach(this);
		TextNode.create(1680, 285).text(Text.create("Autoplay off", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(80, 380, 320, 180)
		.resource(Resource.of("assets/demo/textures/image/placeholder-animated.png"))
		.onEnd(event -> ends.increment())
		.<ResourcePlayerNode>onClick(event -> event.getNode().restart())
		.attach(this);
		TextNode.create(80, 574).text(Text.create("Ends: " + ends.get(), label)).attach(this);
		TextNode.create(240, 625).text(Text.create("Play once", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(440, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/placeholder.webm"))
		.loop(true)
		.onPlay(event -> plays.increment())
		.onPause(event -> pauses.increment())
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		TextNode.create(440, 574).text(Text.create("Play: " + plays.get() + "  Pause: " + pauses.get(), label)).attach(this);
		TextNode.create(600, 625).text(Text.create("Callbacks", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(800, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/placeholder.webm"))
		.loop(true)
		.onProgress(event -> progress.set((float) event.getProgress()))
		.attach(this);
		ProgressNode.create(800, 574, 320, 12).track(RectNode.create(0, 0, 0, 0).color(Color.WHITE)).fill(RectNode.create(0, 0, 0, 0).color(UIDemoPlayer.INK)).progress(progress).attach(this);
		TextNode.create(960, 625).text(Text.create("Progress", caption)).anchorX(Align.CENTER).attach(this);

		final ResourcePlayerNode seeked = ResourcePlayerNode
		.create(1160, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/placeholder.webm"))
		.loop(true)
		.seek(1D)
		.attach(this);
		RectNode
		.create(1160, 570, 100, 36)
		.color(UIDemoPlayer.INK)
		.onClick(event -> seeked.seek(seeked.getProgress() * seeked.getDuration() + 1D))
		.label(Text.create("+1 s", button))
		.attach(this);
		RectNode
		.create(1270, 570, 100, 36)
		.color(UIDemoPlayer.INK)
		.onClick(event -> seeked.restart())
		.label(Text.create("Restart", button))
		.attach(this);
		TextNode.create(1320, 625).text(Text.create("Seek and restart", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/placeholder.webm"))
		.loop(true)
		.onStop(event -> stops.increment())
		.<ResourcePlayerNode>onClick(event -> event.getNode().stop())
		.attach(this);
		TextNode.create(1520, 574).text(Text.create("Stops: " + stops.get(), label)).attach(this);
		TextNode.create(1680, 625).text(Text.create("Stop", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(150, 720, 180, 180).resource(Resource.of("assets/demo/videos/video.mp4")).loop(true).volume(0F).stretch(StretchType.COVER).attach(this);
		TextNode.create(240, 965).text(Text.create("Cover", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(510, 720, 180, 180).color(UIDemoPlayer.INK).attach(this);
		ResourcePlayerNode.create(510, 720, 180, 180).resource(Resource.of("assets/demo/videos/video.mp4")).loop(true).volume(0F).stretch(StretchType.CONTAIN).attach(this);
		TextNode.create(600, 965).text(Text.create("Contain", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(870, 720, 180, 180).resource(Resource.of("assets/demo/videos/video.mp4")).loop(true).volume(0F).stretch(StretchType.STRETCH).attach(this);
		TextNode.create(960, 965).text(Text.create("Stretch", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode.create(1160, 720, 320, 180).resource(Resource.of("assets/demo/videos/video.mp4")).loop(true).volume(0F).attach(this);
		TextNode.create(1320, 965).text(Text.create("Muted", caption)).anchorX(Align.CENTER).attach(this);

		RectNode.create(1520, 720, 320, 180).color(Color.WHITE).attach(this);
		ResourcePlayerNode.create(1522, 722, 316, 176).resource(Resource.of(new ByteArrayInputStream(new byte[] {0, 1, 2, 3})).uniqueId("broken-video")).attach(this);
		TextNode.create(1680, 965).text(Text.create("Failed", caption)).anchorX(Align.CENTER).attach(this);

		TextNode.create(960, 1020).text(() -> Text.create(VideoNatives.isInstalled() ? "FFmpeg natives ready" : "FFmpeg natives not ready", label)).anchorX(Align.CENTER).attach(this);

		Shortcut
		.create(Key.SPACE)
		.onTrigger(event -> video.toggle())
		.attach(this);

		Shortcut
		.create(Key.F)
		.onTrigger(event -> {
			if (video.getWidth() < super.getViewWidth()) {
				video.x(super.getViewX()).y(super.getViewY()).width(super.getViewWidth()).height(super.getViewHeight()).zindex(1);
			} else {
				video.x(80).y(40).width(320).height(180).zindex(0);
			}
		})
		.attach(this);
	}

}