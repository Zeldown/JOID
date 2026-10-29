---
title: "base-lwjgl3-openal"
description: "The OpenAL binding of LWJGL 3, for the audio bridge of base-openal."
genre: build
sidebar:
  order: 5
---

`joid-base-lwjgl3-openal` holds [[Lwjgl3AlBinding]], the [[IAlBinding]] of the OpenAL library of LWJGL 3.
Give `Lwjgl3AlBinding.inst()` to [[AlAudioBridge#create]] of [base-openal](/build/base-modules/openal/) whenever your engine plays its sound through LWJGL 3, whatever its window and its renderer: the LWJGL 3 and Vulkan backends do, and so does the host of [Embedding in a host](/build/embedding-in-a-host/#register-once-the-context-is-current).

The module depends on base-openal and on the `lwjgl` and `lwjgl-openal` jars of LWJGL 3, never on their natives: the application, or the final backend, adds the natives of its platform.
