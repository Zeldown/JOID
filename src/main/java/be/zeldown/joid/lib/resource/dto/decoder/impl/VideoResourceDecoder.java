package be.zeldown.joid.lib.resource.dto.decoder.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import be.zeldown.joid.lib.video.VideoAudioPlayer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class VideoResourceDecoder implements IResourceDecoder {

	private static final int RING_BUFFER_SIZE = 5;

	private final File file;

	private Thread decodeThread;
	private FFmpegFrameGrabber grabber;
	private VideoAudioPlayer audioPlayer;

	private ArrayBlockingQueue<int[]> frameQueue;
	private final AtomicBoolean paused = new AtomicBoolean(false);
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicInteger decodedFrameIndex = new AtomicInteger(0);

	private volatile boolean ended;
	private volatile int displayedFrameIndex;

	private final Object grabberLock = new Object();
	private volatile boolean released;

	private int[] textureIds;
	private int currentBuffer;
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
	private boolean hasLocation;
	private float maxDistance = 50F;
	private float referenceDistance = 5F;

	public VideoResourceDecoder(final @NonNull InputStream inputStream) {
		try {
			this.file = File.createTempFile("joid-video-", ".mp4");
			this.file.deleteOnExit();

			final FileOutputStream fos = new FileOutputStream(this.file);
			final byte[] buffer = new byte[8192];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				fos.write(buffer, 0, read);
			}
			fos.close();
			inputStream.close();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public VideoResourceDecoder(final @NonNull File file) {
		this.file = file;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.textureIds = new int[] { GL11.glGenTextures(), GL11.glGenTextures() };
		resource.textureId(new int[] { this.textureIds[0] });
		AllocatedTextureUtil.allocateTexture(this.textureIds[0], 1, 1);
		AllocatedTextureUtil.uploadTexture(this.textureIds[0], new int[] { 0 }, 1, 1);
		AllocatedTextureUtil.allocateTexture(this.textureIds[1], 1, 1);
		AllocatedTextureUtil.uploadTexture(this.textureIds[1], new int[] { 0 }, 1, 1);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		synchronized (this.grabberLock) {
			if (this.released) {
				return;
			}

			try {
				avutil.av_log_set_level(avutil.AV_LOG_QUIET);
				FFmpegLogCallback.set();

				this.grabber = new FFmpegFrameGrabber(this.file);
				this.grabber.setPixelFormat(avutil.AV_PIX_FMT_BGRA);
				this.grabber.start();

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

		AllocatedTextureUtil.allocateTexture(this.textureIds[0], resource.getWidth(), resource.getHeight());
		AllocatedTextureUtil.uploadTexture(this.textureIds[0], resource.getData()[0], resource.getWidth(), resource.getHeight());
		AllocatedTextureUtil.allocateTexture(this.textureIds[1], resource.getWidth(), resource.getHeight());
		AllocatedTextureUtil.uploadTexture(this.textureIds[1], resource.getData()[0], resource.getWidth(), resource.getHeight());

		if (this.autoplay) {
			this.play();
		}
	}

	@Override
	public void bind(final @NonNull ResourceData resource) {
		if (this.audioPlayer != null) {
			this.audioPlayer.update();
			this.audioPlayer.setVolume(this.volume);
		}

		if (!this.running.get() || this.paused.get()) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureIds[this.currentBuffer]);
			return;
		}

		if (this.ended && !this.loop) {
			this.running.set(false);
			if (this.audioPlayer != null) {
				this.audioPlayer.stop();
			}
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureIds[this.currentBuffer]);
			return;
		}

		final int[] pixels = this.frameQueue.poll();
		if (pixels != null) {
			final int nextBuffer = 1 - this.currentBuffer;
			if (!this.texturesAllocated) {
				AllocatedTextureUtil.allocateTexture(this.textureIds[0], resource.getWidth(), resource.getHeight());
				AllocatedTextureUtil.allocateTexture(this.textureIds[1], resource.getWidth(), resource.getHeight());
				this.texturesAllocated = true;
			}
			AllocatedTextureUtil.uploadTexture(this.textureIds[nextBuffer], pixels, resource.getWidth(), resource.getHeight());
			this.currentBuffer = nextBuffer;
			this.displayedFrameIndex++;
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureIds[this.currentBuffer]);
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.release();

		if (this.textureIds != null) {
			GL11.glDeleteTextures(this.textureIds[0]);
			GL11.glDeleteTextures(this.textureIds[1]);
			this.textureIds = null;
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

	private void reopenGrabber() {
		synchronized (this.grabberLock) {
			this.released = false;

			try {
				avutil.av_log_set_level(avutil.AV_LOG_QUIET);
				FFmpegLogCallback.set();

				this.grabber = new FFmpegFrameGrabber(this.file);
				this.grabber.setPixelFormat(avutil.AV_PIX_FMT_BGRA);
				this.grabber.start();

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

	/* [ Control Section ] */
	public @NonNull VideoResourceDecoder play() {
		if (this.running.get()) {
			return this;
		}

		if (this.grabber == null) {
			this.reopenGrabber();
		}

		this.displayedFrameIndex = 0;
		this.ended = false;
		this.running.set(true);
		this.paused.set(false);

		if (this.audioPlayer != null) {
			this.audioPlayer.play();
		}

		this.startDecodeThread();
		return this;
	}

	public @NonNull VideoResourceDecoder pause() {
		this.paused.set(true);
		if (this.audioPlayer != null) {
			this.audioPlayer.pause();
		}
		return this;
	}

	public @NonNull VideoResourceDecoder resume() {
		this.paused.set(false);
		if (this.audioPlayer != null) {
			this.audioPlayer.resume();
		}
		return this;
	}

	public @NonNull VideoResourceDecoder stop() {
		this.running.set(false);
		if (this.audioPlayer != null) {
			this.audioPlayer.stop();
		}
		return this;
	}

	public @NonNull VideoResourceDecoder seek(final double seconds) {
		this.seekInternal((long) (seconds * 1000000D));
		return this;
	}

	public @NonNull VideoResourceDecoder volume(final float volume) {
		this.volume = volume;
		return this;
	}

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

	public @NonNull VideoResourceDecoder autoplay(final boolean autoplay) {
		this.autoplay = autoplay;
		return this;
	}

	/* [ Static Section ] */
	public static boolean isVideoHeader(final @NonNull byte[] header, final int read) {
		if (read >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a') {
			return true;
		}

		if (read >= 8 && header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p') {
			return true;
		}

		if (read >= 4 && header[0] == (byte) 0x1A && header[1] == (byte) 0x45 && header[2] == (byte) 0xDF && header[3] == (byte) 0xA3) {
			return true;
		}

		if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' && header[8] == 'A' && header[9] == 'V' && header[10] == 'I' && header[11] == ' ') {
			return true;
		}

		return false;
	}

	public static boolean isLoopByDefault(final @NonNull byte[] header, final int read) {
		return read >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
	}

	/* [ Getter Section ] */
	public double getProgress() {
		if (this.totalFrames <= 0) {
			return 0D;
		}
		return Math.min((double) this.displayedFrameIndex / this.totalFrames, 1D);
	}

	public double getCurrentVideoTime() {
		if (this.frameRate <= 0D) {
			return 0D;
		}
		return this.displayedFrameIndex / this.frameRate;
	}

	public boolean isPlaying() {
		return this.running.get() && !this.paused.get();
	}

	public boolean isPaused() {
		return this.paused.get();
	}

	/* [ Internal Section ] */
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

			this.displayedFrameIndex = (int) (microseconds / 1000000D * this.frameRate);
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
				final double frameDurationNs = 1000000000D / this.frameRate;
				long nextFrameTime = System.nanoTime();

				while (this.running.get() && !Thread.currentThread().isInterrupted()) {
					if (this.paused.get()) {
						Thread.sleep(10L);
						nextFrameTime = System.nanoTime();
						continue;
					}

					boolean hasImage = false;
					synchronized (this.grabberLock) {
						if (this.released || this.grabber == null) {
							return;
						}

						final Frame frame = this.grabber.grab();
						if (frame == null) {
							if (this.loop) {
								this.grabber.setTimestamp(0);
								this.decodedFrameIndex.set(0);
								this.displayedFrameIndex = 0;
								if (this.audioPlayer != null) {
									this.audioPlayer.flush();
								}
								nextFrameTime = System.nanoTime();
								continue;
							}
							this.ended = true;
							break;
						}

						if (frame.samples != null && this.audioPlayer != null && this.volume > 0F) {
							this.audioPlayer.pushSamples(frame.samples);
						}

						if (frame.image != null) {
							hasImage = true;
							final int[] pixels = this.frameToPixels(frame, this.grabber.getImageWidth(), this.grabber.getImageHeight());
							if (!this.frameQueue.offer(pixels)) {
								this.frameQueue.poll();
								this.frameQueue.offer(pixels);
							}
							this.decodedFrameIndex.incrementAndGet();
						}
					}

					if (hasImage) {
						nextFrameTime += (long) frameDurationNs;
						final long sleepMs = (nextFrameTime - System.nanoTime()) / 1000000L;
						if (sleepMs > 0L) {
							Thread.sleep(sleepMs);
						} else {
							nextFrameTime = System.nanoTime();
						}
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

}