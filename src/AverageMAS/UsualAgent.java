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

import java.util.ArrayList;


/**
 * Created by User on 10/16/14.
 */
public class UsualAgent extends Agent {
    public static final String PREFIX_NAME = "agent_";

    private ContentManager manager = getContentManager();
    private int myNumber = Common.Random.nextInt(Common.NUMBER_UPPER_BOUND) - Common.NUMBER_RANGE;

    private String mMyName;

    private String neighborName;
    private String[] myNeighbors;

    private AgentController neighbor;

    private int sum = 0;
    private int agentCount = 0;
    private int stopCount = 0;
    private int knowsAboutMe = 0;

    protected void setup(){
        Common.registerOntology(manager);
        Object[] args = getArguments();

        if (args != null && args.length > 0){
            myNeighbors = parseNeigbors((ArrayList<Integer>) args[0]);
            knowsAboutMe = (Integer) args[1];
            mMyName = getLocalName();
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
                }
            }
        });
    }

    private String[] parseNeigbors(ArrayList<Integer> ids) {
        final int size = ids.size();

        if (size == 0){
            neighborName = "";
            return new String[0];
        }

        String[] result = new String[size];
        for (int id = 0; id < size; id++){
            result[id] = PREFIX_NAME + ids.get(id);
        }

        neighborName = result[Common.Random.nextInt(size)];

        return result;
    }

    private void handleMessage(ACLMessage msg){
        try {
            MessageContent receivedMsg =(MessageContent) manager.extractContent(msg);
            String content = receivedMsg.getMessage();

            if (neighborName.isEmpty()){
                if (content.equals(Message.NUMBER)){
//                    System.out.println(String.format("%1$s: %2$s", mMyName, "number was received"));

                    sum += receivedMsg.getNumber();
                    agentCount += receivedMsg.getCount();

//                    System.out.println(String.format("%1$s: summa %2$d agents: %3$d", mMyName, sum, agentCount));
//                    System.out.println(String.format("Total messages: %1$s", Common.messagesTotal));
                }
                else if (content.equals(Message.STOP)){
                    stopCount++;
                    if (stopCount >= knowsAboutMe){
                        sum += myNumber;
                        agentCount++;

                        jade.wrapper.AgentContainer ac = getContainerController();
                        AgentController center = ac.getAgent(CenterAgent.CENTER_NAME);

                        float average = sum / (float) agentCount;

                        System.out.println(String.format("%1$s SUM: %2$d AGENTS: %3$d AVERAGE: %4$f", mMyName, sum, agentCount, average));
                        sendNumber(sum, agentCount, center);
                        System.out.println(String.format("TOTAL MESSAGES: %1$s", Common.messagesTotal));
                    }
                }
            }
            else {
                if (content.equals(Message.NUMBER)) {
//                    System.out.println(String.format("%1$s: %2$s", mMyName, "number was received"));

                    sum += receivedMsg.getNumber();
                    agentCount += receivedMsg.getCount();

                } else if (content.equals(Message.STOP)) {
                    stopCount++;
//                    System.out.println(
//                            String.format("%1$s: %2$s", mMyName, "stop was received. ") +
//                                    String.format("%1$s: %2$d ", "stopCount is ", stopCount) +
//                                    String.format("%1$s %2$d", "knows about me ", knowsAboutMe));

                    if (knowsAboutMe <= stopCount){
//                        System.out.println(String.format("%1$s: knows about me == stopCount. %2$s", mMyName, "I send sum and stop"));
                        sendNumber(sum + myNumber, agentCount + 1, neighbor);
                        sendStopToAllNeigbors();
                    }
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
        System.out.println(
                String.format("%1$s: %2$s ", mMyName, "START was received") +
                        String.format("%1$d: %2$s", knowsAboutMe, "knows about me") +
                        String.format("MY NUMBER: %1$d", myNumber));
        if (neighborName.isEmpty()){
            return;
        }

        jade.wrapper.AgentContainer ac = getContainerController();
        try {
            neighbor = ac.getAgent(neighborName);
        } catch (ControllerException e) {
            e.printStackTrace();
        }

        if (knowsAboutMe == 0){
            sendNumber(myNumber, neighbor);
//            System.out.println(String.format("%1$s: %2$s", mMyName, "no one knows about me. I send Stop"));
            sendStopToAllNeigbors();
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
//            System.out.println(String.format("%1$s sent number %2$s to %3$s", mMyName, myNumber, neighborName));
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void sendStopToNeighbor(AgentController receiver){
        try {

            ACLMessage msg = createMessage(receiver);

            msg.setLanguage(Common.Codec.getName());
            msg.setOntology(AverageOntology.getInstance().getName());

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
    private void sendStopToAllNeigbors(){

        try {
            for (String name : myNeighbors){
                jade.wrapper.AgentContainer ac = getContainerController();

                AgentController receiver = ac.getAgent(name);
                sendStopToNeighbor(receiver);
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private ACLMessage createMessage(AgentController receiver) throws Codec.CodecException, OntologyException, StaleProxyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver.getName(), AID.ISGUID));

        return msg;
    }
}
