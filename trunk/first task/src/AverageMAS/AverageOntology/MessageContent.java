package AverageMAS.AverageOntology;

import AverageMAS.Message;
import jade.content.Predicate;

/**
 * Created by User on 10/17/14.
 */
public class MessageContent implements Predicate {
    public final static String MESSSAGE_CONTENT = "MessageContent";
    public final static String NUMBER = "number";
    public final static String COUNT = "count";
    public final static String MESSAGE = "message";

    private int number = 0;
    private int count = 0;
    private String message = "";

    public MessageContent(){}

    public MessageContent(String msg){
        message = msg;
    }

    public MessageContent(int number){
        this.number = number;
        this.message = Message.NUMBER;
    }

    public MessageContent(int number, int count){
        this(number);
        this.count = count;
    }

    public int getNumber(){
        return number;
    }

    public void setNumber(int number){
        this.number = number ;
    }

    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String msg){
        message = msg;
    }
}
