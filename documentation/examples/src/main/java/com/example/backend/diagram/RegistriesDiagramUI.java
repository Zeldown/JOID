package com.example.backend.diagram;

import dev.joid.lib.ui.node.impl.shape.RectNode;

public class RegistriesDiagramUI extends BuildDiagramUI {

	@Override
	public void init() {
		final RectNode backdrop = super.backdrop(400, 200, 1120, 520).attach(this);

		super.label(20, 20, 1080, 40, "Registered by Backend.register()").attach(backdrop);
		super.box(20, 70, 200, 120, "UI", "WindowUIBridge, by default").attach(backdrop);
		super.subject(240, 70, 200, 120, "WINDOW", "your window bridge").attach(backdrop);
		super.subject(460, 70, 200, 120, "RENDER", "your render bridge").attach(backdrop);
		super.box(680, 70, 200, 120, "AUDIO", "optional").attach(backdrop);
		super.box(900, 70, 200, 120, "THREAD", "FrameThreadBridge").attach(backdrop);

		super.label(20, 230, 1080, 40, "Registered by JOID before any backend").attach(backdrop);
		super.box(20, 280, 200, 120, "CLOCK", "SystemClockBridge").attach(backdrop);
		super.box(240, 280, 200, 120, "THREAD", "DirectThreadBridge").attach(backdrop);
		super.box(460, 280, 200, 120, "SIGNAL_REPLAY", "identity").attach(backdrop);

		super.label(20, 430, 1080, 70, "get() returns the bridge of the highest priority: only UI uses all of its bridges").attach(backdrop);
	}

}