package com.kahl.silir.entity;

/**
 * Created by Paskahlis Anjas Prabowo on 01/08/2017.
 */
public class RiemmanIntegrator {

  public static float integrate(float arg1, float arg2) {
    float delta = arg2 - arg1;
    float result = 0;
    float startPoint = arg1;
    float endPoint = startPoint + delta;
    while (startPoint < arg2) {
      result += (startPoint + endPoint) * delta / 2f;
      startPoint = endPoint;
      endPoint += delta;
    }
    return result;
  }
}
