package LocalVoting;

import jade.content.Predicate;

/**
 * Created by User on 12/12/2014.
 */
public class MessageContent implements Predicate {

    public final static String MESSSAGE_CONTENT = "MessageContent";
    public final static String NUMBER = "number";

    private float number;

    public MessageContent(){}

    public void setNumber(float num){
        number = num;
    }

    public float getNumber(){
        return number;
    }
}
