package AverageMAS;

import AverageMAS.Ontology.Message;
import jade.content.ContentManager;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

import java.util.ArrayList;

/**
 * Created by User on 10/16/14.
 */
public class UsualAgent extends Agent {
    public static final String PREFIX_NAME = "agent_";

    private ContentManager mManager = (ContentManager) getContentManager();
    private int myNumber; //= Constants.Random.nextInt(Constants.NUMBER_UPPER_BOUND);

    public String mMyName;

    private ArrayList<String> mNeighborhoodsNames = new ArrayList<String>();
    private ArrayList<AgentController> mNeighborhoods = new ArrayList<AgentController>();
    private CenterAgent mCenter;

    protected void setup(){
        Object[] args = getArguments();

        if (args != null && args.length > 0)
        {
            mMyName = (String) args[0];
            mNeighborhoodsNames = (ArrayList<String>) args[1];
            myNumber = (Integer) args[2];
        }

        addBehaviour(new CyclicBehaviour(this){
            public void action (){
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg != null){
                    String content = msg.getContent();
                    if (content.equals(Message.START)){
                        mNeighborhoods = getNeighborhoods();
                    }
                }
            }
        });
    }

    private ArrayList<AgentController> getNeighborhoods(){
        try {
            ArrayList<AgentController> result = new ArrayList<AgentController>();

            for (String name : mNeighborhoodsNames){
                jade.wrapper.AgentContainer ac = getContainerController();
                result.add(ac.getAgent(name));
            }
            return result;
        }catch (ControllerException e) {
            e.printStackTrace();
            return null;
        }
    }
}
