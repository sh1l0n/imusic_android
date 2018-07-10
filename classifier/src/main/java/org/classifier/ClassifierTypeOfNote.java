package org.classifier;


import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

/**
 * Singleton class ClassifierTypeOfNote
 */
public class ClassifierTypeOfNote
{
    private static ClassifierTypeOfNote _Instance;

    /** DataSet of notes */
    private List<NoteEvaluator> _Dataset;

    /** Number of neighbours */
    private final int K=6;

    /**
     * Get the instance
     * @param c the context
     * @return instance
     */
    public static ClassifierTypeOfNote getInstance(Context c)
    {
        if(_Instance==null) _Instance = new ClassifierTypeOfNote(c);
        return _Instance;
    }

    /**
     * Builder
     * @param context the context of the app
     */
    private ClassifierTypeOfNote(Context context) {
        _Dataset = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("train_set.pts")));
            String line;

            do {
                line = reader.readLine();
                if(line!=null) _Dataset.add(new NoteEvaluator(line));
            }while(line!=null);

            Log.d(ClassifierTypeOfNote.class.getName(),"Size dataset: " + _Dataset.size());

        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Get the name of a note passed only a vector of points
     * @param test test
     * @return name of note
     */
    public int getNameNote(Vector<Point> test)
    {
        PriorityQueue<ItemRank> pq = new PriorityQueue<>();
        for(NoteEvaluator n :  _Dataset) pq.offer(new ItemRank(n.getName(), DTWDistance(n.getFeatures(), test)));

        HashMap<Integer, Float> items= new HashMap<>();
        int n=K, expected=-1;
        float min=0.0f, dist,  distanceTotal=0.0f;

        while(n!=0)
        {
            ItemRank ir = pq.poll();
            if (items.containsKey(ir.name)) min = items.get(ir.name);
            else min = 0.0f;
            dist= ((1.0f)/ir.distance);
            distanceTotal+= dist;
            min+= dist;
            items.put(ir.name, min);
            --n;
        }

        for(Map.Entry<Integer,Float> entry : items.entrySet())
        {
            dist = (entry.getValue() / distanceTotal);
            if(dist > min) {
                min= dist;
                expected= entry.getKey();
            }
        }
        return expected;
    }




    /**
     * Class ItemRank
     * Used in knn
     */
    private class ItemRank implements Comparable<ItemRank>
    {
        /** name */
        int name;

        /** distance */
        float distance;

        ItemRank(int n, float d){ this.name= n; this.distance=d;}

        @Override
        public int compareTo(ItemRank itemRank) {

            if(distance>itemRank.distance)
                return 1;
            else if(distance==itemRank.distance)
                return 0;
            else
                return -1;
        }
    }

    /**
     * Eucliden distance between two points
     * @param a point 1
     * @param b point 2
     * @return distance
     */
    private  float EuclideanDist(Point a, Point b) {
        return (float)Math.sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y));
    }

    /**
     * DTW distance between two vectors
     * @param v1 one vector
     * @param v2 two vector
     * @return distance
     */
    private float DTWDistance(List<Point> v1, List<Point> v2)
    {
        int i, j;
        float cost;
        float min;
        float[][] stock = new float[v1.size() + 1][v2.size() + 1];

        for (i = 0; i <= v1.size(); ++i)
            stock[i][0] = Float.MAX_VALUE;

        for(j = 0; j <= v2.size(); ++j)
            stock[0][j] = Float.MAX_VALUE;

        stock[0][0] = 0;

        for (i = 1; i <= v1.size(); ++i)
        {
            for(j=1; j<=v2.size(); ++j)
            {
                cost = EuclideanDist(v1.get(i-1), (v2.get(j-1)));

                if(stock[i - 1][j - 1]<stock[i][j - 1])
                    min=stock[i - 1][j - 1];
                else
                    min=stock[i][j - 1];

                if(stock[i - 1][j]<min)
                    min=stock[i - 1][j];

                stock[i][j]= cost+min;
            }
        }
        return stock[v1.size()][v2.size()];
    }
}
