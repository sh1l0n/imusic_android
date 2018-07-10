package org.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME= "database.db";

    private String DATABASE_PATH = "";

    private static final int DATABASE_VERSION= 1;

    private static DBHelper instance_;

    private static SQLiteDatabase database;

    private static Context context;

    public DBHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
        context= c;

        if(android.os.Build.VERSION.SDK_INT >= 17)
            DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";

        try {
            createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");

        }

        try {
            openDataBase();
        }catch(SQLException sqle){
            throw sqle;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {}

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {}

    public static DBHelper getInstance(Context c) {
        if(instance_==null)
            instance_= new DBHelper(c);

        return instance_;
    }


    private boolean checkDataBase() {
        boolean checkdb=false;
        try {
            String path = DATABASE_PATH + DATABASE_NAME;
            File dbfile= new File(path);
            checkdb = dbfile.exists();
        }
        catch (SQLiteException e) {
            e.printStackTrace();;
        }
        return checkdb;
    }

    public void createDataBase() throws IOException
    {
        boolean dbexist= checkDataBase();

        if(!dbexist)
        {
            getReadableDatabase();
            close();

            try {
                copyDataBase();
            }
            catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    public void copyDataBase() throws IOException
    {
        InputStream myinput = context.getAssets().open(DATABASE_NAME);
        String outfilename = DATABASE_PATH + DATABASE_NAME;
        OutputStream myoutput = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0)
            myoutput.write(buffer,0,length);

        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public void openDataBase() throws SQLException {
        String mypath = DATABASE_PATH + DATABASE_NAME;
        database = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE
        );
    }

    @Override
    public synchronized void close() {
        if(database != null) {
            database.close();
        }
        super.close();
    }

    public SQLiteDatabase openConnection(Context context)
    {
        if(database==null)
            database= getInstance(context).getWritableDatabase();

        return database;
    }

    public void closeConnection() {
        if(database!=null) {
            database.close();
            database= null;
        }
    }
}
