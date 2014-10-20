package AverageMAS.Ontology;

import jade.content.Predicate;

/**
 * Created by User on 10/17/14.
 */
public class MessageContent implements Predicate {
    private String message;

    public MessageContent(String msg){
        message = msg;
    }

    public String getMessage(){
        return message;
    }

    private void setMessage(String msg){
        message = msg;
    }
}
