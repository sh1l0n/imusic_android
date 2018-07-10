package org.drawreader.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import org.drawreader.Utils;
import org.drawreader.controller.Draw;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Scene
{
    private Cache _Cache;

    private final Rect _DisplayScene;

    private Rect _PilotScroll;

    public  int _IdSong;

    public int _IdClef;

    public int _IdTime;

    private Point _SizeBackgroundStaff;

    public Context _Context;

    public Scene(int width, int height, Context context, int id_song, int id_clef, int id_time) {
        _Context = context;
        _PilotScroll = new Rect(0, 0, EngineStaff.getInstance(context).getBoxScroll().width()/10, height/10);
        _DisplayScene = new Rect(0, 0, width, height);
        _Cache = new Cache(_DisplayScene.width(), context);
        _IdSong = id_song;
        _IdClef = id_clef;
        _IdTime = id_time;
        start();
        updatePosition(0);
    }

    public void setIds(int id_song, int id_clef, int id_time) {
        _IdSong = id_song;
        _IdClef = id_clef;
        _IdTime = id_time;
    }

    public void start() {
        _Cache.start();
    }

    public void update() { _Cache.update();}

    public void stop() {
        _Cache.stop();
    }

    public void updatePosition(int y) {
        synchronized (this) {
            int w = _DisplayScene.width();
            int h = _DisplayScene.height();
            if (y < 0) y = 0;
            if (y + h > _SizeBackgroundStaff.y) y = _SizeBackgroundStaff.y - h;
            _DisplayScene.set(0, y, w, y + h);

            h = _PilotScroll.height();
            int top = (int)(y*(_DisplayScene.height()*0.9)) / _SizeBackgroundStaff.y;

            if(top+h>_DisplayScene.height())
                top = _DisplayScene.height()-h;

            _PilotScroll.top = top;
            _PilotScroll.bottom = _PilotScroll.top+h;
        }
    }

    public void clearDraws() {
        _Cache.clearStack();
    }

    public void copyToBackground(HashMap<Integer, Stack<Draw>> stack) {
        _Cache.updateDraws(stack);
    }

    public int getTop() {
        return _DisplayScene.top;
    }

    public int getBottom() {return _DisplayScene.bottom; }

    public int getHeight() {
        return _DisplayScene.height();
    }

    public void draw(Canvas c, Paint p)
    {
        int top = _DisplayScene.top - _Cache._DisplayCache.top;
        c.drawBitmap(
                _Cache._BitmapRef,
                new Rect(0, top, _DisplayScene.width(), top + _DisplayScene.height()),
                new Rect(0, 0, _DisplayScene.width(), _DisplayScene.height()),
                p);
        c.drawRect(_PilotScroll, new Paint(Color.GRAY));
    }








































    private class Cache {

        private CacheThread _CacheThread;

        private int mh;

        private HashMap<Integer, Stack<Draw>> _MapDraws;

        private Rect _DisplayCache;

        public Bitmap _BitmapRef;

        public BitmapRegionDecoder _BitmapDecoder;

        private Paint _PaintPath;

        BitmapFactory.Options options = new BitmapFactory.Options();

        public Cache(int w, Context c) {

            try {
                _MapDraws = new HashMap<>();
                options.inMutable=true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                _BitmapDecoder = BitmapRegionDecoder.newInstance(c.getAssets().open("background/background-w"+w+".jpg"), false);
                _SizeBackgroundStaff = new Point(_BitmapDecoder.getWidth(), _BitmapDecoder.getHeight());
                _BitmapRef = _BitmapDecoder.decodeRegion(_DisplayScene, options).copy(Bitmap.Config.RGB_565, true);
                _DisplayCache = new Rect(0, 0, _BitmapRef.getWidth(), _BitmapRef.getHeight());
                mh = _DisplayScene.height() + _DisplayScene.height()/2; //Range up and down of the cache
                _PaintPath = new Paint();
                _PaintPath.setAntiAlias(true);
                _PaintPath.setDither(true);
                _PaintPath.setColor(Color.BLACK);
                _PaintPath.setStyle(Paint.Style.STROKE);
                _PaintPath.setStrokeJoin(Paint.Join.ROUND);
                _PaintPath.setStrokeCap(Paint.Cap.ROUND);
                _PaintPath.setStrokeWidth(8);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        public void start()
        {
            if(_CacheThread!=null) {
                _CacheThread.setRunning(false);
                _CacheThread.interrupt();
                _CacheThread = null;
            }
            _CacheThread = new CacheThread(this);
            _CacheThread.start();
        }

        public void update() { _CacheThread.update();}

        public void stop()
        {
            _CacheThread.setRunning(false);
            _CacheThread.interrupt();
            while(_CacheThread.isUpdating()){}
            boolean retry=true;
            while (retry) {
                try {
                    _CacheThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
            _CacheThread = null;
        }

        public void clearStack(){ _MapDraws.clear();}

        public boolean hasDraws(){ return !_MapDraws.isEmpty();}

        public void updateDraws(HashMap<Integer, Stack<Draw>> stack)
        {
            _MapDraws.clear();
            _MapDraws = new HashMap<>(stack);
            update();
        }

        protected Rect calculateCacheWindow() {

            Rect calculatedCacheWindowRect = new Rect();
            int top = _DisplayScene.top - mh;
            int bottom = _DisplayScene.bottom + mh;
            if (top<0 || top<=Utils.K_SIZE_BOX){
                bottom = bottom - top;
                top = 0;
            }
            if (bottom>_SizeBackgroundStaff.y || bottom+Utils.K_SIZE_BOX>_SizeBackgroundStaff.y){
                top = top - (bottom-_SizeBackgroundStaff.y);
                bottom = _SizeBackgroundStaff.y;
            }

            calculatedCacheWindowRect.set(_DisplayScene.left, top, _DisplayScene.right, bottom);
            if (Utils.DEBUG) {
                Log.d(Scene.class.getName(), "new cache.originRect = " + calculatedCacheWindowRect.toShortString() + " size=" + _SizeBackgroundStaff.toString());
            }
            return calculatedCacheWindowRect;
        }

        public void clear()
        {
            _DisplayCache.setEmpty();
            _BitmapRef.recycle();
            _BitmapRef=null;
            _BitmapDecoder.recycle();
            _BitmapDecoder=null;
        }
    }




































    class CacheThread extends Thread
    {
        private boolean _isRunning;

        private boolean _IsUpdating;

        private final Cache _Cache;

        private boolean _Update;

        public CacheThread(Cache c)
        {
            setName(CacheThread.class.getName());
            _isRunning= true;
            _IsUpdating = false;
            this._Cache = c;
        }

        public void setRunning(boolean r) {
            _isRunning = r;
        }

        public void update() {_Update = true;}

        public boolean isUpdating() { return _IsUpdating; }

        @Override
        public void run() {

            _Cache._DisplayCache = _Cache.calculateCacheWindow();
            Bitmap bmp = _Cache._BitmapDecoder.decodeRegion(this._Cache._DisplayCache, _Cache.options).copy(Bitmap.Config.RGB_565, true);
            EngineStaff.getInstance(_Context).updateNotes(_IdSong, _Cache._DisplayCache.top, _Cache._DisplayCache.bottom);
            _Cache._BitmapRef = EngineStaff.getInstance(_Context).draw(bmp, this._Cache._DisplayCache, _IdClef, _IdTime);
            _Update=false;

            while(_isRunning)
            {
                int p_top = (_DisplayScene.top-(_Cache.mh/2));
                int p_bottom = (_DisplayScene.bottom+(_Cache.mh/2));
                boolean top = p_top>0 && p_top<_Cache._DisplayCache.top;
                boolean bottom = p_bottom<_SizeBackgroundStaff.y && p_bottom>_Cache._DisplayCache.bottom;

                if ((top || bottom || _Update) && _isRunning && !_IsUpdating) {
                    _IsUpdating = true;
                    _Update = false;
                    Rect r = _Cache.calculateCacheWindow();
                    bmp = _Cache._BitmapDecoder.decodeRegion(r, _Cache.options).copy(Bitmap.Config.RGB_565, true);

                    synchronized (_Cache._BitmapRef) {
                        _Cache._DisplayCache.set(r);
                        updatePosition(_DisplayScene.top);
                        EngineStaff.getInstance(_Context).updateNotes(_IdSong, _Cache._DisplayCache.top, _Cache._DisplayCache.bottom);
                        EngineStaff.getInstance(_Context).draw(bmp, r,  _IdClef, _IdTime);
                        _Cache._BitmapRef = bmp;
                    }

                    if (_Cache.hasDraws()) {
                        final Bitmap bmpThread = bmp;
                        new Thread() {
                            @Override
                            public void run() {
                                int box_top = EngineStaff.getInstance(_Context).getBoxDrawing(_Cache._DisplayCache.top);
                                int box_bottom = EngineStaff.getInstance(_Context).getBoxDrawing(_Cache._DisplayCache.bottom);
                                Canvas c = new Canvas(bmpThread);
                                while(box_top<=box_bottom) {
                                    Paint paint = _Cache._PaintPath;
                                    if(_Cache._MapDraws.containsKey(box_top)) {
                                        for (Draw d : _Cache._MapDraws.get(box_top)) {
                                            if(d.isSelected()) paint.setColor(Color.BLUE);
                                            else paint.setColor(Color.BLACK);
                                            for (Path p : d.getPathsForCache(_Cache._DisplayCache.top))
                                                c.drawPath(p, paint);
                                            if (Utils.DEBUG) {
                                                Paint rect = new Paint(_Cache._PaintPath);
                                                rect.setColor(Color.RED);
                                                c.drawRect(d.getBoundingBoxForCache(_Cache._DisplayCache.top), rect);
                                            }
                                        }
                                    }
                                    ++box_top;
                                }
                                synchronized (_Cache._BitmapRef) {
                                    _Cache._BitmapRef = bmpThread;
                                }
                            }
                        }.start();
                    }
                }
                _IsUpdating=false;
            }
        }
    }
}
