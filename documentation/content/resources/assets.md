# Assets

An `Asset` (`dev.joid.lib.asset`) is a named source of bytes: it knows how to open a stream on its content and nothing else. JOID turns every handle you pass to `Resource.of` into an asset, and you teach JOID new kinds of handles (a path type, an archive entry, the resource system of a game) by registering an `IAssetLocator`.

## Asset.of and the built-in handles

`Asset.of(handle)` returns the asset for a handle. `Resource.of` and `ResourceBuilder.of` call it for every input that no [resolver](custom-formats.md#resolvers-for-in-memory-inputs) takes, and `MsdfFontLoader.load` calls it for every font face (see [Fonts](../fonts/adding-fonts.md)).

| Handle | Asset | Unique id | Remote | Reopenable |
|---|---|---|---|---|
| `File` | `FileAsset` | the absolute path | no | yes |
| `String` | `UrlAsset` | the URL | yes | yes |
| `InputStream` | `StreamAsset` | the stream's `toString()` | no | no |
| `Asset` | the asset itself | unchanged | as defined | as defined |

A handle that no locator supports throws an `IllegalArgumentException` (`No asset locator found for input of type ...`), and `null` throws a `NullPointerException`.

```java
final Asset file = Asset.of(new File("images/logo.png"));
final Asset url = Asset.of("https://placehold.co/100x100.png");
final Asset stream = Asset.of(MyUI.class.getResourceAsStream("/assets/logo.png"));
```

The unique id is the key of the [resource cache](resources.md#caching-and-unique-ids): two handles with the same id share their decoded data.

## FileAsset

`FileAsset.create(File file)` reads a file of the disk. `getFile()` returns it. Each `open()` opens a new `FileInputStream`.

## StreamAsset

`StreamAsset.create(InputStream stream)` wraps a stream that is already open, in a `BufferedInputStream` when it is not one. `open()` returns that same stream every time, so its content can be read only once:

- `peek(length)` marks and resets the stream, so format detection does not consume it;
- `isReopenable()` returns `false`;
- its id is the stream's `toString()`, so two `Resource.of(stream)` calls never share data, even on the same file.

A video read from a stream is copied into a temporary file before it is opened, so it can still seek and loop.

## UrlAsset

`UrlAsset.create(String url)` downloads over HTTP or HTTPS. `getUrl()` returns the URL.

- Each `open()` makes a new request with a desktop browser `User-Agent`. Nothing is cached on the disk.
- When an `https:` request fails, the same URL is tried with `http:`.
- `isRemote()` returns `true`: the resource pipeline detects the format of a URL off the calling thread when the resource is asynchronous. See [Remote assets](#remote-assets-and-isremote).
- Only `http:` and `https:` URLs are supported. Load local files with a `File`, and files of your jar with a stream or an [asset of your own](#loading-files-of-your-jar).

## Loading files of your jar

`getResourceAsStream` gives a new stream, and so a new id, at every call. To load a file of your jar once and share it, write an asset with a stable id:

```java
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import dev.joid.lib.asset.Asset;

public final class ClasspathAsset extends Asset {

    private final String path;

    private ClasspathAsset(final String path) {
        super("classpath:" + path);
        this.path = path;
    }

    public static ClasspathAsset create(final String path) {
        return new ClasspathAsset(path);
    }

    @Override
    public InputStream open() throws IOException {
        final InputStream stream = ClasspathAsset.class.getResourceAsStream(this.path);
        if (stream == null) {
            throw new FileNotFoundException(this.path);
        }
        return stream;
    }

}
```

```java
ResourceNode.create(0, 0, 32, 32).resource(Resource.of(ClasspathAsset.create("/assets/icons/close.png"))).attach(this);
```

An `Asset` passed to `Asset.of` is returned as is, so it works everywhere a handle is accepted.

## Writing an IAssetLocator

A locator turns a kind of handle into an asset. This one lets `Resource.of` accept a `java.nio.file.Path`:

```java
import java.nio.file.Path;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.asset.dto.impl.FileAsset;
import dev.joid.lib.asset.dto.locator.IAssetLocator;

public final class PathAssetLocator implements IAssetLocator {

    @Override
    public boolean supports(final Object handle) {
        return handle instanceof Path;
    }

    @Override
    public Asset locate(final Object handle) {
        return FileAsset.create(((Path) handle).toFile());
    }

}
```

Register it once at startup, before loading anything with it:

```java
AssetLocator.register(new PathAssetLocator());

final Resource logo = Resource.of(Paths.get("images", "logo.png"));
```

| Method of `IAssetLocator` | Description |
|---|---|
| `supports(Object handle)` | `true` when this locator handles `handle`. |
| `locate(Object handle)` | The asset for a supported handle. |

### AssetLocator registry

`AssetLocator` (`dev.joid.lib.asset.dto.locator`) holds the locators. The built-in ones are registered first, in this order: `UrlAssetLocator` (`String`), `FileAssetLocator` (`File`), `StreamAssetLocator` (`InputStream`).

| Method | Description |
|---|---|
| `static register(IAssetLocator locator)` | Adds a locator in front of the others: the latest registered locator is asked first, so yours wins over a built-in one for a handle both support. |
| `static supports(Object handle)` | `true` when `handle` is an `Asset` or a registered locator supports it. |
| `static locate(Object handle)` | The asset of the first locator that supports `handle`; `handle` itself when it is an `Asset`. Throws an `IllegalArgumentException` when no locator supports it. |

## Remote assets and isRemote

`isRemote()` tells the resource pipeline that opening the asset may block on the network. For a remote asset that is not cached yet, `ResourceBuilder.of` creates the resource without a decoder and detects the format in a task: on a daemon thread named `ResourceTask/<id>` when the resource is asynchronous, at once when it is blocking. A local asset gets its decoder before `of` returns.

Override `isRemote()` to return `true` in an asset that downloads its content:

```java
@Override
public boolean isRemote() {
    return true;
}
```

`isReopenable()` describes whether `open()` can be called more than once; the library does not change its behavior on it.

## Asset reference

| Method | Description |
|---|---|
| `static of(Object handle)` | The asset of a handle, through the registered locators. |
| `protected Asset(String uniqueId)` | Constructor of a subclass. |
| `getUniqueId()` | The id of the content, used as the cache key. |
| `open()` | Opens a stream on the content. Abstract; throws `IOException`. |
| `peek(int length)` | The first `length` bytes, fewer when the content is shorter, an empty array when the asset cannot be opened. |
| `read()` | The whole content. Throws `IOException`. |
| `isRemote()` | `false` by default. See [Remote assets](#remote-assets-and-isremote). |
| `isReopenable()` | `true` by default. |
| `toString()` | `ClassName[uniqueId]`, for example `FileAsset[/home/me/logo.png]`. |

## See also

- [Resources](resources.md) — loading, caching and releasing what an asset contains.
- [Supported Formats](formats.md) — how the first bytes of an asset choose its decoder.
- [Custom Formats and Decoders](custom-formats.md) — resolvers, formats and decoders.
- [Fonts](../fonts/adding-fonts.md) — font faces load from the same handles.