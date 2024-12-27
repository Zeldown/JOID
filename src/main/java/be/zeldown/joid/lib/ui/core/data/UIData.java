package be.zeldown.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import be.zeldown.joid.lib.ui.core.data.popup.UIDataPopup;
import be.zeldown.joid.lib.utils.align.Align;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIData {

	UIDataPopup popup()       default @UIDataPopup();

	boolean active()          default true;
	boolean visible()         default true;

	boolean background()      default false;
	String  backgroundColor() default "#101010c0";

	boolean hotreload()       default true;
	boolean profiler()        default true;

	int zindex()              default 0;
	double zlevel()           default 0D;

	Align anchorX()           default Align.CENTER;
	Align anchorY()           default Align.CENTER;

}