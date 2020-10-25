package com.example.splitch.features.adding_persons;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactsArray implements Parcelable {
    private ContactInfo[] contacts;

    public ContactsArray(ContactInfo[] contacts) {
        this.contacts = contacts;
    }

    protected ContactsArray(Parcel in) {
        contacts = in.createTypedArray(ContactInfo.CREATOR);
    }

    public ContactInfo[] getContacts() {
        return contacts;
    }

    public void setContacts(ContactInfo[] contacts) {
        this.contacts = contacts;
    }

    public static final Creator<ContactsArray> CREATOR = new Creator<ContactsArray>() {
        @Override
        public ContactsArray createFromParcel(Parcel in) {
            return new ContactsArray(in);
        }

        @Override
        public ContactsArray[] newArray(int size) {
            return new ContactsArray[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(contacts, flags);
    }
}
