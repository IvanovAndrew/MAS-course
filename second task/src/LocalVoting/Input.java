package LocalVoting;

import java.util.StringTokenizer;

/**
 * Created by Andrew on 18.12.2014.
 */
public class  Input{
    public static final float[][] MATRIX = new float[][]{
                                                      {0, (float) 0.5, 0, 0, 1,},
                                                      {(float) 0.5, 0, 1, 0, 0,},
                                                      {0, 1, 0, 1, 0,},
                                                      {0, 0, 1, 0, 1,},
                                                      {1, 0, 0, 1, 0,},
    };

    public static final int AGENTS_COUNT = MATRIX[0].length;
    public static float ALPHA = (float) 0.1;

    public static int getId(String name){
        StringTokenizer tokenizer = new StringTokenizer(name, "_");

        tokenizer.nextToken();
        String idAsString = tokenizer.nextToken();
        return Integer.parseInt(idAsString);
    }

    public static float getKoeff(String rowName, String columnName){
        int row = getId(rowName);
        int column = getId(columnName);

        return MATRIX[row][column];
    }
}