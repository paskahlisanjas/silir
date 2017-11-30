package com.kahl.silir.entity;

import com.kahl.silir.databasehandler.ProfileDbHandler;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Paskahlis Anjas Prabowo on 02/08/2017.
 */

public class MeasurementResult implements Serializable {
    public static final String EMPTY_URL = "empty";

    private float fvc;
    private float fev1;
    private float pef;
    private String time;
    private String profileId;
    private String arrayFlow;
    private String arrayVolume;

    private String vtUrl = EMPTY_URL;
    private String fvUrl = EMPTY_URL;

    public MeasurementResult(float fvc, float fev1, float pef, String time, String profileId,
                             String arrayFlow, String arrayVolume) {
        this.fvc = fvc;
        this.fev1 = fev1;
        this.pef = pef;
        this.time = time;
        this.profileId = profileId;
        this.arrayFlow = arrayFlow;
        this.arrayVolume = arrayVolume;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setFvc(float fvc) {
        this.fvc = fvc;
    }

    public void setFev1(float fev1) {
        this.fev1 = fev1;
    }

    public void setPef(float pef) {
        this.pef = pef;
    }

    public void setArrayFlow(String arrayFlow) { this.arrayFlow = arrayFlow; }

    public void setArrayVolume(String arrayVolume) {this.arrayVolume = arrayVolume; }

    public void setVtUrl(String vtUrl) {
        this.vtUrl = vtUrl;
    }

    public void setFvUrl(String fvUrl) {
        this.fvUrl = fvUrl;
    }

    public float getFvc() {
        return fvc;
    }

    public float getFev1() {
        return fev1;
    }

    public float getPef() {
        return pef;
    }

    public String getArrayFlow() { return arrayFlow; }

    public String getArrayVolume() { return arrayVolume; }


    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileId() {
        return profileId;
    }
}
