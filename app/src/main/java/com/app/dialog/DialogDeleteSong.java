package com.app.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.View;
import android.widget.Button;

import com.app.R;
import com.app.listener.DialogListenerCallbacks;

import org.database.model.Song;


public class DialogDeleteSong extends DialogFragment{

    private DialogListenerCallbacks _Listener;

    private int _Position = -1;

    private String _Name = "";

    private final static String TAG_FAVS = "FAVS";

    private boolean _Favorites;

    private boolean _MultipleDelete;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Boolean.class.getName(), _MultipleDelete);
        outState.putInt(Integer.class.getName(), _Position);
        outState.putString(String.class.getName(), _Name);
        outState.putBoolean(DialogDeleteSong.TAG_FAVS, _Favorites);
    }

    public DialogDeleteSong(DialogListenerCallbacks ls, boolean multiple, int pos, String name, boolean favs) {
        _Listener = ls;
        _MultipleDelete = multiple;
        _Position = pos;
        _Name = name;
        _Favorites = favs;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.CreateDialog)
                .setPositiveButton(R.string.create_dialog_accept, null)
                .setNegativeButton(R.string.create_dialog_decline, null)
                .create();


        if(savedInstanceState!=null) {
            _MultipleDelete = savedInstanceState.getBoolean(Boolean.class.getName());
            _Position = savedInstanceState.getInt(Integer.class.getName());
            _Name = savedInstanceState.getString(String.class.getName());
            _Favorites = savedInstanceState.getBoolean(DialogDeleteSong.TAG_FAVS);
        }
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.delete);

        if(_MultipleDelete) {

            if(_Favorites) builder.setMessage(getActivity().getResources().getString(R.string.sure_delete_mult_fav));
            else builder.setMessage(getActivity().getResources().getString(R.string.sure_delete_mult));
        }
        else {
            String header = getActivity().getResources().getString(R.string.sure_delete);
            String end;
            if(_Favorites)  end = getActivity().getResources().getString(R.string.sure_delete_fav);
            else  end = getActivity().getResources().getString(R.string.sure_delete_one);
            builder.setMessage( Html.fromHtml(header + " <b>" + _Name + "</b> " + end));
        }

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog)
            {
                final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            _Listener.delete(true, _Position);
                            builder.dismiss();
                        }
                    });

                final Button btnDecline = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _Listener.delete(false, _Position);
                        builder.dismiss();
                    }
                });
            }
        });
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
