package be.zeldown.joid.lib.resource.dto.decoder.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.playback.IPlayback;
import be.zeldown.joid.lib.video.VideoAudioPlayer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
public final class VideoResourceDecoder implements IResourceDecoder, IPlayback {

	private static final int RING_BUFFER_SIZE = 5;

	private final Asset asset;
	private final Object grabberLock = new Object();
	private final AtomicBoolean paused = new AtomicBoolean(false);
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicInteger decodedFrameIndex = new AtomicInteger(0);

	private File   file;
	private String codec;

	private Thread decodeThread;
	private FFmpegFrameGrabber grabber;
	private VideoAudioPlayer audioPlayer;

	private ArrayBlockingQueue<DecodedFrame> frameQueue;

	private volatile boolean ended;
	private volatile double loopOffset;
	private volatile int displayedFrameIndex;

	private long startTime;
	private long pauseTime;

	private volatile boolean released;

	private int currentBuffer;
	private ITexture[] textures;
	private boolean texturesAllocated;

	private int totalFrames;
	private double duration;
	private double frameRate;

	private boolean loop;
	private float volume = 1F;
	private boolean autoplay = true;

	private float locationX;
	private float locationY;
	private float locationZ;
	private float maxDistance = 50F;
	private boolean hasLocation;
	private float referenceDistance = 5F;

	public VideoResourceDecoder(final @NonNull File file) {
		this.asset = null;
		this.file = file;
	}

	public VideoResourceDecoder(final @NonNull Asset asset) {
		this.asset = asset;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public @NonNull VideoResourceDecoder stop() {
		this.running.set(false);
		if (this.decodeThread != null) {
			try {
				this.decodeThread.join(1000L);
			} catch (final InterruptedException ignored) {}
			this.decodeThread = null;
		}

		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
		}
		return this;
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		this.textures = new ITexture[] {render.createTexture(), render.createTexture()};
		resource.texture(this.textures[0]);
		this.textures[0].allocate(1, 1).upload(new int[] {0}, 1, 1);
		this.textures[1].allocate(1, 1).upload(new int[] {0}, 1, 1);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		synchronized (this.grabberLock) {
			if (this.released) {
				return;
			}

			try {
				if (this.file == null) {
					this.file = VideoResourceDecoder.extract(this.asset);
				}

				this.grabber = this.open();

				resource.width(this.grabber.getImageWidth());
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

				this.frameQueue = new ArrayBlockingQueue<>(VideoResourceDecoder.RING_BUFFER_SIZE);

				if (this.grabber.getAudioChannels() > 0 && this.volume > 0F) {
					this.audioPlayer = new VideoAudioPlayer(this.grabber.getSampleRate(), this.grabber.getAudioChannels());
					if (this.hasLocation) {
						this.audioPlayer.setLocation(this.locationX, this.locationY, this.locationZ);
						this.audioPlayer.setReferenceDistance(this.referenceDistance);
						this.audioPlayer.setMaxDistance(this.maxDistance);
					}
				}

				final Frame firstFrame = this.grabber.grabImage();
				if (firstFrame != null) {
					final int[] pixels = this.frameToPixels(firstFrame, resource.getWidth(), resource.getHeight());
					resource.data(new int[][] { pixels });
				}
			} catch (final Exception e) {
				System.err.println("Failed to decode video: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		if (resource.getData() == null || resource.getData()[0] == null) {
			return;
		}

		this.textures[0].allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[0], resource.getWidth(), resource.getHeight());
		this.textures[1].allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[0], resource.getWidth(), resource.getHeight());

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

		if (!this.running.get() || this.paused.get()) {
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
			final int nextBuffer = 1 - this.currentBuffer;
			if (!this.texturesAllocated) {
				this.textures[0].allocate(resource.getWidth(), resource.getHeight());
				this.textures[1].allocate(resource.getWidth(), resource.getHeight());
				this.texturesAllocated = true;
			}
			this.textures[nextBuffer].upload(frame.getPixels(), resource.getWidth(), resource.getHeight());
			this.currentBuffer = nextBuffer;
			this.displayedFrameIndex = (int) Math.round(frame.getMediaTime() * this.frameRate);
			resource.texture(this.textures[this.currentBuffer]);
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

		if (this.textures != null) {
			this.textures[0].delete();
			this.textures[1].delete();
			this.textures = null;
		}
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
			return this;
		}

		if (this.grabber == null) {
			this.reopenGrabber();
		}

		this.displayedFrameIndex = 0;
		this.loopOffset = 0D;
		this.startTime = BridgeHandler.CLOCK.get().nanoTime();
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
		this.seekInternal((long) (seconds * 1000000D));
		return this;
	}

	public @NonNull VideoResourceDecoder volume(final float volume) {
		this.volume = volume;
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
		if (!this.running.get() || this.paused.get() || this.frameQueue == null) {
			return true;
		}

		final DecodedFrame next = this.frameQueue.peek();
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
				this.grabber = this.open();

				if (this.frameQueue == null) {
					this.frameQueue = new ArrayBlockingQueue<>(VideoResourceDecoder.RING_BUFFER_SIZE);
				} else {
					this.frameQueue.clear();
				}

				if (this.grabber.getAudioChannels() > 0 && this.volume > 0F) {
					this.audioPlayer = new VideoAudioPlayer(this.grabber.getSampleRate(), this.grabber.getAudioChannels());
					if (this.hasLocation) {
						this.audioPlayer.setLocation(this.locationX, this.locationY, this.locationZ);
						this.audioPlayer.setReferenceDistance(this.referenceDistance);
						this.audioPlayer.setMaxDistance(this.maxDistance);
					}
				}
			} catch (final Exception e) {
				System.err.println("Failed to reopen video grabber: " + e.getMessage());
			}
		}
	}

	private FFmpegFrameGrabber open() throws Exception {
		avutil.av_log_set_level(avutil.AV_LOG_QUIET);
		FFmpegLogCallback.set();

		final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(this.file);
		grabber.setPixelFormat(avutil.AV_PIX_FMT_BGRA);
		if (this.codec != null) {
			grabber.setVideoCodecName(this.codec);
		}
		grabber.start();

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

			if (this.frameQueue != null) {
				this.frameQueue.clear();
			}

			try {
				if (this.grabber != null) {
					this.grabber.setTimestamp(microseconds);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

			final long now = BridgeHandler.CLOCK.get().nanoTime();
			this.displayedFrameIndex = (int) (microseconds / 1000000D * this.frameRate);
			this.startTime = now - microseconds * 1000L;
			this.pauseTime = now;
			this.loopOffset = 0D;
			this.ended = false;

			if (this.audioPlayer != null) {
				this.audioPlayer.flush();
			}
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
					synchronized (this.grabberLock) {
						if (this.released || this.grabber == null) {
							return;
						}

						final Frame frame = this.grabber.grab();
						if (frame == null) {
							if (this.loop) {
								this.loopOffset = lastTime + 1D / this.frameRate;
								this.grabber.setTimestamp(0);
								this.decodedFrameIndex.set(0);
								continue;
							}
							this.ended = true;
							break;
						}

						if (frame.samples != null && this.audioPlayer != null && this.volume > 0F) {
							this.audioPlayer.pushSamples(frame.samples);
						}

						if (frame.image == null) {
							continue;
						}

						final double mediaTime = frame.timestamp / 1000000D;
						lastTime = this.loopOffset + mediaTime;
						decoded = new DecodedFrame(lastTime, this.frameToPixels(frame, this.grabber.getImageWidth(), this.grabber.getImageHeight()), mediaTime);
						this.decodedFrameIndex.incrementAndGet();
					}

					boolean queued = false;
					while (!queued && this.running.get()) {
						queued = this.frameQueue.offer(decoded, 50L, TimeUnit.MILLISECONDS);
					}
				}
			} catch (final InterruptedException ignored) {
			} catch (final Exception e) {
				System.err.println("Video decode thread error: " + e.getMessage());
				e.printStackTrace();
			}
		}, "joid-video-decode");
		this.decodeThread.setDaemon(true);
		this.decodeThread.start();
	}

	private int[] frameToPixels(final @NonNull Frame frame, final int width, final int height) {
		if (frame.image == null || frame.image[0] == null) {
			return new int[width * height];
		}

		final ByteBuffer buffer = (ByteBuffer) frame.image[0];
		buffer.rewind();
		final int[] pixels = new int[width * height];
		final int stride = frame.imageStride;

		if (stride == width * 4) {
			buffer.asIntBuffer().get(pixels);
		} else {
			final IntBuffer intBuffer = buffer.asIntBuffer();
			final int strideInts = stride / 4;
			for (int y = 0; y < height; y++) {
				intBuffer.position(y * strideInts);
				intBuffer.get(pixels, y * width, width);
			}
		}

		return pixels;
	}

	private static @NonNull File extract(final @NonNull Asset asset) throws IOException {
		final File target = File.createTempFile("joid-video-", ".mp4");
		target.deleteOnExit();

		try (InputStream stream = asset.open(); FileOutputStream output = new FileOutputStream(target)) {
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = stream.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
		}
		return target;
	}

	@Getter
	@RequiredArgsConstructor
	public static final class DecodedFrame {

		private final double time;
		private final int[]  pixels;
		private final double mediaTime;

	}

}