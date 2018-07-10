package com.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.app.R;
import com.app.activity.StaffActivity;
import com.app.adapter.AdapterRVDialogNewSong;
import com.app.adapter.CardTextRadioButton;
import com.app.fragment.FragmentBase;
import com.app.listener.DialogListenerCallbacks;

import org.database.GestorDatabase;
import org.database.model.Song;

import java.util.ArrayList;
import java.util.List;

public class DialogAddEditSong extends DialogFragment
{
    private DialogListenerCallbacks _Listener;

    private EditText etNickName;

    private RecyclerView rvClefs;

    private RecyclerView rvTimes;

    private Song _Song = null;

    private boolean _Edit;


    public static final String TAG_CLEFS = "CLEFS";
    public static final String TAG_TIMES = "TIMES";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Boolean.class.getName(), _Edit);
        outState.putParcelable(Song.class.getName(), _Song);
        outState.putString(String.class.getName(), etNickName.getText().toString());
        outState.putInt(Integer.class.getName()+TAG_CLEFS, ((AdapterRVDialogNewSong)rvClefs.getAdapter()).getCurrentCheked());
        outState.putInt(Integer.class.getName()+TAG_TIMES, ((AdapterRVDialogNewSong)rvTimes.getAdapter()).getCurrentCheked());
        outState.putParcelableArrayList(TAG_CLEFS, (ArrayList<CardTextRadioButton>) ((AdapterRVDialogNewSong)rvClefs.getAdapter()).getData());
        outState.putParcelableArrayList(TAG_TIMES, (ArrayList<CardTextRadioButton>) ((AdapterRVDialogNewSong)rvTimes.getAdapter()).getData());
    }

    public DialogAddEditSong(DialogListenerCallbacks ls, boolean edit, Song s)
    {
        _Listener = ls;
        _Edit = edit;
        _Song = s;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.CreateDialog)
                .setPositiveButton(R.string.create_dialog_accept, null)
                .setNegativeButton(R.string.create_dialog_decline, null)
                .create();

        View v = builder.getLayoutInflater().inflate(R.layout.dialog_add_song, null);
        etNickName = (EditText) v.findViewById(R.id.etName);
        rvClefs = (RecyclerView)v.findViewById(R.id.rvClefs);
        rvTimes = (RecyclerView)v.findViewById(R.id.rvTimes);
        rvClefs.setAdapter(new AdapterRVDialogNewSong(getActivity()));
        rvTimes.setAdapter(new AdapterRVDialogNewSong(getActivity()));
        rvClefs.setLayoutManager(new LinearLayoutManager(getActivity(), GridLayoutManager.HORIZONTAL, false));
        rvTimes.setLayoutManager(new LinearLayoutManager(getActivity(), GridLayoutManager.HORIZONTAL, false));
        rvClefs.setHasFixedSize(true);
        rvTimes.setHasFixedSize(true);

        if(savedInstanceState==null) {
            List<CardTextRadioButton> data = new ArrayList<>();
            data.add(new CardTextRadioButton("G-Clef"));
            data.add(new CardTextRadioButton("F-Clef"));
            data.add(new CardTextRadioButton("C-Clef"));
            ((AdapterRVDialogNewSong)rvClefs.getAdapter()).setData(data);
            data.clear();
            data.add(new CardTextRadioButton("2/2"));
            data.add(new CardTextRadioButton("2/4"));
            data.add(new CardTextRadioButton("3/4"));
            data.add(new CardTextRadioButton("4/4"));
            data.add(new CardTextRadioButton("3/8"));
            data.add(new CardTextRadioButton("6/8"));
            data.add(new CardTextRadioButton("9/8"));
            data.add(new CardTextRadioButton("12/8"));
            data.add(new CardTextRadioButton("C Time"));
            data.add(new CardTextRadioButton("C Cut Time"));
            ((AdapterRVDialogNewSong)rvTimes.getAdapter()).setData(data);
            if(!_Edit) {
                ((AdapterRVDialogNewSong)rvClefs.getAdapter()).setCurrentChecked(0);
                ((AdapterRVDialogNewSong)rvTimes.getAdapter()).setCurrentChecked(0);
            }
            else {
                etNickName.setText(_Song.getName());
                ((AdapterRVDialogNewSong) rvClefs.getAdapter()).setCurrentChecked(_Song.getClef());
                ((AdapterRVDialogNewSong) rvTimes.getAdapter()).setCurrentChecked(_Song.getTime());
            }

        }
        else
        {
            _Edit = savedInstanceState.getBoolean(Boolean.class.getName());
            _Song = savedInstanceState.getParcelable(Song.class.getName());
            etNickName.setText(savedInstanceState.getString(String.class.getName()));
            List<CardTextRadioButton> data = savedInstanceState.getParcelableArrayList(TAG_CLEFS);
            ((AdapterRVDialogNewSong)rvClefs.getAdapter()).setData(data);
            data = savedInstanceState.getParcelableArrayList(TAG_TIMES);
            ((AdapterRVDialogNewSong)rvTimes.getAdapter()).setData(data);
            ((AdapterRVDialogNewSong)rvClefs.getAdapter()).setCurrentChecked(savedInstanceState.getInt(Integer.class.getName()+TAG_CLEFS));
            ((AdapterRVDialogNewSong)rvTimes.getAdapter()).setCurrentChecked(savedInstanceState.getInt(Integer.class.getName()+TAG_TIMES));
        }

        ((AdapterRVDialogNewSong)rvClefs.getAdapter()).notifyDataSetChanged();
        ((AdapterRVDialogNewSong)rvTimes.getAdapter()).notifyDataSetChanged();

        if(_Edit) builder.setTitle(R.string.title_edit_song);
        else builder.setTitle(R.string.title_new_song);

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etNickName.getText().toString().isEmpty()) {

                            if(!_Edit)
                                Snackbar.make(((FragmentBase) _Listener).getView(), R.string.no_empty_name, Snackbar.LENGTH_SHORT).show();
                            else
                                etNickName.setText(_Song.getName());
                        }

                        if (!etNickName.getText().toString().isEmpty()) {
                            String autor = getActivity().getSharedPreferences(getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE).getString(getActivity().getString(R.string.preference_autor),"");


                            if(!_Edit) {
                                _Song = new Song(etNickName.getText().toString(), autor, ((AdapterRVDialogNewSong)rvClefs.getAdapter()).getCurrentCheked(), ((AdapterRVDialogNewSong)rvTimes.getAdapter()).getCurrentCheked());
                                _Song.setId((int) GestorDatabase.getInstance(getActivity()).insertSong(_Song));
                            }
                            else {
                                _Song.setName(etNickName.getText().toString());
                                _Song.setClef(((AdapterRVDialogNewSong)rvClefs.getAdapter()).getCurrentCheked());
                                _Song.setTime(((AdapterRVDialogNewSong)rvTimes.getAdapter()).getCurrentCheked());
                                GestorDatabase.getInstance(getActivity()).updateSong(_Song);
                            }
                            _Listener.onFinishNewSongDialog(_Song);
                            builder.dismiss();
                        }
                    }
                });

                final Button btnDecline = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });
            }
        });

        builder.setView(v);
        return builder;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
