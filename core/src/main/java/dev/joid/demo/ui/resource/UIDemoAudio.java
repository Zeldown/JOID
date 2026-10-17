package dev.joid.demo.ui.resource;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.signal.impl.primitive.BooleanSignal;
import dev.joid.lib.signal.impl.primitive.FloatSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;
import dev.joid.lib.video.VideoAudioPlayer;

@UIData(background = true)
public class UIDemoAudio extends UIDemo {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo caption = TextInfo.create(DemoFont.MONTSERRAT, 24, UIDemoAudio.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 20, UIDemoAudio.INK);
		final TextInfo button = TextInfo.create(DemoFont.MONTSERRAT, 20, Color.WHITE);
		final FloatSignal volume = new FloatSignal(1F);
		final BooleanSignal far = new BooleanSignal(false);
		final BooleanSignal away = new BooleanSignal(false);
		VideoAudioPlayer.setAudioListenerPosition(() -> Point.create(0D, 0D, away.get() ? 30D : 0D));
		super.onClose(event -> VideoAudioPlayer.setAudioListenerPosition(null));

		ResourcePlayerNode
		.create(80, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.autoplay(false)
		.volume(volume)
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		RectNode
		.create(80, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> volume.set(Math.max(0F, volume.peek() - 0.25F)))
		.label(Text.create("-", button))
		.attach(this);
		RectNode
		.create(190, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> volume.set(Math.min(1F, volume.peek() + 0.25F)))
		.label(Text.create("+", button))
		.attach(this);
		TextNode.create(300, 576).text(Text.create(Math.round(volume.get() * 100F) + " %", label)).attach(this);
		TextNode.create(240, 625).text(Text.create("Volume", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(440, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.autoplay(false)
		.location(() -> Point.create(0D, 0D, far.get() ? 30D : 0D))
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		RectNode
		.create(440, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> far.set(false))
		.label(Text.create("Near", button))
		.attach(this);
		RectNode
		.create(550, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> far.set(true))
		.label(Text.create("Far", button))
		.attach(this);
		TextNode.create(660, 576).text(Text.create("z: " + (far.get() ? 30 : 0), label)).attach(this);
		TextNode.create(600, 625).text(Text.create("Distance", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(800, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 30D))
		.referenceDistance(40F)
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		TextNode.create(800, 576).text(Text.create("z: 30, full up to 40", label)).attach(this);
		TextNode.create(960, 625).text(Text.create("Reference distance", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1160, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 30D))
		.maxDistance(20F)
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		TextNode.create(1160, 576).text(Text.create("z: 30, silent from 20", label)).attach(this);
		TextNode.create(1320, 625).text(Text.create("Max distance", caption)).anchorX(Align.CENTER).attach(this);

		ResourcePlayerNode
		.create(1520, 380, 320, 180)
		.resource(Resource.of("assets/demo/videos/video.mp4"))
		.loop(true)
		.autoplay(false)
		.location(Point.create(0D, 0D, 0D))
		.<ResourcePlayerNode>onClick(event -> event.getNode().toggle())
		.attach(this);
		RectNode
		.create(1520, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> away.set(false))
		.label(Text.create("Here", button))
		.attach(this);
		RectNode
		.create(1630, 570, 100, 36)
		.color(UIDemoAudio.INK)
		.onClick(event -> away.set(true))
		.label(Text.create("Away", button))
		.attach(this);
		TextNode.create(1740, 576).text(Text.create("z: " + (away.get() ? 30 : 0), label)).attach(this);
		TextNode.create(1680, 625).text(Text.create("Listener", caption)).anchorX(Align.CENTER).attach(this);
	}

}