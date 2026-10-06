package dev.joid.lib.ui.node.impl.design.resource;

import javax.vecmath.Vector3f;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.resource.dto.playback.IResourcePlayback;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.resource.callback.NodeResourcePlayerEndCallback;
import dev.joid.lib.ui.node.impl.design.resource.callback.NodeResourcePlayerPauseCallback;
import dev.joid.lib.ui.node.impl.design.resource.callback.NodeResourcePlayerPlayCallback;
import dev.joid.lib.ui.node.impl.design.resource.callback.NodeResourcePlayerProgressCallback;
import dev.joid.lib.ui.node.impl.design.resource.callback.NodeResourcePlayerStopCallback;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class ResourcePlayerNode extends Node {

	public static final int CALLBACK_END      = NodeCallbackRegistry.next(NodeResourcePlayerEndCallback.class);
	public static final int CALLBACK_PLAY     = NodeCallbackRegistry.next(NodeResourcePlayerPlayCallback.class);
	public static final int CALLBACK_STOP     = NodeCallbackRegistry.next(NodeResourcePlayerStopCallback.class);
	public static final int CALLBACK_PAUSE    = NodeCallbackRegistry.next(NodeResourcePlayerPauseCallback.class);
	public static final int CALLBACK_PROGRESS = NodeCallbackRegistry.next(NodeResourcePlayerProgressCallback.class);

	private Resource resource;
	private boolean wasPlaying;
	private double lastProgress;
	private boolean resourceStarted;

	private boolean loop;
	private float volume = 1F;
	private boolean autoplay = true;

	private Vector3f location;
	private Float    maxDistance;
	private Float    referenceDistance;

	private StretchType stretchType = StretchType.STRETCH;

	protected ResourcePlayerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y) {
		return new ResourcePlayerNode(x, y, 0, 0);
	}

	public static @NonNull ResourcePlayerNode create(final double x, final double y, final double width, final double height) {
		return new ResourcePlayerNode(x, y, width, height);
	}

	public final @NonNull ResourcePlayerNode stop() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.stop();
			this.wasPlaying = false;
			super.executeCallback(ResourcePlayerNode.CALLBACK_STOP, InternalContext.create());
		}
		return this;
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (this.resource != null) {
			this.resource.prepareBind();
		}

		if (this.resource == null || !this.resource.isLoaded()) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.LOADING());
			return;
		}

		final double videoWidth = this.resource.getWidth();
		final double videoHeight = this.resource.getHeight();
		if (videoWidth == 0 || videoHeight == 0) {
			DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.LOADING());
			return;
		}

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
				video.volume(this.volume);
				if (this.location != null) {
					video.location(this.location.x, this.location.y, this.location.z);
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
				if (this.autoplay) {
					playback.play();
				}
			}
		}

		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			final boolean playing = playback.isPlaying();
			if (playing && !this.wasPlaying) {
				super.executeCallback(ResourcePlayerNode.CALLBACK_PLAY, InternalContext.create());
			} else if (!playing && this.wasPlaying && !playback.isPaused() && !this.loop) {
				super.executeCallback(ResourcePlayerNode.CALLBACK_END, InternalContext.create());
				super.executeCallback(ResourcePlayerNode.CALLBACK_STOP, InternalContext.create());
			}

			this.wasPlaying = playing;
			if (playing) {
				final double progress = playback.getProgress();
				if (progress != this.lastProgress) {
					this.lastProgress = progress;
					super.executeCallback(ResourcePlayerNode.CALLBACK_PROGRESS, InternalContext.create(), progress, playback.getCurrentTime());
				}
			}
		}

		Color.WHITE.bind();
		this.stretchType.draw(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
	}

	public final @NonNull ResourcePlayerNode play() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.play();
		}
		return this;
	}

	public final @NonNull ResourcePlayerNode pause() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.pause();
			super.executeCallback(ResourcePlayerNode.CALLBACK_PAUSE, InternalContext.create());
		}
		return this;
	}

	public final @NonNull ResourcePlayerNode resume() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.resume();
		}
		return this;
	}

	public final @NonNull ResourcePlayerNode seek(final double seconds) {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.seek(seconds);
		}
		return this;
	}

	public final @NonNull ResourcePlayerNode seekTo(final double seconds) {
		return this.seek(seconds);
	}

	public final @NonNull ResourcePlayerNode restart() {
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null && playback.restart().isPlaying()) {
			this.wasPlaying = true;
			super.executeCallback(ResourcePlayerNode.CALLBACK_PLAY, InternalContext.create());
		}
		return this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T onPlay(final @NonNull NodeResourcePlayerPlayCallback<T> callback) {
		super.registerCallback(ResourcePlayerNode.CALLBACK_PLAY, callback);
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T onPause(final @NonNull NodeResourcePlayerPauseCallback<T> callback) {
		super.registerCallback(ResourcePlayerNode.CALLBACK_PAUSE, callback);
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T onStop(final @NonNull NodeResourcePlayerStopCallback<T> callback) {
		super.registerCallback(ResourcePlayerNode.CALLBACK_STOP, callback);
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T onEnd(final @NonNull NodeResourcePlayerEndCallback<T> callback) {
		super.registerCallback(ResourcePlayerNode.CALLBACK_END, callback);
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T onProgress(final @NonNull NodeResourcePlayerProgressCallback<T> callback) {
		super.registerCallback(ResourcePlayerNode.CALLBACK_PROGRESS, callback);
		return (T) this;
	}

	@Override
	public void detach() {
		this.release();
	}

	public final <T extends ResourcePlayerNode> @NonNull T resource(final @NonNull Resource resource) {
		this.release();
		this.resource = resource;
		this.resourceStarted = false;
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		this.stretchType = stretchType;
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T autoplay(final boolean autoplay) {
		this.autoplay = autoplay;
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T loop(final boolean loop) {
		this.loop = loop;
		final IResourcePlayback playback = this.getPlayback();
		if (playback != null) {
			playback.loop(loop);
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T volume(final float volume) {
		this.volume = volume;
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.volume(volume);
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T location(final float x, final float y, final float z) {
		this.location = new Vector3f(x, y, z);
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.location(x, y, z);
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T referenceDistance(final float distance) {
		this.referenceDistance = distance;
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.referenceDistance(distance);
		}
		return (T) this;
	}

	public final <T extends ResourcePlayerNode> @NonNull T maxDistance(final float distance) {
		this.maxDistance = distance;
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.maxDistance(distance);
		}
		return (T) this;
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
		return this.resource == null ? null : this.resource.getPlayback();
	}

	public final VideoResourceDecoder getVideo() {
		return this.resource == null || !(this.resource.getDecoder() instanceof VideoResourceDecoder) ? null : (VideoResourceDecoder) this.resource.getDecoder();
	}

	public final boolean isPaused() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null && playback.isPaused();
	}

	public final boolean isPlaying() {
		final IResourcePlayback playback = this.getPlayback();
		return playback != null && playback.isPlaying();
	}

	private void release() {
		final VideoResourceDecoder video = this.getVideo();
		if (video != null) {
			video.release();
		}
	}

}