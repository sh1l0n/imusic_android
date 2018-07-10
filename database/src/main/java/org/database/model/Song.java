package org.database.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Song implements Parcelable
{
    private int _Id;

    private String _Name;

    private String _Autor;

    private int _Clef;

    private int _Time;

    private List<Note> _LNotes;

    public Song(String n, String autor, int clef, int time) {
        _Name=n;
        _Id =-1;
        _Autor = autor;
        _Clef = clef;
        _Time = time;
        _LNotes = new ArrayList<>();
    }

    public Song(int id, String n, String autor, int clef, int time) {
        _Name=n;
        _Id = id;
        _Autor = autor;
        _Clef = clef;
        _Time = time;
        _LNotes = new ArrayList<>();
    }

    public Song(int id, String n, String autor, List<Note> ln, int clef, int time) {
        _Name=n;
        _Id = id;
        _Autor = autor;
        _Clef = clef;
        _Time = time;
        _LNotes = new ArrayList<>(ln);
    }

    public Song(Parcel parcel) {
        _Id = parcel.readInt();
        _Name = parcel.readString();
        _Autor = parcel.readString();
        _Clef = parcel.readInt();
        _Time = parcel.readInt();
        _LNotes = (ArrayList<Note>)parcel.readArrayList(Note.class.getClassLoader());
    }

    public int getTime() {
        return _Time;
    }

    public void setTime(int _Time) {
        this._Time = _Time;
    }

    public int getClef() { return _Clef; }

    public void setClef(int clef) { _Clef = clef; }

    public String getName() {
        return _Name;
    }

    public void setName(String name) {
        _Name = name;
    }

    public int getId() {
        return _Id;
    }

    public void setId(int _Id) {
        this._Id = _Id;
    }

    public String getAutor() {return _Autor;}

    public void setAutor(String autor) {_Autor = autor;}

    public List<Note> getLNotes() {
        return _LNotes;
    }

    public void setLNotes(List<Note> _LNotes) {
        this._LNotes = _LNotes;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel parcel) {
            return new Song(parcel);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_Id);
        dest.writeString(_Name);
        dest.writeString(_Autor);
        dest.writeInt(_Clef);
        dest.writeInt(_Time);
        dest.writeList(_LNotes);
    }
}
