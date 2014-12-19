package LocalVoting;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.ControllerException;

import java.util.ArrayList;

/**
 * Created by User on 12/12/2014.
 */
public class UsualAgent extends Agent {

    public static final String PREFIX_NAME = "agent_";
    private ContentManager manager = getContentManager();

    private ArrayList<String> myNeighbors = new ArrayList<String>();

    private float myNumber = Constants.RANDOM.nextInt(Constants.NUMBER_UPPER) - Constants.NUMBER_RANGE;

    private int receivedMsg = 0;
    private float totalDelta = 0;
    private int degree;
    private int ticks = 0;

    protected void setup() {
        manager.registerLanguage(Constants.CODEC);
        manager.registerOntology(MyOntology.getInstance());
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            myNeighbors = parseNeigbors((ArrayList<Integer>) args[0]);
            degree = myNeighbors.size();
        }

        System.out.println(String.format("%1$s INITIAL NUM %2$f", getLocalName(), myNumber));

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null){
                    String content = msg.getContent();
                    String senderName = msg.getSender().getLocalName();

                    if (content.equals(Message.GO)){
                        sendNumberToAll();
                    }
                    else{
                        try {
                            updateTasks((MessageContent) manager.extractContent(msg), senderName);
                        } catch (Codec.CodecException e) {
                            e.printStackTrace();
                        } catch (OntologyException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    block();
                }
            }
        });
    }

    private ArrayList<String> parseNeigbors(ArrayList<Integer> ids) {
        final int size = ids.size();
        if (size == 0){
            return new ArrayList<String>();
        }

        ArrayList<String> result = new ArrayList<String>();
        for (int id = 0; id < size; id++){
            result.add(PREFIX_NAME + ids.get(id));
        }

        return result;
    }

    private void sendNumberToAll(){
        for(String name : myNeighbors){
            sendNumber(name);
        }
    }

    private void updateTasks(MessageContent msg, String sender){

        float b = Input.getProbability(getLocalName(), sender);
        boolean isReceived = Constants.RANDOM.nextFloat() <= b;

        if (isReceived){
            float number = msg.getNumber();
            totalDelta += Input.ALPHA * (number - myNumber);
        }
        receivedMsg++;

        if (receivedMsg >= degree){
            myNumber += totalDelta;
            totalDelta = 0;
            receivedMsg = 0;
            ticks++;
            if (ticks < Constants.MAX_TICKS){
                sendNumberToAll();
            }else {
                System.out.println(String.format("TOTAL: %1$s number %2$f", getLocalName(), myNumber));
            }

        }
    }

    private void sendNumber(String name){
        jade.wrapper.AgentContainer ac = getContainerController();

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setLanguage(Constants.CODEC.getName());
        msg.setOntology(MyOntology.ONTOLOGY_NAME);
        try {
            msg.addReceiver(new AID(ac.getAgent(name).getName(), AID.ISGUID));
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        float num = addNoise(myNumber);
        MessageContent content = new MessageContent();
        content.setNumber(num);

        try {
            manager.fillContent(msg, content);
            send(msg);
            //System.out.println(String.format("%1$s send to %2$s number %3$f", getLocalName(), name, num));
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }

    private float addNoise(float num){
        num += Constants.RANDOM.nextFloat() - 0.5f;
        return num;
    }
}
