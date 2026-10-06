package dev.joid.lib.ui.core.data.popup;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;

public class UIDataPopupTest {

	@Test
	public void staysStillWithoutTransition() {
		Assert.assertFalse(PopupTransition.NONE.isIn());
		Assert.assertFalse(PopupTransition.NONE.isOut());
		Assert.assertFalse(PopupTransition.NONE.isActive());
	}

	@Test
	public void onlyEntersWithIn() {
		Assert.assertTrue(PopupTransition.IN.isIn());
		Assert.assertFalse(PopupTransition.IN.isOut());
		Assert.assertTrue(PopupTransition.IN.isActive());
	}

	@Test
	public void onlyLeavesWithOut() {
		Assert.assertFalse(PopupTransition.OUT.isIn());
		Assert.assertTrue(PopupTransition.OUT.isOut());
		Assert.assertTrue(PopupTransition.OUT.isActive());
	}

	@Test
	public void entersAndLeavesWithInOut() {
		Assert.assertTrue(PopupTransition.IN_OUT.isIn());
		Assert.assertTrue(PopupTransition.IN_OUT.isOut());
		Assert.assertTrue(PopupTransition.IN_OUT.isActive());
	}

}