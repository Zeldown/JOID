---
summary: The contract tests of the textures that a backend borrows from its host.
---

Learn: [The suites](/build/testing-a-backend/#the-suites), [Lend a texture of the host](/build/resource-resolvers/#lend-a-texture-of-the-host).

## BorrowedTextureContractSuite()

Creates the suite, for a subclass.

## startBackend()

Creates the snapshot backend at the first test of the class.

## stopBackend()

Destroys the snapshot backend after the last test of the class.

## drawsABorrowedTexture()

Checks that a resource made from a borrowed texture draws its pixels at its size.

## keepsTheBorrowedTextureOnceTheResourceIsReleased()

Checks that releasing the resource leaves the texture of the host alive.

## refusesToWriteABorrowedTexture()

Checks that `allocate` and `upload` throw an `UnsupportedOperationException` on a borrowed texture.

## readsTheMipLevelsOfTheBorrowedTexture()

Checks that a borrowed texture reports whether the host gave it mipmaps.

## restoresTheTextureParametersAfterTheFrame()

Checks that the filter and the wrap of the texture of the host are back after a frame that sampled it.

## followsTheHandleOfItsSupplier()

Checks that a borrowed texture follows the handle that its supplier returns, size included.

## createBackend()

JOID calls it once, at the first test of the class.
Return a new snapshot backend of your backend that implements [[IBorrowedTextureBackend]].
