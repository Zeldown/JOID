package be.zeldown.joid.lib.animation.tweenengine;



import be.zeldown.joid.lib.animation.tweenengine.equation.Back;
import be.zeldown.joid.lib.animation.tweenengine.equation.Bounce;
import be.zeldown.joid.lib.animation.tweenengine.equation.Circ;
import be.zeldown.joid.lib.animation.tweenengine.equation.Cubic;
import be.zeldown.joid.lib.animation.tweenengine.equation.Elastic;
import be.zeldown.joid.lib.animation.tweenengine.equation.Expo;
import be.zeldown.joid.lib.animation.tweenengine.equation.Linear;
import be.zeldown.joid.lib.animation.tweenengine.equation.Quad;
import be.zeldown.joid.lib.animation.tweenengine.equation.Quart;
import be.zeldown.joid.lib.animation.tweenengine.equation.Quint;
import be.zeldown.joid.lib.animation.tweenengine.equation.Sine;

public interface TweenEquations {

	public static final Linear  LINEAR        = Linear.INOUT;
	public static final Quad    QUAD_IN       = Quad.IN;
	public static final Quad    QUAD_OUT      = Quad.OUT;
	public static final Quad    QUAD_INOUT    = Quad.INOUT;
	public static final Cubic   CUBIC_IN      = Cubic.IN;
	public static final Cubic   CUBIC_OUT     = Cubic.OUT;
	public static final Cubic   CUBIC_INOUT   = Cubic.INOUT;
	public static final Quart   QUART_IN      = Quart.IN;
	public static final Quart   QUART_OUT     = Quart.OUT;
	public static final Quart   QUART_INOUT   = Quart.INOUT;
	public static final Quint   QUINT_IN      = Quint.IN;
	public static final Quint   QUINT_OUT     = Quint.OUT;
	public static final Quint   QUINT_INOUT   = Quint.INOUT;
	public static final Circ    CIRC_IN       = Circ.IN;
	public static final Circ    CIRC_OUT      = Circ.OUT;
	public static final Circ    CIRC_INOUT    = Circ.INOUT;
	public static final Sine    SINE_IN       = Sine.IN;
	public static final Sine    SINE_OUT      = Sine.OUT;
	public static final Sine    SINE_INOUT    = Sine.INOUT;
	public static final Expo    EXPO_IN       = Expo.IN;
	public static final Expo    EXPO_OUT      = Expo.OUT;
	public static final Expo    EXPO_INOUT    = Expo.INOUT;
	public static final Back    BACK_IN       = Back.IN;
	public static final Back    BACK_OUT      = Back.OUT;
	public static final Back    BACK_INOUT    = Back.INOUT;
	public static final Bounce  BOUNCE_IN     = Bounce.IN;
	public static final Bounce  BOUNCE_OUT    = Bounce.OUT;
	public static final Bounce  BOUNCE_INOUT  = Bounce.INOUT;
	public static final Elastic ELASTIC_IN    = Elastic.IN;
	public static final Elastic ELASTIC_OUT   = Elastic.OUT;
	public static final Elastic ELASTIC_INOUT = Elastic.INOUT;

}