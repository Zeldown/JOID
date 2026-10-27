---
title: "Resource resolvers"
description: "Let Resource.of accept the objects of your engine, and lend the textures of the engine to JOID without copying them."
genre: build
sidebar:
  order: 2
---

[[Resource#of]] accepts files, URLs, streams and assets, and the textures that a backend lends ([Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application) gives the user side).
The host of your backend also has images of its own: a texture it rendered, a sprite of its atlas, a video frame on the GPU.
A borrowed texture lets JOID draw them without copying a pixel, and a resource resolver lets `Resource.of` accept the objects of your engine directly.

## How Resource.of picks a resolver

1. `Resource.of(input)` asks the resolvers of [[ResourceResolver]] in turn, the last registered first.
2. The first one whose `supports(input)` returns `true` builds the resource.
3. When none does, the input is read as an asset (a path, a URL, a stream) and decoded by its format.

The core registers two resolvers: one for an [[ITexture]], which turns any texture into a resource, and one for a `BufferedImage`.
So a texture that your backend lends is a resource already: `Resource.of(texture)`.

## Lend a texture of the host

A [[BorrowedTexture]] wraps a handle of the host behind a `Supplier`.
JOID reads the supplier each time it draws, so the texture follows the host when it replaces its handle (a resize, a new frame of a video).

```java file="com/example/backend/EngineBorrowedTexture.java" region="borrowed"
```

| Hook | What it answers for the handle |
|---|---|
| `getWidth(handle)`, `getHeight(handle)` | Its size in pixels. |
| `isValid(handle)` | Whether the host still owns it. |
| `isMipmapped(handle)` | Whether it has mip levels, so that JOID samples it with a mipmap filter. |

A `null` handle reads as a texture of size `0`, which JOID does not draw.
JOID never writes into a borrowed texture nor deletes it: `allocate` and `upload` throw an `UnsupportedOperationException`, and `delete` does nothing.

Your render bridge binds a borrowed texture like its own textures: here both implement `IEngineTexture`, so `drawPrimitive` reads the id of either.
On OpenGL and Vulkan, [[GlBorrowedTexture]] and [[VulkanBorrowedTexture]] do it already, created by `of(...)` once their render bridge is registered.

## Write the resolver

A resolver turns an object of the engine into a resource:

```java file="com/example/backend/EngineSpriteResourceResolver.java" region="resolver"
```

- `supports` answers from the type of the input only, quickly: it runs for every `Resource.of`.
- `resolve` gives the resource a unique id, so that two calls with the same object share one resource through `builder.compute(...)`.
- Register it from `registerDefaults()` with `ResourceResolver.register(...)` ([Writing a backend](/build/writing-a-backend/#register-the-defaults)): it then comes before the resolvers of the core.

:::caution
Keep `supports` narrow.
A resolver that accepts every `Integer` or every `String` takes the inputs that the user meant as asset paths or as the textures of another resolver.
:::

## Guarantees

[[BorrowedTextureContractSuite]] checks a borrowed texture on a real context ([Testing a backend](/build/testing-a-backend/#the-suites)):

| Rule | Test |
|---|---|
| JOID draws the texture of the host. | `drawsABorrowedTexture` |
| Releasing the resource keeps the texture of the host alive. | `keepsTheBorrowedTextureOnceTheResourceIsReleased` |
| Writing into it fails. | `refusesToWriteABorrowedTexture` |
| JOID sees whether it has mip levels. | `readsTheMipLevelsOfTheBorrowedTexture` |
| The host finds its texture parameters unchanged after the frame. | `restoresTheTextureParametersAfterTheFrame` |
| A new handle from the supplier is drawn on the next frame. | `followsTheHandleOfItsSupplier` |
