---
summary: The snapshot backend of Vulkan, which renders through a `SnapshotVulkanHost` into an offscreen image.
---

A hidden GLFW window without client API gives the window and input bridges; it ships in the dev and demo jars of the backend.

Learn: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface), [Tests](/build/backends/vulkan/#tests).

## create(int,int)

Opens the hidden window, creates the host and registers the backend without audio, then begins the first frame.

## destroy()

Ends the frame, destroys the backend, the host and the window, and gives the registries back the bridges they had before `create`.

## present()

Ends the frame of the host and begins the next one.

## capture(int,int)

Copies the bottom rows of the target image of that size into a buffer, ends the frame and returns the pixels.

## getRenderer()

Returns the name of the device.

## getBackend()

Returns the Vulkan backend.

## getHost()

Returns the Vulkan host.

## getWindow()

Returns the hidden window.
