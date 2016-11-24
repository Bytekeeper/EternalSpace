package org.bk;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;

/**
 * Created by dante on 22.11.2016.
 */
public class BiasedScaledNumericValueDelegate extends ParticleEmitter.ScaledNumericValue {
    private final ParticleEmitter.ScaledNumericValue delegate;
    public float bias;

    public BiasedScaledNumericValueDelegate(ParticleEmitter.ScaledNumericValue delegate) {
        this.delegate = delegate;
        load(delegate);
    }

    public void setBias(float bias) {
        this.bias = bias;
        delegate.setHigh(getHighMin() + bias, getHighMax() + bias);
        delegate.setLow(getLowMin() + bias, getLowMax() + bias);
    }

    @Override
    public void setHigh(float min, float max) {
        delegate.setHigh(min + bias, max + bias);
        super.setHigh(min, max);
    }

    @Override
    public void setHigh(float value) {
        delegate.setHigh(value + bias);
        super.setHigh(value);
    }

    @Override
    public void setHighMin(float highMin) {
        delegate.setHighMin(highMin + bias);
        super.setHighMin(highMin);
    }

    @Override
    public void setHighMax(float highMax) {
        delegate.setHighMax(highMax + bias);
        super.setHighMax(highMax);
    }

    @Override
    public void setLow(float min, float max) {
        delegate.setLow(min + bias, max + bias);
        super.setLow(min, max);
    }

    @Override
    public void setLow(float value) {
        delegate.setLow(value + bias);
        super.setLow(value);
    }

    @Override
    public void setLowMax(float lowMax) {
        delegate.setLowMax(lowMax + bias);
        super.setLowMax(lowMax);
    }

    @Override
    public void setLowMin(float lowMin) {
        delegate.setLowMin(lowMin + bias);
        super.setLowMin(lowMin);
    }

    public static void setBias(Iterable<ParticleEmitter> emitters, float value) {
        for (ParticleEmitter emitter: emitters) {
            ((BiasedScaledNumericValueDelegate) emitter.getAngle()).setBias(value);
        }
    }
}
