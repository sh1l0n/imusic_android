package org.drawreader;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import org.database.GestorDatabase;
import org.database.model.Note;
import org.database.model.Song;
import org.drawreader.model.EngineStaff;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Utils {

    public static final boolean DEBUG = false;
    public static final int K_NLINES = 13; //11+1+1 space top and bottom
    public static final int K_SIZE_BOX = 494;
    public static final int WIDTH_PDF = 2480;
    public static final int HEIGHT_PDF = 3508;
    public static final int MARGIN_TOP = 365;
    public static final int MARGIN_LEFTRIGHT = 140;
    public static final int WIDTH_STAFF = 1940;
    public static final int K_SIZE_SPACE_BOX = 38;

    public static void generatePdf(final String file_name, final Context c, final Song song)
    {
        final ProgressDialog _ProgressBar;
        
        _ProgressBar = new ProgressDialog(c);
        _ProgressBar.setCancelable(true);
        _ProgressBar.setMessage(c.getString(R.string.generate_pdf));
        _ProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        _ProgressBar.setProgress(0);
        _ProgressBar.setMax(100);
        _ProgressBar.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(file_name));
                    Rectangle page_one = new Rectangle(WIDTH_PDF, HEIGHT_PDF);
                    document.setPageSize(page_one);

                    document.open();

                    ////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////////////
                    /** First Page */
                    Bitmap bmp = BitmapFactory.decodeStream(c.getAssets().open("pdf_page_intro.jpg")).copy(Bitmap.Config.RGB_565, true);
                    Canvas canvas = new Canvas(bmp);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextAlign(Paint.Align.CENTER);

                    textPaint.setTextSize(50);
                    int xPos = (WIDTH_PDF / 2);
                    int yPos = (int) (102 - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
                    canvas.drawText(song.getName(), xPos, yPos, textPaint);

                    Rect bounds = new Rect();
                    textPaint.setColor(Color.DKGRAY);
                    textPaint.setTextSize(25);
                    textPaint.getTextBounds("Autor: " + song.getAutor(), 0, ("Autor: " + song.getAutor()).length(), bounds);
                    yPos = MARGIN_TOP - (int) (102 - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
                    canvas.drawText("Autor: " + song.getAutor(), WIDTH_PDF - MARGIN_LEFTRIGHT - bounds.width(), yPos, textPaint);


                    Bitmap clef_image = BitmapFactory.decodeStream(c.getAssets().open("clefs/"+song.getClef()+".png"));
                    Bitmap time_image = BitmapFactory.decodeStream(c.getAssets().open("times/"+song.getTime()+".png"));
                    HashMap<Integer, String> categorys = GestorDatabase.getInstance(c).getCategorys();

                    for(int i=0; i<6; ++i) {
                            if(song.getClef()==0) canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, MARGIN_TOP+(i*Utils.K_SIZE_BOX)+153, null);
                            else if(song.getClef()==1) canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, MARGIN_TOP+(i*Utils.K_SIZE_BOX)+192, null);
                            else canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, MARGIN_TOP+(i*Utils.K_SIZE_BOX)+195, null);
                            canvas.drawBitmap(time_image, MARGIN_LEFTRIGHT+clef_image.getWidth(), MARGIN_TOP+(i*Utils.K_SIZE_BOX)+195, null);
                        }

                    if(time_image!=null) {
                        time_image.recycle();
                        time_image = null;
                    }
                    if(clef_image!=null) {
                        clef_image.recycle();;
                        clef_image=null;
                    }

                    int box;
                    int[] left_right = new int[]{0, 6*WIDTH_STAFF};
                    int[] left_top =  new int[2];
                    List<Note> lnotes = GestorDatabase.getInstance(c).getNotesOfSong(song.getId(), left_right[0], left_right[1]);

                    for(Note note : lnotes)
                    {
                        box = note.getLeft()/WIDTH_STAFF;
                        left_top[0] = (note.getLeft() - box*WIDTH_STAFF) + MARGIN_LEFTRIGHT + 260;
                        left_top[1] = note.getTop()+box*K_SIZE_BOX;

                        switch(note.getCategory())
                        {
                            case 0:case 3:case 4:case 19:case 15:case 17:
                            if(note.getTop()<247)
                                time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/inv/"+categorys.get(note.getCategory())));
                            else
                                time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/"+categorys.get(note.getCategory())));
                            break;
                            default:
                                time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/"+categorys.get(note.getCategory())));
                                break;
                        }

                        canvas.drawBitmap(time_image, left_top[0], left_top[1]+MARGIN_TOP, null);
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    Image signature = Image.getInstance(stream.toByteArray());
                    document.add(signature);


                    ////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////////////
                    /** Next pages */
                    Rectangle page_two = new Rectangle(WIDTH_PDF, HEIGHT_PDF+11);
                    for(int page=0; page<3; ++page)
                    {
                        document.newPage();
                        document.setPageSize(page_two);
                        bmp.recycle();
                        bmp = BitmapFactory.decodeStream(c.getAssets().open("pdf_page.jpg")).copy(Bitmap.Config.RGB_565, true);
                       // canvas.restore();
                        canvas = new Canvas(bmp);
                        clef_image = BitmapFactory.decodeStream(c.getAssets().open("clefs/"+song.getClef()+".png"));
                        time_image = BitmapFactory.decodeStream(c.getAssets().open("times/"+song.getTime()+".png"));

                        for(int i=0; i<7; ++i) {
                            if(song.getClef()==0) canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, 11+(i*Utils.K_SIZE_BOX)+153, null);
                            else if(song.getClef()==1) canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, 11+(i*Utils.K_SIZE_BOX)+192, null);
                            else canvas.drawBitmap(clef_image, MARGIN_LEFTRIGHT, 11+(i*Utils.K_SIZE_BOX)+195, null);
                            canvas.drawBitmap(time_image, MARGIN_LEFTRIGHT+clef_image.getWidth(), 11 +(i*Utils.K_SIZE_BOX)+195, null);
                        }

                        if(time_image!=null) {
                            time_image.recycle();
                            time_image = null;
                        }
                        if(clef_image!=null) {
                            clef_image.recycle();;
                            clef_image=null;
                        }

                        left_top = new int[]{left_right[1], left_right[1] + 7*WIDTH_STAFF};
                        lnotes = GestorDatabase.getInstance(c).getNotesOfSong(song.getId(), left_top[0], left_top[1]);

                        for(Note note : lnotes)
                        {
                            box = note.getLeft()/WIDTH_STAFF;
                            left_top[0] = (note.getLeft() - box*WIDTH_STAFF) + MARGIN_LEFTRIGHT + 260;
                            left_top[1] = note.getTop()+box*K_SIZE_BOX;

                            switch(note.getCategory())
                            {
                                case 0:case 3:case 4:case 19:case 15:case 17:
                                    if(note.getTop()<247)
                                        time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/inv/"+categorys.get(note.getCategory())));
                                    else
                                        time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/"+categorys.get(note.getCategory())));
                                    break;
                                default:
                                    time_image = BitmapFactory.decodeStream(c.getAssets().open("notes/"+categorys.get(note.getCategory())));
                                    break;
                            }

                            canvas.drawBitmap(time_image, left_top[0], left_top[1]+11, null);
                        }
                        stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        signature = Image.getInstance(stream.toByteArray());
                        document.add(signature);
                    }
                    document.close();
                }
                catch (IOException | DocumentException e) {
                    e.printStackTrace();
                }

                _ProgressBar.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(file_name, ""));
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                c.startActivity(intent);
            }
        }).start();
    }

}
