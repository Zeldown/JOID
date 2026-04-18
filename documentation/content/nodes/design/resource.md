# ResourceNode

Displays a `Resource` — PNG, JPG, GIF, any format your loader produces.

## Create

```java
try {
    ResourceNode.create(0, 0)
        .resource(Resource.of(MyClass.class.getResourceAsStream("/icon.png")))
        .attach(parent);
} catch (IOException e) {
    e.printStackTrace();
}
```

Sizing rules:

- `create(x, y)` — auto-size to the image's natural dimensions.
- `create(x, y, w, h)` — forced size. Behavior depends on `stretch()`.

## Sources

```java
node.resource(Resource resource);
node.resource(String url);                       // downloads async
node.resource(Resource main, Resource hovered);  // two-state swap
node.resource(String url, String hoveredUrl);
```

`Resource.of(String url)` queues an HTTP download in the background. The node renders a skeleton placeholder until loaded.

## Hover swap

```java
ResourceNode.create(0, 0, 48, 48)
    .resource(
        Resource.of(getClass().getResourceAsStream("/icon-off.png")),
        Resource.of(getClass().getResourceAsStream("/icon-on.png"))
    )
    .attach(parent);
```

The hovered resource auto-fades in via `hoverValue(1F)`.

## Stretch modes

```java
node.stretch(StretchType.STRETCH);  // fill bounds (default)
node.stretch(StretchType.CONTAIN);  // fit preserving aspect ratio, centered
```

## Filtering

```java
node.linear(true);   // smooth (default)
node.linear(false);  // nearest-neighbor (pixel art)
```

## Color tint

```java
node.color(Color.decode("#4a90e2"));              // tint
node.color(Color.WHITE, Color.decode("#ef4444")); // tint + hover tint
```

## Partial sizing

If only `width` is set, height is computed from the aspect ratio; vice versa:

```java
ResourceNode.create(0, 0).resource(r).height(100).attach(parent);  // width auto
ResourceNode.create(0, 0).resource(r).width(200).attach(parent);   // height auto
```

## Animated resources

JOID auto-detects GIF / APNG / MP4 / WebM / MKV streams via magic bytes — you get a `VideoResourceDecoder` under the hood. `ResourceNode` plays them automatically. For control (pause, seek, callbacks) use [VideoPlayerNode](video-player.md) instead.

## Best practices

- **Load resources once.** The `ResourceBuilder` caches by `uniqueId`, so `Resource.of(stream)` with the same stream returns the cached entry. If you're re-loading from disk, cache the `Resource` yourself.
- **Close streams in `try-with-resources` only if you're not using `Resource.of(InputStream)`.** The resource consumes the stream.
- **Prefer `StretchType.CONTAIN` for icons** inside a fixed slot; `STRETCH` looks cheap.

## See also

- [ResourceBuilder](../../resources/resource-builder.md)
- [Decoders](../../resources/decoders.md)
- [VideoPlayerNode](video-player.md)
