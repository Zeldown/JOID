---
summary: A framebuffer binding that notes in a `GlStateJournal` the framebuffers and renderbuffers that JOID binds, before passing the call on.
---

[[JournalGlBinding#getFrameBufferBinding]] returns it.

Learn: [The state journal](/build/embedding-in-a-host/#the-state-journal).

## JournalGlFrameBufferBinding(IGlFrameBufferBinding,GlStateJournal)

Wraps the framebuffer binding around the journal.

## genFramebuffer()

Calls the wrapped binding.

## deleteFramebuffer(int)

Calls the wrapped binding.

## bindFramebuffer(int,int)

Notes the framebuffers bound to that target in the journal, then calls the wrapped binding.

## framebufferTexture2D(int,int,int,int,int)

Calls the wrapped binding.

## genRenderbuffer()

Calls the wrapped binding.

## deleteRenderbuffer(int)

Calls the wrapped binding.

## bindRenderbuffer(int,int)

Notes the bound renderbuffer in the journal, then calls the wrapped binding.

## renderbufferStorage(int,int,int,int)

Calls the wrapped binding.

## framebufferRenderbuffer(int,int,int,int)

Calls the wrapped binding.

## blitFramebuffer(int,int,int,int,int,int,int,int,int,int)

Calls the wrapped binding.
