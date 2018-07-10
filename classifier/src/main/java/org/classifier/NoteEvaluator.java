package org.classifier;

import android.graphics.Point;
import java.util.Vector;

/**
 * Class NoteEvaluator
 * Note for evalue data
 */
public class NoteEvaluator {

    /** name of the note */
    private int _Name;

    /** features of the note */
    private Vector<Point> _Features;

    /**
     * Builder
     * @param data data for initialize
     */
    public NoteEvaluator(String data) {
        String x = "", y = "";
        Boolean coord = false;
        String name = "";
        int its = 0;
        while (data.charAt(its) != ' ') {
            name += data.charAt(its);
            its++;
        }
        _Name = Integer.parseInt(name);
        _Features = new Vector<>();

        while (data.charAt(its) == ' ')
            its++;

        while (its < data.length()) {
            if (!coord) {
                if (data.charAt(its) != ',')
                    x += data.charAt(its);
                else
                    coord = true;
            } else {
                if (data.charAt(its) != ';')
                    y += data.charAt(its);
                else {
                    coord = false;
                    int aux_x = Integer.parseInt(x);
                    int aux_y = Integer.parseInt(y);
                    _Features.add(new Point(aux_x, aux_y));
                    x = y = "";
                }
            }
            ++its;
        }
    }

    /**
     * Builder
     * @param f vector of poitns
     */
    public NoteEvaluator(Vector<Point> f) {
        _Name = -1;
        _Features = new Vector<>(f);
    }

    /**
     * Getter
     * @return points features
     */
    public Vector<Point> getFeatures() {
        return _Features;
    }

    /**
     * Getter
     * @return the name
     */
    public int getName() {
        return _Name;
    }
}