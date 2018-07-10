package org.drawreader.controller;

import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.Vector;

public class Draw
{
    private Vector<Vector<Point>> pointsLocal;
    private Vector<Vector<Point>> pointsGlobal;
    private int x_origin;
    private int y_origin;
    private int _TopScene;
    private boolean _Selected;
    private Rect _BoundingBox;
    private Vector<Path> _GlobalPath;

    public Draw(int top_scene) {
        _TopScene = top_scene;
        _Selected=false;
        pointsLocal= new Vector<>();
        pointsGlobal = new Vector<>();
        pointsLocal.add(new Vector<Point>());
        pointsGlobal.add(new Vector<Point>());
        _BoundingBox = new Rect(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE, Integer.MIN_VALUE);
        _GlobalPath = new Vector<>();
    }


    public Draw(Draw draw)
    {
        pointsGlobal = new Vector<>(draw.pointsGlobal);
        pointsLocal = new Vector<>(draw.pointsLocal);
        _GlobalPath = new Vector<>(draw._GlobalPath);
        _Selected=draw._Selected;
        x_origin = draw.x_origin;
        y_origin = draw.y_origin;
        _BoundingBox = draw._BoundingBox;
        _TopScene = draw._TopScene;
    }

    public void addPoints(int x, int y)
    {
        if(x<_BoundingBox.left) _BoundingBox.left = x;
        if(x>_BoundingBox.right) _BoundingBox.right = x;
        if(y>_BoundingBox.bottom) _BoundingBox.bottom = y;
        if(y<_BoundingBox.top) _BoundingBox.top = y;

        if(pointsLocal.get(0).isEmpty()) {
            pointsLocal.get(pointsLocal.size()-1).add(new Point(0,0));
            pointsGlobal.get(pointsGlobal.size()-1).add(new Point(x,y));
            x_origin=x;
            y_origin=y;
        }
        else {
            pointsLocal.get(pointsLocal.size()-1).add(new Point((x - x_origin), (y - y_origin)));
            pointsGlobal.get(pointsGlobal.size()-1).add(new Point(x,y));
        }
    }

    public void removeLast() {
        Log.i(Draw.class.getName(), "Remove last: " + pointsLocal.size());
        pointsLocal.remove(pointsLocal.size()-1);
        pointsGlobal.remove(pointsGlobal.size()-1);
        _GlobalPath.remove(_GlobalPath.size()-1);

        _BoundingBox = new Rect(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE, Integer.MIN_VALUE);
        for(Vector<Point> vec : pointsGlobal){
            for(Point p : vec) {
                if(p.x<_BoundingBox.left) _BoundingBox.left = p.x;
                if(p.x>_BoundingBox.right) _BoundingBox.right = p.x;
                if(p.y>_BoundingBox.bottom) _BoundingBox.bottom = p.y;
                if(p.y<_BoundingBox.top) _BoundingBox.top = p.y;
            }
        }
    }

    public void setSelected(boolean s) {_Selected=s;}

    public boolean isSelected() {return _Selected;}

    public Vector<Path> getPaths() {return _GlobalPath; }

    public void addPath(Path path) {
        _GlobalPath.add(new Path(path));
    }

    public Rect getBoundingBoxForDisplayScene() {
        return _BoundingBox;
    }

    public Rect getBoundingBoxForSave() {
        return new Rect(_BoundingBox.left, _BoundingBox.top+_TopScene, _BoundingBox.right, _BoundingBox.bottom+_TopScene);
    }

    public int getTopScene() { return _TopScene;}

    public void mergeDraw(Draw draw)
    {
        for(Vector<Point> global_org: draw.pointsGlobal) {
            pointsLocal.add(new Vector<Point>());
            pointsGlobal.add(new Vector<Point>());
            for(Point p : global_org) addPoints(p.x, p.y);
        }
        _GlobalPath.addAll(draw._GlobalPath);
    }

    public Rect getBoundingBoxForCache(int top_cache) {
        int dist= _TopScene - top_cache;
        return new Rect(_BoundingBox.left, _BoundingBox.top+dist, _BoundingBox.right, _BoundingBox.bottom+dist);
    }

    public Vector<Path> getPathsForCache(int top_cache) {

        int lastX;
        int lastY;
        Vector<Path> cachePath = new Vector<>();
        Path path = new Path();
        int dist = _TopScene - top_cache;
        for(Vector<Point> vpoints : pointsGlobal)
        {
            path.reset();
            lastX = vpoints.get(0).x;
            lastY = (vpoints.get(0).y) + dist;
            path.moveTo(lastX, lastY);

            for(int i=1; i<vpoints.size(); ++i)
            {
                path.quadTo(lastX, lastY, (vpoints.get(i).x + lastX)/2, (vpoints.get(i).y + dist + lastY)/2);
                lastX = vpoints.get(i).x;
                lastY = (vpoints.get(i).y) + dist;
            }
            path.lineTo(lastX, lastY);
            cachePath.add(new Path(path));
        }
        return cachePath;
    }

    public Vector<Point> getPointsLocal() {
        Vector<Point> local = new Vector<>();
        for(Vector<Point> local_v : pointsLocal) local.addAll(local_v);
        return local;
    }
}
