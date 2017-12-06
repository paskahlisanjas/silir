package com.kahl.silir.entity;

import com.github.mikephil.charting.data.Entry;
import com.kahl.silir.databasehandler.ProfileDbHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Paskahlis Anjas Prabowo on 02/08/2017.
 */

public class MeasurementResult implements Serializable {
    private float fvc;
    private float fev1;
    private float pef;
    private String time;
    private String profileId;
    private List<Entry> volumeTimeCurve;
    private List<Entry> flowVolumeCurve;

    public MeasurementResult() {
        // for firebase realtime database purpose
    }

    public MeasurementResult(float fvc, float fev1, float pef, String time,
                             String profileId, List<Entry> vt, List<Entry> fv) {
        this.fvc = fvc;
        this.fev1 = fev1;
        this.pef = pef;
        this.time = time;
        this.profileId = profileId;
        volumeTimeCurve = vt;
        flowVolumeCurve = fv;
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

    public float getFvc() {
        return fvc;
    }

    public float getFev1() {
        return fev1;
    }

    public float getPef() {
        return pef;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileId() {
        return profileId;
    }

    public List<Entry> getVolumeTimeCurve() {
        return volumeTimeCurve;
    }

    public List<Entry> getFlowVolumeCurve() {
        return flowVolumeCurve;
    }
}
