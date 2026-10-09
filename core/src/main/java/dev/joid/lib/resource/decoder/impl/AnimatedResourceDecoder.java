package dev.joid.lib.resource.decoder.impl;

import java.io.IOException;
import java.io.InputStream;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.animation.IResourceAnimationReader;
import dev.joid.lib.resource.animation.ResourceAnimation;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.playback.IResourcePlayback;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class AnimatedResourceDecoder implements IResourceDecoder, IResourcePlayback {

	private final Asset                    asset;
	private final IResourceAnimationReader reader;

	private ResourceAnimation animation;
	private ITexture          texture;

	private Integer plays;
	private boolean autoplay = true;

	private boolean running;
	private boolean paused;
	private long    startTime;
	private long    pauseTime;
	private long    position;
	private int     displayed = -1;

	public AnimatedResourceDecoder(final @NonNull Asset asset, final @NonNull IResourceAnimationReader reader) {
		this.asset = asset;
		this.reader = reader;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1);
		resource.texture(this.texture);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try (InputStream stream = this.asset.open()) {
			this.animation = this.reader.read(stream);
		} catch (final IOException exception) {
			throw new RuntimeException("Unable to read the animation of " + this.asset.getUniqueId(), exception);
		}

		resource.width(this.animation.getWidth());
		resource.height(this.animation.getHeight());
		resource.data(new int[][] {this.animation.getFrames().get(0).getPixels()});
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		this.texture.allocate(this.animation.getWidth(), this.animation.getHeight()).upload(this.animation.getFrames().get(0).getPixels(), this.animation.getWidth(), this.animation.getHeight());
		this.displayed = 0;
		if (this.autoplay) {
			this.play();
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		if (this.animation == null || this.displayed < 0) {
			return;
		}

		final long time = this.getTime();
		final int plays = this.getPlayCount();
		final long duration = this.animation.getDuration();
		if (this.running && !this.paused && plays > 0 && time >= duration * plays) {
			this.running = false;
			this.position = duration * plays - 1L;
		}

		final int index = this.animation.indexAt(this.getTime() % duration);
		if (index != this.displayed) {
			this.texture.upload(this.animation.getFrames().get(index).getPixels(), this.animation.getWidth(), this.animation.getHeight());
			this.displayed = index;
		}
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.running = false;
		this.animation = null;
		this.displayed = -1;
	}

	@Override
	public @NonNull AnimatedResourceDecoder play() {
		if (this.running) {
			return this.paused ? this.resume() : this;
		}

		this.position = 0L;
		this.startTime = BridgeHandler.CLOCK.get().nanoTime();
		this.running = true;
		this.paused = false;
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder stop() {
		this.position = this.getTime();
		this.running = false;
		this.paused = false;
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder pause() {
		if (this.running && !this.paused) {
			this.pauseTime = BridgeHandler.CLOCK.get().nanoTime();
			this.paused = true;
		}
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder resume() {
		if (this.paused) {
			this.startTime += BridgeHandler.CLOCK.get().nanoTime() - this.pauseTime;
			this.paused = false;
		}
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder seek(final double seconds) {
		final long end = this.animation == null ? Long.MAX_VALUE : this.animation.getDuration() - 1L;
		final long time = Math.max(0L, Math.min((long) (seconds * 1000D), end));
		this.position = time;
		this.startTime = (this.paused ? this.pauseTime : BridgeHandler.CLOCK.get().nanoTime()) - time * 1000000L;
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder loop(final boolean loop) {
		this.plays = loop ? 0 : 1;
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder autoplay(final boolean autoplay) {
		this.autoplay = autoplay;
		return this;
	}

	@Override
	public boolean isLoop() {
		return this.getPlayCount() == 0;
	}

	@Override
	public boolean isPlaying() {
		return this.running && !this.paused;
	}

	@Override
	public double getDuration() {
		return this.animation == null ? 0D : this.animation.getDuration() / 1000D;
	}

	@Override
	public double getProgress() {
		final double duration = this.getDuration();
		return duration <= 0D ? 0D : this.getCurrentTime() / duration;
	}

	@Override
	public double getCurrentTime() {
		return this.animation == null ? 0D : this.getTime() % this.animation.getDuration() / 1000D;
	}

	private int getPlayCount() {
		if (this.plays != null) {
			return this.plays;
		}
		return this.animation == null ? 0 : this.animation.getPlays();
	}

	private long getTime() {
		if (!this.running) {
			return this.position;
		}

		final long now = this.paused ? this.pauseTime : BridgeHandler.CLOCK.get().nanoTime();
		return (now - this.startTime) / 1000000L;
	}

}