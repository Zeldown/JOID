package dev.joid.lib.bridge;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.clock.IClockBridge;
import dev.joid.lib.bridge.clock.SystemClockBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.signal.ISignalReplayBridge;
import dev.joid.lib.bridge.thread.DirectThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.ui.UIBridgeRegistry;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BridgeHandler {

	public static final UIBridgeRegistry                    UI            = new UIBridgeRegistry();
	public static final BridgeRegistry<IAudioBridge>        AUDIO         = new BridgeRegistry<>("AUDIO");
	public static final BridgeRegistry<IClockBridge>        CLOCK         = new BridgeRegistry<>("CLOCK");
	public static final BridgeRegistry<IWindowBridge>       WINDOW        = new BridgeRegistry<>("WINDOW");
	public static final BridgeRegistry<IRenderBridge>       RENDER        = new BridgeRegistry<>("RENDER");
	public static final BridgeRegistry<IThreadBridge>       THREAD        = new BridgeRegistry<>("THREAD");
	public static final BridgeRegistry<ISignalReplayBridge> SIGNAL_REPLAY = new BridgeRegistry<>("SIGNAL_REPLAY");

	static {
		BridgeHandler.CLOCK.register(new SystemClockBridge());
		BridgeHandler.THREAD.register(new DirectThreadBridge());
		BridgeHandler.SIGNAL_REPLAY.register(new ISignalReplayBridge() {});
	}

}