package com.app.listener;

import org.database.model.Song;

public interface DialogListenerCallbacks {
    void onFinishEditDialog(String n);
    void onFinishNewSongDialog(Song s);
    void delete(boolean delete, int position);
}