package com.kahl.silir.entity;

/**
 * Created by Paskahlis Anjas on 24/07/2017.
 */

public class User extends MeasurementProfile {
    public static final String EMPTY_PROFILE_PICTURE = "empty";
    public static final String NAME = "name";
    public static final String DOB = "dob";
    public static final String GENDER = "gender";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String PROFILE_PICTURE_URL = "profilePictureUri";
    public static final String HEIGHT = "height";
    public static final String WEIGHT = "weight";
    public static final String PROFILE_SAVED = "profileSaved";

    public static final String KEY_IN_LOCAL_DB = "main_user";

    private String phoneNumber;
    private String profilePictureUri;

    public User() {
    }

    public User(String name, String dob, String gender, String phoneNumber,
                int height, int weight, String profilePictureUri) {
        super(name, dob, gender, height, weight);
        this.phoneNumber = phoneNumber;
        this.profilePictureUri = profilePictureUri;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }
}
