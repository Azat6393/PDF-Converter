package woynapp.wsann.activity.auth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import woynapp.wsann.model.User;
import woynapp.wsann.util.Constants;

public class UserUtils {

    Activity mActivity;

    public UserUtils(Activity activity) {
        this.mActivity = activity;
    }

    public void updateUser(String name, String surname, String photoUrl, String uuid, String email, String phoneNumber) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_NAME, name).apply();
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, surname).apply();
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, photoUrl).apply();
        mSharedPreferences.edit().putString(Constants.USER_UUID, uuid).apply();
        mSharedPreferences.edit().putString(Constants.USER_EMAIL, email).apply();
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, phoneNumber).apply();
    }

    public void deleteUser() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_NAME, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_UUID, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_EMAIL, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, "").apply();
    }

    public User getUser() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        User user = new User();
        user.setUser_name(mSharedPreferences.getString(Constants.USER_NAME, ""));
        user.setSurname(mSharedPreferences.getString(Constants.USER_SURNAME, ""));
        user.setUser_photo(mSharedPreferences.getString(Constants.USER_PHOTO, ""));
        user.setUser_uuid(mSharedPreferences.getString(Constants.USER_UUID, ""));
        user.setUser_email(mSharedPreferences.getString(Constants.USER_EMAIL, ""));
        user.setPhone_number(mSharedPreferences.getString(Constants.USER_PHONE_NUMBER,""));
        return user;
    }

    public void updatePhoneNumber(String number){
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, number).apply();
    }

    public void updateName(String name) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_NAME, name).apply();
    }

    public void updateSurname(String surname) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, surname).apply();
    }

    public void updatePhoto(String photoUrl) {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, photoUrl).apply();
    }
}
