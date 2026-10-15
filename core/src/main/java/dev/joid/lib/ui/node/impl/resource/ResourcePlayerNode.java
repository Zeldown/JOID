package dev.joid.lib.ui.node.impl.resource;

import java.util.function.Supplier;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.resource.playback.IResourcePlayback;
import dev.joid.lib.signal.Signal;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.event.NodeEvent;
import dev.joid.lib.ui.event.impl.node.NodeProgressEvent;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.resource.ResourceNode.StretchType;
import dev.joid.lib.utils.geom.Point;
import lombok.Getter;
import lombok.NonNull;

public class ResourcePlayerNode extends Node {

	private static final EventType<NodeEvent<?>>         END      = EventType.create("onEnd");
	private static final EventType<NodeEvent<?>>         PLAY     = EventType.create("onPlay");
	private static final EventType<NodeEvent<?>>         STOP     = EventType.create("onStop");
	private static final EventType<NodeEvent<?>>         PAUSE    = EventType.create("onPause");
	private static final EventType<NodeProgressEvent<?>> PROGRESS = EventType.create("onProgress");

	@Getter private Resource resource;

	private Resource fork;
	private boolean  wasPlaying;
	private double   lastProgress;
	private boolean  resourceStarted;

	private Double  pendingTime;
	private Boolean pendingPlaying;

	@Getter private boolean loop;
	@Getter private float   volume;
	@Getter private Object  audioGroup;
	@Getter private boolean autoplay;

	@Getter private Point location;
	@Getter private Float maxDistance;
	@Getter private Float referenceDistance;

	@Getter private StretchType stretchType;

	protected ResourcePlayerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);

		this.volume      = 1F;
		this.autoplay    = true;
		this.stretchType = StretchType.STRETCH;

		super.<ResourcePlayerNode>wait(node -> node.isReady());
		super.<ResourcePlayerNode>require("resource", "call resource(...) before attaching it", node -> node.resource != null);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y) {
		return new ResourcePlayerNode(x, y, 0, 0);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y, final double width, final double height) {
		return new ResourcePlayerNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.fork.isFailed()) {
			Color.WHITE.bind();
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.fork);
			return;
		}

		final double videoWidth = this.fork.getWidth();
		final double videoHeight = this.fork.getHeight();

		final double scale = super.getDefaultWidth() != 0D ? super.getWidth() / videoWidth : super.getDefaultHeight() != 0D ? super.getHeight() / videoHeight : 1D;
		super.fit(videoWidth * scale, videoHeight * scale);

		if (!this.resourceStarted) {
			this.resourceStarted = true;
			this.applyAudio();

			final IResourcePlayback playback = this.getPlayback();
			if (playback != null) {
				playback.stop().seek(0D).loop(this.loop).autoplay(this.autoplay);
				if (this.pendingPlaying != null ? this.pendingPlaying : this.autoplay) {
					playback.play();
				}

				if (this.pendingTime != null) {
					playback.seek(this.pendingTime);
				}
			}

			this.pendingTime = null;
			this.pendingPlaying = null;
		}

		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			final boolean playing = playback.isPlaying();
			if (playing && !this.wasPlaying) {
				super.fire(ResourcePlayerNode.PLAY, () -> NodeEvent.create(this));
			} else if (!playing && this.wasPlaying && !playback.isPaused() && !this.loop) {
				super.fire(ResourcePlayerNode.END, () -> NodeEvent.create(this));
				super.fire(ResourcePlayerNode.STOP, () -> NodeEvent.create(this));
			}

			this.wasPlaying = playing;
			final double progress = playback.getProgress();
			if (progress != this.lastProgress) {
				this.lastProgress = progress;
				super.fire(ResourcePlayerNode.PROGRESS, () -> NodeProgressEvent.create(this, progress, playback.getCurrentTime()));
			}
		}

		Color.WHITE.bind();
		this.stretchType.draw(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.fork);
	}

	@Override
	public void detach() {
		this.release();
		this.clearFork();
		this.wasPlaying = false;
		this.lastProgress = 0D;
		this.resourceStarted = false;
	}

	public final <T extends ResourcePlayerNode> @NonNull T play() {
		if (!this.resourceStarted) {
			this.pendingPlaying = true;
			return (T) this;
		}

		this.requirePlayback().play();
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T pause() {
		if (!this.resourceStarted) {
			this.pendingPlaying = false;
			return (T) this;
		}

		final IResourcePlayback playback = this.requirePlayback();
		if (playback.isPlaying()) {
			playback.pause();
			super.fire(ResourcePlayerNode.PAUSE, () -> NodeEvent.create(this));
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T resume() {
		if (!this.resourceStarted) {
			this.pendingPlaying = true;
			return (T) this;
		}

		this.requirePlayback().resume();
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T stop() {
		if (!this.resourceStarted) {
			this.pendingTime = null;
			this.pendingPlaying = false;
			return (T) this;
		}

		this.requirePlayback().stop();
		this.wasPlaying = false;
		super.fire(ResourcePlayerNode.STOP, () -> NodeEvent.create(this));
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T restart() {
		if (!this.resourceStarted) {
			this.pendingTime = null;
			this.pendingPlaying = true;
			return (T) this;
		}

		if (this.requirePlayback().restart().isPlaying()) {
			this.wasPlaying = true;
			super.fire(ResourcePlayerNode.PLAY, () -> NodeEvent.create(this));
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T seek(final double seconds) {
		if (!this.resourceStarted) {
			this.pendingTime = seconds;
			return (T) this;
		}

		this.requirePlayback().seek(seconds);
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T resource(final @NonNull Resource resource) {
		return this.resource(Signal.from(resource));
	}

	public final <T extends ResourcePlayerNode> @NonNull T resource(final @NonNull Supplier<@NonNull Resource> resource) {
		return super.follow("resource", resource, value -> {
			this.release();
			this.clearFork();
			this.resource = value;
			this.fork = ResourcePlayerNode.fork(value);
			this.resourceStarted = false;
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		return this.stretch(Signal.from(stretchType));
	}

	public final <T extends ResourcePlayerNode> @NonNull T stretch(final @NonNull Supplier<@NonNull StretchType> stretchType) {
		return super.follow("stretchType", stretchType, value -> this.stretchType = value);
	}

	public final <T extends ResourcePlayerNode> @NonNull T autoplay(final boolean autoplay) {
		return this.autoplay(Signal.from(autoplay));
	}

	public final <T extends ResourcePlayerNode> @NonNull T autoplay(final @NonNull Supplier<Boolean> autoplay) {
		return super.follow("autoplay", autoplay, value -> this.autoplay = value);
	}

	public final <T extends ResourcePlayerNode> @NonNull T loop(final boolean loop) {
		return this.loop(Signal.from(loop));
	}

	public final <T extends ResourcePlayerNode> @NonNull T loop(final @NonNull Supplier<Boolean> loop) {
		return super.follow("loop", loop, value -> {
			this.loop = value;
			final IResourcePlayback playback = this.getPlayback();
			if (playback != null) {
				playback.loop(value);
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T volume(final float volume) {
		return this.volume(Signal.from(volume));
	}

	public final <T extends ResourcePlayerNode> @NonNull T volume(final @NonNull Supplier<Float> volume) {
		return super.follow("volume", volume, value -> {
			this.volume = value;
			this.applyAudio();
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T audioGroup(final @NonNull Object audioGroup) {
		return this.audioGroup(Signal.from(audioGroup));
	}

	public final <T extends ResourcePlayerNode> @NonNull T audioGroup(final @NonNull Supplier<?> audioGroup) {
		return super.follow("audioGroup", (Supplier<Object>) audioGroup, value -> {
			this.audioGroup = value;
			this.applyAudio();
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T location(final @NonNull Point location) {
		return this.location(Signal.from(location));
	}

	public final <T extends ResourcePlayerNode> @NonNull T location(final @NonNull Supplier<@NonNull Point> location) {
		return super.follow("location", location, value -> {
			this.location = value;
			this.applyAudio();
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T referenceDistance(final float referenceDistance) {
		return this.referenceDistance(Signal.from(referenceDistance));
	}

	public final <T extends ResourcePlayerNode> @NonNull T referenceDistance(final @NonNull Supplier<Float> referenceDistance) {
		return super.follow("referenceDistance", referenceDistance, value -> {
			this.referenceDistance = value;
			this.applyAudio();
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T maxDistance(final float maxDistance) {
		return this.maxDistance(Signal.from(maxDistance));
	}

	public final <T extends ResourcePlayerNode> @NonNull T maxDistance(final @NonNull Supplier<Float> maxDistance) {
		return super.follow("maxDistance", maxDistance, value -> {
			this.maxDistance = value;
			this.applyAudio();
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T onPlay(final @NonNull EventListener<NodeEvent<T>> listener) {
		return super.listen(ResourcePlayerNode.PLAY, listener);
	}

	public final <T extends ResourcePlayerNode> @NonNull T onPause(final @NonNull EventListener<NodeEvent<T>> listener) {
		return super.listen(ResourcePlayerNode.PAUSE, listener);
	}

	public final <T extends ResourcePlayerNode> @NonNull T onStop(final @NonNull EventListener<NodeEvent<T>> listener) {
		return super.listen(ResourcePlayerNode.STOP, listener);
	}

	public final <T extends ResourcePlayerNode> @NonNull T onEnd(final @NonNull EventListener<NodeEvent<T>> listener) {
		return super.listen(ResourcePlayerNode.END, listener);
	}

	public final <T extends ResourcePlayerNode> @NonNull T onProgress(final @NonNull EventListener<NodeProgressEvent<T>> listener) {
		return super.listen(ResourcePlayerNode.PROGRESS, listener);
	}

	public final double getDuration() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null ? playback.getDuration() : 0D;
	}

	public final double getProgress() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null ? playback.getProgress() : 0D;
	}

	public final IResourcePlayback getPlayback() {
		return this.fork == null ? null : this.fork.getPlayback();
	}

	public final VideoResourceDecoder getVideo() {
		return this.fork == null || !(this.fork.getDecoder() instanceof VideoResourceDecoder) ? null : (VideoResourceDecoder) this.fork.getDecoder();
	}

	public final boolean isPaused() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null && playback.isPaused();
	}

	public final boolean isPlaying() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null && playback.isPlaying();
	}

	private @NonNull IResourcePlayback requirePlayback() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback == null) {
			throw new IllegalStateException("The resource " + this.fork.getUniqueId() + " of this ResourcePlayerNode has no playback: give it a video or an animated image, or show a still image with a ResourceNode");
		}
		return playback;
	}

	private boolean isReady() {
		if (this.fork == null) {
			if (this.resource.isBlocking()) {
				this.resource.getResourceData().await();
			}
			this.fork = ResourcePlayerNode.fork(this.resource);
			if (this.fork == null) {
				return false;
			}
		}

		this.fork.prepareBind();
		return this.fork.isFailed() || this.fork.isLoaded() && this.fork.getWidth() != 0 && this.fork.getHeight() != 0;
	}

	private void clearFork() {
		if (this.fork != null && this.fork != this.resource) {
			this.fork.clear();
		}
		this.fork = null;
	}

	private void release() {
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.release();
		}
	}

	private void applyAudio() {
		final VideoResourceDecoder video = this.getVideo();
		if (video == null) {
			return;
		}

		video.volume(this.volume).audioGroup(this.audioGroup);
		if (this.location != null) {
			video.location((float) this.location.getX(), (float) this.location.getY(), (float) this.location.getZ());
		}

		if (this.maxDistance != null) {
			video.maxDistance(this.maxDistance);
		}

		if (this.referenceDistance != null) {
			video.referenceDistance(this.referenceDistance);
		}
	}

	private static Resource fork(final @NonNull Resource resource) {
		return resource.getDecoder() != null || resource.isFailed() || resource.getTexture() != null ? resource.fork() : null;
	}

}