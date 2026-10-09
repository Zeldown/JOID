package dev.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.joid.lib.utils.align.Align;

@Target(ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface UIData {

	public boolean active()          default true;
	public boolean visible()         default true;
	public boolean closeable()       default true;

	public boolean zoomable()        default true;
	public boolean background()      default true;
	public boolean projection()      default true;
	public String  backgroundColor() default "#101010c0";

	public int    zindex()           default 0;
	public double zlevel()           default 0D;

	public Align anchorX()           default Align.CENTER;
	public Align anchorY()           default Align.CENTER;

}