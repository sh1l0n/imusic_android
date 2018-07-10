package com.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.app.R;
import com.app.adapter.AdapterRV;
import com.app.dialog.DialogAddEditSong;
import com.app.dialog.DialogDeleteSong;
import com.app.dialog.DialogEditAutor;
import com.app.listener.DialogListenerCallbacks;
import com.app.listener.OnClickListenerLaunchActivity;
import com.app.listener.RecyclerViewListener;
import org.database.GestorDatabase;
import org.database.model.Song;
import org.drawreader.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class PageFragmentSongs extends FragmentBase
{
    private RecyclerView _RecyclerView;
    private FloatingActionButton _FloatingActionButton;
    private AdapterRV _Adapter;
    private boolean _IsLongClick = false;
    private View _View;
    private OnClickListenerLaunchActivity _Listener;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(PageFragmentSongs.class.getName(), "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        getArguments().putBoolean(Boolean.class.getName(),_IsLongClick);
        getArguments().putParcelableArrayList(List.class.getName(), (ArrayList<Song>)_Adapter.getData());
        getArguments().putIntegerArrayList(HashSet.class.getName(), (ArrayList<Integer>) _Adapter.getActivatedElements());
        getArguments().putInt(Integer.class.getName(), _Adapter.getPositionCLickItemMenuOverflow());
    }

    @Override
    public boolean clearFragmentState() {

        getArguments().putInt(Integer.class.getName(), -1);
        if(_IsLongClick) {
            _Adapter.clearActivated();
            _Adapter.notifyDataSetChanged();
            _IsLongClick = false;
            getActivity().invalidateOptionsMenu();
            return false;
        }
        return true;
    }


    public static FragmentBase newInstance(OnClickListenerLaunchActivity l) {
        Log.d(PageFragmentSongs.class.getName(), "newInstance");
        Bundle args = new Bundle();
        args.putBoolean(Boolean.class.getName(), false);
        args.putParcelableArrayList(List.class.getName(), null);
        args.putIntegerArrayList(HashSet.class.getName(), null);
        args.putInt(Integer.class.getName(), -1);
        FragmentBase fragment = new PageFragmentSongs();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(PageFragmentSongs.class.getName(), "onAttach()");
        try {
            _Listener = (OnClickListenerLaunchActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(PageFragmentSongs.class.getName(), "onCreate()");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(PageFragmentSongs.class.getName(), "onCreateView()");
        setHasOptionsMenu(true);
        _View = View.inflate(getActivity(), R.layout.fragment_pager_songs, null);
        return _View;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(PageFragmentSongs.class.getName(), "onActivityCread()");
        _Adapter = new AdapterRV(getActivity(), this);

        _RecyclerView = (RecyclerView) _View.findViewById(R.id.rv);
        _RecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _RecyclerView.setHasFixedSize(true);
        _RecyclerView.setAdapter(_Adapter);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        itemAnimator.setMoveDuration(1000);

        _RecyclerView.setItemAnimator(itemAnimator);
        _RecyclerView.addOnItemTouchListener(
                new RecyclerViewListener(getActivity(), _RecyclerView, new RecyclerViewListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        _Adapter.activateElement(position);
                        _Adapter.notifyItemChanged(position);

                        if (!_IsLongClick) {
                            _IsLongClick = true;
                            getActivity().invalidateOptionsMenu();
                        } else {
                            if (_Adapter.isAnyActivated()) {
                                _IsLongClick = false;
                                getActivity().invalidateOptionsMenu();
                            }
                        }

                        _Adapter.notifyDataSetChanged();
                        updateFragmentDataAdapter(null, null);
                    }
                }));

        _FloatingActionButton = (FloatingActionButton) _View.findViewById(R.id.fab);
        final DialogListenerCallbacks l = this;
        _FloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                if (!sp.contains(getString(R.string.preference_autor)) || sp.getString(getString(R.string.preference_autor), "").equals(""))
                    new DialogEditAutor(l).show(getFragmentManager(), DialogEditAutor.class.getName());
                else
                    new DialogAddEditSong(l, false, null).show(getFragmentManager(), DialogAddEditSong.class.getName());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(PageFragmentSongs.class.getName(), "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        _IsLongClick = getArguments().getBoolean(Boolean.class.getName());
        List<Integer> activated = getArguments().getIntegerArrayList(HashSet.class.getName());
        List<Song> lsong = getArguments().getParcelableArrayList(List.class.getName());
        updateFragmentDataAdapter(lsong, activated);
        Log.d(PageFragmentSongs.class.getName(), "onResume() : item: " + _Adapter.getPositionCLickItemMenuOverflow());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(PageFragmentSongs.class.getName(), "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(PageFragmentSongs.class.getName(), "onStop()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(PageFragmentSongs.class.getName(), "onDestroy()");
    }

    @Override
    public void updateFragmentDataAdapter(List<Song> lsong, List<Integer> lact) {
        if (lsong == null) lsong = GestorDatabase.getInstance(getActivity()).getAllSongs();
        if (lact == null) lact = new ArrayList<>();
        _Adapter.setIsClickItemMenuOverflow(getArguments().getInt(Integer.class.getName()));
        _Adapter.setData(lsong);
        _Adapter.activateElements(lact);
        _Adapter.notifyDataSetChanged();
        TextView tv = (TextView)_View.findViewById(R.id.tv_info);
        if(tv!=null) {
            if (_Adapter.isEmpty()) tv.setVisibility(View.VISIBLE);
            else tv.setVisibility(View.GONE);
        }
        _Adapter.notifyDataSetChanged();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(!_IsLongClick) inflater.inflate(com.app.R.menu.menu_main, menu);
        if(_IsLongClick) inflater.inflate(com.app.R.menu.menu_main_delete, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_about) {
            final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.CreateDialog)
                    .create();
            builder.setTitle(getString(R.string.action_about_title));
            builder.setMessage(getString(R.string.action_about_message));
            builder.show();
        }
        else if(item.getItemId() == R.id.action_autor) new DialogEditAutor(this).show(getFragmentManager(),DialogEditAutor.class.getName());
        else if(item.getItemId() == R.id.action_erase) {
            if(_Adapter.getActivatedElements().size()>1)
                new DialogDeleteSong(this, true, -1, "", false).show(getFragmentManager(), DialogDeleteSong.class.getName());
            else {
                int pos = _Adapter.getActivatedElements().get(0);
                new DialogDeleteSong(this, false, pos, _Adapter.getItem(pos).getName(), false).show(getFragmentManager(), DialogDeleteSong.class.getName());
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickItemCardOptions(View v, final int position) {
        if(_IsLongClick) return;
        _Adapter.setIsClickItemMenuOverflow(position);
        final PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_card, popup.getMenu());

        final boolean fav = GestorDatabase.getInstance(getActivity()).isFavorite(_Adapter.getItem(position).getId());
        if(!fav) popup.getMenu().findItem(R.id.action_fav).setTitle(getString(R.string.action_add_fav));
        else popup.getMenu().findItem(R.id.action_fav).setTitle(getString(R.string.action_rm_fav));



        final DialogListenerCallbacks l = this;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.action_edit:
                        _Adapter.setIsClickItemMenuOverflow(-1);
                        new DialogAddEditSong(l, true, _Adapter.getItem(position)).show(getFragmentManager(), DialogAddEditSong.class.getName());
                        break;
                    case R.id.action_pdf:
                        _Adapter.setIsClickItemMenuOverflow(-1);
                        Utils.generatePdf(Environment.getExternalStorageDirectory()+ File.separator+_Adapter.getItem(position).getName()+".pdf", getActivity(), _Adapter.getItem(position));
                        break;
                    case R.id.action_erase:
                        _Adapter.setIsClickItemMenuOverflow(-1);
                        new DialogDeleteSong(l, _Adapter.getActivatedElements().size()>1, position, _Adapter.getItem(position).getName(), false).show(getFragmentManager(), DialogDeleteSong.class.getName());
                        break;
                    case R.id.action_fav:
                        _Adapter.setIsClickItemMenuOverflow(-1);
                         if(!fav) GestorDatabase.getInstance(getActivity()).addFavorite(_Adapter.getItem(position).getId());
                         else GestorDatabase.getInstance(getActivity()).removeFavorite(_Adapter.getItem(position).getId());
                        _Listener.updateFragments();
                        break;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onClickItemCard(View v, int position) {

        if(_IsLongClick) {
            _Adapter.activateElement(position);
            _Adapter.notifyItemChanged(position);

            if(_Adapter.isAnyActivated()) {
                _IsLongClick=false;
                getActivity().invalidateOptionsMenu();
            }
            _Adapter.notifyDataSetChanged();
        }
        else _Listener.launchActivity(_Adapter.getItem(position));
    }

    @Override
    public void onFinishEditDialog(String n) {
        clearFragmentState(); _Listener.updateFragments();
    }

    @Override
    public void onFinishNewSongDialog(Song s) {
        clearFragmentState();
        updateFragmentDataAdapter(null, null);
        //_Listener.launchActivity(s);
    }

    @Override
    public void delete(boolean delete, int position) {

        if(delete) {
            if(_Adapter.getActivatedElements().size()>1) {
                List<Integer> ids = _Adapter.removeElementsActivated();
                GestorDatabase.getInstance(getActivity()).removeSongs(ids);
                _IsLongClick = false;
                getActivity().invalidateOptionsMenu();
            }
            else GestorDatabase.getInstance(getActivity()).removeSong(_Adapter.getItem(position).getId());
            clearFragmentState();
            _Listener.updateFragments();
        }

    }
}
