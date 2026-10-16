package dev.joid.lib.ui.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.joid.lib.utils.align.Align;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIData {

	public boolean active()          default true;
	public boolean visible()         default true;
	public boolean closeable()       default true;

	public boolean zoomable()        default true;
	public boolean background()      default false;
	public boolean projection()      default true;
	public String  backgroundColor() default "#101010C0";

	public int    zindex()           default 0;
	public double zlevel()           default 0D;

	public Align anchorX()           default Align.CENTER;
	public Align anchorY()           default Align.CENTER;

}