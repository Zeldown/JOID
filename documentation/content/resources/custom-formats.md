# Custom Formats

Every step of the resource pipeline is open: add a format, write a decoder, build animations from your own frames, or accept new in-memory inputs. Use this page when the formats of [Images and Media](../concepts/media.md) do not cover your content.

```java
ResourceFormat.register(new PpmResourceFormat());
ResourceNode.create(100, 100, 256, 256).resource(Resource.of(new File("render.ppm"))).attach(this);
```

![A 256 by 256 gray image with a ring, decoded from a PPM file by a custom decoder](../images/custom-formats-ppm.png "A PPM file, a format JOID does not know, read by the decoder of this page.")

## The loading pipeline

![of(input) goes to the resolvers; an input already in memory goes straight to a ResourceData; any other input becomes an asset, whose format chooses a decoder, held by a ResourceData cached by unique id](../images/diagram-resource-pipeline.png "Five extension points, in the order JOID uses them.")

| Step | Extension point | Role |
|---|---|---|
| 1. Resolver | `IResourceResolver`, registered in `ResourceResolver` | Turns an input already in memory (`BufferedImage`, `ITexture`, your objects) into a resource; the next steps are skipped. |
| 2. Asset | `IAssetLocator`, registered in `AssetLocator` | Turns the input into an `Asset`, a source of bytes. |
| 3. Format | `IResourceFormat`, registered in `ResourceFormat` | Reads the first 512 bytes and chooses the decoder. |
| 4. Decoder | `IResourceDecoder` | Decodes the bytes into pixels and textures, and updates them over time. |
| 5. Data | `ResourceData` | Holds the decoder, the textures and the state, shared by every `Resource` of the same unique id. |

The latest registered format, locator or resolver is asked first, before the built-in ones.

## Adding a format with IResourceFormat

A format recognizes its content from its header and returns a new decoder. This one sends Ogg files to the video decoder:

```java
public class OggResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return header.length >= 4 && header[0] == 'O' && header[1] == 'g' && header[2] == 'g' && header[3] == 'S';
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return new VideoResourceDecoder(asset);
	}

}
```

Register it once at startup with `ResourceFormat.register(new OggResourceFormat())`. A `RuntimeException` thrown by `decoder(...)` makes the resource fail instead of throwing out of `Resource.of`.

## Writing a decoder with IResourceDecoder

JOID calls the methods of a decoder in this order:

| Method | When | Thread | What to do |
|---|---|---|---|
| `init(data)` | The data gets this decoder. | Creating thread | Nothing heavy. |
| `prepare(data)` | First draw. | Render thread | Create the textures with `BridgeHandler.RENDER.get().createTexture()` and give the data a placeholder with `data.texture(...)`. |
| `decode(data)` | Right after `prepare`. | Background, or render thread when blocking | Read the asset, set `width`, `height` and the ARGB pixels with `data(...)`. Never touch the GPU. |
| `upload(data)` | First draw after `decode`. | Render thread | Allocate the textures and upload the pixels. |
| `update(data)` | Every draw. | Render thread | Advance an animation, swap the displayed texture. |
| `clear(data)` | The resource is cleared. | Calling thread | Release files, threads, extra textures. |

Unreadable content never throws: call `data.fail(exception)` and return. The resource is drawn empty and `isFailed()` turns `true`. This decoder reads binary PPM images (`P6`, 8 bits per channel):

```java
public class PpmResourceDecoder implements IResourceDecoder {

	private final Asset asset;

	private ITexture texture;

	public PpmResourceDecoder(final @NonNull Asset asset) {
		this.asset = asset;
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1);
		resource.texture(this.texture);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try (InputStream stream = new BufferedInputStream(this.asset.open())) {
			PpmResourceDecoder.token(stream);
			final int width = Integer.parseInt(PpmResourceDecoder.token(stream));
			final int height = Integer.parseInt(PpmResourceDecoder.token(stream));
			final int maximum = Integer.parseInt(PpmResourceDecoder.token(stream));
			final int[] pixels = new int[width * height];
			for (int i = 0; i < pixels.length; i++) {
				final int red = stream.read() * 255 / maximum;
				final int green = stream.read() * 255 / maximum;
				final int blue = stream.read() * 255 / maximum;
				pixels[i] = 0xFF000000 | red << 16 | green << 8 | blue;
			}
			resource.width(width).height(height).data(new int[][] {pixels});
		} catch (final IOException | NumberFormatException exception) {
			resource.fail(exception);
		}
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		this.texture.allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[0], resource.getWidth(), resource.getHeight());
	}

	@Override
	public void update(final @NonNull ResourceData resource) {}

	@Override
	public void clear(final @NonNull ResourceData resource) {}

	private static String token(final InputStream stream) throws IOException {
		int read = stream.read();
		while (read == '#' || Character.isWhitespace(read)) {
			if (read == '#') {
				while (read != '\n' && read != -1) {
					read = stream.read();
				}
			}
			read = stream.read();
		}

		final StringBuilder builder = new StringBuilder();
		while (read != -1 && !Character.isWhitespace(read)) {
			builder.append((char) read);
			read = stream.read();
		}
		return builder.toString();
	}

}
```

Its format checks the `P6` magic number followed by a whitespace:

```java
public class PpmResourceFormat implements IResourceFormat {

	@Override
	public boolean supports(final @NonNull byte[] header) {
		return header.length >= 3 && header[0] == 'P' && header[1] == '6' && Character.isWhitespace(header[2]);
	}

	@Override
	public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
		return new PpmResourceDecoder(asset);
	}

}
```

Built-in decoders: `RasterResourceDecoder`, `AnimatedResourceDecoder`, `VectorResourceDecoder`, `VideoResourceDecoder`.

## A decoder without a format with ResourceBuilder.compute

`compute(uniqueId, supplier)` creates a resource around a decoder you build, without format detection. The unique id keys the cache: pick one that cannot collide with another source.

```java
final File file = new File("videos/archive.mkv");
final String uniqueId = "video:" + file.getAbsolutePath();
final Resource video = ResourceBuilder
.create()
.async()
.linear()
.compute(uniqueId, () -> new ResourceData(uniqueId, new VideoResourceDecoder(file)));
```

## Animations with IResourceAnimationReader

`AnimatedResourceDecoder` plays any animation that an `IResourceAnimationReader` reads; a reader returns the whole animation at once. This one plays a horizontal sprite strip:

```java
public class StripAnimationReader implements IResourceAnimationReader {

	private final int  frameCount;
	private final long frameDuration;

	public StripAnimationReader(final int frameCount, final long frameDuration) {
		this.frameCount = frameCount;
		this.frameDuration = frameDuration;
	}

	@Override
	public @NonNull ResourceAnimation read(final @NonNull InputStream stream) throws IOException {
		final BufferedImage strip = ImageIO.read(stream);
		if (strip == null) {
			throw new IOException("The sprite strip cannot be read");
		}

		final int width = strip.getWidth() / this.frameCount;
		final int height = strip.getHeight();
		final ResourceAnimationCanvas canvas = ResourceAnimationCanvas.create(width, height);
		final List<ResourceAnimationFrame> frames = new ArrayList<>();
		for (int i = 0; i < this.frameCount; i++) {
			final int[] pixels = strip.getRGB(i * width, 0, width, height, null, 0, width);
			frames.add(ResourceAnimationFrame.create(canvas.compose(pixels, 0, 0, width, height, Blend.SOURCE, Disposal.NONE), this.frameDuration));
		}
		return ResourceAnimation.create(width, height, 0, frames);
	}

}
```

```java
final File file = new File("sprites/coin.png");
final String uniqueId = "strip:" + file.getAbsolutePath();
final Resource coin = ResourceBuilder.create().async().nearest().compute(uniqueId, () -> new ResourceData(uniqueId, new AnimatedResourceDecoder(FileAsset.create(file), new StripAnimationReader(8, 80L))));
ResourceNode.create(100, 100, 128, 128).resource(coin).attach(this);
```

![A spinning coin played from an eight-frame sprite strip](../images/custom-formats-strip.gif "Eight frames of 80 ms, looping forever.")

| Class | Members |
|---|---|
| `ResourceAnimation` | `create(width, height, plays, frames)`: `plays` is the number of plays, `0` to loop forever. |
| `ResourceAnimationFrame` | `create(int[] pixels, long duration)`: full-size ARGB pixels and a duration in milliseconds. |
| `ResourceAnimationCanvas` | `create(width, height)`, then `compose(frame, x, y, frameWidth, frameHeight, blend, disposal)` draws a partial frame and returns the whole canvas. |
| `Blend`, `Disposal` | `SOURCE` replaces, `OVER` blends; before the next frame, `NONE` keeps, `BACKGROUND` clears, `PREVIOUS` restores. |

## Resolvers for in-memory inputs

An `IResourceResolver` handles inputs that need no asset. It creates its resource through `builder.compute`, so the cache and options of the builder apply, and calls the callback itself. This one accepts Swing icons:

```java
public class IconResourceResolver implements IResourceResolver {

	@Override
	public boolean supports(final @NonNull Object input) {
		return input instanceof Icon;
	}

	@Override
	public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
		final Icon icon = (Icon) input;
		final String uniqueId = "icon@" + Integer.toHexString(System.identityHashCode(icon));
		final Resource resource = builder.compute(uniqueId, () -> {
			final BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = image.createGraphics();
			icon.paintIcon(null, graphics, 0, 0);
			graphics.dispose();
			return new ResourceData(uniqueId, new RasterResourceDecoder(image));
		});

		if (callback != null) {
			callback.accept(resource);
		}
		return resource;
	}

}
```

```java
ResourceResolver.register(new IconResourceResolver());
final Resource folder = Resource.of(UIManager.getIcon("FileView.directoryIcon"));
```

## Reference

| Method | Description |
|---|---|
| `ResourceFormat.register(IResourceFormat)` | Adds a format in front of the others. |
| `ResourceResolver.register(IResourceResolver)` | Adds a resolver in front of the others. |
| `AssetLocator.register(IAssetLocator)` | Adds a locator that turns your handles into an `Asset`. |
| `ResourceBuilder.compute(uniqueId, supplier)` | A resource over the cached data of `uniqueId`, created by `supplier` when missing. |
| `new ResourceData(uniqueId, decoder)` | New data; calls `decoder.init`. |
| `ResourceData`: `texture(...)`, `width(...)`, `height(...)`, `data(int[][])`, `fail(Throwable)` | What a decoder fills. |

## Good to know

- Never touch the GPU in `decode`: it runs on a background thread. Create textures in `prepare`, fill them in `upload` or `update`.
- A registered format is asked before the built-in ones: make `supports` strict, or it steals PNG or MP4 files.
- Pick unique ids that cannot collide (`"strip:" + path`, not `path`): two sources with the same id share one `ResourceData`.

## See also

- [Images and Media](../concepts/media.md): loading, options, playback and errors.
- [ResourceNode](../nodes/visual/resource.md): the node that draws resources.
- [Drawing](../drawing/drawing.md): drawing resources in your own code.
- [Writing a Backend](../integration/writing-a-backend.md): textures and the render bridge.