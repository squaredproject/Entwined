package com.charlesgadeken.entwined;

import heronarts.lx.utils.LXUtils;
import java.lang.reflect.Modifier;
import java.util.Random;

public class Utilities {
    public static float PI = (float) Math.PI;
    public static final float HALF_PI = (float) (Math.PI / 2.0);
    public static final float THIRD_PI = (float) (Math.PI / 3.0);
    public static final float QUARTER_PI = (float) (Math.PI / 4.0);
    public static final float TWO_PI = (float) (2.0 * Math.PI);

    private static final long millisOffset = System.currentTimeMillis();

    public static int millis() {
        return (int) (System.currentTimeMillis() - millisOffset);
    }

    public static float abs(float n) {
        return (n < 0) ? -n : n;
    }

    public static int abs(int n) {
        return (n < 0) ? -n : n;
    }

    public static float sqrt(float n) {
        return (float) Math.sqrt(n);
    }

    public static float pow(float n, float e) {
        return (float) Math.pow(n, e);
    }

    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public static float max(float a, float b) {
        return (a > b) ? a : b;
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static float min(float a, float b) {
        return (a < b) ? a : b;
    }

    public static int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    public static float constrain(float amt, float low, float high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    public static float sin(float angle) {
        return (float) Math.sin(angle);
    }

    public static float cos(float angle) {
        return (float) Math.cos(angle);
    }

    public static float asin(float value) {
        return (float) Math.asin(value);
    }

    public static float acos(float value) {
        return (float) Math.acos(value);
    }

    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    public static int ceil(float n) {
        return (int) Math.ceil(n);
    }

    public static int floor(float n) {
        return (int) Math.floor(n);
    }

    public static float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    private static Random internalRandom;

    public static float random(float high) {
        // avoid an infinite loop when 0 or NaN are passed in
        if (high == 0 || high != high) {
            return 0;
        }

        if (internalRandom == null) {
            internalRandom = new Random();
        }

        // for some reason (rounding error?) Math.random() * 3
        // can sometimes return '3' (once in ~30 million tries)
        // so a check was added to avoid the inclusion of 'howbig'
        float value = 0;
        do {
            value = internalRandom.nextFloat() * high;
        } while (value == high);
        return value;
    }

    public static float random(float low, float high) {
        if (low >= high) return low;
        float diff = high - low;
        return random(diff) + low;
    }

    public static float map(float value, float currentMin, float currentMax) {
        return (value - currentMin) / (currentMax - currentMin);
    }

    public static float wrappedDistance(float v1, float v2, float modulus) {
        v1 = (v1 % modulus);
        v1 = v1 < 0 ? v1 + modulus : v1;

        v2 = (v2 % modulus);
        v2 = v2 < 0 ? v2 + modulus : v2;
        return LXUtils.wrapdistf(v1, v2, modulus);
    }

    public static float degreeDifference(float v1, float v2) {
        return wrappedDistance(v1, v2, 360);
    }

    public static boolean isConcrete(Class<?> cls) {
        return !Modifier.isAbstract(cls.getModifiers());
    }
}
