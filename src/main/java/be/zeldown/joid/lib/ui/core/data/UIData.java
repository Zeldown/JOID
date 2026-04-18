package be.zeldown.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import be.zeldown.joid.lib.utils.align.Align;

@Target(ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface UIData {

	boolean active()          default true;
	boolean visible()         default true;
	boolean pause()           default true;
	boolean closeable()       default true;

	boolean zoomable()        default true;
	boolean projection()      default true;
	boolean background()      default true;
	String  backgroundColor() default "#101010c0";

	double zlevel()           default 0D;

	Align anchorX()           default Align.CENTER;
	Align anchorY()           default Align.CENTER;

}