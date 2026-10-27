package dev.joid.lib.resource.decoder.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

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

public final class AnimatedResourceDecoder implements IResourceDecoder, IResourcePlayback {

	@Getter private final Asset            asset;
	private final AnimatedResourceDecoder  origin;
	private final IResourceAnimationReader reader;

	private ITexture          texture;
	private ResourceAnimation source;
	private ResourceAnimation animation;

	private Integer         plays;
	@Getter private boolean autoplay = true;

	private long            position;
	private int             displayed = -1;
	private boolean         seeked;
	private long            startTime;
	private long            pauseTime;
	private boolean         running;
	@Getter private boolean paused;

	private AnimatedResourceDecoder(final Asset asset, final IResourceAnimationReader reader) {
		this.asset = asset;
		this.reader = reader;
		this.origin = this;
	}

	private AnimatedResourceDecoder(final AnimatedResourceDecoder origin) {
		this.asset = origin.asset;
		this.reader = origin.reader;
		this.origin = origin;
	}

	public static @NonNull AnimatedResourceDecoder create(final @NonNull Asset asset, final @NonNull IResourceAnimationReader reader) {
		return new AnimatedResourceDecoder(asset, reader);
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.texture = ResourceData.placeholder();
		resource.texture(this.texture);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		this.animation = this.origin.read();
		resource.width(this.animation.getWidth());
		resource.height(this.animation.getHeight());
		resource.data(new int[][] {this.animation.getFrames().get(0).getPixels()});
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		this.texture.allocate(this.animation.getWidth(), this.animation.getHeight()).upload(this.animation.getFrames().get(0).getPixels());
		this.displayed = 0;
		if (this.autoplay) {
			this.play();
		}
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.running = false;
		this.animation = null;
		this.displayed = -1;
		if (this.origin == this) {
			this.clearSource();
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		if (this.displayed < 0) {
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
			this.texture.upload(this.animation.getFrames().get(index).getPixels());
			this.displayed = index;
		}
	}

	@Override
	public @NonNull AnimatedResourceDecoder fork() {
		return new AnimatedResourceDecoder(this.origin);
	}

	@Override
	public @NonNull AnimatedResourceDecoder play() {
		if (this.running) {
			if (this.paused) {
				this.startTime += BridgeHandler.CLOCK.get().nanoTime() - this.pauseTime;
				this.paused = false;
			}
			return this;
		}

		this.position = this.seeked ? this.position : 0L;
		this.startTime = BridgeHandler.CLOCK.get().nanoTime() - this.position * 1000000L;
		this.seeked = false;
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
	public @NonNull AnimatedResourceDecoder seek(final double seconds) {
		if (this.animation != null && !this.running) {
			this.play().pause();
		}

		final long end = this.animation == null ? Long.MAX_VALUE : this.animation.getDuration() - 1L;
		final long time = Math.max(0L, Math.min((long) (seconds * 1000D), end));
		this.position = time;
		this.startTime = (this.paused ? this.pauseTime : BridgeHandler.CLOCK.get().nanoTime()) - time * 1000000L;
		this.seeked = !this.running;
		return this;
	}

	@Override
	public @NonNull AnimatedResourceDecoder loop(final boolean loop) {
		if (this.running && this.animation != null) {
			final long duration = this.animation.getDuration();
			this.startTime += this.getTime() / duration * duration * 1000000L;
		}
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

	private synchronized @NonNull ResourceAnimation read() {
		if (this.source == null) {
			try (final InputStream stream = this.asset.open()) {
				this.source = this.reader.read(stream);
			} catch (final IOException exception) {
				throw new UncheckedIOException("The animation " + this.asset.getUniqueId() + " cannot be read", exception);
			}
		}
		return this.source;
	}

	private synchronized void clearSource() {
		this.source = null;
	}

	private long getTime() {
		if (!this.running) {
			return this.position;
		}

		final long now = this.paused ? this.pauseTime : BridgeHandler.CLOCK.get().nanoTime();
		return (now - this.startTime) / 1000000L;
	}

	private int getPlayCount() {
		if (this.plays != null) {
			return this.plays;
		}
		return this.animation == null ? 0 : this.animation.getPlays();
	}

}