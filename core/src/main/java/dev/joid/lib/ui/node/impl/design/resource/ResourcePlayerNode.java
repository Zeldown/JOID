package dev.joid.lib.ui.node.impl.design.resource;

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
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.utils.point.Point;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ResourcePlayerNode extends Node {

	private static final EventType<NodeEvent<?>>         END      = EventType.create("onEnd");
	private static final EventType<NodeEvent<?>>         PLAY     = EventType.create("onPlay");
	private static final EventType<NodeEvent<?>>         STOP     = EventType.create("onStop");
	private static final EventType<NodeEvent<?>>         PAUSE    = EventType.create("onPause");
	private static final EventType<NodeProgressEvent<?>> PROGRESS = EventType.create("onProgress");

	private Resource resource;
	private Resource fork;
	private boolean wasPlaying;
	private double lastProgress;
	private boolean resourceStarted;

	private Double  pendingTime;
	private Boolean pendingPlaying;

	private boolean loop;
	private float volume = 1F;
	private boolean autoplay = true;
	private Object audioGroup;

	private Point location;
	private Float    maxDistance;
	private Float    referenceDistance;

	private StretchType stretchType = StretchType.STRETCH;

	protected ResourcePlayerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
		super.<ResourcePlayerNode>wait(node -> node.isReady());
		super.<ResourcePlayerNode>require("resource", "call resource(...) before attaching it", node -> node.resource != null);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y) {
		return new ResourcePlayerNode(x, y, 0, 0);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y, final double width, final double height) {
		return new ResourcePlayerNode(x, y, width, height);
	}

	public final @NonNull ResourcePlayerNode stop() {
		if (!this.resourceStarted) {
			this.pendingTime = null;
			this.pendingPlaying = false;
			return this;
		}

		this.requirePlayback().stop();
		this.wasPlaying = false;
		super.fire(ResourcePlayerNode.STOP, () -> NodeEvent.create(this));
		return this;
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

		if (super.getWidth() == 0 && super.getHeight() == 0) {
			super.width(videoWidth);
			super.height(videoHeight);
			return;
		}

		if (super.getWidth() == 0) {
			super.width(videoWidth * super.getHeight() / videoHeight);
			return;
		}

		if (super.getHeight() == 0) {
			super.height(videoHeight * super.getWidth() / videoWidth);
			return;
		}

		if (!this.resourceStarted) {
			this.resourceStarted = true;
			final VideoResourceDecoder video = this.getVideo();
			if (video != null) {
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

	public final @NonNull ResourcePlayerNode play() {
		if (!this.resourceStarted) {
			this.pendingPlaying = true;
			return this;
		}

		this.requirePlayback().play();
		return this;
	}

	public final @NonNull ResourcePlayerNode pause() {
		if (!this.resourceStarted) {
			this.pendingPlaying = false;
			return this;
		}

		final IResourcePlayback playback = this.requirePlayback();
		if (playback.isPlaying()) {
			playback.pause();
			super.fire(ResourcePlayerNode.PAUSE, () -> NodeEvent.create(this));
		}
		return this;
	}

	public final @NonNull ResourcePlayerNode resume() {
		if (!this.resourceStarted) {
			this.pendingPlaying = true;
			return this;
		}

		this.requirePlayback().resume();
		return this;
	}

	public final @NonNull ResourcePlayerNode seek(final double seconds) {
		if (!this.resourceStarted) {
			this.pendingTime = seconds;
			return this;
		}

		this.requirePlayback().seek(seconds);
		return this;
	}

	public final @NonNull ResourcePlayerNode restart() {
		if (!this.resourceStarted) {
			this.pendingTime = null;
			this.pendingPlaying = true;
			return this;
		}

		if (this.requirePlayback().restart().isPlaying()) {
			this.wasPlaying = true;
			super.fire(ResourcePlayerNode.PLAY, () -> NodeEvent.create(this));
		}
		return this;
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

	@Override
	public void detach() {
		this.release();
		this.wasPlaying = false;
		this.lastProgress = 0D;
		this.resourceStarted = false;
	}

	public final <T extends ResourcePlayerNode> @NonNull T resource(final @NonNull Resource resource) {
		return this.resource(Signal.from(resource));
	}

	public final <T extends ResourcePlayerNode> @NonNull T resource(final @NonNull Supplier<@NonNull Resource> resource) {
		return super.follow("resource", resource, (final ResourcePlayerNode node, final Resource value) -> {
			node.release();
			if (node.fork != null && node.fork != node.resource) {
				node.fork.clear();
			}

			node.resource = value;
			node.fork = ResourcePlayerNode.fork(value);
			node.resourceStarted = false;
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		return this.stretch(Signal.from(stretchType));
	}

	public final <T extends ResourcePlayerNode> @NonNull T stretch(final @NonNull Supplier<@NonNull StretchType> stretchType) {
		return super.follow("stretchType", stretchType, (final ResourcePlayerNode node, final StretchType value) -> node.stretchType = value);
	}

	public final <T extends ResourcePlayerNode> @NonNull T autoplay(final boolean autoplay) {
		return this.autoplay(Signal.from(autoplay));
	}

	public final <T extends ResourcePlayerNode> @NonNull T autoplay(final @NonNull Supplier<Boolean> autoplay) {
		return super.follow("autoplay", autoplay, (final ResourcePlayerNode node, final Boolean value) -> node.autoplay = value);
	}

	public final <T extends ResourcePlayerNode> @NonNull T loop(final boolean loop) {
		return this.loop(Signal.from(loop));
	}

	public final <T extends ResourcePlayerNode> @NonNull T loop(final @NonNull Supplier<Boolean> loop) {
		return super.follow("loop", loop, (final ResourcePlayerNode node, final Boolean value) -> {
			node.loop = value;
			final IResourcePlayback playback = node.getPlayback();
			if (playback != null) {
				playback.loop(value);
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T volume(final float volume) {
		return this.volume(Signal.from(volume));
	}

	public final <T extends ResourcePlayerNode> @NonNull T volume(final @NonNull Supplier<Float> volume) {
		return super.follow("volume", volume, (final ResourcePlayerNode node, final Float value) -> {
			node.volume = value;
			final VideoResourceDecoder video = node.getVideo();
			if (video != null) {
				video.volume(value);
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T audioGroup(final @NonNull Object audioGroup) {
		return this.audioGroup(Signal.from(audioGroup));
	}

	public final <T extends ResourcePlayerNode> @NonNull T audioGroup(final @NonNull Supplier<?> audioGroup) {
		return super.follow("audioGroup", (Supplier<Object>) audioGroup, (final ResourcePlayerNode node, final Object value) -> {
			node.audioGroup = value;
			final VideoResourceDecoder video = node.getVideo();
			if (video != null) {
				video.audioGroup(value);
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T location(final @NonNull Point location) {
		return this.location(Signal.from(location));
	}

	public final <T extends ResourcePlayerNode> @NonNull T location(final @NonNull Supplier<@NonNull Point> location) {
		return super.follow("location", location, (final ResourcePlayerNode node, final Point value) -> {
			node.location = value;
			final VideoResourceDecoder video = node.getVideo();
			if (video != null) {
				video.location((float) value.getX(), (float) value.getY(), (float) value.getZ());
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T referenceDistance(final float referenceDistance) {
		return this.referenceDistance(Signal.from(referenceDistance));
	}

	public final <T extends ResourcePlayerNode> @NonNull T referenceDistance(final @NonNull Supplier<Float> referenceDistance) {
		return super.follow("referenceDistance", referenceDistance, (final ResourcePlayerNode node, final Float value) -> {
			node.referenceDistance = value;
			final VideoResourceDecoder video = node.getVideo();
			if (video != null) {
				video.referenceDistance(value);
			}
		});
	}

	public final <T extends ResourcePlayerNode> @NonNull T maxDistance(final float maxDistance) {
		return this.maxDistance(Signal.from(maxDistance));
	}

	public final <T extends ResourcePlayerNode> @NonNull T maxDistance(final @NonNull Supplier<Float> maxDistance) {
		return super.follow("maxDistance", maxDistance, (final ResourcePlayerNode node, final Float value) -> {
			node.maxDistance = value;
			final VideoResourceDecoder video = node.getVideo();
			if (video != null) {
				video.maxDistance(value);
			}
		});
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
		}

		this.fork.prepareBind();
		return this.fork.isFailed() || this.fork.isLoaded() && this.fork.getWidth() != 0 && this.fork.getHeight() != 0;
	}

	private void release() {
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.release();
		}
	}

	private static Resource fork(final @NonNull Resource resource) {
		return resource.getDecoder() != null || resource.isFailed() || resource.getTexture() != null ? resource.fork() : null;
	}

}