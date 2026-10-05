package dev.joid.lib.ui.node.impl.design.video;

import java.util.Optional;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.resource.dto.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.resource.dto.playback.IPlayback;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.callback.registry.NodeCallbackRegistry;
import dev.joid.lib.ui.node.impl.design.resource.ResourceNode.StretchType;
import dev.joid.lib.ui.node.impl.design.video.callback.NodeVideoEndCallback;
import dev.joid.lib.ui.node.impl.design.video.callback.NodeVideoPauseCallback;
import dev.joid.lib.ui.node.impl.design.video.callback.NodeVideoPlayCallback;
import dev.joid.lib.ui.node.impl.design.video.callback.NodeVideoProgressCallback;
import dev.joid.lib.utils.context.InternalContext;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public class VideoPlayerNode extends Node {

	public static final int CALLBACK_END      = NodeCallbackRegistry.next(NodeVideoEndCallback.class);
	public static final int CALLBACK_PLAY     = NodeCallbackRegistry.next(NodeVideoPlayCallback.class);
	public static final int CALLBACK_PAUSE    = NodeCallbackRegistry.next(NodeVideoPauseCallback.class);
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

	public final @NonNull VideoPlayerNode stop() {
		this.getPlayback().ifPresent(IPlayback::stop);
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

		if (!this.resourceStarted) {
			this.resourceStarted = true;
			this.getVideo().ifPresent(video -> video.volume(this.volume));
			this.getPlayback().ifPresent(playback -> {
				playback.stop().seek(0D).loop(this.loop).autoplay(this.autoplay);
				if (this.autoplay) {
					playback.play();
				}
			});
		}

		final Optional<IPlayback> playback = this.getPlayback();
		if (playback.isPresent()) {
			final boolean playing = playback.get().isPlaying();
			if (playing && !this.wasPlaying) {
				super.executeCallback(VideoPlayerNode.CALLBACK_PLAY, InternalContext.create());
			} else if (!playing && this.wasPlaying && !playback.get().isPaused()) {
				super.executeCallback(VideoPlayerNode.CALLBACK_END, InternalContext.create());
			}

			this.wasPlaying = playing;
			if (playing) {
				final double progress = playback.get().getProgress();
				if (progress != this.lastProgress) {
					this.lastProgress = progress;
					super.executeCallback(VideoPlayerNode.CALLBACK_PROGRESS, InternalContext.create(), progress, playback.get().getCurrentTime());
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

	public final @NonNull VideoPlayerNode play() {
		this.getPlayback().ifPresent(IPlayback::play);
		return this;
	}

	public final @NonNull VideoPlayerNode pause() {
		this.getPlayback().ifPresent(playback -> {
			playback.pause();
			super.executeCallback(VideoPlayerNode.CALLBACK_PAUSE, InternalContext.create());
		});
		return this;
	}

	public final @NonNull VideoPlayerNode resume() {
		this.getPlayback().ifPresent(IPlayback::resume);
		return this;
	}

	public final @NonNull VideoPlayerNode seek(final double seconds) {
		this.getPlayback().ifPresent(playback -> playback.seek(seconds));
		return this;
	}

	public final @NonNull VideoPlayerNode seekTo(final double seconds) {
		return this.seek(seconds);
	}

	public final @NonNull VideoPlayerNode restart() {
		this.getPlayback().ifPresent(playback -> playback.stop().seek(0D).play());
		return this;
	}

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
		this.getPlayback().ifPresent(playback -> playback.loop(loop));
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T volume(final float volume) {
		this.volume = volume;
		this.getVideo().ifPresent(video -> video.volume(volume));
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T location(final float x, final float y, final float z) {
		this.getVideo().ifPresent(video -> video.location(x, y, z));
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T referenceDistance(final float distance) {
		this.getVideo().ifPresent(video -> video.referenceDistance(distance));
		return (T) this;
	}

	public final <T extends VideoPlayerNode> @NonNull T maxDistance(final float distance) {
		this.getVideo().ifPresent(video -> video.maxDistance(distance));
		return (T) this;
	}

	public final double getDuration() {
		return this.getPlayback().map(IPlayback::getDuration).orElse(0D);
	}

	public final double getProgress() {
		return this.getPlayback().map(IPlayback::getProgress).orElse(0D);
	}

	public final @NonNull Optional<IPlayback> getPlayback() {
		return this.resource == null ? Optional.empty() : this.resource.getPlayback();
	}

	public final @NonNull Optional<VideoResourceDecoder> getVideo() {
		return this.resource == null || !(this.resource.getDecoder() instanceof VideoResourceDecoder) ? Optional.empty() : Optional.of((VideoResourceDecoder) this.resource.getDecoder());
	}

	public final boolean isPaused() {
		return this.getPlayback().map(IPlayback::isPaused).orElse(false);
	}

	public final boolean isPlaying() {
		return this.getPlayback().map(IPlayback::isPlaying).orElse(false);
	}

	private void release() {
		this.getVideo().ifPresent(VideoResourceDecoder::release);
	}

}