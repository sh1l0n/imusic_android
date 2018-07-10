package org.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.database.model.Note;
import org.database.model.Song;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GestorDatabase
{
    private static GestorDatabase instance_;

    private SQLiteDatabase database_;

    public GestorDatabase(Context context) {
        database_ = DBHelper.getInstance(context).openConnection(context);
    }

    public static GestorDatabase getInstance(Context context) {
        if (instance_ == null) {
            instance_ = new GestorDatabase(context);
        }
        return instance_;
    }

















    public List<Song> getAllFavoritesSongs()
    {
        List<Song> list = new ArrayList<>();
        String query = "SELECT * FROM " + DBTables.TSong.TABLE_NAME + " s " +
                        "INNER JOIN " + DBTables.TFavs.TABLE_NAME + " f " +
                        "ON s."+DBTables.TSong._ID+"=f."+DBTables.TSong._ID+";";
        Cursor c = database_.rawQuery(query, new String[]{});
        if(c.moveToFirst())
        {
            do {
                list.add(getSongFromCursor(c));
            }while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean isFavorite(int id)
    {
        String[] args = new String[]{String.valueOf(id)};
        String where = DBTables.TSong._ID + "=?";
        Cursor c = database_.rawQuery("SELECT COUNT(*) FROM " + DBTables.TFavs.TABLE_NAME + " WHERE " + where, args);
        if (!c.moveToFirst()) return false;
        return (c.getInt(0)==1);
    }

    public void addFavorite(int id)
    {
        ContentValues cv = new ContentValues();
        cv.put(DBTables.TSong._ID, id);
        database_.insert(DBTables.TFavs.TABLE_NAME, null, cv);
    }

    public void removeFavorite(int id)
    {
        String[] args = new String[]{String.valueOf(id)};
        String where = DBTables.TSong._ID + "=?";
        database_.delete(DBTables.TFavs.TABLE_NAME, where, args);
    }

    public void removeFavorites(List<Integer> ids)
    {
        String[] args = new String[1];
        String where = DBTables.TSong._ID + "=?";
        for(int i=0;i<ids.size();++i) {
            args[0] = String.valueOf(ids.get(i));
            database_.delete(DBTables.TFavs.TABLE_NAME, where, args);
        }
    }



















    private Song getSongFromCursor(Cursor c)
    {
        int id_note=c.getInt(c.getColumnIndex(DBTables.TSong._ID));
        String autor = c.getString(c.getColumnIndex(DBTables.TSong._AUTOR));
        String name = c.getString(c.getColumnIndex(DBTables.TSong._NAME));
        int clef = c.getInt(c.getColumnIndex(DBTables.TSong._CLEF));
        int time = c.getInt(c.getColumnIndex(DBTables.TSong._TIME));
        return new Song(id_note, name, autor, clef, time);
    }

    public ContentValues getCVSong(Song s, boolean insert)
    {
        ContentValues cv = new ContentValues();
        if(!insert)cv.put(DBTables.TSong._ID, s.getId());
        cv.put(DBTables.TSong._NAME, s.getName());
        cv.put(DBTables.TSong._AUTOR, s.getAutor());
        cv.put(DBTables.TSong._CLEF, s.getClef());
        cv.put(DBTables.TSong._TIME, s.getTime());
        return cv;
    }

    public List<Song> getAllSongs()
    {
        List<Song> list = new ArrayList<>();
        String[] args = new String[]{};
        String sql = "SELECT * FROM SONG;";
        Cursor c = database_.rawQuery(sql, args);

        if (c.moveToFirst())
        {
            do {
                list.add(getSongFromCursor(c));
            }while (c.moveToNext());
        }
        c.close();
        return list;

    }

    public void removeSongs(List<Integer> song)
    {
        String[] args = new String[1];
        String where = DBTables.TSong._ID + "=?";
        for(int i=0;i<song.size();++i) {
            args[0] = String.valueOf(song.get(i));
            database_.delete(DBTables.TSong.TABLE_NAME, where, args);
        }
    }

    public void removeSong(int id)
    {
        String[] args = new String[]{String.valueOf(id)};
        String where_s = DBTables.TSong._ID + "=?";;
        String where_n = DBTables.TNote._ID_SONG + "=?";;
        database_.delete(DBTables.TNote.TABLE_NAME, where_n, args);
        database_.delete(DBTables.TSong.TABLE_NAME, where_s, args);
        database_.delete(DBTables.TSong.TABLE_NAME, where_s, args);
    }

    public long insertSong(Song s) {
        long l = database_.insert(DBTables.TSong.TABLE_NAME, null, getCVSong(s, true));
        return l;
    }

    public void updateSong(Song s)
    {
        String[] args= new String[]{String.valueOf(s.getId())};
        String where= DBTables.TSong._ID+"=?";
         database_.update(DBTables.TSong.TABLE_NAME, getCVSong(s, false), where, args);
    }


    public List<Note> getNotesOfSong(int id_song, int left, int right)
    {
        List<Note> list = new ArrayList<>();
        String[] args = new String[]{String.valueOf(id_song),String.valueOf(left),String.valueOf(right)};
        String sql = "SELECT * FROM " +  DBTables.TNote.TABLE_NAME +
                    " WHERE " + DBTables.TNote._ID_SONG + "=? AND " +
                                DBTables.TNote._LEFT + ">=? AND " +
                                "(" + DBTables.TNote._RIGHT +"-" + DBTables.TNote._LEFT + ")<?;";

        Cursor c = database_.rawQuery(sql, args);
        if(c.moveToFirst())
        {
            do {
                list.add(getNoteFromCursor(c));
            }while (c.moveToNext());
        }
        c.close();

        Log.d(GestorDatabase.class.getName(), "Number of notes of current song: " + list.size());
        return list;
    }





















    public HashMap<Integer, String> getCategorys()
    {
        HashMap<Integer, String> map = new HashMap<>();
        String sql = "SELECT * FROM " + DBTables.TCategory.TABLE_NAME +";";
        Cursor c = database_.rawQuery(sql, new String[]{});
        int id;
        String image;
        if(c.moveToFirst())
        {
            do {
                id = c.getInt(c.getColumnIndex(DBTables.TCategory._ID_CATEGORY));
                image = c.getString(c.getColumnIndex(DBTables.TCategory._IMAGE));
                map.put(id, image);
            }while (c.moveToNext());
        }
        c.close();
        return map;
    }
























    public void deselectAllNotes(int id_song) {

        String[] args = new String[]{String.valueOf(id_song)};
        String where = DBTables.TNote._ID_SONG + "=?;";
        ContentValues cv = new ContentValues();
        cv.put(DBTables.TNote._SELECTED, 0);
        database_.update(DBTables.TNote.TABLE_NAME, cv, where, args);
    }

    public void updateNote(Note n) {
        String[] args = new String[]{String.valueOf(n.getIdSong()), String.valueOf(n.getId())};
        String where = DBTables.TNote._ID_SONG + "=? AND " + DBTables.TNote._ID_NOTE + "=?;";
        database_.update(DBTables.TNote.TABLE_NAME, getCVNote(n, false), where, args);
    }


    public void addNotes(List<Note> lnotes) {
        for(Note n : lnotes) n.setId((int)database_.insert(DBTables.TNote.TABLE_NAME, null, getCVNote(n, true)));
    }

    public void removeNotesSelectedFromSong(int idsong) {
        String[] args = new String[]{String.valueOf(idsong), "1"};
        String where = DBTables.TNote._ID_SONG + "=? AND " + DBTables.TNote._SELECTED + "=?";
        database_.delete(DBTables.TNote.TABLE_NAME, where, args);
    }

    public Note getNoteFromCursor(Cursor c) {

        int id_song = c.getInt(c.getColumnIndex(DBTables.TNote._ID_SONG));
        int id_note = c.getInt(c.getColumnIndex(DBTables.TNote._ID_NOTE));
        int id_category = c.getInt(c.getColumnIndex(DBTables.TNote._ID_CATEGORY));
        int top = c.getInt(c.getColumnIndex(DBTables.TNote._TOP));
        int left = c.getInt(c.getColumnIndex(DBTables.TNote._LEFT));
        int right = c.getInt(c.getColumnIndex(DBTables.TNote._RIGHT));
        int bottom = c.getInt(c.getColumnIndex(DBTables.TNote._BOTTOM));
        int selected = c.getInt(c.getColumnIndex(DBTables.TNote._SELECTED));
        return new Note(id_song, id_note, id_category, left, top, right, bottom, (selected==1));
    }

    public ContentValues getCVNote(Note n,  boolean insert)
    {
        ContentValues cv = new ContentValues();
        if(!insert)     cv.put(DBTables.TNote._ID_NOTE, n.getId());
        else cv.put(DBTables.TNote._ID_NOTE, -1);
        cv.put(DBTables.TNote._ID_SONG, n.getIdSong());
        cv.put(DBTables.TNote._ID_CATEGORY, n.getCategory());
        cv.put(DBTables.TNote._LEFT, n.getLeft());
        cv.put(DBTables.TNote._TOP, n.getTop());
        cv.put(DBTables.TNote._RIGHT, n.getRight());
        cv.put(DBTables.TNote._BOTTOM, n.getBottom());
        cv.put(DBTables.TNote._SELECTED, n.isSelected()?1:0);
        return cv;
    }

}
