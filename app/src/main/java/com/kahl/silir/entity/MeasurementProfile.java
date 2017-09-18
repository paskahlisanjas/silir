package com.kahl.silir.entity;

import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Paskahlis Anjas Prabowo on 30/07/2017.
 */

public class MeasurementProfile implements Serializable {
    protected String name;
    protected String dob;
    protected String gender;
    protected int height;
    protected int weight;

    public MeasurementProfile() {}

    public MeasurementProfile(String name, String dob, String gender, int height, int weight) {
        this.name = name;
        this.dob = dob;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }

    public MeasurementProfile(User user) {
        name = user.getName();
        dob = user.getDob();
        gender = user.getGender();
        height = user.getHeight();
        weight = user.getWeight();
    }

    public int getAge() {
        String[] splittedDob = dob.split("/");
        Calendar birthday = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        birthday.set(Integer.parseInt(splittedDob[2]), Integer.parseInt(splittedDob[1]),
                Integer.parseInt(splittedDob[0]));
        int age = today.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        return birthday.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) < 0 ? age : age--;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public int getHeight() {
        return height;
    }

    public int getWeight() {
        return weight;
    }
}
