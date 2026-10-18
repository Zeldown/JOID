package dev.joid.lib.resource.decoder.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.impl.FileAsset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.cache.FileCacheArea;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.resource.playback.IResourcePlayback;
import dev.joid.lib.utils.geom.Point;
import dev.joid.lib.utils.thread.ThreadUtils;
import dev.joid.lib.video.VideoAudioPlayer;
import dev.joid.lib.video.VideoNatives;
import dev.joid.lib.video.VideoNativesException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class VideoResourceDecoder implements IResourceDecoder, IResourcePlayback {

	private static final FileCacheArea AREA = FileCache.area("video").retention(Duration.ofDays(7L));

	@Getter private final Asset        asset;
	private final VideoResourceDecoder origin;
	private final Object               grabberLock = new Object();
	private final AtomicInteger        seeks       = new AtomicInteger(0);
	private final AtomicBoolean        paused      = new AtomicBoolean(false);
	private final AtomicBoolean        running     = new AtomicBoolean(false);

	private File   file;
	private String codec;

	private File shared;

	private Thread             decodeThread;
	private FFmpegFrameGrabber grabber;
	private VideoAudioPlayer   audioPlayer;

	private ArrayBlockingQueue<DecodedFrame> frameQueue;

	private volatile boolean     ended;
	private volatile boolean     seeked;
	private volatile double      seekTime;
	private volatile double      loopOffset;
	@Getter private volatile int displayedFrameIndex;

	private long startTime;
	private long pauseTime;

	private volatile boolean released;
	private boolean          playRequested;

	private int        currentBuffer;
	private ITexture[] textures;
	private boolean    texturesAllocated;

	@Getter private int    videoWidth;
	@Getter private int    videoHeight;
	@Getter private int    totalFrames;
	@Getter private double duration;
	@Getter private double frameRate;

	@Getter private boolean loop;
	@Getter private float   volume   = 1F;
	@Getter private Object  audioGroup;
	@Getter private boolean autoplay = true;

	@Getter private Point location;
	@Getter private float maxDistance       = 50F;
	@Getter private float referenceDistance = 5F;

	protected VideoResourceDecoder(final Asset asset) {
		this.asset = asset;
		this.origin = this;
	}

	private VideoResourceDecoder(final VideoResourceDecoder origin) {
		this.asset = origin.asset;
		this.origin = origin;
	}

	public static @NonNull VideoResourceDecoder create(final @NonNull File file) {
		return new VideoResourceDecoder(FileAsset.of(file));
	}

	public static @NonNull VideoResourceDecoder create(final @NonNull Asset asset) {
		return new VideoResourceDecoder(asset);
	}

	@Override
	public @NonNull VideoResourceDecoder stop() {
		this.playRequested = false;
		this.running.set(false);
		this.paused.set(false);
		this.seeked = false;
		this.joinDecodeThread();

		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
		}
		return this;
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.released = false;
		this.texturesAllocated = false;
		this.textures = new ITexture[] {ResourceData.placeholder(), ResourceData.placeholder()};
		resource.texture(this.textures[0]);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try {
			VideoNatives.install().join();
		} catch (final CompletionException exception) {
			final Throwable cause = exception.getCause();
			throw cause instanceof RuntimeException ? (RuntimeException) cause : new VideoNativesException("The FFmpeg natives cannot be installed (" + cause + "), check the video libraries on the classpath", cause);
		}

		if (this.file == null) {
			try {
				this.file = this.origin.share();
			} catch (final IOException exception) {
				throw new UncheckedIOException("The video " + this.asset.getUniqueId() + " cannot be read", exception);
			}
		}

		synchronized (this.grabberLock) {
			if (this.released) {
				return;
			}

			try {
				this.grabber = this.open();

				resource.width(this.videoWidth);
				resource.height(this.videoHeight);

				this.frameRate = this.grabber.getVideoFrameRate();
				if (this.frameRate <= 0D) {
					this.frameRate = 30D;
				}

				this.duration = this.grabber.getLengthInTime() / 1000000D;
				this.totalFrames = this.grabber.getLengthInVideoFrames();
				if (this.totalFrames <= 0) {
					this.totalFrames = (int) (this.duration * this.frameRate);
				}

				this.frameQueue = new ArrayBlockingQueue<>(5);

				this.createAudioPlayer();

				final Frame firstFrame = this.grabber.grabImage();
				if (firstFrame != null) {
					final int[] pixels = this.frameToPixels(firstFrame, this.grabber.getImageWidth(), this.videoHeight);
					resource.data(new int[][] { pixels });
				}

				if (this.seeked) {
					this.seek(this.seekTime);
				}
			} catch (final Exception exception) {
				throw new IllegalStateException("The video " + this.asset.getUniqueId() + " cannot be decoded", exception);
			}
		}
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		this.textures[0].allocate(this.videoWidth, this.videoHeight).upload(resource.getData(0));
		this.textures[1].allocate(this.videoWidth, this.videoHeight).upload(resource.getData(0));
		this.texturesAllocated = true;

		if (this.autoplay || this.playRequested) {
			this.playRequested = false;
			this.play();
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		if (this.audioPlayer != null) {
			this.audioPlayer.update(this.getPlaybackTime() - this.seekTime);
		}

		if (!this.running.get()) {
			return;
		}

		if (this.paused.get()) {
			if (this.seeked) {
				final DecodedFrame frame = this.frameQueue.poll();
				if (frame != null) {
					this.show(resource, frame);
				}
			}
			return;
		}

		final double time = this.getPlaybackTime();
		DecodedFrame frame = null;
		DecodedFrame next = this.frameQueue.peek();
		while (next != null && next.time <= time) {
			frame = this.frameQueue.poll();
			next = this.frameQueue.peek();
		}

		if (frame != null) {
			this.show(resource, frame);
		}

		if (this.ended && next == null) {
			this.running.set(false);
			if (this.audioPlayer != null) {
				this.audioPlayer.stop();
			}
		}
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.release();

		this.file = null;
		if (this.origin == this) {
			this.forget();
		}

		if (this.textures != null) {
			this.textures[0].delete();
			this.textures[1].delete();
			this.textures = null;
		}

		this.released = false;
	}

	@Override
	public @NonNull VideoResourceDecoder fork() {
		return new VideoResourceDecoder(this.origin);
	}

	public void release() {
		this.released = true;
		this.running.set(false);

		if (this.decodeThread != null) {
			this.decodeThread.interrupt();
		}
		this.joinDecodeThread();

		synchronized (this.grabberLock) {
			if (this.audioPlayer != null) {
				this.audioPlayer.cleanup();
				this.audioPlayer = null;
			}

			if (this.grabber != null) {
				try {
					this.grabber.release();
				} catch (final Exception ignored) {}
				this.grabber = null;
			}

			if (this.frameQueue != null) {
				this.frameQueue.clear();
			}

			this.ended = false;
		}
	}

	@Override
	public @NonNull VideoResourceDecoder play() {
		if (this.running.get()) {
			if (this.paused.getAndSet(false)) {
				this.startTime += BridgeHandler.CLOCK.get().nanoTime() - this.pauseTime;
				if (this.audioPlayer != null) {
					this.audioPlayer.resume();
				}
			}
			return this;
		}

		if (this.grabber == null && !this.released) {
			this.playRequested = true;
			return this;
		}

		this.joinDecodeThread();
		if (this.grabber == null) {
			this.reopenGrabber();
			if (this.grabber == null) {
				return this;
			}
		} else if (!this.seeked) {
			this.seekInternal(0L);
		}

		this.seekTime = this.seeked ? this.seekTime : 0D;
		this.displayedFrameIndex = (int) (this.seekTime * this.frameRate);
		this.loopOffset = 0D;
		this.startTime = BridgeHandler.CLOCK.get().nanoTime() - (long) (this.seekTime * 1000000000D);
		this.ended = false;
		this.running.set(true);
		this.paused.set(false);

		if (this.audioPlayer != null) {
			this.audioPlayer.play();
		}

		this.startDecodeThread();
		return this;
	}

	@Override
	public @NonNull VideoResourceDecoder pause() {
		if (!this.running.get()) {
			return this;
		}

		if (!this.paused.getAndSet(true)) {
			this.pauseTime = BridgeHandler.CLOCK.get().nanoTime();
		}

		if (this.audioPlayer != null) {
			this.audioPlayer.pause();
		}
		return this;
	}

	@Override
	public @NonNull VideoResourceDecoder seek(final double seconds) {
		final double end = this.duration > 0D ? this.duration - 1D / this.frameRate : seconds;
		this.seekInternal((long) (Math.max(0D, Math.min(seconds, end)) * 1000000D));
		return this;
	}

	public @NonNull VideoResourceDecoder volume(final float volume) {
		this.volume = volume;
		return this;
	}

	public @NonNull VideoResourceDecoder audioGroup(final Object audioGroup) {
		this.audioGroup = audioGroup;
		return this;
	}

	@Override
	public @NonNull VideoResourceDecoder loop(final boolean loop) {
		this.loop = loop;
		return this;
	}

	public @NonNull VideoResourceDecoder location(final float x, final float y, final float z) {
		this.location = Point.of(x, y, z);
		return this;
	}

	public @NonNull VideoResourceDecoder referenceDistance(final float distance) {
		this.referenceDistance = distance;
		return this;
	}

	public @NonNull VideoResourceDecoder maxDistance(final float distance) {
		this.maxDistance = distance;
		return this;
	}

	@Override
	public @NonNull VideoResourceDecoder autoplay(final boolean autoplay) {
		this.autoplay = autoplay;
		return this;
	}

	@Override
	public boolean isPaused() {
		return this.paused.get();
	}

	@Override
	public boolean isPlaying() {
		return this.running.get() && !this.paused.get();
	}

	@Override
	public boolean isSettled() {
		if (!this.running.get() || this.frameQueue == null) {
			return true;
		}

		final DecodedFrame next = this.frameQueue.peek();
		if (this.paused.get()) {
			return !this.seeked || next != null || this.ended;
		}

		if (next == null) {
			return this.ended || this.isAudioFull();
		}
		return this.ended || this.isAudioFull() || this.frameQueue.remainingCapacity() == 0 || next.time > this.getPlaybackTime();
	}

	@Override
	public double getProgress() {
		if (this.totalFrames <= 0) {
			return 0D;
		}
		return Math.min((double) this.displayedFrameIndex / this.totalFrames, 1D);
	}

	@Override
	public double getCurrentTime() {
		if (this.frameRate <= 0D) {
			return 0D;
		}
		return this.displayedFrameIndex / this.frameRate;
	}

	private void reopenGrabber() {
		synchronized (this.grabberLock) {
			this.released = false;

			try {
				if (this.file == null) {
					this.file = this.origin.share();
				}

				this.grabber = this.open();
				this.frameQueue = new ArrayBlockingQueue<>(5);
				this.createAudioPlayer();

				if (this.seeked) {
					this.seek(this.seekTime);
				}
			} catch (final Exception exception) {
				JOIDLog.warn("The video " + this.asset.getUniqueId() + " cannot be reopened (" + exception.getMessage() + "), check that the file can still be read");
			}
		}
	}

	private synchronized @NonNull File share() throws IOException {
		if (this.shared == null) {
			final File file = this.asset.getFile();
			this.shared = file != null ? file : VideoResourceDecoder.extract(this.asset);
		}
		return this.shared;
	}

	private synchronized void forget() {
		this.shared = null;
	}

	private void createAudioPlayer() {
		if (this.grabber.getAudioChannels() > 0 && !BridgeHandler.AUDIO.getBridgeList().isEmpty()) {
			this.audioPlayer = new VideoAudioPlayer(this.grabber.getAudioChannels(), this.grabber.getSampleRate(), this);
		}
	}

	private boolean isAudioFull() {
		final VideoAudioPlayer audioPlayer = this.audioPlayer;
		return audioPlayer != null && audioPlayer.isFull();
	}

	private void seekGrabber(final long microseconds) throws Exception {
		final long start = this.grabber.getFormatContext().start_time();
		this.grabber.setTimestamp(microseconds - (start == avutil.AV_NOPTS_VALUE ? 0L : Math.min(0L, start)), true);
	}

	private int getSkippedSamples(final long timestamp) {
		final long start = (long) (this.seekTime * 1000000D);
		return (int) Math.ceil((start - timestamp) * this.grabber.getSampleRate() / 1000000D);
	}

	private FFmpegFrameGrabber open() throws Exception {
		if (!VideoNatives.isInstalled()) {
			throw new IllegalStateException("The FFmpeg natives are not installed yet, wait for VideoNatives.install()");
		}

		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		FFmpegLogCallback.set();

		final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(this.file);
		grabber.setPixelFormat(avutil.AV_PIX_FMT_BGRA);
		if (this.codec != null) {
			grabber.setVideoCodecName(this.codec);
		}
		grabber.start();

		this.videoWidth = grabber.getImageWidth();
		this.videoHeight = grabber.getImageHeight();
		if (this.videoWidth % 8 != 0) {
			grabber.stop();
			grabber.setImageWidth((this.videoWidth + 7) / 8 * 8);
			grabber.setImageHeight(this.videoHeight);
			grabber.start();
		}

		if (this.codec != null || !"1".equals(grabber.getVideoMetadata("alpha_mode"))) {
			return grabber;
		}

		if (grabber.getVideoCodec() == avcodec.AV_CODEC_ID_VP9) {
			this.codec = "libvpx-vp9";
		} else if (grabber.getVideoCodec() == avcodec.AV_CODEC_ID_VP8) {
			this.codec = "libvpx";
		} else {
			return grabber;
		}

		grabber.release();
		return this.open();
	}

	private double getPlaybackTime() {
		final long now = this.paused.get() ? this.pauseTime : BridgeHandler.CLOCK.get().nanoTime();
		return (now - this.startTime) / 1000000000D;
	}

	private void seekInternal(final long microseconds) {
		synchronized (this.grabberLock) {
			if (this.released) {
				return;
			}

			this.seeks.incrementAndGet();
			if (this.frameQueue != null) {
				this.frameQueue.clear();
			}

			try {
				if (this.grabber != null) {
					this.seekGrabber(microseconds);
				}
			} catch (final Exception exception) {
				JOIDLog.warn("The video " + this.asset.getUniqueId() + " cannot seek to " + microseconds + "us (" + exception + "), check that the file can still be read", exception);
			}

			final long now = BridgeHandler.CLOCK.get().nanoTime();
			this.displayedFrameIndex = (int) (microseconds / 1000000D * this.frameRate);
			this.startTime = now - microseconds * 1000L;
			this.pauseTime = now;
			this.loopOffset = 0D;
			this.seekTime = microseconds / 1000000D;
			this.ended = false;
			this.seeked = true;

			if (this.audioPlayer != null) {
				this.audioPlayer.flush();
			}
		}
	}

	private void show(final ResourceData resource, final DecodedFrame frame) {
		final int nextBuffer = 1 - this.currentBuffer;
		if (!this.texturesAllocated) {
			this.textures[0].allocate(this.videoWidth, this.videoHeight);
			this.textures[1].allocate(this.videoWidth, this.videoHeight);
			this.texturesAllocated = true;
		}
		this.textures[nextBuffer].upload(frame.pixels);
		this.currentBuffer = nextBuffer;
		this.displayedFrameIndex = (int) Math.round(frame.mediaTime * this.frameRate);
		this.seeked = false;
		resource.texture(this.textures[this.currentBuffer]);
	}

	private void joinDecodeThread() {
		if (this.decodeThread != null) {
			try {
				this.decodeThread.join(1000L);
			} catch (final InterruptedException interrupted) {
				Thread.currentThread().interrupt();
			}
			this.decodeThread = null;
		}
	}

	private void startDecodeThread() {
		if (this.decodeThread != null && this.decodeThread.isAlive()) {
			return;
		}

		this.decodeThread = ThreadUtils.daemonThread(() -> {
			try {
				double lastTime = 0D;
				while (this.running.get() && !Thread.currentThread().isInterrupted()) {
					if (this.isAudioFull()) {
						Thread.sleep(5L);
						continue;
					}

					final DecodedFrame decoded;
					final int seek;
					synchronized (this.grabberLock) {
						if (this.released || this.grabber == null) {
							return;
						}

						seek = this.seeks.get();
						final Frame frame = this.grabber.grab();
						if (frame == null) {
							if (this.loop) {
								this.loopOffset = lastTime + 1D / this.frameRate;
								this.seekGrabber(0L);
								this.seekTime = 0D;
								continue;
							}
							this.ended = true;
							decoded = null;
						} else {
							if (frame.samples != null && this.audioPlayer != null) {
								this.audioPlayer.pushSamples(frame.samples, this.getSkippedSamples(frame.timestamp));
							}

							if (frame.image == null) {
								continue;
							}

							final double mediaTime = frame.timestamp / 1000000D;
							if (mediaTime + 0.5D / this.frameRate < this.seekTime) {
								continue;
							}

							lastTime = this.loopOffset + mediaTime;
							decoded = new DecodedFrame(lastTime, this.frameToPixels(frame, this.grabber.getImageWidth(), this.grabber.getImageHeight()), mediaTime);
						}
					}

					if (decoded == null) {
						while (seek == this.seeks.get() && this.running.get()) {
							Thread.sleep(5L);
						}
						continue;
					}

					boolean queued = false;
					while (!queued && this.running.get()) {
						synchronized (this.grabberLock) {
							if (seek != this.seeks.get()) {
								break;
							}

							queued = this.frameQueue.offer(decoded);
						}

						if (!queued) {
							Thread.sleep(5L);
						}
					}
				}
			} catch (final InterruptedException ignored) {
			} catch (final Exception exception) {
				this.ended = true;
				JOIDLog.warn("The video " + this.asset.getUniqueId() + " stops decoding (" + exception + "), check that the file can still be read", exception);
			}
		}, "ResourceVideo/" + this.asset.getUniqueId());
		this.decodeThread.start();
	}

	private int[] frameToPixels(final Frame frame, final int width, final int height) {
		if (frame.image == null || frame.image[0] == null) {
			return new int[this.videoWidth * height];
		}

		final ByteBuffer buffer = (ByteBuffer) frame.image[0];
		buffer.rewind();
		final int[] pixels = new int[this.videoWidth * height];
		final IntBuffer intBuffer = buffer.asIntBuffer();
		final int strideInts = frame.imageStride / 4;
		if (width == this.videoWidth && strideInts == width) {
			intBuffer.get(pixels);
			return pixels;
		}

		final int[] row = new int[width];
		for (int y = 0; y < height; y++) {
			intBuffer.position(y * strideInts);
			intBuffer.get(row, 0, width);
			for (int x = 0; x < this.videoWidth; x++) {
				pixels[y * this.videoWidth + x] = row[x * width / this.videoWidth];
			}
		}

		return pixels;
	}

	private static @NonNull File extract(final Asset asset) throws IOException {
		return VideoResourceDecoder.AREA.store(file -> {
			try (final InputStream stream = asset.open()) {
				Files.copy(stream, file.toPath());
			}
		});
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class DecodedFrame {

		private final double time;
		private final int[]  pixels;
		private final double mediaTime;

	}

}