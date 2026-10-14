package dev.joid.backend.vulkan;

import dev.joid.backend.vulkan.host.IVulkanHost;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.backend.vulkan.resource.VulkanImageResourceResolver;
import dev.joid.base.glfw.GlfwWindowBridge;
import dev.joid.base.glfw.input.GlfwInputForwarder;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.openal.binding.Lwjgl3AlBinding;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.resource.resolver.ResourceResolver;
import lombok.NonNull;

public final class VulkanBackend extends Backend<VulkanBackend> {

	private final long        window;
	private final IVulkanHost host;

	private VulkanRenderBridge render;

	private VulkanBackend(final long window, final IVulkanHost host) {
		super(JOID.VERSION);
		this.window = window;
		this.host   = host;
	}

	public static @NonNull VulkanBackend create(final long window, final @NonNull IVulkanHost host) {
		return new VulkanBackend(window, host);
	}

	public void destroy() {
		if (this.render == null) {
			throw new IllegalStateException("VulkanBackend has no render bridge to destroy: call register() first and destroy() only once");
		}

		this.render.destroy();
		this.render = null;
	}

	@Override
	protected @NonNull IAudioBridge createAudioBridge() {
		return AlAudioBridge.create(Lwjgl3AlBinding.inst());
	}

	@Override
	protected @NonNull IThreadBridge createThreadBridge() {
		return FrameThreadBridge.create();
	}

	@Override
	protected @NonNull IWindowBridge createWindowBridge() {
		return new GlfwWindowBridge(this.window);
	}

	@Override
	protected @NonNull IRenderBridge createRenderBridge() {
		this.render = new VulkanRenderBridge(this.host);
		return this.render;
	}

	@Override
	protected void registerDefaults() {
		ResourceResolver.register(VulkanImageResourceResolver.inst());
		BridgeHandler.UI.registerDefault(WindowUIBridge.create());
	}

	@Override
	protected void attachInput() {
		GlfwInputForwarder.create().attach(this.window);
	}

}