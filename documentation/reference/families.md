## backends
The base class of the backends, the window bridge contract and the UI bridges that hold, route and draw the UIs of a window.
Learn: [Choosing a backend](/use/learn/running/backends/), [Embedding JOID](/use/learn/running/embedding/), [Writing a backend](/build/writing-a-backend/).

## bridges
The registries of bridges and the bridge contracts of the render, audio, clock, thread and signal replay services, with the textures, framebuffers, shaders and resource resolvers of the render bridge.
Learn: [The bridges](/build/bridges/), [Render bridge](/build/bridges/render/), [Resource resolvers](/build/resource-resolvers/).

## testing
The testkit: the snapshot runner that renders UIs on a hidden surface, the snapshot suite and the contract suites that check a backend, and the tools that compare renders.
Learn: [Testing your UIs](/use/learn/running/testing/), [Testing a backend](/build/testing-a-backend/).

## base-glfw
The window bridge, the input forwarder and the key table of GLFW, the OpenAL binding of LWJGL 3 and the hidden window of the snapshot backends.
Learn: [base-glfw](/build/base-modules/glfw/).

## base-openal
The audio bridge of OpenAL, its streaming sources and the binding that a backend gives it.
Learn: [base-openal](/build/base-modules/openal/).

## base-opengl
The render bridge of OpenGL, with its binding, its capabilities, its textures, framebuffers and shaders, and the state guard that gives a host its state back.
Learn: [base-opengl](/build/base-modules/opengl/), [Embedding in a host](/build/embedding-in-a-host/).

## lwjgl2
The backend for LWJGL 2: its natives, its OpenGL and OpenAL bindings, its window bridge, its input forwarder, its native cursors and its snapshot backend.
Learn: [LWJGL 2 backend](/build/backends/lwjgl2/).

## lwjgl3
The backend for LWJGL 3: its context requests, its OpenGL bindings and its snapshot backend with the OpenGL profiles of the tests.
Learn: [LWJGL 3 backend](/build/backends/lwjgl3/).

## vulkan
The backend for Vulkan: the hosts that lend their device and their frame, the render bridge that records into it, and its textures, framebuffers, shaders and caches.
Learn: [Vulkan backend](/build/backends/vulkan/).

## tools
The MSDF generator, which builds the atlas of a font file ahead of time or at its first load.
Learn: [Fonts](/use/learn/going-further/fonts/), [MSDF text](/use/in-depth/msdf-text/).
