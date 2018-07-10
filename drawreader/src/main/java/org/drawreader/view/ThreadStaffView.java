package org.drawreader.view;

import android.util.Log;

public class ThreadStaffView extends Thread
{
    private StaffSurfaceView _View;

    private boolean _Running;

    public ThreadStaffView(StaffSurfaceView v) {
        setName(StaffSurfaceView.class.getName());
        _View = v;
        _Running=false;
    }

    public void setRunning(boolean b) {
        _Running = b;
    }

    public boolean isRunning() { return _Running;}

    @Override
    public void run()
    {
        _View.onDraw(null);
        while(_Running) {

            if(_Running) _View.onDraw(null);

        }
        _View.clearFocus();
    }
}
