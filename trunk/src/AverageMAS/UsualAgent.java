package AverageMAS;

import AverageMAS.Ontology.AverageOntology;
import AverageMAS.Ontology.Message;
import AverageMAS.Ontology.MessageContent;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;


/**
 * Created by User on 10/16/14.
 */
public class UsualAgent extends Agent {
    public static final String PREFIX_NAME = "agent_";

    private ContentManager manager = getContentManager();
    private int myNumber; //= Common.Random.nextInt(Common.NUMBER_UPPER_BOUND);

    public String mMyName;

    private String neighborName;
    private AgentController neighbor;

    private boolean numberIsSent = false;
//    private ArrayList<ACLMessage> unhandledMessages = new ArrayList<ACLMessage>();
//    private boolean isFinished = false;

    private int sum = 0;
    private int agentCount = 0;
    private int stopCount = 0;

    protected void setup(){
        Common.registerOntology(manager);
        Object[] args = getArguments();

        if (args != null && args.length > 0){
            mMyName = (String) args[0];
            neighborName = (String) args[1];
            myNumber = (Integer) args[2];
        }

        addBehaviour(new CyclicBehaviour(this){
            public void action (){

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null){
                    if (msg.getContent().equals(Message.START)){
                        startAction();
                    }else{
                        handleMessage(msg);
                    }
                }
                else{
                    block();
                    //System.out.println(String.format("%1$s received null", mMyName));
                }
            }
        });
    }

    private void handleMessage(ACLMessage msg){
        try {
            MessageContent receivedMsg =(MessageContent) manager.extractContent(msg);
            String content = receivedMsg.getMessage();

            if (neighborName.isEmpty()){
                if (content.equals(Message.NUMBER)){
                    System.out.println(String.format("%1$s: %2$s", mMyName, "number was received"));
                    sum += receivedMsg.getNumber();
                    agentCount++;
                    System.out.println(String.format("%1$s: summa %2$d agents: %3$d", mMyName, sum, agentCount));
                    System.out.println(String.format("Total messages: %1$s", Common.messagesTotal));
                }
                else if (content.equals(Message.STOP)){
                    stopCount++;
                    if (stopCount >= agentCount){
                        sum += myNumber;
                        agentCount++;

                        jade.wrapper.AgentContainer ac = getContainerController();
                        AgentController center = ac.getAgent(CenterAgent.CENTER_NAME);
                        sendNumber(sum, agentCount, center);
                        System.out.println(String.format("Total messages: %1$s", Common.messagesTotal));
                    }
                }
            }
            else {
                if (content.equals(Message.NUMBER)) {

                    System.out.println(String.format("%1$s: %2$s", mMyName, "number was received"));
                    sendNumber(receivedMsg.getNumber(), receivedMsg.getCount(), neighbor);

                } else if (content.equals(Message.STOP)) {

                    System.out.println(String.format("%1$s: %2$s", mMyName, "stop was received"));
                    sendStop(neighbor);
                }
                else{
                    System.out.println(String.format("%1$s: %2$s %3$s", mMyName, content, "was received"));
                }
            }
        }catch (UngroundedException e) {
            e.printStackTrace();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private void startAction(){
        System.out.println(String.format("%1$s: %2$s", mMyName, "START was received"));
        if (neighborName.isEmpty()){

        }else{
            jade.wrapper.AgentContainer ac = getContainerController();
            try {
                neighbor = ac.getAgent(neighborName);
            } catch (ControllerException e) {
                e.printStackTrace();
            }

            sendNumber(myNumber, neighbor);
            numberIsSent = true;
        }
    }

    private void sendNumber(int number, AgentController neighbor){
        sendNumber(number, 1, neighbor);
    }

    private void sendNumber(int number, int count, AgentController neighbor) {
        try {

            ACLMessage msg = createMessage(neighbor);

            msg.setLanguage(Common.Codec.getName());
            msg.setOntology(AverageOntology.getInstance().getName());

            MessageContent content = new MessageContent(number, count);

            manager.fillContent(msg, content);

            send(msg);
            Common.messagesTotal++;
            System.out.println(String.format("%1$s sent number %2$s", mMyName, myNumber));
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void sendStop(AgentController receiver){
        try {

            ACLMessage msg = createMessage(receiver);
            manager.fillContent(msg, new MessageContent(Message.STOP));
            send(msg);
            Common.messagesTotal++;

        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private ACLMessage createMessage(AgentController receiver) throws Codec.CodecException, OntologyException, StaleProxyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver.getName(), AID.ISGUID));

        return msg;
    }
}
