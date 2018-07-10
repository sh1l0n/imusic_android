package org.drawreader.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;

import org.classifier.ClassifierJoinDraws;
import org.drawreader.Utils;
import org.drawreader.model.EngineStaff;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ControllerPathDrawer {
    private static final float TOUCH_TOLERANCE = 2;

    private Path _PathForDisplay;

    private Paint _PaintPath;

    private float _LastX;

    private float _LastY;

    private int width;

    private int height;

    private HashMap<Integer, Stack<Draw>> _LatestDraws = null;

    private HashMap<Integer, Stack<Draw>> _CurrentDrawsScreen = null;

    private List<Integer> _ItemsSelectedLatest;

    private Stack<Integer> _ElementsAdded;

    private Draw currentDraw = null;

    private Context _Context;

    public ControllerPathDrawer(int w, int h, Context c) {
        _LatestDraws = new HashMap<>();
        _ElementsAdded = new Stack<>();
        _Context = c;
        _CurrentDrawsScreen = new HashMap<>();
        width = w;
        height = h;
        _PaintPath = new Paint();
        _PaintPath.setAntiAlias(true);
        _PaintPath.setDither(true);
        _PaintPath.setColor(Color.BLACK);
        _PaintPath.setStyle(Paint.Style.STROKE);
        _PaintPath.setStrokeJoin(Paint.Join.ROUND);
        _PaintPath.setStrokeCap(Paint.Cap.ROUND);
        _PaintPath.setStrokeWidth(8);
        _ItemsSelectedLatest = new ArrayList<>();

    }

    public boolean hasSelectedDraws() {
        return !_ItemsSelectedLatest.isEmpty();
    }

    public boolean selectDraw(int x, int y) {
        int box = EngineStaff.getInstance(_Context).getBoxDrawing(y);
        boolean ret = false;
        int count_selected = 0;
        Rect r;
        if(_LatestDraws.containsKey(box) && !_LatestDraws.get(box).empty()) {
            for(int i=0; i<_LatestDraws.get(box).size(); ++i) {
                r = _LatestDraws.get(box).get(i).getBoundingBoxForSave();
                if(r.contains(x,y)) {
                    ret = true;
                    if(!_ItemsSelectedLatest.contains(box)) _ItemsSelectedLatest.add(box);
                    _LatestDraws.get(box).get(i).setSelected(!_LatestDraws.get(box).get(i).isSelected());
                }
                if(_LatestDraws.get(box).get(i).isSelected()) ++count_selected;
            }
            if(count_selected==0 && !_ItemsSelectedLatest.isEmpty()) {
                int j;
                for (j = 0; j < _ItemsSelectedLatest.size(); ++j)
                    if (_ItemsSelectedLatest.get(j) == box) break;
                _ItemsSelectedLatest.remove(j);
            }
        }
        return ret;
    }

    public void clearSelectedDraws() {
        Stack<Draw> listD;
        Draw d;
        for(Integer box : _ItemsSelectedLatest) {
            listD = new Stack<>();
            while(!_LatestDraws.get(box).empty()) {
                d = _LatestDraws.get(box).pop();
                if(!d.isSelected()) listD.push(d);
            }
            Collections.reverse(listD);
            _LatestDraws.get(box).clear();

            if(listD.empty()) _LatestDraws.remove(box);
            else _LatestDraws.get(box).addAll(listD);
        }
        _ItemsSelectedLatest.clear();
        _PaintPath.setColor(Color.BLACK);
    }

    public void deselectAll() {
        for(Integer box : _ItemsSelectedLatest)
            for(int i=0; i<_LatestDraws.get(box).size(); ++i)
                _LatestDraws.get(box).get(i).setSelected(false);
        _ItemsSelectedLatest.clear();
        _PaintPath.setColor(Color.BLACK);
    }

    public boolean hasDrawsInScene() {
        return !_CurrentDrawsScreen.isEmpty();
    }

    public boolean hasDrawsInCache() {
        return !_LatestDraws.isEmpty();
    }

    public HashMap<Integer, Stack<Draw>> getLatestDraws() { return _LatestDraws; }

    public List<Draw> getDraws() {

        List<Draw> ret = new ArrayList<>();
        for(Map.Entry<Integer, Stack<Draw>> entry : new HashMap<>(_LatestDraws).entrySet())
            ret.addAll(entry.getValue());
        return ret;
    }

    public void clearCacheDraws() {
        _LatestDraws.clear();
    }

    public void clearCurrent() {
        currentDraw = null;
    }

    public int removeLastDraw()
    {
        if(_ElementsAdded.empty()) return -1;

        int box_erase = _ElementsAdded.pop();
        int ret = -1;
        if(!_CurrentDrawsScreen.isEmpty()) {
            _CurrentDrawsScreen.get(box_erase).peek().removeLast();
            ret = _CurrentDrawsScreen.get(box_erase).peek().getTopScene();
            if (_CurrentDrawsScreen.get(box_erase).peek().getPointsLocal().isEmpty()) _CurrentDrawsScreen.get(box_erase).pop();
            if(_CurrentDrawsScreen.get(box_erase).isEmpty()) _CurrentDrawsScreen.remove(box_erase);
        }

        if(!_LatestDraws.isEmpty()) {
            ret = _LatestDraws.get(box_erase).peek().getTopScene();
            _LatestDraws.get(box_erase).peek().removeLast();
            if (_LatestDraws.get(box_erase).peek().getPointsLocal().isEmpty()) _LatestDraws.get(box_erase).pop();
            if(_LatestDraws.get(box_erase).isEmpty()) _LatestDraws.remove(box_erase);
        }
        return ret;
    }

    public void touchDown(int top_scene, int x, int y) {
        _PathForDisplay = new Path();
        currentDraw = new Draw(top_scene);
        currentDraw.addPoints(x, y);
        _PathForDisplay.moveTo(x, y);
        _LastX = x;
        _LastY = y;
    }


    public void touchMove(int x, int y)
    {

        float dx = Math.abs(x - _LastX);
        float dy = Math.abs(y - _LastY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
        {
            _PathForDisplay.quadTo(_LastX, _LastY, (x + _LastX) / 2, (y + _LastY) / 2);
            _LastX = x;
            _LastY = y;
            currentDraw.addPoints(x, y);
        }
    }

    public boolean touchUp(int x, int y, boolean isSameBox) {

        _PathForDisplay.lineTo(_LastX, _LastY);
        currentDraw.addPath(new Path(_PathForDisplay));
        _PathForDisplay=null;
        if(isSameBox) currentDraw.addPoints(x, y);

        int box = EngineStaff.getInstance(_Context).getBoxDrawing(currentDraw.getBoundingBoxForSave().top);
        _ElementsAdded.push(box);
        List<Draw> stackList;

        if(_LatestDraws.containsKey(box) && !_LatestDraws.get(box).isEmpty()) {
            stackList = new ArrayList<>(_LatestDraws.get(box));
            Rect r1 = currentDraw.getBoundingBoxForDisplayScene();
            Rect r2;
            int i;
            for (i = stackList.size() - 1; i >= 0; --i) {
                r2 = stackList.get(i).getBoundingBoxForDisplayScene();
                if ((r1.contains(r2) || r1.intersect(r2) ||
                        ClassifierJoinDraws.isJoin(new Rect(
                                    r1.left - r2.left,
                                    r1.top - r2.top,
                                    r1.right - r2.right,
                                    r1.bottom - r2.bottom
                            ))))
                {

                    _LatestDraws.get(box).get(i).mergeDraw(currentDraw);
                    if(_CurrentDrawsScreen.containsKey(box) &&
                            !_CurrentDrawsScreen.get(box).isEmpty() &&
                            _CurrentDrawsScreen.get(box).size()>i) {
                        _CurrentDrawsScreen.get(box).get(i).mergeDraw(currentDraw);
                        currentDraw = null;
                        return true;
                    }
                }
            }
        }
        Stack<Draw> stack, stack_cc;
        if(!_CurrentDrawsScreen.containsKey(box)) stack = new Stack<>();
        else stack = _CurrentDrawsScreen.get(box);
        stack.push(new Draw(currentDraw));
        _CurrentDrawsScreen.put(box, stack);

        if(!_LatestDraws.containsKey(box)) stack_cc = new Stack<>();
        else stack_cc = _LatestDraws.get(box);
        stack_cc.push(new Draw(currentDraw));
        _LatestDraws.put(box, stack_cc);
        currentDraw = null;
        return false;
    }

    public void scrollScreen() {
        _CurrentDrawsScreen.clear();
        _PathForDisplay=null;
    }

    public void draw(Canvas c, Paint p)
    {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if(!_CurrentDrawsScreen.isEmpty()) {
            for (Map.Entry<Integer, Stack<Draw>> entry : new HashMap<>(_CurrentDrawsScreen).entrySet()) {
                for (Draw d : new ArrayList<>(entry.getValue())) {
                    if (d.isSelected()) _PaintPath.setColor(Color.BLUE);
                    else _PaintPath.setColor(Color.BLACK);
                    for (Path path : new ArrayList<>(d.getPaths()))
                        canvas.drawPath(path, _PaintPath);
                    if (Utils.DEBUG) {
                        Paint rect = new Paint(_PaintPath);
                        rect.setColor(Color.BLUE);
                        canvas.drawRect(d.getBoundingBoxForDisplayScene(), rect);
                    }
                }
            }
        }
        if(_PathForDisplay!=null) canvas.drawPath(_PathForDisplay, _PaintPath);

        c.drawBitmap(bmp, 0F,  0F, p);
    }
}
