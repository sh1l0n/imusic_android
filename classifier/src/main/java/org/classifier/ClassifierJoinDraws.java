package org.classifier;

import android.graphics.Rect;
import org.ejml.simple.SimpleMatrix;

public class ClassifierJoinDraws {

    private final static SimpleMatrix mu = new SimpleMatrix(new double[][]{{-13.74468085106383,-33.48936170212766,-20.91489361702128,20.15957446808511,95478.53191489361,25567.3829787234,92436.57446808511,28502.54255319149}});

    private final static SimpleMatrix sigma = new SimpleMatrix(new double[][]{{310.345348259735,157.1900257226423,304.9399609362391,168.5177826800094,219846.9936061693,42311.22876335139,212642.8566375008,44231.15851724596}});

    private final static SimpleMatrix theta = new SimpleMatrix(new double[][]{{-6.628025169355921,0.1990604506281564,-0.4235770581774129,0.2866852138311,-2.317142648864942,-21.86908861087593,0.1083400719097274,-14.69559365446249,3.838335380118589}}).transpose();

    private final static double thresold = 0.5;

    private final static int poly = 2;

    public static boolean isJoin(Rect x)
    {
        SimpleMatrix X = new SimpleMatrix(new double[][]{{x.left, x.top, x.right, x.bottom}});
        SimpleMatrix X_poly = new SimpleMatrix(X);
        //Add poly features
        
        for(int i=2; i<=poly; ++i) X_poly = X_poly.combine(0, X.numCols(), X.elementPower(i));
        
        // Normalize features
        X_poly = X_poly.minus(mu).elementDiv(sigma);
        
        //Add 1's first column
        X_poly = new SimpleMatrix(X_poly.numRows(), 1).plus(1).combine(0,1,X_poly);
        
        return (1/(1+Math.pow(Math.E, -X_poly.mult(theta).elementSum())))>=thresold;
    }
}