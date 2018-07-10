package com.app.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.R;


public class HolderRV extends RecyclerView.ViewHolder
{
    public ImageView _Icon;
    public TextView _Name;
    public TextView _Autor;
    public ImageButton _Share;

    public HolderRV(View itemView) {
        super(itemView);
        _Icon = (ImageView)itemView.findViewById(R.id.ivImageIcon);
        _Name = (TextView)itemView.findViewById(R.id.tvtName);
        _Autor = (TextView)itemView.findViewById(R.id.tvAutor);
        _Share = (ImageButton) itemView.findViewById(R.id.ivImageShare);
    }
}
