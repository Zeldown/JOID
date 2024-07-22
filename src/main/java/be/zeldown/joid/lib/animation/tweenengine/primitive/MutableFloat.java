package be.zeldown.joid.lib.animation.tweenengine.primitive;



import be.zeldown.joid.lib.animation.tweenengine.TweenAccessor;

@SuppressWarnings("serial")
public class MutableFloat extends Number implements TweenAccessor<MutableFloat> {

	private float value;

	public MutableFloat(final float value) {
		this.value = value;
	}

	public void setValue(final float value) {
		this.value = value;
	}

	@Override
	public int intValue() {
		return (int) this.value;
	}

	@Override
	public long longValue() {
		return (long) this.value;
	}

	@Override
	public float floatValue() {
		return this.value;
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public int getValues(final MutableFloat target, final int tweenType, final float[] returnValues) {
		returnValues[0] = target.value;
		return 1;
	}

	@Override
	public void setValues(final MutableFloat target, final int tweenType, final float[] newValues) {
		target.value = newValues[0];
	}

}