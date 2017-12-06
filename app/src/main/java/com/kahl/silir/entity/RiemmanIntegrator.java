package com.kahl.silir.entity;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paskahlis Anjas Prabowo on 01/08/2017.
 */

public class RiemmanIntegrator {

    public static List<Float> integrate(List<Float> data, float delta) {
        List<Float> result = new ArrayList<>();

        float prevVal = 0;
        float prevArea = 0;
        for (float unit : data) {
            float temp = calculateArea(prevVal, unit, delta);
            result.add(temp + prevArea);
            prevArea += temp;
            prevVal = unit;
        }

        return result;
    }

    private static float calculateArea(float prev, float curr, float delta) {
        return 0.5f * (prev + curr) * delta;
    }
}
