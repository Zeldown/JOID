package dev.joid.lib.animation.tween;

import dev.joid.lib.animation.tween.path.CatmullRomTweenPath;
import dev.joid.lib.animation.tween.path.LinearTweenPath;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweenPaths {

	public static final LinearTweenPath     LINEAR      = new LinearTweenPath();
	public static final CatmullRomTweenPath CATMULL_ROM = new CatmullRomTweenPath();

}