package dev.joid.tool.docgen.model;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum Family {

	UI("ui", "UI", "dev.joid.internal", "dev.joid.lib.JOID", "dev.joid.lib.JOIDEnv", "dev.joid.lib.ui.core", "dev.joid.lib.ui.shortcut", "dev.joid.lib.ui.task"),
	ANNOTATIONS("annotations", "Annotations", "dev.joid.lib.ui.core.data", "dev.joid.lib.utils.annotation"),
	NODES("nodes", "Nodes", "dev.joid.lib.ui.node"),
	CONTROLS("controls", "Controls", "dev.joid.internal.RangeList", "dev.joid.lib.ui.node.impl.control"),
	LAYOUT("layout", "Layout", "dev.joid.lib.ui.node.impl.layout", "dev.joid.lib.ui.node.property.OverflowProperty", "dev.joid.lib.ui.node.property.PositionProperty", "dev.joid.lib.utils.align"),
	EVENTS("events", "Events", "dev.joid.lib.ui.event"),
	EFFECTS("effects", "Effects", "dev.joid.lib.ui.node.effect"),
	SIGNALS("signals", "Signals", "dev.joid.internal.signal", "dev.joid.lib.signal", "dev.joid.lib.ui.binding", "dev.joid.lib.ui.node.property.WatchProperty"),
	ANIMATION("animation", "Animation", "dev.joid.lib.animation"),
	TEXT("text", "Text", "dev.joid.lib.font", "dev.joid.lib.draw.text", "dev.joid.internal.font"),
	RESOURCES("resources", "Resources", "dev.joid.lib.asset", "dev.joid.lib.resource", "dev.joid.lib.video", "dev.joid.lib.cache"),
	DRAWING("drawing", "Drawing", "dev.joid.lib.draw", "dev.joid.lib.color", "dev.joid.lib.obj", "dev.joid.lib.render"),
	SHADERS("shaders", "Shaders", "dev.joid.lib.shader"),
	INPUT("input", "Input and keys", "dev.joid.lib.input"),
	UTILITIES("utilities", "Utilities", "dev.joid.internal.JsonFiles", "dev.joid.lib.utils"),
	BACKENDS("backends", "Backends", "dev.joid.lib.bridge.backend", "dev.joid.lib.bridge.ui", "dev.joid.lib.bridge.window"),
	BRIDGES("bridges", "Bridges", "dev.joid.lib.bridge", "dev.joid.lib.resource.resolver"),
	PIPELINE("pipeline", "Pipeline", "dev.joid.lib.bridge.render.matrix", "dev.joid.lib.bridge.render.shader.source", "dev.joid.lib.bridge.render.shader.uniform", "dev.joid.lib.bridge.render.state", "dev.joid.lib.bridge.render.vertex"),
	TESTING("testing", "Testing", "dev.joid.test"),
	BASE_LWJGL3_GLFW("base-lwjgl3-glfw", "base-lwjgl3-glfw", "dev.joid.base.lwjgl3.glfw"),
	BASE_OPENAL("base-openal", "base-openal", "dev.joid.base.openal"),
	BASE_OPENGL("base-opengl", "base-opengl", "dev.joid.base.opengl"),
	LWJGL2("lwjgl2", "LWJGL 2 backend", "dev.joid.backend.lwjgl2"),
	LWJGL3("lwjgl3", "LWJGL 3 backend", "dev.joid.backend.lwjgl3"),
	VULKAN("vulkan", "Vulkan backend", "dev.joid.backend.vulkan"),
	TOOLS("tools", "Tools", "dev.joid.tool"),
	OTHER("other", "Other");

	private final String       id;
	private final String       label;
	private final List<String> prefixes;

	private Family(final String id, final String label, final String... prefixes) {
		this.id       = id;
		this.label    = label;
		this.prefixes = Arrays.asList(prefixes);
	}

	public static Family of(final String name) {
		Family family = Family.OTHER;
		int length = 0;
		for (final Family candidate : Family.values()) {
			for (final String prefix : candidate.prefixes) {
				if (prefix.length() > length && (name.equals(prefix) || name.startsWith(prefix + "."))) {
					family = candidate;
					length = prefix.length();
				}
			}
		}
		return family;
	}

}