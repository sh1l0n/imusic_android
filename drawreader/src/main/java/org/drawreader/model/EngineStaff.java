package org.drawreader.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import org.database.GestorDatabase;
import org.database.model.Note;
import org.drawreader.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineStaff
{
    private static EngineStaff _EngineStaff = null;

    public static final int K_SIZEOFTOTALSTAFF = 51200;

    private int _RealLeftStaff;

    private List<Rect> _Boxes;

    public static int K_STARTNOTE;

    public static int K_ENDNOTE;

    private Rect _BoxScroll;

    private int _RealWidth;

    private Context _Context;

    private HashMap<Integer, List<Note>> _MapNotes;

    private List<Integer> _ItemsSelected;

    public static EngineStaff getInstance(Context c) {
        if(_EngineStaff ==null)
            _EngineStaff = new EngineStaff(c);
        return _EngineStaff;
    }

    public EngineStaff(Context c) {_Context=c;}

    public void initStaff(int width, int height, int widthOfScrollRect) {

        _BoxScroll = new Rect(0, 0, widthOfScrollRect,height);
        int numberOfStaffsForThisScreen = K_SIZEOFTOTALSTAFF/(width-_BoxScroll.width()-_BoxScroll.width()/5-260);
        //Initialize the staff with the number of row on the left for all boxes.
        int h = 0;
        _Boxes = new ArrayList<>();
        for(int i=0; i< numberOfStaffsForThisScreen; ++i) {
            _Boxes.add(new Rect(_BoxScroll.width()+ _BoxScroll.width()/5, h, width-_BoxScroll.width()-_BoxScroll.width()/5, h + Utils.K_SIZE_BOX));
            h+= Utils.K_SIZE_BOX;
        }
       _RealLeftStaff = _BoxScroll.width()+ _BoxScroll.width()/5;
        K_STARTNOTE = _RealLeftStaff + 260;
        K_ENDNOTE = (width-_BoxScroll.width()/5);
        _RealWidth = K_ENDNOTE-K_STARTNOTE;
        _MapNotes = new HashMap<>();
        _ItemsSelected = new ArrayList<>();
    }

    public void updateNotes(int id_song, int top_cache, int bottom_cache) {
        _MapNotes.clear();
        int box;
        List<Note> laux;
        int[] left_rigth = getProportionalLeftRightForShow(top_cache, bottom_cache);
        List<Note> lnotes =  GestorDatabase.getInstance(_Context).getNotesOfSong(id_song, left_rigth[0], left_rigth[1]);
        for(Note n : lnotes) {
            box = getBoxDrawingForShow(n.getLeft());
            if(_MapNotes.containsKey(box)) laux = _MapNotes.get(box);
            else laux = new ArrayList<>();
            laux.add(n);
            _MapNotes.put(box, laux);
        }
    }

    public void deleteSelectedNotes()
    {
        List<Note> listD;
        List<Integer> idsErase = new ArrayList<>();
        int idsong = -1;
        for(Integer box : _ItemsSelected) {
            listD = new ArrayList<>();
            for(Note n : _MapNotes.get(box)) {
                if (!n.isSelected()) listD.add(n);
                else idsErase.add(n.getId());
                idsong = n.getIdSong();
            }
            _MapNotes.get(box).clear();
            if(listD.isEmpty()) _MapNotes.remove(box);
            else  _MapNotes.get(box).addAll(listD);
        }
        GestorDatabase.getInstance(_Context).removeNotesSelectedFromSong(idsong);
        _ItemsSelected.clear();
    }

    public void deselectAll(int id_song)
    {
        GestorDatabase.getInstance(_Context).deselectAllNotes(id_song);
        for(Integer box : _ItemsSelected)
            for(int i=0; i<_MapNotes.get(box).size(); ++i)
                _MapNotes.get(box).get(i).setSelected(false);
        _ItemsSelected.clear();
    }

    public boolean selectNote(int x, int y)
    {
        int box = getBoxDrawing(y);
        int[] left_top;
        int[] right_bottom;
        Rect r;
        Note n;
        int count_selected = 0;
        boolean ret = false;
        if(_MapNotes.containsKey(box) && !_MapNotes.get(box).isEmpty())
        {
            for(int i=0; i<_MapNotes.get(box).size(); ++i) {
                n = _MapNotes.get(box).get(i);
                left_top = getProportionalLeftTopForShow(n.getLeft(), n.getTop());
                right_bottom = getProportionalLeftTopForShow(n.getRight(), n.getBottom());
                r = new Rect(left_top[0], left_top[1], right_bottom[0], right_bottom[1]);
                if(r.contains(x,y)) {
                    ret = true;
                    if(!_ItemsSelected.contains(box)) _ItemsSelected.add(box);
                    _MapNotes.get(box).get(i).setSelected(!_MapNotes.get(box).get(i).isSelected());
                    GestorDatabase.getInstance(_Context).updateNote(_MapNotes.get(box).get(i));
                }
                if(_MapNotes.get(box).get(i).isSelected()) ++count_selected;
            }
            if(count_selected==0 && !_ItemsSelected.isEmpty()) {
                int j;
                for (j = 0; j < _ItemsSelected.size(); ++j)
                    if (_ItemsSelected.get(j) == box) break;
                _ItemsSelected.remove(j);
            }
        }
        return ret;
    }

    public boolean hasNotesSelected() { return !_ItemsSelected.isEmpty();}


    public int getBoxDrawingForShow(int x)
    {
        return x/_RealWidth;
    }

    public int getBoxDrawing(int y) {return y/Utils.K_SIZE_BOX;}


    public int[] getProportionalLeftTopForSave(int x, int y, int category)
    {
        int box = getBoxDrawing(y);
        int[] left_top = new int[2];
        left_top[0] = box *_RealWidth + (x - K_STARTNOTE); //left

        switch (category)
        {
            case 6: //barline
            case 14: //sixteenth rest
                left_top[1] = 190;
                break;
            case 1: //quarter rest
            case 7: //eight rest
            case 12: //thirty two rest
            case 18: //whole half rest
                left_top[1] = 228;
                break;
            case 16: //sixty four rest
                left_top[1] = 152; //Thirty two rest
                break;
            default: left_top[1] = (y-_Boxes.get(box).top);
                break;
        }
        return left_top;
    }

    private int[] getProportionalLeftTopForShow(int x, int y)
    {
        int[] left_top = new int[2];
        int box = getBoxDrawingForShow(x);
        left_top[0] = (x - box*_RealWidth) + K_STARTNOTE;
        left_top[1] = y+_Boxes.get(box).top;
        return left_top;
    }

    public Rect getBoxScroll() { return _BoxScroll; }

    public int[] getProportionalLeftRightForShow(int top, int bottom)
    {
        int[] left_top = new int[2];
        int box_left = getBoxDrawing(top);
        int box_right = getBoxDrawing(bottom);
        left_top[0] = box_left*_RealWidth ;
        left_top[1] = box_right*_RealWidth + _RealWidth;
        return left_top;
    }

    public Bitmap draw(Bitmap bmp, Rect _DisplayCache, int clef, int time) {

        try {
            Bitmap clef_image = BitmapFactory.decodeStream(_Context.getAssets().open("clefs/"+clef+".png"));
            Bitmap time_image = BitmapFactory.decodeStream(_Context.getAssets().open("times/"+time+".png"));
            HashMap<Integer, String> categorys = GestorDatabase.getInstance(_Context).getCategorys();
            Rect r;

            Canvas c = new Canvas(bmp);

            for(int i=0; i<_Boxes.size(); ++i) {
                r = _Boxes.get(i);
                //Draw only the full staff of the cache view
                if (r.top >= _DisplayCache.top && r.bottom <= _DisplayCache.bottom) {

                    if(clef==0) c.drawBitmap(clef_image, _RealLeftStaff, (r.top - _DisplayCache.top)+152, null);
                    else if(clef==1) c.drawBitmap(clef_image, _RealLeftStaff, (r.top - _DisplayCache.top)+190, null);
                    else c.drawBitmap(clef_image, _RealLeftStaff, (r.top - _DisplayCache.top)+190, null);
                    c.drawBitmap(time_image, _RealLeftStaff+clef_image.getWidth(),(r.top - _DisplayCache.top)+190, null);
                }
            }

            if(time_image!=null) {
                time_image.recycle();
                time_image = null;
            }
            if(clef_image!=null) {
                clef_image.recycle();;
                clef_image=null;
            }

            int[] left_top;
            String dir;

            for(Map.Entry<Integer, List<Note>> values : _MapNotes.entrySet()) {
                for (Note note : values.getValue()) {
                    if(note.isSelected()) dir = "notes_selected/";
                    else dir = "notes/";
                    Log.i(EngineStaff.class.getName(), "dir: " +dir);
                    left_top = getProportionalLeftTopForShow(note.getLeft(), note.getTop());
                   /* switch (note.getCategory()) {
                        case 0:
                        case 3:
                        case 4:
                        case 19:
                        case 15:
                        case 17:
                            if (note.getTop() + (note.getBottom() - note.getTop()) / 2 < 247)
                                dir += "inv/";
                            break;
                    }*/
                    time_image = BitmapFactory.decodeStream(_Context.getAssets().open(dir + categorys.get(note.getCategory())));
                    c.drawBitmap(time_image, left_top[0], left_top[1] - _DisplayCache.top, null);
                }
            }
            return bmp;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
