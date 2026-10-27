package com.example.backend;

import java.util.Map;

import dev.joid.lib.bridge.signal.ISignalReplayBridge;

// #region replay
public final class EngineSignalReplayBridge implements ISignalReplayBridge {

	private final Map<String, String> classes;
	private final Map<String, String> members;

	public EngineSignalReplayBridge(final Map<String, String> classes, final Map<String, String> members) {
		this.classes = classes;
		this.members = members;
	}

	@Override
	public String mapClass(final String name) {
		return this.classes.getOrDefault(name, name);
	}

	@Override
	public String mapField(final String owner, final String name, final String descriptor) {
		return this.members.getOrDefault(owner + "." + name, name);
	}

	@Override
	public String mapMethod(final String owner, final String name, final String descriptor) {
		return this.members.getOrDefault(owner + "." + name + descriptor, name);
	}

}
// #endregion