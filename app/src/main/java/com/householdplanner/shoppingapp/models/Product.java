package com.householdplanner.shoppingapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by JuanCarlos on 21/05/2015.
 */
public class Product implements Parcelable {
    //Local identifier for the product
    public int _id;
    //Product Id relationship
    public int productId;
    //Product name
    public String name;
    //Photo file name
    public String photoName;
    //Market Id
    public int marketId;
    //Market name
    public String marketName;
    //Quantity
    public String amount;
    //Measure unit id
    public int unitId;
    //Hold if the product is in the basket
    public boolean committed;

    public Product() {
        super();
    }

    /**
     * Return if the entity is valid
     *
     * @return
     */
    public boolean validate() {
        if (!TextUtils.isEmpty(name)) {
            return true;
        } else {
            return false;
        }
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public Product(Parcel parcel) {
        _id = parcel.readInt();
        productId = parcel.readInt();
        name = parcel.readString();
        photoName = parcel.readString();
        marketId = parcel.readInt();
        marketName = parcel.readString();
        amount = parcel.readString();
        unitId = parcel.readInt();
        committed = parcel.readInt() == 1 ? true : false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(productId);
        dest.writeString(name);
        dest.writeString(photoName);
        dest.writeInt(marketId);
        dest.writeString(marketName);
        dest.writeString(amount);
        dest.writeInt(unitId);
        dest.writeInt(committed ? 1 : 0);
    }
}
