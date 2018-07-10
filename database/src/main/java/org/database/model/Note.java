package org.database.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable
{
    private int _Category;

    private int _Top;

    private int _Id;

    private int _IdSong;

    private int _Left;

    private int _Right;

    private int _Bottom;

    private boolean _Selected;

    public final static int MAX_WIDTH = 160;

    public final static int MAX_HEIGHT = 250;


    public Note(int id_song, int id_note, int id_category, int left, int top, int right, int bottom, boolean sel)
    {
        _IdSong = id_song;
        _Id = id_note;
        _Category = id_category;
        _Top = top;
        _Left=left;
        _Right = right;
        _Bottom = bottom;
        _Selected = sel;
    }

    public Note(int id_song, int id_category, int left, int top, int right, int bottom)
    {
        _IdSong = id_song;
        _Id = -1;
        _Category = id_category;
        _Top = top;
        _Left=left;
        _Right = right;
        _Bottom = bottom;
        _Selected = false;
    }

    public Note(Parcel parcel)
    {
        _IdSong = parcel.readInt();
        _Id = parcel.readInt();
        _Category = parcel.readInt();
        _Left =parcel.readInt();
        _Top = parcel.readInt();
        _Right = parcel.readInt();
        _Bottom = parcel.readInt();
        _Selected = parcel.readInt()==1;
    }

    public int getRight() {
        return _Right;
    }

    public void setRight(int right) {
        _Right = right;
    }

    public int getBottom() {
        return _Bottom;
    }

    public void setBottom(int bottom) {
        _Bottom = bottom;
    }

    public int getLeft() {
        return _Left;
    }

    public void setLeft(int left) {
        _Left = left;
    }

    public int getId() {
        return _Id;
    }

    public void setId(int _Id) {
        this._Id = _Id;
    }

    public int getIdSong() {
        return _IdSong;
    }

    public int getTop() {
        return _Top;
    }

    public void setTop(int _Top) {
        this._Top = _Top;
    }

    public int getCategory() {
        return _Category;
    }

    public void setSelected(boolean b) { _Selected = b;}

    public boolean isSelected() { return _Selected; }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel parcel) {
            return new Note(parcel);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_IdSong);
        dest.writeInt(_Id);
        dest.writeInt(_Category);
        dest.writeInt(_Left);
        dest.writeInt(_Top);
        dest.writeInt(_Right);
        dest.writeInt(_Bottom);
        dest.writeInt(_Selected?1:0);
    }

}