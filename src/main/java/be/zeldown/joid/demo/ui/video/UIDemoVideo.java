package be.zeldown.joid.demo.ui.video;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.draw.text.builder.Text;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.ui.node.impl.design.video.VideoPlayerNode;

public class UIDemoVideo extends UIDemo {

	private static final double SMALL_W = 640D;
	private static final double SMALL_H = 360D;

	private VideoPlayerNode player;

	private boolean fullscreen;
	private double speedX = 0.75D;
	private double speedY = 0.5D;
	private double bounceX = 100D;
	private double bounceY = 100D;

	private int fps;
	private long fpsTimer;
	private int frameCount;
	private long frameTime;
	private long lastFrameTime;

	@Override
	public void init() {
		final Resource resource;
		try {
			resource = Resource.of(JOID.class.getResourceAsStream("/assets/test/videos/video.mp4"));
		} catch (final IOException e) {
			e.printStackTrace();
			JOID.close(this);
			return;
		}

		this.player = VideoPlayerNode
				.create(this.bounceX, this.bounceY, UIDemoVideo.SMALL_W, UIDemoVideo.SMALL_H)
				.resource(resource)
				.loop(true)
				.volume(1F)
				.onClick((n, mouseX, mouseY, clickType) -> {
					final VideoPlayerNode video = (VideoPlayerNode) n;
					if (video.isPlaying()) {
						video.pause();
					} else {
						video.resume();
					}
				})
				.attach(this);

		this.keybind(() -> {
			this.fullscreen = !this.fullscreen;
			if (this.fullscreen) {
				this.player.position(0D, 0D);
				this.player.size(1920D, 1080D);
			} else {
				this.player.position(this.bounceX, this.bounceY);
				this.player.size(UIDemoVideo.SMALL_W, UIDemoVideo.SMALL_H);
			}
		}, Keyboard.KEY_F);

		this.keybind(() -> {
			if (this.player.isPlaying()) {
				this.player.pause();
			} else {
				this.player.resume();
			}
		}, Keyboard.KEY_SPACE);
	}

	@Override
	public void preDraw(final double mouseX, final double mouseY) {
		if (!this.fullscreen && this.player != null && this.player.isPlaying()) {
			this.bounceX += this.speedX;
			this.bounceY += this.speedY;

			if (this.bounceX <= 0D || this.bounceX + UIDemoVideo.SMALL_W >= 1920D) {
				this.speedX = -this.speedX;
				this.bounceX = Math.max(0D, Math.min(this.bounceX, 1920D - UIDemoVideo.SMALL_W));
			}

			if (this.bounceY <= 0D || this.bounceY + UIDemoVideo.SMALL_H >= 1080D) {
				this.speedY = -this.speedY;
				this.bounceY = Math.max(0D, Math.min(this.bounceY, 1080D - UIDemoVideo.SMALL_H));
			}

			this.player.position(this.bounceX, this.bounceY);
		}
	}

	@Override
	public void postDraw(final double mouseX, final double mouseY) {
		if (this.player == null) {
			return;
		}

		final long now = System.nanoTime();
		this.frameTime = now - this.lastFrameTime;
		this.lastFrameTime = now;
		this.frameCount++;

		if (now - this.fpsTimer >= 1000000000L) {
			this.fps = this.frameCount;
			this.frameCount = 0;
			this.fpsTimer = now;
		}

		final Runtime runtime = Runtime.getRuntime();
		final long usedMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
		final long totalMB = runtime.totalMemory() / 1048576L;

		final VideoResourceDecoder decoder = this.player.getDecoder();
		final double progress = decoder != null ? decoder.getProgress() : 0D;
		final double duration = decoder != null ? decoder.getDuration() : 0D;
		final double currentTime = decoder != null ? decoder.getCurrentVideoTime() : 0D;
		final int videoQueueSize = decoder != null && decoder.getFrameQueue() != null ? decoder.getFrameQueue().size() : 0;
		final int audioQueueSize = decoder != null && decoder.getAudioPlayer() != null ? decoder.getAudioPlayer().getQueueSize() : 0;
		final double videoFps = decoder != null ? decoder.getFrameRate() : 0D;

		final double x = 1920D - 310D;
		final double y = 10D;
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 18, Color.WHITE);

		DrawUtils.SHAPE.drawRect(x - 10D, y - 5D, 310D, 165D, new Color(0F, 0F, 0F, 0.7F));
		DrawUtils.TEXT.drawText(x, y, Text.create("FPS: " + this.fps + " | Frame: " + String.format("%.1f", this.frameTime / 1000000D) + "ms", info));
		DrawUtils.TEXT.drawText(x, y + 22D, Text.create("RAM: " + usedMB + " / " + totalMB + " MB", info));
		DrawUtils.TEXT.drawText(x, y + 44D, Text.create("Video: " + (int) videoFps + "fps | V:" + videoQueueSize + "/5 A:" + audioQueueSize + "/128", info));
		DrawUtils.TEXT.drawText(x, y + 66D, Text.create("Time: " + String.format("%.1f", currentTime) + "s / " + String.format("%.1f", duration) + "s", info));
		DrawUtils.TEXT.drawText(x, y + 88D, Text.create("Progress: " + String.format("%.1f", progress * 100D) + "%", info));
		DrawUtils.TEXT.drawText(x, y + 110D, Text.create("State: " + (this.player.isPlaying() ? "Playing" : this.player.isPaused() ? "Paused" : "Stopped"), info));
		DrawUtils.TEXT.drawText(x, y + 132D, Text.create("Mode: " + (this.fullscreen ? "Fullscreen (F)" : "Bounce (F)"), info));
	}

}