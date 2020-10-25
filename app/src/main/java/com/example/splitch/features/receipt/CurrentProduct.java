package com.example.splitch.features.receipt;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import java.util.UUID;

public class CurrentProduct implements Parcelable {
    @NonNull
    @PrimaryKey
    public String productId;
    public String name;
    public String price;

    public CurrentProduct(@NonNull String productId, String name, String price){
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public CurrentProduct(String name, String price) {
        productId = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
    }

    protected CurrentProduct(Parcel in) {
        productId = in.readString();
        name = in.readString();
        price = in.readString();
    }

    public static final Creator<CurrentProduct> CREATOR = new Creator<CurrentProduct>() {
        @Override
        public CurrentProduct createFromParcel(Parcel in) {
            return new CurrentProduct(in);
        }

        @Override
        public CurrentProduct[] newArray(int size) {
            return new CurrentProduct[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(name);
        dest.writeString(price);
    }
}
