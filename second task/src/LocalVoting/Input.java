package LocalVoting;

import java.util.StringTokenizer;

/**
 * Created by Andrew on 18.12.2014.
 */
public class  Input{
    public static final float[][] MATRIX = new float[][]{
                                                      {0, 0.5f, 0, 0, 1,},
                                                      {0.5f, 0, 1, 0, 0,},
                                                      {0, 1, 0, 1, 0,},
                                                      {0, 0, 1, 0, 1,},
                                                      {1, 0, 0, 1, 0,},
    };

    public static final float[][] DELAY_MATRIX = new float[][]{
            {0, 1, 0, 0, 1},
            {1, 0, 1, 0, 0},
            {0, 1, 0, 1, 0},
            {0, 0, 1, 0, 1},
            {0.8f, 0, 0, 0.8f, 0},
    };

    public static final int AGENTS_COUNT = MATRIX[0].length;
    public static float ALPHA = 0.1f;

    public static int getId(String name){
        StringTokenizer tokenizer = new StringTokenizer(name, "_");

        tokenizer.nextToken();
        String idAsString = tokenizer.nextToken();
        return Integer.parseInt(idAsString);
    }

    public static float getProbability(String rowName, String columnName){
        int row = getId(rowName);
        int column = getId(columnName);

        return MATRIX[row][column];
    }

    public static float getDelayProbability(String rowName, String columnName){
        int row = getId(rowName);
        int column = getId(columnName);

        return DELAY_MATRIX[row][column];
    }
}