package AverageMAS;

import AverageMAS.AverageOntology.AverageOntology;
import AverageMAS.CyclesOntology.CyclesOntology;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;

import java.util.Random;

/**
 * Created by User on 10/16/14.
 */
public class Common {
    public static Codec Codec = new SLCodec();

    public static final Random Random = new Random();
    public static final int NUMBER_RANGE = 10;
    public static final int NUMBER_UPPER_BOUND = NUMBER_RANGE * 2;

    public static int messagesTotal = 0;

    public static void registerAverageOntology(ContentManager manager){
        manager.registerLanguage(Codec);
        manager.registerOntology(AverageOntology.getInstance());
    }

    public static void registerCyclesOntology(ContentManager manager){
        manager.registerLanguage(Codec);
        manager.registerOntology(CyclesOntology.getInstance());
    }
}
