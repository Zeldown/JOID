package dev.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIDataPopup {

	public boolean         active()     default false;
	public PopupTransition transition() default PopupTransition.IN_OUT;

	public enum PopupTransition {

		NONE,
		IN,
		OUT,
		IN_OUT;

		public boolean isIn() {
			return this == PopupTransition.IN || this == PopupTransition.IN_OUT;
		}

		public boolean isOut() {
			return this == PopupTransition.OUT || this == PopupTransition.IN_OUT;
		}

		public boolean isActive() {
			return this != PopupTransition.NONE;
		}

	}

}