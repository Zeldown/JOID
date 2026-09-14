package be.zeldown.joid.lib.ui.core.hook.store.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import be.zeldown.joid.lib.ui.core.hook.store.context.StoreContext;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIStoreData {

	String id()            default "";
	StoreContext context() default StoreContext.LOCAL;

}