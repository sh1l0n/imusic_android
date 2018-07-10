package com.app.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.app.R;


public class HolderCardCreateSongDialog extends RecyclerView.ViewHolder
{
    public TextView _Name;

    public HolderCardCreateSongDialog(View itemView) {
        super(itemView);
        _Name = (TextView)itemView.findViewById(R.id.tv);
    }
}
