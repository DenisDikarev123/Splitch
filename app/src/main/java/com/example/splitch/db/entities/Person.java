package com.example.splitch.db.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "persons")
public class Person implements Parcelable {

    @NonNull
    @PrimaryKey
    public String personId;

    public String name;

    public double total;

    public int colorId;

    @Ignore
    public Person(String name, double total, int colorId) {
        personId = UUID.randomUUID().toString();
        this.name = name;
        this.total = total;
        this.colorId = colorId;
    }

    public Person(@NonNull String personId, String name, double total, int colorId) {
        this.personId = personId;
        this.name = name;
        this.total = total;
        this.colorId = colorId;
    }

    protected Person(Parcel in) {
        personId = in.readString();
        name = in.readString();
        total = in.readDouble();
        colorId = in.readInt();
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (Double.compare(person.total, total) != 0) return false;
        if (colorId != person.colorId) return false;
        if (!personId.equals(person.personId)) return false;
        return name != null ? name.equals(person.name) : person.name == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = personId.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(total);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + colorId;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(personId);
        dest.writeString(name);
        dest.writeDouble(total);
        dest.writeInt(colorId);
    }
}
