package LocalVoting;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;

import java.util.Random;

/**
 * Created by User on 12/12/2014.
 */
public class Constants {

    public static final Codec CODEC = new SLCodec();
    public static final Random RANDOM = new Random();
    public static final int NUMBER_RANGE = 100;
    public static final int NUMBER_UPPER = 2 * NUMBER_RANGE;
    public static final int MAX_TICKS = 200;
}
