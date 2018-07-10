package com.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.app.R;
import com.app.adapter.AdapterViewPager;
import com.app.dialog.DialogEditAutor;
import com.app.fragment.FragmentBase;
import com.app.fragment.PageFragmentFavs;
import com.app.fragment.PageFragmentSongs;
import com.app.listener.DialogListenerCallbacks;
import com.app.listener.OnClickListenerLaunchActivity;

import org.classifier.ClassifierTypeOfNote;
import org.database.GestorDatabase;
import org.database.model.Song;

public class MainActivity extends AppCompatActivity implements OnClickListenerLaunchActivity, DialogListenerCallbacks
{
    private Toolbar _Toolbar;
    private TabLayout _TabLayout;
    private ViewPager _ViewPager;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(MainActivity.class.getName(), "onSaveInstanceActivity");
        super.onSaveInstanceState(outState);
        outState.putInt(Integer.class.getName(), ((AdapterViewPager)_ViewPager.getAdapter()).getCurrentNumberOfFragment());

        getSupportFragmentManager().putFragment(outState, PageFragmentSongs.class.getName(), ((AdapterViewPager)_ViewPager.getAdapter()).getItem(0));
        getSupportFragmentManager().putFragment(outState, PageFragmentFavs.class.getName(), ((AdapterViewPager)_ViewPager.getAdapter()).getItem(1));
    }

    private void setupActionBar()
    {
        if(_Toolbar==null) {
            _Toolbar = (Toolbar) findViewById(R.id.appbar);
            setSupportActionBar(_Toolbar);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupActionBar();
        Log.d(FragmentBase.class.getName(), "MainActivity onCreate() number of fragments: " + getSupportFragmentManager().getBackStackEntryCount());
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(!sp.contains(getString(R.string.preference_autor)) || sp.getString(getString(R.string.preference_autor),"").equals(""))
            new DialogEditAutor(this).show(getSupportFragmentManager(),DialogEditAutor.class.getName());

        new AsyncTaskCreateDDBB().execute();
        _ViewPager = (ViewPager) findViewById(R.id.viewpager);
        AdapterViewPager adapter = new AdapterViewPager(getSupportFragmentManager());

        if(savedInstanceState==null) {
            adapter.addFragment(PageFragmentSongs.newInstance(this), getString(R.string.tab1));
            adapter.addFragment(PageFragmentFavs.newInstance(this), getString(R.string.tab2));
        }
        else
        {
            adapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState, PageFragmentSongs.class.getName()), getString(R.string.tab1));
            adapter.addFragment(getSupportFragmentManager().getFragment(savedInstanceState, PageFragmentFavs.class.getName()), getString(R.string.tab2));
        }
        _ViewPager.setOffscreenPageLimit(2);
        _ViewPager.setAdapter(adapter);
        _ViewPager.setOffscreenPageLimit(adapter.getCount());
        _ViewPager.addOnPageChangeListener(adapter);
        if(savedInstanceState!=null) ((AdapterViewPager)_ViewPager.getAdapter()).setCurrentFragment(savedInstanceState.getInt(Integer.class.getName()));
        _TabLayout = (TabLayout) findViewById(R.id.tabs);
        _ViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(_TabLayout));
        _TabLayout.setupWithViewPager(_ViewPager);

        Log.d(FragmentBase.class.getName(), "MainActivity onCreate() number of fragments: " + adapter.getCount());
    }

    @Override
    protected void onRestart() {
        Log.d(FragmentBase.class.getName(), "MainActivity onRestart()");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(FragmentBase.class.getName(), "MainActivity onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(FragmentBase.class.getName(), "MainActivity onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(FragmentBase.class.getName(), "MainActivity onPause()");

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(FragmentBase.class.getName(), "MainActivity onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(FragmentBase.class.getName(), "MainActivity onDestroy()");super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        boolean clear = ((FragmentBase)((AdapterViewPager)_ViewPager.getAdapter()).getCurrentFragment()).clearFragmentState();
        if(clear) { super.onBackPressed(); }
    }

    @Override
    public void launchActivity(Song s) {
        Intent intent = new Intent(this, StaffActivity.class);
        intent.putExtra(Song.class.getName(), s);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    @Override
    public void updateFragments() {
        ((AdapterViewPager)_ViewPager.getAdapter()).updateDataFragments();
    }

    @Override
    public void onFinishEditDialog(String n) {}

    @Override
    public void onFinishNewSongDialog(Song s) {}

    @Override
    public void delete(boolean delete, int position) {
    }

    class AsyncTaskCreateDDBB extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            GestorDatabase.getInstance(getApplicationContext());
            ClassifierTypeOfNote.getInstance(getApplicationContext());
            return null;
        }
    }
}