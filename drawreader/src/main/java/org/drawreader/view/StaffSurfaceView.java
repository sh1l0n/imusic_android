package org.drawreader.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.database.GestorDatabase;
import org.drawreader.controller.ControllerPathDrawer;
import org.drawreader.controller.Draw;
import org.drawreader.listener.ListenerInvalidateMenu;
import org.drawreader.model.EngineStaff;
import org.drawreader.model.Scene;

import java.util.List;


public class StaffSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
    private boolean _LongClickActive;

    private boolean _MultiSelect;

    private long _TimeLastLongClick;

    private boolean _IsFling = false;

    private boolean _isDrawing = false;

    private boolean _CopyDrawsToBitmap = false;

    private int _IdSong;

    private int _IdClef;

    private int _IdTime;

    private int _CurrentBoxDrawing;

    private Paint _PaintForBlitTing;

    private TouchController _TouchController;

    private ControllerPathDrawer _ControllerPathDrawer;

    private Scene _Scene;

    private Context _Context;

    private ThreadStaffView _GameLoopThread;

    private ListenerInvalidateMenu _Listener;

    private int _StartPosition;

    public void setListener(ListenerInvalidateMenu l) {
        _Listener = l;
    }

    private void init(Context context)
    {
        _GameLoopThread = new ThreadStaffView(this);
        _TouchController = new TouchController();
        _PaintForBlitTing = new Paint(Paint.DITHER_FLAG);
        _CurrentBoxDrawing = -1;
        _LongClickActive=false;
        _MultiSelect = false;
        _TimeLastLongClick=0;
        _Context = context;
        getHolder().addCallback(this);
    }

    public StaffSurfaceView(Context context)
    {
        super(context);
        init(context);
    }

    public StaffSurfaceView(Context context, AttributeSet at)
    {
        super(context, at);
        init(context);
    }

    public StaffSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setStartPosition(int pos) {
        _StartPosition = pos;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        Log.i(StaffSurfaceView.class.getName(), "++++ onSizeChanged() w: " + w + " h: " + h + " oldw: " + w + " oldh : " + oldh);
        super.onSizeChanged(w, h, oldw, oldh);
        EngineStaff.getInstance(_Context).initStaff(w, h, w/10);
        _Scene = new Scene(w, h, _Context, _IdSong, _IdClef, _IdTime);
        _ControllerPathDrawer = new ControllerPathDrawer(w, h, _Context);
        _Scene.updatePosition(_StartPosition);
        start();
    }


    public List<Draw> getDraws() {
        return _ControllerPathDrawer.getDraws();
    }

    public void drawOwn(Canvas canvas) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if(_CopyDrawsToBitmap) {
            _CopyDrawsToBitmap = false;
            _ControllerPathDrawer.scrollScreen();
            _Scene.copyToBackground(_ControllerPathDrawer.getLatestDraws());
            _Listener.invalidateOptionsMenuFromView();
        }
        _Scene.draw(canvas, _PaintForBlitTing);
        _ControllerPathDrawer.draw(canvas,  _PaintForBlitTing);
    }

    public void setIds(int id_song, int id_clef, int id_time) {
        _IdSong = id_song;
        _IdClef = id_clef;
        _IdTime = id_time;
        if(_Scene!=null)_Scene.setIds(id_song, id_clef, id_time);
    }

    public void clearDraws() {
        _ControllerPathDrawer.clearCacheDraws();
        _Scene.clearDraws();
        _CopyDrawsToBitmap=true;
    }

    public boolean hasSelectedDraws() {
        return _MultiSelect;
    }

    public void update() { _Scene.update();}

    @Override
    protected void onDraw(Canvas canvas)
    {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}

        try {
            canvas = getHolder().lockCanvas();
            synchronized (getHolder()) {
                if(canvas!=null) drawOwn(canvas);
            }
        } finally {
            if (canvas != null) getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void clearSelectedDraws() {
        _ControllerPathDrawer.clearSelectedDraws();
        EngineStaff.getInstance(_Context).deleteSelectedNotes();
        _MultiSelect = false;
        _CopyDrawsToBitmap=true;
    }

    public void deselectAll() {
        _ControllerPathDrawer.deselectAll();
        EngineStaff.getInstance(_Context).deselectAll(_IdSong);
        _MultiSelect=false;
        _CopyDrawsToBitmap=true;
    }

    public void removeLastDraw()
    {
        int pos = _ControllerPathDrawer.removeLastDraw();
        if(pos==-1) return;
        _Scene.updatePosition(pos);
        _CopyDrawsToBitmap=true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)event.getX();
        int y = (int)event.getY();
        boolean found=false, found_n=false;


        if(x<=EngineStaff.getInstance(_Context).getBoxScroll().right) {
            if(_ControllerPathDrawer.hasDrawsInScene()) _CopyDrawsToBitmap = true;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if(_isDrawing) return true;
                    _IsFling = true;
                    _TouchController.down(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(_isDrawing) return true;
                    _TouchController.move(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if(_isDrawing) {_isDrawing=false;_LongClickActive=false;_TimeLastLongClick=0; return true; }
                    _IsFling = false;
                    break;
            }
        }
        else
        {
            if((x<=EngineStaff.K_STARTNOTE || x>=EngineStaff.K_ENDNOTE) && (event.getAction() & MotionEvent.ACTION_MASK)!=MotionEvent.ACTION_UP) return false;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if(_IsFling) return false;

                    if(!_MultiSelect) {
                        _LongClickActive= true;
                        _TimeLastLongClick = event.getEventTime();
                        _isDrawing=true;
                        _CurrentBoxDrawing = EngineStaff.getInstance(_Context).getBoxDrawing(y + _Scene.getTop());
                        _ControllerPathDrawer.touchDown(_Scene.getTop(), x, y);
                    }
                    else {
                        found = _ControllerPathDrawer.selectDraw(x, y + _Scene.getTop());
                        found_n = EngineStaff.getInstance(_Context).selectNote(x, y+_Scene.getTop());
                        if(found || found_n) _CopyDrawsToBitmap = true;
                    }

                    break;

                case MotionEvent.ACTION_MOVE:
                    if(_IsFling || _MultiSelect) return false;
                    if(!_MultiSelect && _CurrentBoxDrawing==EngineStaff.getInstance(_Context).getBoxDrawing(y+_Scene.getTop())) {
                        _ControllerPathDrawer.touchMove(x, y);
                        _TimeLastLongClick=0;
                        _LongClickActive=false;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if(_IsFling) {_IsFling=false; return false; }

                    //If no select any item and this if the first item to select
                    if(!_MultiSelect && _LongClickActive && (event.getEventTime()-_TimeLastLongClick)>=300) {
                        found = _ControllerPathDrawer.selectDraw(x,y + _Scene.getTop());
                        found_n = EngineStaff.getInstance(_Context).selectNote(x, y+_Scene.getTop());
                        if(found || found_n) {
                            _MultiSelect = true;
                            _ControllerPathDrawer.clearCurrent();
                            _CopyDrawsToBitmap = true;
                        }
                        _LongClickActive=false;
                        _TimeLastLongClick=0;
                    }

                    if(!_MultiSelect) {
                        _ControllerPathDrawer.touchUp(x, y, (_CurrentBoxDrawing == EngineStaff.getInstance(_Context).getBoxDrawing(y + _Scene.getTop())));
                    }

                    if(!_ControllerPathDrawer.hasSelectedDraws() && !EngineStaff.getInstance(_Context).hasNotesSelected()) {
                        _MultiSelect = false;
                    }

                    _CurrentBoxDrawing = -1;
                    _isDrawing = false;
                    _Listener.invalidateOptionsMenuFromView();
                    break;
            }
        }

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(StaffSurfaceView.class.getName(), "surfaceCreated()");
    }

    public void start()
    {
        if(!_GameLoopThread.isRunning()) {
            _GameLoopThread.setRunning(true);
            _GameLoopThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i(StaffSurfaceView.class.getName(), "surfaceDestroyed()");
        GestorDatabase.getInstance(_Context).deselectAllNotes(_IdSong);
    }

    public boolean hasDraws() {
        return _ControllerPathDrawer.hasDrawsInScene() ||  _ControllerPathDrawer.hasDrawsInCache() || EngineStaff.getInstance(_Context).hasNotesSelected();
    }

    public void stop()
    {
        _Scene.stop();
        _GameLoopThread.interrupt();
        if(_GameLoopThread.isRunning()) {
            boolean retry = true;
            _GameLoopThread.setRunning(false);
            while (retry) {
                try {
                    _GameLoopThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        getHolder().getSurface().release();
        getHolder().removeCallback(this);
    }


    public class TouchController
    {
        private int _PositionYViewDown = 0;

        private int _TopDisplayOriginAtDown = 0;

        public void down(MotionEvent event)
        {
            synchronized (this)
            {
                _PositionYViewDown = (int) event.getY();
                _TopDisplayOriginAtDown = _Scene.getTop();
            }
        }

        public boolean move(MotionEvent event)
        {
            synchronized (this) {
                float zoom = 0.7f;
                _Scene.updatePosition((int) ((float) _TopDisplayOriginAtDown - (zoom * (event.getY() - (float) _PositionYViewDown))));
                return true;
            }
        }
    }

}
