package com.app.fragment;

import android.support.v4.app.Fragment;

import com.app.listener.ItemCardViewClickListener;
import com.app.listener.DialogListenerCallbacks;

import org.database.model.Song;

import java.util.List;


public abstract class FragmentBase extends Fragment  implements ItemCardViewClickListener, DialogListenerCallbacks {
    public abstract boolean clearFragmentState();
    public abstract void updateFragmentDataAdapter(List<Song> lsong, List<Integer> lact);
}
