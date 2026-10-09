package dev.joid.lib.ui.core.hook.store.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.joid.lib.ui.core.hook.store.scope.StoreScope;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIStoreData {

	public String id()          default "";

	public StoreScope scope() default StoreScope.LOCAL;

}