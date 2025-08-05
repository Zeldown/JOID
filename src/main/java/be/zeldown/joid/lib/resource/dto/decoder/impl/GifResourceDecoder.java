package be.zeldown.joid.lib.resource.dto.decoder.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.resource.dto.ResourceData;
import be.zeldown.joid.lib.resource.dto.decoder.IResourceDecoder;
import be.zeldown.joid.lib.resource.dto.decoder.impl.GifResourceDecoder.GifDecoder.GifImage;
import be.zeldown.joid.lib.utils.texture.AllocatedTextureUtil;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class GifResourceDecoder implements IResourceDecoder {

	private float speed = 1F;
	private GifLoopMode loopMode = GifLoopMode.LOOP;

	private GifImage gif;
	private float[] delays;

	private long startTime;
	private long pauseTime;
	private int currentIndex;

	public GifResourceDecoder(final @NonNull InputStream inputStream) {
		try {
			this.gif = GifDecoder.read(inputStream);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.delays = new float[this.gif.getFrameCount()];
		resource.textureId(new int[] { GL11.glGenTextures()});
		AllocatedTextureUtil.allocateTexture(resource.getTextureId()[0], 1, 1);
		AllocatedTextureUtil.uploadTexture(resource.getTextureId()[0], new int[] {0}, 1, 1);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		resource.width(this.gif.getWidth());
		resource.height(this.gif.getHeight());

		for (int i = 0; i < this.delays.length; i++) {
			this.delays[i] = this.gif.getDelay(i) * 10;
		}

		resource.data(new int[resource.getTextureId().length][resource.getWidth() * resource.getHeight()]);
		this.gif.getFrame(0).getRGB(0, 0, resource.getWidth(), resource.getHeight(), resource.getData()[0], 0, resource.getWidth());
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		AllocatedTextureUtil.allocateTexture(resource.getTextureId()[0], resource.getWidth(), resource.getHeight());
		AllocatedTextureUtil.uploadTexture(resource.getTextureId()[0], resource.getData()[0], resource.getWidth(), resource.getHeight());
	}

	@Override
	public void bind(final @NonNull ResourceData resource) {
		if (this.startTime == 0) {
			this.startTime = System.currentTimeMillis();
		}

		final long time = this.getCurrentTime() - this.startTime;

		int index = 0;
		float delay = this.getDelay(0);
		for (int i = 0; i < this.delays.length; i++) {
			if (time < delay) {
				break;
			}

			if (i == this.delays.length - 1) {
				if (this.loopMode == GifLoopMode.FIRST_FRAME) {
					index = 0;
				} else if (this.loopMode == GifLoopMode.LAST_FRAME) {
					index = this.delays.length - 1;
				} else {
					index = i;
				}

				this.startTime = System.currentTimeMillis();
				delay = 0;
				break;
			}

			index = i;
			delay += this.getDelay(i);
		}

		if (index != this.currentIndex) {
			resource.data(new int[resource.getTextureId().length][resource.getWidth() * resource.getHeight()]);
			this.gif.getFrame(index).getRGB(0, 0, resource.getWidth(), resource.getHeight(), resource.getData()[0], 0, resource.getWidth());

			AllocatedTextureUtil.allocateTexture(resource.getTextureId()[0], resource.getWidth(), resource.getHeight());
			AllocatedTextureUtil.uploadTexture(resource.getTextureId()[0], resource.getData()[0], resource.getWidth(), resource.getHeight());
			this.currentIndex = index;
		}

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, resource.getTextureId()[0]);
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		this.gif.frames.forEach(frame -> frame.img = null);
		this.gif = null;
	}

	/* [ Query Section ] */
	public @NonNull GifResourceDecoder start() {
		this.startTime = System.currentTimeMillis();
		this.pauseTime = 0;
		return this;
	}

	public @NonNull GifResourceDecoder resume() {
		if (this.pauseTime != 0) {
			this.startTime += System.currentTimeMillis() - this.pauseTime;
			this.pauseTime = 0;
		}
		return this;
	}

	public @NonNull GifResourceDecoder pause() {
		this.pauseTime = System.currentTimeMillis();
		return this;
	}

	public @NonNull GifResourceDecoder speed(final float speed) {
		this.speed = speed;
		return this;
	}

	public @NonNull GifResourceDecoder mode(final @NonNull GifLoopMode loopMode) {
		this.loopMode = loopMode;
		return this;
	}

	/* [ Getter Section ] */
	public float getDelay(final int index) {
		return this.delays[index] / this.speed;
	}

	public long getCurrentTime() {
		if (this.pauseTime != 0) {
			return this.pauseTime;
		}
		return System.currentTimeMillis();
	}

	/* [ DTO Section ] */
	public enum GifLoopMode {

		LOOP,
		FIRST_FRAME,
		LAST_FRAME,

	}

	protected static final class GifDecoder {

		private final class BitReader {

			private int nextBitToRead;
			private int numberOfBitsToRead;
			private int bitMask;
			private byte[] bytes;

			private void init(final byte[] bytes) {
				this.bytes = bytes;
				this.nextBitToRead = 0;
			}

			private int read() {
				int byteIndex = this.nextBitToRead >>> 3;
		final int bitsToShiftRight = this.nextBitToRead & 7;
		int byte0, byte1, byte2;
		byte0 = this.bytes[byteIndex++] & 0xFF;
		byte1 = this.bytes[byteIndex++] & 0xFF;
		byte2 = this.bytes[byteIndex] & 0xFF;
		final int buffer = ((byte2 << 8 | byte1) << 8 | byte0) >>> bitsToShiftRight;
		this.nextBitToRead += this.numberOfBitsToRead;
		return buffer & this.bitMask;
			}

			private void setNumberOfBitsToRead(final int numberOfBitsToRead) {
				this.numberOfBitsToRead = numberOfBitsToRead;
				this.bitMask = (1 << numberOfBitsToRead) - 1;
			}

		}

		private final class CodeTable {

			private final int[][] table;
			private int initTableSize;
			private int initCodeSize;
			private int initCodeLimit;
			private int codeSize;
			private int nextCode;
			private int nextCodeLimit;
			private BitReader bitReader;

			public CodeTable() {
				this.table = new int[4096][1];
			}

			private int add(final int[] indices) {
				if (this.nextCode < 4096) {
					if (this.nextCode == this.nextCodeLimit && this.codeSize < 12) {
						this.codeSize++;
						this.bitReader.setNumberOfBitsToRead(this.codeSize);
						this.nextCodeLimit = (1 << this.codeSize) - 1;
					}
					this.table[this.nextCode++] = indices;
				}
				return this.codeSize;
			}

			private int clear() {
				this.codeSize = this.initCodeSize;
				this.bitReader.setNumberOfBitsToRead(this.codeSize);
				this.nextCodeLimit = this.initCodeLimit;
				this.nextCode = this.initTableSize;
				return this.codeSize;
			}

			private void init(final GifFrame fr, final int[] activeColTbl, final BitReader br) {
				this.bitReader = br;
				final int numColors = activeColTbl.length;
				this.initCodeSize = fr.firstCodeSize;
				this.initCodeLimit = (1 << this.initCodeSize) - 1;
				this.initTableSize = fr.endOfInfoCode + 1;
				this.nextCode = this.initTableSize;
				for (int c = numColors - 1; c >= 0; c--) {
					this.table[c][0] = activeColTbl[c];
				}
				this.table[fr.clearCode] = new int[]{fr.clearCode};
				this.table[fr.endOfInfoCode] = new int[]{fr.endOfInfoCode};
				if (fr.transpColFlag && fr.transpColIndex < numColors) {
					this.table[fr.transpColIndex][0] = 0;
				}
			}

		}

		@SuppressWarnings("unused")
		private final class GifFrame {

			private int disposalMethod;
			private boolean transpColFlag;
			private int delay;
			private int transpColIndex;

			private int x;
			private int y;
			private int w;
			private int h;
			private int wh;
			private boolean hasLocColTbl;
			private boolean interlaceFlag;
			private boolean sortFlag;
			private int sizeOfLocColTbl;
			private int[] localColTbl;

			private int firstCodeSize;
			private int clearCode;
			private int endOfInfoCode;
			private byte[] data;
			private BufferedImage img;

		}

		@SuppressWarnings("unused")
		public final class GifImage {

			private final List<GifFrame> frames = new ArrayList<>(64);
			private final BitReader bits = new BitReader();
			private final CodeTable codes = new CodeTable();

			private String header;
			private int w;
			private int h;
			private int wh;
			private boolean hasGlobColTbl;
			private int colorResolution;
			private boolean sortFlag;
			private int sizeOfGlobColTbl;
			private int bgColIndex;
			private int pxAspectRatio;
			private int[] globalColTbl;
			private String appId = "";
			private String appAuthCode = "";
			private int repetitions = 0;
			private BufferedImage img = null;
			private Graphics2D g;

			private int[] decode(final GifFrame fr, final int[] activeColTbl) {
				this.codes.init(fr, activeColTbl, this.bits);
				this.bits.init(fr.data);
				final int clearCode = fr.clearCode, endCode = fr.endOfInfoCode;
				final int[] out = new int[this.wh];
				final int[][] tbl = this.codes.table;
				int outPos = 0;
				this.codes.clear();
				this.bits.read();
				int code = this.bits.read();
				int[] pixels = tbl[code];
				System.arraycopy(pixels, 0, out, outPos, pixels.length);
				outPos += pixels.length;

				try {
					while (true) {
						final int prevCode = code;
						code = this.bits.read();
						if (code == clearCode) {
							this.codes.clear();
							code = this.bits.read();
							pixels = tbl[code];
							System.arraycopy(pixels, 0, out, outPos, pixels.length);
							outPos += pixels.length;
							continue;
						}
						if (code == endCode) {
							break;
						}

						final int[] prevVals = tbl[prevCode];
						final int[] prevValsAndK = new int[prevVals.length + 1];
						System.arraycopy(prevVals, 0, prevValsAndK, 0, prevVals.length);
						if (code < this.codes.nextCode) {
							pixels = tbl[code];
							System.arraycopy(pixels, 0, out, outPos, pixels.length);
							outPos += pixels.length;
							prevValsAndK[prevVals.length] = tbl[code][0];
						} else {
							prevValsAndK[prevVals.length] = prevVals[0];
							System.arraycopy(prevValsAndK, 0, out, outPos, prevValsAndK.length);
							outPos += prevValsAndK.length;
						}

						this.codes.add(prevValsAndK);
					}
				} catch (final ArrayIndexOutOfBoundsException ignored) {}

				return out;
			}

			private int[] deinterlace(final int[] src, final GifFrame fr) {
				final int w = fr.w, h = fr.h, wh = fr.wh;
				final int[] dest = new int[src.length];

				final int set2Y = h + 7 >>> 3;
				final int set3Y = set2Y + (h + 3 >>> 3);
				final int set4Y = set3Y + (h + 1 >>> 2);

				final int set2 = w * set2Y, set3 = w * set3Y, set4 = w * set4Y;
				final int w2 = w << 1, w4 = w2 << 1, w8 = w4 << 1;

				int from = 0, to = 0;
				for (; from < set2; from += w, to += w8) {
					System.arraycopy(src, from, dest, to, w);
				}

				for (to = w4; from < set3; from += w, to += w8) {
					System.arraycopy(src, from, dest, to, w);
				}

				for (to = w2; from < set4; from += w, to += w4) {
					System.arraycopy(src, from, dest, to, w);
				}

				for (to = w; from < wh; from += w, to += w2) {
					System.arraycopy(src, from, dest, to, w);
				}

				return dest;
			}

			private void drawFrame(final GifFrame fr) {
				final int[] activeColTbl = fr.hasLocColTbl ? fr.localColTbl : this.globalColTbl;

				int[] pixels = this.decode(fr, activeColTbl);
				if (fr.interlaceFlag) {
					pixels = this.deinterlace(pixels, fr);
				}

				final BufferedImage frame = new BufferedImage(fr.w, fr.h, 2);
				System.arraycopy(pixels, 0, ((DataBufferInt) frame.getRaster().getDataBuffer()).getData(), 0, fr.wh);

				this.g.drawImage(frame, fr.x, fr.y, null);

				final int[] prevPx = new int[this.wh];
				System.arraycopy(((DataBufferInt) this.img.getRaster().getDataBuffer()).getData(), 0, prevPx, 0, this.wh);

				fr.img = new BufferedImage(this.w, this.h, 2);
				System.arraycopy(prevPx, 0, ((DataBufferInt) fr.img.getRaster().getDataBuffer()).getData(), 0, this.wh);

				if (fr.disposalMethod == 2) {
					this.g.clearRect(fr.x, fr.y, fr.w, fr.h);
				} else if (fr.disposalMethod == 3) {
					System.arraycopy(prevPx, 0, ((DataBufferInt) this.img.getRaster().getDataBuffer()).getData(), 0, this.wh);
				}
			}

			private final int getBackgroundColor() {
				final GifFrame frame = this.frames.get(0);
				if (frame.hasLocColTbl) {
					return frame.localColTbl[this.bgColIndex];
				}

				if (this.hasGlobColTbl) {
					return this.globalColTbl[this.bgColIndex];
				}

				return 0;
			}

			public final int getDelay(final int index) {
				return this.frames.get(index).delay;
			}

			public BufferedImage getFrame(final int index) {
				if (this.img == null) {
					this.img = new BufferedImage(this.w, this.h, 2);
					this.g = this.img.createGraphics();
					this.g.setBackground(new Color(0, true));
				}

				GifFrame fr = this.frames.get(index);
				if (fr.img == null) {
					for (int i = 0; i <= index; i++) {
						fr = this.frames.get(i);
						if (fr.img == null) {
							this.drawFrame(fr);
						}
					}
				}

				return fr.img;
			}

			public final int getFrameCount() {
				return this.frames.size();
			}

			public final int getHeight() {
				return this.h;
			}

			public final int getWidth() {
				return this.w;
			}

		}

		public static GifImage read(final InputStream is) throws IOException {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			final byte[] tmp = new byte[8192];

			int read;
			while ((read = is.read(tmp)) != -1) {
				buffer.write(tmp, 0, read);
			}

			is.close();
			return GifDecoder.read(buffer.toByteArray());
		}

		private static GifImage read(final byte[] in) throws IOException {
			final GifDecoder decoder = new GifDecoder();
			final GifImage img = decoder.new GifImage();
			GifFrame frame = null;
			int pos = GifDecoder.readHeader(in, img);
			pos = GifDecoder.readLogicalScreenDescriptor(img, in, pos);
			if (img.hasGlobColTbl) {
				img.globalColTbl = new int[img.sizeOfGlobColTbl];
				pos = GifDecoder.readColTbl(in, img.globalColTbl, pos);
			}
			while (pos < in.length) {
				final int block = in[pos] & 0xFF;
				switch (block) {
				case 0x21:
					if (pos + 1 >= in.length) {
						throw new IOException("Unexpected end of file.");
					}
					switch (in[pos + 1] & 0xFF) {
					case 0xFE:
						pos = GifDecoder.readTextExtension(in, pos);
						break;
					case 0xFF:
						pos = GifDecoder.readAppExt(img, in, pos);
						break;
					case 0x01:
						frame = null;
						pos = GifDecoder.readTextExtension(in, pos);
						break;
					case 0xF9:
						if (frame == null) {
							frame = decoder.new GifFrame();
							img.frames.add(frame);
						}
						pos = GifDecoder.readGraphicControlExt(frame, in, pos);
						break;
					default:
						throw new IOException("Unknown extension at " + pos);
					}
					break;
				case 0x2C:
					if (frame == null) {
						frame = decoder.new GifFrame();
						img.frames.add(frame);
					}
					pos = GifDecoder.readImgDescr(frame, in, pos);
					if (frame.hasLocColTbl) {
						frame.localColTbl = new int[frame.sizeOfLocColTbl];
						pos = GifDecoder.readColTbl(in, frame.localColTbl, pos);
					}
					pos = GifDecoder.readImgData(frame, in, pos);
					frame = null;
					break;
				case 0x3B:
					return img;
				default:
					final double progress = 1.0 * pos / in.length;
					if (progress < 0.9) {
						throw new IOException("Unknown block at: " + pos);
					}
					pos = in.length;
				}
			}
			return img;
		}

		private static int readAppExt(final GifImage img, final byte[] in, int i) {
			img.appId = new String(in, i + 3, 8);
			img.appAuthCode = new String(in, i + 11, 3);
			i += 14;
			final int subBlockSize = in[i] & 0xFF;

			if (subBlockSize == 3) {
				img.repetitions = in[i + 2] & 0xFF | in[i + 3] & 0xFF << 8;
				return i + 5;
			}

			while ((in[i] & 0xFF) != 0) {
				i += (in[i] & 0xFF) + 1;
			}

			return i + 1;
		}

		private static int readColTbl(final byte[] in, final int[] colors, int i) {
			final int numColors = colors.length;
			for (int c = 0; c < numColors; c++) {
				final int a = 0xFF;
				final int r = in[i++] & 0xFF;
				final int g = in[i++] & 0xFF;
				final int b = in[i++] & 0xFF;
				colors[c] = ((a << 8 | r) << 8 | g) << 8 | b;
			}
			return i;
		}

		private static int readGraphicControlExt(final GifFrame fr, final byte[] in, final int i) {
			fr.disposalMethod = (in[i + 3] & 0b00011100) >>> 2;
				fr.transpColFlag = (in[i + 3] & 1) == 1;
				fr.delay = in[i + 4] & 0xFF | (in[i + 5] & 0xFF) << 8;
				fr.transpColIndex = in[i + 6] & 0xFF;
				return i + 8;
		}

		private static int readHeader(final byte[] in, final GifImage img) throws IOException {
			if (in.length < 6) {
				throw new IOException("Image is truncated.");
			}

			img.header = new String(in, 0, 6);
			if (!"GIF87a".equals(img.header) && !"GIF89a".equals(img.header)) {
				throw new IOException("Invalid GIF header.");
			}

			return 6;
		}

		private static int readImgData(final GifFrame fr, final byte[] in, int i) {
			final int fileSize = in.length;
			final int minCodeSize = in[i++] & 0xFF;
			final int clearCode = 1 << minCodeSize;
			fr.firstCodeSize = minCodeSize + 1;
			fr.clearCode = clearCode;
			fr.endOfInfoCode = clearCode + 1;
			final int imgDataSize = GifDecoder.readImgDataSize(in, i);
			final byte[] imgData = new byte[imgDataSize + 2];
			int imgDataPos = 0;
			int subBlockSize = in[i] & 0xFF;
			while (subBlockSize > 0) {
				try {
					final int nextSubBlockSizePos = i + subBlockSize + 1;
					final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
					System.arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
					imgDataPos += subBlockSize;
					i = nextSubBlockSizePos;
					subBlockSize = nextSubBlockSize;
				} catch (final Exception e) {
					subBlockSize = fileSize - i - 1;
					System.arraycopy(in, i + 1, imgData, imgDataPos, subBlockSize);
					imgDataPos += subBlockSize;
					i += subBlockSize + 1;
					break;
				}
			}

			fr.data = imgData;
			i++;
			return i;
		}

		private static int readImgDataSize(final byte[] in, int i) {
			final int fileSize = in.length;
			int imgDataPos = 0;
			int subBlockSize = in[i] & 0xFF;
			while (subBlockSize > 0) {
				try {
					final int nextSubBlockSizePos = i + subBlockSize + 1;
					final int nextSubBlockSize = in[nextSubBlockSizePos] & 0xFF;
					imgDataPos += subBlockSize;
					i = nextSubBlockSizePos;
					subBlockSize = nextSubBlockSize;
				} catch (final Exception e) {
					subBlockSize = fileSize - i - 1;
					imgDataPos += subBlockSize;
					break;
				}
			}
			return imgDataPos;
		}

		private static int readImgDescr(final GifFrame fr, final byte[] in, int i) {
			fr.x = in[++i] & 0xFF | (in[++i] & 0xFF) << 8;
			fr.y = in[++i] & 0xFF | (in[++i] & 0xFF) << 8;
			fr.w = in[++i] & 0xFF | (in[++i] & 0xFF) << 8;
			fr.h = in[++i] & 0xFF | (in[++i] & 0xFF) << 8;
			fr.wh = fr.w * fr.h;
			final byte b = in[++i];
			fr.hasLocColTbl = (b & 0b10000000) >>> 7 == 1;
			fr.interlaceFlag = (b & 0b01000000) >>> 6 == 1;
			fr.sortFlag = (b & 0b00100000) >>> 5 == 1;
			final int colTblSizePower = (b & 7) + 1;
			fr.sizeOfLocColTbl = 1 << colTblSizePower;
			return ++i;
		}

		private static int readLogicalScreenDescriptor(final GifImage img, final byte[] in, final int i) {
			img.w = in[i] & 0xFF | (in[i + 1] & 0xFF) << 8;
			img.h = in[i + 2] & 0xFF | (in[i + 3] & 0xFF) << 8;
			img.wh = img.w * img.h;
			final byte b = in[i + 4];
			img.hasGlobColTbl = (b & 0b10000000) >>> 7 == 1;
			final int colResPower = ((b & 0b01110000) >>> 4) + 1;
			img.colorResolution = 1 << colResPower;
			img.sortFlag = (b & 0b00001000) >>> 3 == 1;
			final int globColTblSizePower = (b & 7) + 1;
			img.sizeOfGlobColTbl = 1 << globColTblSizePower;
			img.bgColIndex = in[i + 5] & 0xFF;
			img.pxAspectRatio = in[i + 6] & 0xFF;
			return i + 7;
		}

		private static int readTextExtension(final byte[] in, final int pos) {
			int i = pos + 2;
			int subBlockSize = in[i++] & 0xFF;
			while (subBlockSize != 0 && i < in.length) {
				i += subBlockSize;
				subBlockSize = in[i++] & 0xFF;
			}
			return i;
		}

	}

}