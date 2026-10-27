---
summary: An image of the host, by image view, that JOID samples without owning it.
---

The image must be in `SHADER_READ_ONLY_OPTIMAL` when JOID samples it.

Learn: [Show a texture of your application](/use/learn/describing-the-ui/media/#show-a-texture-of-your-application), [Lend a texture of the host](/build/resource-resolvers/#lend-a-texture-of-the-host).

## of(VulkanImage)

Wraps that image.
Throws an `IllegalStateException` when the Vulkan backend is not registered.

## of(Supplier)

Wraps the image that the supplier returns, read again at each use, for a host that recreates its image.
Throws an `IllegalStateException` when the Vulkan backend is not registered.

## getView()

Returns the view of the current image; when the supplier returns another image, forgets the descriptor sets of the previous view.
