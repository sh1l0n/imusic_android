package com.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.app.R;
import com.app.holder.HolderCardCreateSongDialog;

import java.util.ArrayList;
import java.util.List;


public class AdapterRVDialogNewSong extends RecyclerView.Adapter<HolderCardCreateSongDialog>
{
    private List<CardTextRadioButton> _Data;

    private Context _Context;

    private int _LastPosition;

    public AdapterRVDialogNewSong(Context c) {
        _Data = new ArrayList<>();
        _Context = c;
        _LastPosition = -1;
    }

    public void setCurrentChecked(int pos) {
        _LastPosition = pos;
        _Data.get(_LastPosition).setisChecked(true);
    }


    public void setData(List<CardTextRadioButton> s) {
        _Data = new ArrayList<>(s);
    }

    public boolean isEmpty() { return _Data.isEmpty();}

    public List<CardTextRadioButton> getData() {
        return _Data;
    }

    public CardTextRadioButton getItem(int position) {
        if(position>=0 && position<_Data.size())
            return _Data.get(position);
        return null;
    }

    public int getCurrentCheked() {return _LastPosition;}


    @Override
    public HolderCardCreateSongDialog onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HolderCardCreateSongDialog(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_dialog_addsong, parent, false));
    }

    @Override
    public void onBindViewHolder(final HolderCardCreateSongDialog holder, final int position) {

        holder.itemView.setSoundEffectsEnabled(true);
        holder.itemView.playSoundEffect(android.view.SoundEffectConstants.CLICK);
        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);

        holder._Name.setText(_Data.get(position).getName());

        if(_Data.get(position)._isChecked) holder.itemView.setBackgroundColor(Color.LTGRAY);
        else holder.itemView.setBackgroundColor(Color.WHITE);

        holder._Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(_LastPosition!=position)
                {
                    _Data.get(_LastPosition).setisChecked(false);
                    _LastPosition = position;
                    _Data.get(position).setisChecked(true);
                    notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return _Data.size();
    }
}
