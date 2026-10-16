package dev.joid.lib.resource.decoder.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import dev.joid.lib.video.VideoAudioPlayer;
import dev.joid.lib.video.VideoNatives;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class VideoResourceDecoder implements IResourceDecoder, IResourcePlayback {

	private static final FileCacheArea AREA = FileCache.area("video").retention(Duration.ofDays(7L));

	@Getter private final Asset        asset;
	private final VideoResourceDecoder origin;
	private final Object               grabberLock       = new Object();
	private final AtomicInteger        seeks             = new AtomicInteger(0);
	private final AtomicBoolean        paused            = new AtomicBoolean(false);
	private final AtomicBoolean        running           = new AtomicBoolean(false);
	private final AtomicInteger        decodedFrameIndex = new AtomicInteger(0);

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

	private int        currentBuffer;
	private ITexture[] textures;
	private boolean    texturesAllocated;

	@Getter private int    videoWidth;
	@Getter private int    totalFrames;
	@Getter private double duration;
	@Getter private double frameRate;

	@Getter private boolean loop;
	@Getter private float   volume   = 1F;
	@Getter private Object  audioGroup;
	@Getter private boolean autoplay = true;

	private float         locationX;
	private float         locationY;
	private float         locationZ;
	private boolean       hasLocation;
	@Getter private float maxDistance       = 50F;
	@Getter private float referenceDistance = 5F;

	public VideoResourceDecoder(final @NonNull File file) {
		this(FileAsset.create(file));
	}

	public VideoResourceDecoder(final @NonNull Asset asset) {
		this.asset = asset;
		this.origin = this;
	}

	private VideoResourceDecoder(final VideoResourceDecoder origin) {
		this.asset = origin.asset;
		this.origin = origin;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public @NonNull VideoResourceDecoder stop() {
		this.running.set(false);
		this.seeked = false;
		this.joinDecodeThread();

		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
		}
		return this;
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.textures = new ITexture[] {ResourceData.placeholder(), ResourceData.placeholder()};
		resource.texture(this.textures[0]);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try {
			VideoNatives.install().join();
		} catch (final CompletionException exception) {
			resource.fail(exception.getCause());
			return;
		}

		synchronized (this.grabberLock) {
			if (this.released) {
				return;
			}

			try {
				if (this.file == null) {
					this.file = this.origin.share();
				}

				this.grabber = this.open();

				resource.width(this.videoWidth);
				resource.height(this.grabber.getImageHeight());

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
					final int[] pixels = this.frameToPixels(firstFrame, this.grabber.getImageWidth(), resource.getHeight());
					resource.data(new int[][] { pixels });
				}
			} catch (final Exception | LinkageError exception) {
				resource.fail(exception);
			}
		}
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		if (resource.getData(0) == null) {
			return;
		}

		this.textures[0].allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData(0));
		this.textures[1].allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData(0));

		if (this.autoplay) {
			this.play();
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		if (this.audioPlayer != null) {
			this.audioPlayer.update();
			this.audioPlayer.setVolume(this.volume);
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
		while (next != null && next.getTime() <= time) {
			frame = this.frameQueue.poll();
			next = this.frameQueue.peek();
		}

		if (frame != null) {
			this.show(resource, frame);
		}

		if (this.ended && next == null && !this.loop) {
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
			try {
				this.decodeThread.join(1000L);
			} catch (final InterruptedException ignored) {}
			this.decodeThread = null;
		}

		synchronized (this.grabberLock) {
			if (this.audioPlayer != null) {
				this.audioPlayer.cleanup();
				this.audioPlayer = null;
			}

			if (this.grabber != null) {
				try {
					this.grabber.stop();
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
			return this.paused.get() ? this.resume() : this;
		}

		this.joinDecodeThread();
		if (this.grabber == null) {
			this.reopenGrabber();
		} else if (!this.seeked && this.decodedFrameIndex.get() > 0) {
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
	public @NonNull VideoResourceDecoder resume() {
		if (this.paused.getAndSet(false)) {
			this.startTime += BridgeHandler.CLOCK.get().nanoTime() - this.pauseTime;
		}

		if (this.audioPlayer != null) {
			this.audioPlayer.resume();
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
		if (this.audioPlayer != null) {
			this.audioPlayer.setGroup(audioGroup);
		}
		return this;
	}

	@Override
	public @NonNull VideoResourceDecoder loop(final boolean loop) {
		this.loop = loop;
		return this;
	}

	public @NonNull VideoResourceDecoder location(final float x, final float y, final float z) {
		this.locationX = x;
		this.locationY = y;
		this.locationZ = z;
		this.hasLocation = true;
		if (this.audioPlayer != null) {
			this.audioPlayer.setLocation(x, y, z);
		}
		return this;
	}

	public @NonNull VideoResourceDecoder referenceDistance(final float distance) {
		this.referenceDistance = distance;
		if (this.audioPlayer != null) {
			this.audioPlayer.setReferenceDistance(distance);
		}
		return this;
	}

	public @NonNull VideoResourceDecoder maxDistance(final float distance) {
		this.maxDistance = distance;
		if (this.audioPlayer != null) {
			this.audioPlayer.setMaxDistance(distance);
		}
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
			return this.ended;
		}
		return this.ended || this.frameQueue.remainingCapacity() == 0 || next.getTime() > this.getPlaybackTime();
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

				if (this.frameQueue == null) {
					this.frameQueue = new ArrayBlockingQueue<>(5);
				} else {
					this.frameQueue.clear();
				}

				this.createAudioPlayer();
			} catch (final Exception e) {
				JOIDLog.warn("The video " + this.describe() + " cannot be reopened (" + e.getMessage() + "), check that the file can still be read");
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
		if (this.grabber.getAudioChannels() > 0 && this.volume > 0F && !BridgeHandler.AUDIO.getBridgeList().isEmpty()) {
			this.audioPlayer = new VideoAudioPlayer(this.describe(), this.grabber.getSampleRate(), this.grabber.getAudioChannels());
			this.audioPlayer.setGroup(this.audioGroup);
			if (this.hasLocation) {
				this.audioPlayer.setLocation(this.locationX, this.locationY, this.locationZ);
				this.audioPlayer.setReferenceDistance(this.referenceDistance);
				this.audioPlayer.setMaxDistance(this.maxDistance);
			}
		}
	}

	private void seekGrabber(final long microseconds) throws Exception {
		final long start = this.grabber.getFormatContext().start_time();
		this.grabber.setTimestamp(microseconds - (start == avutil.AV_NOPTS_VALUE ? 0L : Math.min(0L, start)), true);
	}

	private int getSkippedSamples(final long timestamp) {
		final long start = (long) (this.seekTime * 1000000D);
		return (int) Math.ceil((start - timestamp) * this.grabber.getSampleRate() / 1000000D);
	}

	private String describe() {
		return this.asset.getUniqueId();
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
		if (this.videoWidth % 8 != 0) {
			final int height = grabber.getImageHeight();
			grabber.stop();
			grabber.setImageWidth((this.videoWidth + 7) / 8 * 8);
			grabber.setImageHeight(height);
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

		grabber.stop();
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
			} catch (final Exception e) {
				JOIDLog.warn("The video " + this.describe() + " cannot seek to " + microseconds + "us (" + e + "), check that the file can still be read", e);
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
			this.textures[0].allocate(resource.getWidth(), resource.getHeight());
			this.textures[1].allocate(resource.getWidth(), resource.getHeight());
			this.texturesAllocated = true;
		}
		this.textures[nextBuffer].upload(frame.getPixels());
		this.currentBuffer = nextBuffer;
		this.displayedFrameIndex = (int) Math.round(frame.getMediaTime() * this.frameRate);
		this.seeked = false;
		resource.texture(this.textures[this.currentBuffer]);
	}

	private void joinDecodeThread() {
		if (this.decodeThread != null) {
			try {
				this.decodeThread.join(1000L);
			} catch (final InterruptedException ignored) {}
			this.decodeThread = null;
		}
	}

	private void startDecodeThread() {
		if (this.decodeThread != null && this.decodeThread.isAlive()) {
			return;
		}

		this.decodeThread = new Thread(() -> {
			try {
				double lastTime = 0D;
				while (this.running.get() && !Thread.currentThread().isInterrupted()) {
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
								this.decodedFrameIndex.set(0);
								continue;
							}
							this.ended = true;
							decoded = null;
						} else {
							if (frame.samples != null && this.audioPlayer != null && this.volume > 0F) {
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
							this.decodedFrameIndex.incrementAndGet();
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
			} catch (final Exception e) {
				JOIDLog.warn("The video " + this.describe() + " stops decoding (" + e + "), check that the file can still be read", e);
			}
		}, "joid-video-decode");
		this.decodeThread.setDaemon(true);
		this.decodeThread.start();
	}

	private int[] frameToPixels(final @NonNull Frame frame, final int width, final int height) {
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

	private static @NonNull File extract(final @NonNull Asset asset) throws IOException {
		return VideoResourceDecoder.AREA.store(file -> {
			try (InputStream stream = asset.open()) {
				Files.copy(stream, file.toPath());
			}
		});
	}

	@Getter
	@RequiredArgsConstructor
	public static final class DecodedFrame {

		private final double time;
		private final int[]  pixels;
		private final double mediaTime;

	}

}