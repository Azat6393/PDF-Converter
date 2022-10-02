package woynapp.wsann.activity.auth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import woynapp.wsann.model.User;
import woynapp.wsann.util.Constants;

public class UserUtils {

    Activity mActivity;
    SharedPreferences mSharedPreferences;

    public UserUtils(Activity activity) {
        this.mActivity = activity;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    public void updateUser(String name, String surname, String photoUrl, String uuid, String email, String phoneNumber, boolean contactUploaded) {
        mSharedPreferences.edit().putString(Constants.USER_NAME, name).apply();
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, surname).apply();
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, photoUrl).apply();
        mSharedPreferences.edit().putString(Constants.USER_UUID, uuid).apply();
        mSharedPreferences.edit().putString(Constants.USER_EMAIL, email).apply();
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, phoneNumber).apply();
        mSharedPreferences.edit().putBoolean(Constants.USER_CONTACT_UPLOADED, contactUploaded).apply();
    }

    public void deleteUser() {
        mSharedPreferences.edit().putString(Constants.USER_NAME, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_UUID, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_EMAIL, "").apply();
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, "").apply();
        mSharedPreferences.edit().putBoolean(Constants.USER_CONTACT_UPLOADED, false).apply();
    }

    public User getUser() {
        User user = new User();
        user.setUser_name(mSharedPreferences.getString(Constants.USER_NAME, ""));
        user.setSurname(mSharedPreferences.getString(Constants.USER_SURNAME, ""));
        user.setUser_photo(mSharedPreferences.getString(Constants.USER_PHOTO, ""));
        user.setUser_uuid(mSharedPreferences.getString(Constants.USER_UUID, ""));
        user.setUser_email(mSharedPreferences.getString(Constants.USER_EMAIL, ""));
        user.setPhone_number(mSharedPreferences.getString(Constants.USER_PHONE_NUMBER, ""));
        user.setContacts_uploaded(mSharedPreferences.getBoolean(Constants.USER_CONTACT_UPLOADED, false));
        return user;
    }

    public void updatePhoneNumber(String number) {
        mSharedPreferences.edit().putString(Constants.USER_PHONE_NUMBER, number).apply();
    }

    public void updateName(String name) {
        mSharedPreferences.edit().putString(Constants.USER_NAME, name).apply();
    }

    public void updateSurname(String surname) {
        mSharedPreferences.edit().putString(Constants.USER_SURNAME, surname).apply();
    }

    public void updatePhoto(String photoUrl) {
        mSharedPreferences.edit().putString(Constants.USER_PHOTO, photoUrl).apply();
    }

    public void updateContactUploaded(boolean uploaded) {
        mSharedPreferences.edit().putBoolean(Constants.USER_CONTACT_UPLOADED, uploaded).apply();
    }
}
