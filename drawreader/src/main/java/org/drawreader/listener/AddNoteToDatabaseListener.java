package org.drawreader.listener;

import org.drawreader.controller.Draw;

import java.util.List;

public interface AddNoteToDatabaseListener {
    void addToDatabase(final int top_scene, List<Draw> l);
}
