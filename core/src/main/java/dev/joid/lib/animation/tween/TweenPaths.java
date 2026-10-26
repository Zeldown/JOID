package dev.joid.lib.animation.tween;

import dev.joid.lib.animation.tween.path.CatmullRomTweenPath;
import dev.joid.lib.animation.tween.path.LinearTweenPath;

public interface TweenPaths {

	public static final LinearTweenPath     LINEAR      = new LinearTweenPath();
	public static final CatmullRomTweenPath CATMULL_ROM = new CatmullRomTweenPath();

}