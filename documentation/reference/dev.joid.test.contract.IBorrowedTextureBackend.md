---
summary: A snapshot backend that can create, lend and inspect a texture of its host, for the borrowed texture suite.
---

Learn: [The suites](/build/testing-a-backend/#the-suites).

## borrow(Supplier)

Returns the borrowed texture of your backend over that supplier of host handles.

## isBorrowableTexture(Object)

If `true`, the object is a handle of a texture of the host that `borrow` accepts.

## createBorrowableTexture(int,int,int,boolean)

Creates a texture of the host of that size, filled with that ARGB color, with mipmaps when the boolean is `true`, and returns its handle.

## readBorrowableParameters(Object)

Returns the sampling parameters of the texture of the host by name, such as its filter and its wrap, to compare before and after a frame.
