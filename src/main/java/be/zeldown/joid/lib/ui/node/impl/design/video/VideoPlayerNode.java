package be.zeldown.joid.lib.ui.node.impl.design.video;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import be.zeldown.joid.lib.ui.node.Node;
import be.zeldown.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import be.zeldown.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import be.zeldown.joid.lib.ui.node.impl.design.video.callback.NodeVideoEndCallback;
import be.zeldown.joid.lib.ui.node.impl.design.video.callback.NodeVideoPauseCallback;
import be.zeldown.joid.lib.ui.node.impl.design.video.callback.NodeVideoPlayCallback;
import be.zeldown.joid.lib.ui.node.impl.design.video.callback.NodeVideoProgressCallback;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class VideoPlayerNode extends Node {

	public static final int CALLBACK_PLAY     = NodeCallbackRegistry.next(NodeVideoPlayCallback.class);
	public static final int CALLBACK_PAUSE    = NodeCallbackRegistry.next(NodeVideoPauseCallback.class);
	public static final int CALLBACK_END      = NodeCallbackRegistry.next(NodeVideoEndCallback.class);
	public static final int CALLBACK_PROGRESS = NodeCallbackRegistry.next(NodeVideoProgressCallback.class);

	private Resource resource;
	private boolean wasPlaying;
	private double lastProgress;
	private boolean resourceStarted;

	private boolean loop;
	private float volume = 1F;
	private boolean autoplay = true;

	private StretchType stretchType = StretchType.STRETCH;

	protected VideoPlayerNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull VideoPlayerNode create(final double x, final double y) {
		return new VideoPlayerNode(x, y, 0, 0);
	}

	public static @NonNull VideoPlayerNode create(final double x, final double y, final double width, final double height) {
		return new VideoPlayerNode(x, y, width, height);
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

		if (!this.resourceStarted) {
			this.resourceStarted = true;
			final VideoResourceDecoder decoder = this.getDecoder();
			if (decoder != null) {
				decoder.stop();
				decoder.seek(0D);
				decoder.loop(this.loop);
				decoder.volume(this.volume);
				decoder.autoplay(this.autoplay);

				if (this.autoplay) {
					decoder.play();
				}
			}
		}

		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			final boolean playing = decoder.isPlaying();
			if (playing && !this.wasPlaying) {
				super.executeCallback(VideoPlayerNode.CALLBACK_PLAY, InternalContext.create());
			} else if (!playing && this.wasPlaying && !decoder.isPaused()) {
				super.executeCallback(VideoPlayerNode.CALLBACK_END, InternalContext.create());
			}

			this.wasPlaying = playing;
			if (playing) {
				final double progress = decoder.getProgress();
				if (progress != this.lastProgress) {
					this.lastProgress = progress;
					super.executeCallback(VideoPlayerNode.CALLBACK_PROGRESS, InternalContext.create(), progress, decoder.getCurrentVideoTime());
				}
			}
		}

		Color.WHITE.bind();
		if (this.stretchType == StretchType.STRETCH) {
			DrawUtils.RESOURCE.drawResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
		} else if (this.stretchType == StretchType.CONTAIN) {
			DrawUtils.RESOURCE.drawCenteredResource(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.resource);
		}
	}

	/* [ Control Section ] */
	public final @NonNull VideoPlayerNode play() {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.play();
		}
		return this;
	}

	public final @NonNull VideoPlayerNode pause() {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.pause();
			super.executeCallback(VideoPlayerNode.CALLBACK_PAUSE, InternalContext.create());
		}
		return this;
	}

	public final @NonNull VideoPlayerNode resume() {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.resume();
		}
		return this;
	}

	public final @NonNull VideoPlayerNode stop() {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.stop();
		}
		return this;
	}

	public final @NonNull VideoPlayerNode seek(final double seconds) {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.seek(seconds);
		}
		return this;
	}

	public final @NonNull VideoPlayerNode seekTo(final double seconds) {
		return this.seek(seconds);
	}

	public final @NonNull VideoPlayerNode restart() {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.stop();
			decoder.seek(0D);
			decoder.play();
		}
		return this;
	}

	/* [ Callback Section ] */
	public final <T extends VideoPlayerNode> @NonNull T onPlay(final @NonNull NodeVideoPlayCallback<T> callback) {
		super.registerCallback(VideoPlayerNode.CALLBACK_PLAY, callback);
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T onPause(final @NonNull NodeVideoPauseCallback<T> callback) {
		super.registerCallback(VideoPlayerNode.CALLBACK_PAUSE, callback);
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T onEnd(final @NonNull NodeVideoEndCallback<T> callback) {
		super.registerCallback(VideoPlayerNode.CALLBACK_END, callback);
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T onProgress(final @NonNull NodeVideoProgressCallback<T> callback) {
		super.registerCallback(VideoPlayerNode.CALLBACK_PROGRESS, callback);
		return (T) this;
	}

	@Override
	public void detach() {
		this.release();
	}

	/* [ Setter Section ] */
	public final <T extends VideoPlayerNode> @NonNull T resource(final @NonNull Resource resource) {
		this.release();
		this.resource = resource;
		this.resourceStarted = false;
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T stretch(final @NonNull StretchType stretchType) {
		this.stretchType = stretchType;
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T autoplay(final boolean autoplay) {
		this.autoplay = autoplay;
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T loop(final boolean loop) {
		this.loop = loop;
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.loop(loop);
		}
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T volume(final float volume) {
		this.volume = volume;
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.volume(volume);
		}
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T location(final float x, final float y, final float z) {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.location(x, y, z);
		}
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T referenceDistance(final float distance) {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.referenceDistance(distance);
		}
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T maxDistance(final float distance) {
		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.maxDistance(distance);
		}
		return (T) this;
	}

	/* [ Internal Section ] */
	private void release() {
		if (this.resource == null) {
			return;
		}

		final VideoResourceDecoder decoder = this.getDecoder();
		if (decoder != null) {
			decoder.release();
		}
	}

	/* [ Getter Section ] */
	public final VideoResourceDecoder getDecoder() {
		if (this.resource == null || !(this.resource.getDecoder() instanceof VideoResourceDecoder)) {
			return null;
		}
		return (VideoResourceDecoder) this.resource.getDecoder();
	}

	public final boolean isPlaying() {
		final VideoResourceDecoder decoder = this.getDecoder();
		return decoder != null && decoder.isPlaying();
	}

	public final boolean isPaused() {
		final VideoResourceDecoder decoder = this.getDecoder();
		return decoder != null && decoder.isPaused();
	}

	public final double getDuration() {
		final VideoResourceDecoder decoder = this.getDecoder();
		return decoder != null ? decoder.getDuration() : 0D;
	}

	public final double getProgress() {
		final VideoResourceDecoder decoder = this.getDecoder();
		return decoder != null ? decoder.getProgress() : 0D;
	}

}