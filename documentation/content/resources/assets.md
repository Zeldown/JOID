# Assets

An `Asset` is a **lazy, named source of bytes**. It answers one question — *where do these bytes come from?* — and nothing else. Decoding, texture upload and caching happen above it, in [Resource](resource-builder.md).

This is the single extension point to teach JOID about your project's own way of addressing files: a Minecraft `ResourceLocation`, a mod container entry, a CDN key, an encrypted archive. Register one locator at startup and **every** subsystem that reads bytes accepts your handle — textures, videos and fonts alike.

## `Asset`

```java
public abstract class Asset {

    public static @NonNull Asset of(final @NonNull Object handle);

    public abstract @NonNull InputStream open() throws IOException;

    public boolean isReopenable();
    public boolean isRemote();

    public @NonNull byte[] peek(final int length);
    public final @NonNull byte[] read() throws IOException;

    public final @NonNull String getUniqueId();

}
```

- **`open()`** is lazy and is called on the thread that needs the bytes, never at creation time.
- **`getUniqueId()`** keys the resource cache. Two handles pointing at the same file must produce the same id; two distinct sources must not collide.
- **`isReopenable()`** is `false` only for an already-open `InputStream`, which can be consumed once.
- **`isRemote()`** tells JOID whether opening may block on the network. A remote asset gets its decoder picked on a worker thread; a local one is resolved inline.
- **`peek(length)`** reads the first bytes **without consuming** the asset, and returns an empty array when the source cannot be opened.

## Built-in assets

| Handle | Asset | Unique id |
|---|---|---|
| `InputStream` | `StreamAsset` | the stream's identity — not reopenable, not cacheable across calls |
| `File` | `FileAsset` | the absolute path |
| `String` | `UrlAsset` | the URL itself — remote, with an HTTPS → HTTP fallback |
| `Asset` | itself | unchanged |

Locators are registered in `AssetLocator`'s static initializer and sit at the bottom of the registry, so your own always take precedence.

## Writing a locator

Two small classes: the asset says how to open, the locator says which handles it recognises.

```java
public final class ModAsset extends Asset {

    private final ResourceLocation location;

    private ModAsset(final @NonNull ResourceLocation location) {
        super(location.toString());
        this.location = location;
    }

    public static @NonNull ModAsset create(final @NonNull ResourceLocation location) {
        return new ModAsset(location);
    }

    @Override
    public @NonNull InputStream open() throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(this.location).getInputStream();
    }

}
```

```java
public class ModAssetLocator implements IAssetLocator {

    @Override
    public boolean supports(final @NonNull Object handle) {
        return handle instanceof ResourceLocation;
    }

    @Override
    public @NonNull Asset locate(final @NonNull Object handle) {
        return ModAsset.create((ResourceLocation) handle);
    }

}
```

Register it once, before the first load:

```java
AssetLocator.register(new ModAssetLocator());
```

From there, the handle works everywhere:

```java
Resource.of(new ResourceLocation(MOD_ID, "textures/gui/panel.png"));
FontLoader.load(new ResourceLocation(MOD_ID, "fonts/Inter/font.msdf"), font -> this.font = font);
```

`register(...)` prepends, so the registry is walked latest-first and a custom locator beats a built-in for a handle both accept.

## Why laziness matters

An `Asset` holds no bytes. Nothing is read until a decoder asks, which means the work lands on the thread that was meant to do it:

- A 200 MB video is copied to its temp file inside `decode()`, on the resource worker, not when you call `Resource.of(...)`.
- A font atlas is read on the font loader's pool, and a failure completes its `CompletableFuture` exceptionally instead of being thrown at the caller.
- A remote asset only opens a connection once its decoder is being chosen, on a worker thread.

## See also

- [ResourceBuilder](resource-builder.md) — caching and the texture pipeline above assets.
- [Resolvers](resolvers.md) — handling inputs that are already decoded.
- [Decoders](decoders.md) — what turns asset bytes into a texture.