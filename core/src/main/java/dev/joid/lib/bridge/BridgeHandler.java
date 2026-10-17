package dev.joid.lib.bridge;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.clock.IClockBridge;
import dev.joid.lib.bridge.clock.SystemClockBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.signal.ISignalReplayRemapper;
import dev.joid.lib.bridge.thread.DirectThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.ui.UIBridgeRegistry;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BridgeHandler {

	public static final UIBridgeRegistry                      UI            = UIBridgeRegistry.create();
	public static final BridgeRegistry<IAudioBridge>          AUDIO         = BridgeRegistry.create("AUDIO");
	public static final BridgeRegistry<IClockBridge>          CLOCK         = BridgeRegistry.create("CLOCK");
	public static final BridgeRegistry<IWindowBridge>         WINDOW        = BridgeRegistry.create("WINDOW");
	public static final BridgeRegistry<IRenderBridge>         RENDER        = BridgeRegistry.create("RENDER");
	public static final BridgeRegistry<IThreadBridge>         THREAD        = BridgeRegistry.create("THREAD");
	public static final BridgeRegistry<ISignalReplayRemapper> SIGNAL_REPLAY = BridgeRegistry.create("SIGNAL_REPLAY");

	static {
		BridgeHandler.CLOCK.register(SystemClockBridge.create());
		BridgeHandler.THREAD.register(DirectThreadBridge.create());
		BridgeHandler.SIGNAL_REPLAY.register(new ISignalReplayRemapper() {});
	}

}