package be.zeldown.joid.lib.ui.core.data.popup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataPopup {

	boolean         active()     default false;
	PopupTransition transition() default PopupTransition.IN_OUT;

	public enum PopupTransition {

		NONE,
		IN,
		OUT,
		IN_OUT;

		public boolean isActive() {
			return this != NONE;
		}

		public boolean isIn() {
			return this == IN || this == IN_OUT;
		}

		public boolean isOut() {
			return this == OUT || this == IN_OUT;
		}

	}

}