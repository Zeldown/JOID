package dev.joid.lib.ui.core.hook.store;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIStoreData {

	public String id()          default "";

	public UIStoreScope scope() default UIStoreScope.LOCAL;

}