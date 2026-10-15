package dev.joid.demo.ui.option.popup;

import dev.joid.demo.ui.core.transition.DemoSlideTransition;

public class UIDemoSlidePopup extends UIDemoOptionPopup {

	public UIDemoSlidePopup() {
		super.transition(DemoSlideTransition.create());
	}

}