package dev.joid.lib.bridge;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.clock.IClockBridge;
import dev.joid.lib.bridge.clock.SystemClockBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.signal.ISignalReplayRemapper;
import dev.joid.lib.bridge.signal.IdentitySignalReplayRemapper;
import dev.joid.lib.bridge.ui.UIBridgeRegistry;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BridgeHandler {

	public static final UIBridgeRegistry                      UI;
	public static final BridgeRegistry<IAudioBridge>          AUDIO;
	public static final BridgeRegistry<IClockBridge>          CLOCK;
	public static final BridgeRegistry<IWindowBridge>         WINDOW;
	public static final BridgeRegistry<IRenderBridge>         RENDER;
	public static final BridgeRegistry<ISignalReplayRemapper> SIGNAL_REPLAY;

	static {
		UI            = UIBridgeRegistry.create();
		WINDOW        = BridgeRegistry.create("WINDOW");
		RENDER        = BridgeRegistry.create("RENDER");
		AUDIO         = BridgeRegistry.create("AUDIO");
		CLOCK         = BridgeRegistry.create("CLOCK");
		SIGNAL_REPLAY = BridgeRegistry.create("SIGNAL_REPLAY");

		CLOCK.register(new SystemClockBridge());
		SIGNAL_REPLAY.register(new IdentitySignalReplayRemapper());
	}

}