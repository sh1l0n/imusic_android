package com.app.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;

import com.app.R;
import com.app.dialog.DialogAddEditSong;
import com.app.listener.DialogListenerCallbacks;
import com.app.listener.ListenerAddToDabase;

import org.classifier.ClassifierTypeOfNote;
import org.database.GestorDatabase;
import org.database.model.Note;
import org.database.model.Song;
import org.drawreader.Utils;
import org.drawreader.controller.Draw;
import org.drawreader.model.EngineStaff;
import org.drawreader.listener.ListenerInvalidateMenu;
import org.drawreader.view.StaffSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.os.Handler;
import android.widget.Button;

public class StaffActivity extends AppCompatActivity implements DialogListenerCallbacks, ListenerAddToDabase, ListenerInvalidateMenu
{
    private Toolbar _Toolbar;

    private Song _Song;

    private SurfaceView _View;

    private ProgressDialog _ProgressBar;

    private Handler _ProgressBarbHandler = new Handler();

    private int _ProgressBarStatus = 0;

    public void backPressed() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {

        if(((StaffSurfaceView)_View).hasDraws()) {

            final AlertDialog builder = new AlertDialog.Builder(this, R.style.CreateDialog)
                    .setPositiveButton(R.string.create_dialog_accept, null)
                    .setNegativeButton(R.string.create_dialog_decline, null)
                    .create();
            builder.setMessage(getString(R.string.alert_exit_staff));

            builder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GestorDatabase.getInstance(getApplicationContext()).deselectAllNotes(_Song.getId());
                            backPressed();
                            builder.dismiss();
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
                                builder.dismiss();
                            }
                            return true;
                        }
                    });
                }
            });

            builder.show();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Song.class.getName(), _Song);
        outState.putInt(Integer.class.getName(), ((StaffSurfaceView)_View).getTop());
    }

    private void setupActionBar(String title)
    {
        if(_Toolbar==null) {
            _Toolbar = (Toolbar) findViewById(R.id.appbar);
            setSupportActionBar(_Toolbar);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setHideOnContentScrollEnabled(false);
            _Toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        else getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(StaffActivity.class.getName(), "onCreate()");

        setContentView(R.layout.activity_staff);
        _View = (StaffSurfaceView)findViewById(R.id.viewStaff);
        if(savedInstanceState==null) {
            _Song = getIntent().getParcelableExtra(Song.class.getName());
            ((StaffSurfaceView)_View).setStartPosition(0);
        }
        else {
            _Song = savedInstanceState.getParcelable(Song.class.getName());
            ((StaffSurfaceView)_View).setStartPosition(savedInstanceState.getInt(Integer.class.getName()));
        }

        ((StaffSurfaceView)_View).setIds(_Song.getId(), _Song.getClef(), _Song.getTime());
        ((StaffSurfaceView) _View).setListener(this);
        setupActionBar(_Song.getName());
    }

    public View getView() {
        return _View;
    }

    @Override
    protected void onStart() {
        Log.i(StaffActivity.class.getName(), "onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(StaffActivity.class.getName(), "onResume()");
        ((StaffSurfaceView)_View).start();
        _View.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(StaffActivity.class.getName(), "onPause()");
        ((StaffSurfaceView)_View).stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(StaffActivity.class.getName(), "onDestroy()");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(((StaffSurfaceView)_View).hasSelectedDraws()) {
            getMenuInflater().inflate(R.menu.menu_staff_select, menu);
        }
        else if(((StaffSurfaceView)_View).hasDraws()) {
            getMenuInflater().inflate(R.menu.menu_staff_editor, menu);
        }
        else {
            getMenuInflater().inflate(R.menu.menu_staff, menu);
            boolean act = GestorDatabase.getInstance(this).isFavorite(_Song.getId());
            if(act)  menu.findItem(R.id.action_fav).setIcon(R.drawable.ic_star_black_24dp);
            else  menu.findItem(R.id.action_fav).setIcon(R.drawable.ic_star_border_black_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_pdf:
                    Utils.generatePdf(Environment.getExternalStorageDirectory()+ File.separator+_Song.getName()+".pdf", this, _Song);
                break;
            case R.id.action_fav:
                boolean act = GestorDatabase.getInstance(this).isFavorite(_Song.getId());
                if(!act) {
                    GestorDatabase.getInstance(this).addFavorite(_Song.getId());
                    item.setIcon(R.drawable.ic_star_black_24dp);
                }
                else {
                    GestorDatabase.getInstance(this).removeFavorite(_Song.getId());
                    item.setIcon(R.drawable.ic_star_border_black_24dp);
                }
                break;
            case R.id.action_edit:
                    new DialogAddEditSong(this, true, _Song).show(getSupportFragmentManager(), DialogAddEditSong.class.getName());
                break;
            case R.id.action_sync:
                    addToDataBase(((StaffSurfaceView)_View).getDraws());
                break;
            case R.id.action_back:
                    ((StaffSurfaceView) _View).removeLastDraw();
                    ((StaffSurfaceView) _View).update();
                    invalidateOptionsMenu();
                break;

            case R.id.action_cancel:
                final AlertDialog builder = new AlertDialog.Builder(this, R.style.CreateDialog)
                        .setPositiveButton(R.string.create_dialog_accept, null)
                        .setNegativeButton(R.string.create_dialog_decline, null)
                        .create();

                builder.setTitle(getString(R.string.deselect_items));
                builder.setMessage(getString(R.string.deselect_items_message));

                builder.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button btnAccept = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                        btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((StaffSurfaceView)_View).deselectAll();
                                ((StaffSurfaceView) _View).update();
                                invalidateOptionsMenu();
                                builder.dismiss();
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
                                    builder.dismiss();
                                }
                                return true;
                            }
                        });
                    }
                });
                builder.show();

                break;
            case R.id.action_erase:
                final AlertDialog builder2 = new AlertDialog.Builder(this, R.style.CreateDialog)
                        .setPositiveButton(R.string.create_dialog_accept, null)
                        .setNegativeButton(R.string.create_dialog_decline, null)
                        .create();

                builder2.setTitle(getString(R.string.delete_select));
                builder2.setMessage(getString(R.string.delete_select_message));

                builder2.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button btnAccept = builder2.getButton(AlertDialog.BUTTON_POSITIVE);
                        btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((StaffSurfaceView)_View).clearSelectedDraws();
                                ((StaffSurfaceView) _View).update();
                                invalidateOptionsMenu();
                                builder2.dismiss();
                            }
                        });
                        final Button btnDecline = builder2.getButton(DialogInterface.BUTTON_NEGATIVE);
                        btnDecline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                builder2.dismiss();
                            }
                        });
                        builder2.setOnKeyListener(new DialogInterface.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface arg0, int keyCode,
                                                 KeyEvent event) {
                                // TODO Auto-generated method stub
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    builder2.dismiss();
                                }
                                return true;
                            }
                        });
                    }
                });

                builder2.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishEditDialog(String n) {
    }

    @Override
    public void onFinishNewSongDialog(Song s) {
        _Song = s;
        setupActionBar(_Song.getName());
        invalidateOptionsMenu();
        ((StaffSurfaceView) _View).setIds(_Song.getId(), _Song.getClef(), _Song.getTime());
        ((StaffSurfaceView) _View).update();
    }

    @Override
    public void delete(boolean delete, int position) {

    }

    public void  addToDataBase(final List<Draw> _lDraws)
    {
        if(_lDraws.isEmpty())return;
        _ProgressBar = new ProgressDialog(this);
        _ProgressBar.setCancelable(true);
        _ProgressBar.setMessage(getString(R.string.title_add_notes));
        _ProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        _ProgressBar.setProgress(0);
        _ProgressBar.setMax(100);
        _ProgressBar.show();
        _ProgressBarStatus = 0;
        final int sizeOfOneProgress = 100/_lDraws.size();

        new Thread(new Runnable() {
            public void run() {
                List<Note> lnotes = new ArrayList<>();
                int cat;
                int[] left_top;
                HashMap<Integer, String> categorys = GestorDatabase.getInstance(getApplicationContext()).getCategorys();
                Bitmap bmp;
                Rect r_save;
                Draw d;
                for(int i=0; i<_lDraws.size(); ++i)
                {
                    d = _lDraws.get(i);
                    cat = ClassifierTypeOfNote.getInstance(getApplicationContext()).getNameNote(d.getPointsLocal());
                    r_save = d.getBoundingBoxForSave();
                    left_top = EngineStaff.getInstance(getApplicationContext()).getProportionalLeftTopForSave(r_save.left, r_save.top, cat);
                    try {
                        bmp = BitmapFactory.decodeStream(getApplicationContext().getAssets().open("notes/" + categorys.get(cat)));
                        lnotes.add(new Note(_Song.getId(), cat, left_top[0], left_top[1], left_top[0]+bmp.getWidth(), left_top[1]+bmp.getHeight()));
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    _ProgressBarStatus+=sizeOfOneProgress;
                    _ProgressBarbHandler.post(new Runnable() {
                        public void run() {
                            _ProgressBar.setProgress(_ProgressBarStatus);
                        }
                    });
                }
                GestorDatabase.getInstance(getApplicationContext()).addNotes(lnotes);
                notesAddedToDatabase();
                _ProgressBar.dismiss();
            }
        }).start();


    }

    @Override
    public void notesAddedToDatabase() {
        ((StaffSurfaceView) _View).clearDraws();
        ((StaffSurfaceView) _View).update();
        invalidateOptionsMenu();
    }

    @Override
    public void invalidateOptionsMenuFromView() {
        invalidateOptionsMenu();
    }
}
