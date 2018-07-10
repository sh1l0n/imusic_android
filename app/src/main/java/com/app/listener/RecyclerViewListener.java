package com.app.listener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerViewListener implements RecyclerView.OnItemTouchListener
{
    private OnItemClickListener listener_;

    GestureDetector gestureDetector_;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public RecyclerViewListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener)
    {
        listener_= listener;
        gestureDetector_= new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (childView != null && listener_ != null) {
                    listener_.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e)
    {
        View childView= rv.findChildViewUnder(e.getX(), e.getY());

        if(childView!=null && listener_!=null && gestureDetector_.onTouchEvent(e))
            listener_.onItemClick(childView, rv.getChildAdapterPosition(childView));

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
