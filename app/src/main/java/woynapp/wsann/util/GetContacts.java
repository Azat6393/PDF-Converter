package woynapp.wsann.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import woynapp.wsann.model.Contact;
import woynapp.wsann.model.Tag;

public class GetContacts {

    private Context context;

    public GetContacts(Context context) {
        this.context = context;
    }

    public ArrayList<Contact> getContacts() {
        Cursor cursorContacts = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
        );
        Set<Contact> contacts = new HashSet<Contact>();
        if (cursorContacts != null) {
            cursorContacts.moveToFirst();
            while (cursorContacts.moveToNext()) {
                @SuppressLint("Range") String id = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts._ID)
                );
                @SuppressLint("Range") String name = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                );
                @SuppressLint("Range") String phoneNumberString = cursorContacts.getString(
                        cursorContacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                );
                int phoneNumber = Integer.parseInt(phoneNumberString);
                if (phoneNumber > 0) {
                    Cursor cursorPhone = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id},
                            null
                    );
                    if (cursorPhone.getCount() > 0) {
                        while (cursorPhone.moveToNext()) {
                            @SuppressLint("Range") String phoneNo = cursorPhone.getString(cursorPhone.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                            ));
                            Contact contact = new Contact();
                            contact.setId(id);
                            contact.setName(name);
                            contact.setNumber(phoneNo);
                            contacts.add(contact);
                        }
                        cursorPhone.close();
                    }
                }
            }
        } else {
            cursorContacts.close();
        }
        return new ArrayList<>(contacts);
    }

    public ArrayList<Tag> convertToTag(ArrayList<Contact> contacts) {
        ArrayList<Tag> tagList = new ArrayList<>();
        for (Contact contact : contacts) {
            Tag tag = new Tag(contact.getName(), contact.getNumber());
            tagList.add(tag);
        }
        return tagList;
    }

    public void addCountryCode(ArrayList<Contact> contacts) {
        for (Contact contact : contacts) {
            System.out.println(getE164FormattedMobileNumber(contact.getNumber(), "TR")
                    + "    old number: " + contact.getNumber());
        }
    }

    public static String getE164FormattedMobileNumber(String mobile, String locale) {
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneProto = phoneUtil.parse(mobile, locale);
            if (phoneUtil.isValidNumber(phoneProto)
                    && phoneUtil.isPossibleNumberForType(phoneProto, PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                return phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
            System.out.println("Mobile number is invalid with the provided locale:  " + mobile);

        } catch (NumberParseException e) {
            System.out.println("Error in parsing mobile number");
        }
        return null;
    }

    public boolean isValid(String phoneNumber) {

        // Use the library’s functions
        PhoneNumberUtil phoneUtil = PhoneNumberUtil
                .getInstance();
        Phonenumber.PhoneNumber phNumberProto = null;

        try {

            // I set the default region to PH (Philippines)
            // You can find your country code here http://www.iso.org/iso/country_names_and_code_elements
            phNumberProto = phoneUtil.parse(phoneNumber, "TR");

        } catch (NumberParseException e) {
            // if there’s any error
            System.out
                    .println("NumberParseException was thrown: "
                            + e.toString());
        }

        // check if the number is valid
        boolean isValid = phoneUtil
                .isValidNumber(phNumberProto);

        if (isValid) {

            // get the valid number’s international format
            String internationalFormat = phoneUtil.format(
                    phNumberProto,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            System.out.println("Phone number VALID: " + internationalFormat + "     old number: " + phoneNumber);

            return true;
        } else {
            System.out.println("Phone number is INVALID: " + phoneNumber + "     old number: " + phoneNumber);

            return false;
        }
    }

    public String deleteCountryCode(String number) {
        PhoneNumberUtil phoneInstance = PhoneNumberUtil.getInstance();
        try {
            if (number.startsWith("+")) {
                Phonenumber.PhoneNumber phoneNumber = phoneInstance.parse(number, null);
                return String.valueOf(phoneNumber.getNationalNumber());
            } else {
                Phonenumber.PhoneNumber phoneNumber = phoneInstance.parse(number, "CN");
                return String.valueOf(phoneNumber.getNationalNumber());
            }
        } catch (Exception e) {

        }
        return number;
    }
}
