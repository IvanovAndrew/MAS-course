package AverageMAS.CyclesOntology;

import jade.content.Predicate;
import jade.util.leap.ArrayList;

/**
 * Created by User on 12/4/2014.
 */
public class CyclesMessage implements Predicate {
    public static final String CYCLES_MESSAGE = "CyclesMessage";
    public static final String PATH = "path";
    public static final String MESSAGE = "message";
    public static final String NAME = "name";

    private ArrayList path = new ArrayList();
    private String message = "";
    private String name = "";

    public CyclesMessage(){}

    public CyclesMessage(ArrayList path){
        this.path = path;
    }

    public ArrayList getPath(){
        return path;
    }

    public void setPath(ArrayList list){
        this.path = list;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String msg){
        message = msg;
    }

    public String getName(){
        return name;
    }

    public void setName(String newName){
        name = newName;
    }
}