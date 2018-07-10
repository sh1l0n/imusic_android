package com.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.app.listener.ItemCardViewClickListener;
import com.app.holder.HolderRV;
import com.app.R;

import org.database.model.Song;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class AdapterRV extends RecyclerView.Adapter<HolderRV>
{
    private List<Song> _Data;

    private HashSet<Integer> _ElemetsActivated;

    private Context _Context;

    private int _PositionMenuOverflowClick = -1;

    private ItemCardViewClickListener _ClickListener;

    public AdapterRV(Context c, ItemCardViewClickListener icl) {
        _ElemetsActivated = new HashSet<>();
        _Data = new ArrayList<>();
        _Context = c;
        _ClickListener = icl;
    }

    public List<Integer> removeElementsActivated() {
        List<Integer> idx = new ArrayList<>();
        for(Integer i : _ElemetsActivated) {
            _Data.remove(i);
            idx.add(_Data.get(i).getId());
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, _Data.size());
        }
        _ElemetsActivated.clear();
        return idx;
    }

    public void clearActivated() { _ElemetsActivated.clear(); _PositionMenuOverflowClick=-1;}
    public void activateElement(int pos) {
        if(_ElemetsActivated.contains(pos)) _ElemetsActivated.remove(pos);
        else _ElemetsActivated.add(pos);
    }
    public void activateElements(List<Integer> pos) { _ElemetsActivated.addAll(pos);}
    public List<Integer> getActivatedElements() { return new ArrayList<>(_ElemetsActivated);}

    public void setData(List<Song> s) {
        _Data = new ArrayList<>(s);
    }

    public boolean isEmpty() { return _Data.isEmpty();}

    public List<Song> getData() {
        return _Data;
    }

    public Song getItem(int position) {
        if(position>=0 && position<_Data.size())
            return _Data.get(position);
        return null;
    }

    public boolean isAnyActivated() { return _ElemetsActivated.isEmpty();}

    @Override
    public HolderRV onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HolderRV(LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(final HolderRV holder, final int position) {

        holder.itemView.setSoundEffectsEnabled(true);
        holder.itemView.playSoundEffect(android.view.SoundEffectConstants.CLICK);
        holder.itemView.setFocusable(true);
        holder.itemView.setClickable(true);
        holder._Name.setText(_Data.get(position).getName());
        holder._Autor.setText("Autor: " +  _Data.get(position).getAutor());

        if(_ElemetsActivated.isEmpty()) holder._Share.setVisibility(View.VISIBLE);
        else holder._Share.setVisibility(View.GONE);

        if(_ElemetsActivated.contains(position)) {
            holder.itemView.setSelected(true);
            ((CardView)holder.itemView).setCardBackgroundColor(Color.LTGRAY);
        }
        else {
            ((CardView)holder.itemView).setCardBackgroundColor(Color.WHITE);
            holder._Share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _ClickListener.onClickItemCardOptions(holder._Share, position);
                }
            });
        }

        if(position==_PositionMenuOverflowClick) holder._Share.callOnClick();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _ClickListener.onClickItemCard(holder.itemView, position);
            }
        });
    }

    public void setIsClickItemMenuOverflow(int item) {
        _PositionMenuOverflowClick = item;
    }

    public int getPositionCLickItemMenuOverflow() {
        return _PositionMenuOverflowClick;
    }

    @Override
    public int getItemCount() {
        return _Data.size();
    }
}
