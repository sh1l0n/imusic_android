package com.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.R;
import com.app.fragment.FragmentBase;
import com.app.listener.DialogListenerCallbacks;

public class DialogEditAutor extends DialogFragment
{
    private DialogListenerCallbacks _Listener;
    private EditText etNickName;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(String.class.getName(),  etNickName.getText().toString());
    }

    public DialogEditAutor(DialogListenerCallbacks l) {
        _Listener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.CreateDialog)
                .setPositiveButton(R.string.create_dialog_accept, null)
                .setNegativeButton(R.string.create_dialog_decline, null)
                .create();

        etNickName  = new EditText(getActivity());
        etNickName.setTextColor(Color.BLACK);
        etNickName.setSoundEffectsEnabled(true);
        if(savedInstanceState!=null) etNickName.setText(savedInstanceState.getString(String.class.getName(),""));
        builder.setView(etNickName);
        builder.setTitle(R.string.change_autor_title);
        builder.setMessage(getActivity().getResources().getString(R.string.change_autor_intro));

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(etNickName.getText().toString().isEmpty())
                            Snackbar.make(((FragmentBase)_Listener).getView(), R.string.no_empty_name, Snackbar.LENGTH_SHORT).show();
                        else {
                            getActivity().getSharedPreferences(
                                    getActivity().getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                                    .edit()
                                    .putString(getActivity().getString(R.string.preference_autor), etNickName.getText().toString())
                                    .apply();
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

                builder.setOnKeyListener(new DialogInterface.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            etNickName.setText("");
                            builder.dismiss();
                        }
                        return true;
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
