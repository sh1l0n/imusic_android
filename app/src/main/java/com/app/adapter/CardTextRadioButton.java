package com.app.adapter;

import android.os.Parcel;
import android.os.Parcelable;


public class CardTextRadioButton implements Parcelable {

    public String _Name;
    public boolean _isChecked;

    public CardTextRadioButton() {
        _Name = "";
        _isChecked= false;
    }

    public CardTextRadioButton(String n) {
        _Name = n;
    }

    public CardTextRadioButton(String n, boolean c) {
        _Name= n;
        _isChecked= c;
    }

    public CardTextRadioButton(Parcel p)
    {
        _Name = p.readString();
        _isChecked = p.readInt()==1;
    }

    public String getName() {
        return _Name;
    }

    public void setName(String _Name) {
        this._Name = _Name;
    }

    public void setisChecked(boolean _isChecked) {
        this._isChecked = _isChecked;
    }

    public boolean isChecked() {
        return _isChecked;
    }

    public static final Creator<CardTextRadioButton> CREATOR = new Creator<CardTextRadioButton>() {
        @Override
        public CardTextRadioButton createFromParcel(Parcel parcel) {
            return new CardTextRadioButton(parcel);
        }

        @Override
        public CardTextRadioButton[] newArray(int size) {
            return new CardTextRadioButton[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_Name);
        parcel.writeInt(_isChecked?1:0);
    }
}
