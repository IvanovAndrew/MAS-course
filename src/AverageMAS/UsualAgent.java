package AverageMAS;

import AverageMAS.AverageOntology.AverageOntology;
import AverageMAS.AverageOntology.MessageContent;
import AverageMAS.CyclesOntology.CyclesMessage;
import AverageMAS.CyclesOntology.CyclesOntology;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;


/**
 * Created by User on 10/16/14.
 */
public class UsualAgent extends Agent {
    public static final String PREFIX_NAME = "agent_";

    private boolean isRemovingCycles = true;

    private ContentManager manager = getContentManager();
    private int myNumber = Common.Random.nextInt(Common.NUMBER_UPPER_BOUND) - Common.NUMBER_RANGE;

    private String mMyName;

    private String neighborName;
    private ArrayList<String> myNeighbors;

    private AgentController neighbor;

    private ArrayList<String> knowsAboutMe = new ArrayList<String>();
    private int finallyAccepted = 0;
    private int rejectedMe = 0;

    private int sum = 0;
    private int agentCount = 0;
    private int stopCount = 0;
    private int initiallyKnowsAboutMe = 0;

    private int incomingEdgesCount = initiallyKnowsAboutMe;

    protected void setup(){
        Common.registerAverageOntology(manager);
        Common.registerCyclesOntology(manager);
        Object[] args = getArguments();

        if (args != null && args.length > 0){
            myNeighbors = parseNeigbors((ArrayList<Integer>) args[0]);
            initiallyKnowsAboutMe = (Integer) args[1];
            mMyName = getLocalName();
        }

        addBehaviour(new CyclicBehaviour(this){
            public void action (){

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null){

                    String content = msg.getContent();

                    if (content.equals(Message.START)){
                        startAverageAction();
                    }
                    else if (content.equals(Message.REMOVE_CYCLES)){
                        startRemovingCycles();
                    }else{
                        if (isRemovingCycles)
                            handleRemovingCycles(msg);
                        else
                            handleAverageMessage(msg);
                    }
                }
                else{
                    block();
                }
            }
        });
    }

    private ArrayList<String> parseNeigbors(ArrayList<Integer> ids) {
        final int size = ids.size();

        if (size == 0){
            neighborName = "";
            return new ArrayList<String>();
        }

        ArrayList<String> result = new ArrayList<String>();
        for (int id = 0; id < size; id++){
            result.add(PREFIX_NAME + ids.get(id));
        }

        neighborName = result.get(Common.Random.nextInt(size));

        return result;
    }

    private void startRemovingCycles() {
        isRemovingCycles = true;

        if (initiallyKnowsAboutMe == 0){
            jade.wrapper.AgentContainer ac = getContainerController();
            jade.util.leap.ArrayList path = new jade.util.leap.ArrayList();
            path.add(mMyName);

            for(String neigh : myNeighbors){
                try {
                    AgentController receiver = ac.getAgent(neigh);
                    sendAccept(receiver, path, true);
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                } catch (ControllerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendFinallyAccept() throws ControllerException, Codec.CodecException, OntologyException {
        jade.util.leap.ArrayList path = createPath(knowsAboutMe);
        path.add(mMyName);

        AgentContainer ac = getContainerController();

        if (incomingEdgesCount > 0){
            for (String name : myNeighbors){
                AgentController receiver = ac.getAgent(name);
                sendAccept(receiver, path, true);
            }
            incomingEdgesCount = knowsAboutMe.size();
        }
        else{
            sendIAloneMsg(path);
        }
    }

    private void sendAccept(AgentController receiver, jade.util.leap.ArrayList path, boolean isFinally) throws StaleProxyException, Codec.CodecException, OntologyException {
        ACLMessage msg = createMessage(receiver);

        msg.setLanguage(Common.Codec.getName());
        msg.setOntology(CyclesOntology.getInstance().getName());

        CyclesMessage info = new CyclesMessage();
        if (isFinally){
            info.setMessage(Message.FINALLY_ACCEPT);
        }
        else {
            info.setMessage(Message.ACCEPT);
        }

        info.setPath(path);

        manager.fillContent(msg, info);
        send(msg);
    }

    private void sendRejectMessage(AgentController receiver) throws StaleProxyException, Codec.CodecException, OntologyException {
        ACLMessage msg = createMessage(receiver);

        msg.setLanguage(Common.Codec.getName());
        msg.setOntology(CyclesOntology.getInstance().getName());

        CyclesMessage info = new CyclesMessage();
        info.setMessage(Message.REJECT);

        manager.fillContent(msg, info);
        send(msg);
    }

    private void sendIAloneMsg(jade.util.leap.ArrayList path) throws ControllerException, Codec.CodecException, OntologyException {
        AgentContainer ac = getContainerController();
        AgentController receiver = ac.getAgent(GeneratorAgent.Name);
        ACLMessage msg = createMessage(receiver);

        msg.setLanguage(Common.Codec.getName());
        msg.setOntology(CyclesOntology.getInstance().getName());

        CyclesMessage info = new CyclesMessage();
        info.setMessage(Message.I_AM_ALONE);

        info.setPath(path);

        manager.fillContent(msg, info);
        send(msg);
    }

    private void handleRejectMsg(String name) throws OntologyException, Codec.CodecException, ControllerException {
        rejectedMe += 1;

        knowsAboutMe.remove(name);

        if (finallyAccepted + rejectedMe >= initiallyKnowsAboutMe){
            sendFinallyAccept();
        }

    }

    private jade.util.leap.ArrayList createPath(ArrayList<String> list){
        jade.util.leap.ArrayList result = new jade.util.leap.ArrayList();

        for (String name : list){
            result.add(name);
        }
        return result;
    }

    private void handleFinallyAcceptMsg(CyclesMessage msg) throws OntologyException, Codec.CodecException, ControllerException {
        finallyAccepted += 1;

        jade.util.leap.ArrayList path = msg.getPath();
        updateIncomingEdges(path);

        if (finallyAccepted + rejectedMe >= initiallyKnowsAboutMe){
            sendFinallyAccept();
        }
    }

    private void updateIncomingEdges(jade.util.leap.ArrayList path){
        int length = path.size();

        for (int i = 0; i < length; i++){
            String name = (String)path.get(i);
            if (!knowsAboutMe.contains(name)){
                knowsAboutMe.add(name);
            }
        }
    }

    private void handleIncomingEdge(CyclesMessage msg){
        String name = msg.getName();
        myNeighbors.add(name);
    }

    private void handleAddEdgeMsg(CyclesMessage msg){
        incomingEdgesCount += 1;
    }

    private void handleAcceptMsg(CyclesMessage msg) throws ControllerException, Codec.CodecException, OntologyException {
        jade.util.leap.ArrayList path = msg.getPath();

        updateIncomingEdges(path);

        AgentContainer ac = getContainerController();

        for (String name : myNeighbors){
            AgentController receiver = ac.getAgent(name);

            if (knowsAboutMe.contains(name)){
                sendRejectMessage(receiver);
                myNeighbors.remove(name);
            }
            else{
                jade.util.leap.ArrayList newPath = createPath(knowsAboutMe);
                newPath.add(mMyName);
                sendAccept(receiver, path, false);
            }
        }
    }

    private void handleRemovingCycles(ACLMessage msg){
        try{
            CyclesMessage receivedMsg =(CyclesMessage) manager.extractContent(msg);
            String content = receivedMsg.getMessage();

            if (content.equals(Message.ACCEPT)){
                handleAcceptMsg(receivedMsg);
            }
            else if (content.equals(Message.REJECT)){
                String name = msg.getSender().getLocalName();
                handleRejectMsg(name);
            }
            else if (content.equals(Message.FINALLY_ACCEPT)){
                handleFinallyAcceptMsg(receivedMsg);
            }
            else if (content.equals(Message.INCOMING_EDGE)){
                handleIncomingEdge(receivedMsg);
            }
            else if (content.equals(Message.ADD_EDGE)){
                handleAddEdgeMsg(receivedMsg);
            }
        } catch (UngroundedException e) {
            e.printStackTrace();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    //region Average calculation
    private void handleAverageMessage(ACLMessage msg){
        try {
            MessageContent receivedMsg =(MessageContent) manager.extractContent(msg);
            String content = receivedMsg.getMessage();

            if (content.equals(Message.NUMBER)) {
                handleNumberMsg(receivedMsg);
            } else if (content.equals(Message.STOP)) {
                handleStopMsg(receivedMsg);
            }
            else{
                System.out.println(String.format("%1$s: %2$s %3$s", mMyName, content, "was received"));
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

    private void handleNumberMsg(MessageContent msg){
        sum += msg.getNumber();
        agentCount += msg.getCount();
    }

    private void handleStopMsg(MessageContent msg) throws ControllerException {
        stopCount++;

        if (stopCount >= initiallyKnowsAboutMe && neighborName.isEmpty()){
            sum += myNumber;
            agentCount++;

            jade.wrapper.AgentContainer ac = getContainerController();
            AgentController center = ac.getAgent(CenterAgent.CENTER_NAME);

            float average = sum / (float) agentCount;

            System.out.println(String.format("%1$s SUM: %2$d AGENTS: %3$d AVERAGE: %4$f", mMyName, sum, agentCount, average));
            sendNumber(sum, agentCount, center);
            System.out.println(String.format("TOTAL MESSAGES: %1$s", Common.messagesTotal));
        }
        else if (stopCount >= initiallyKnowsAboutMe){
            sendNumber(sum + myNumber, agentCount + 1, neighbor);
            sendStopToAllNeigbors();
        }
    }

    private void startAverageAction(){
        isRemovingCycles = true;
        System.out.println(
                String.format("%1$s: %2$s ", mMyName, "START was received.") +
                        String.format("%1$d %2$s", initiallyKnowsAboutMe, "agent(s) know(s) about me. ") +
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

        if (initiallyKnowsAboutMe == 0){
            sendNumber(myNumber, neighbor);
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
            jade.wrapper.AgentContainer ac = getContainerController();
            for (String name : myNeighbors){
                AgentController receiver = ac.getAgent(name);
                sendStopToNeighbor(receiver);
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
    //endregion

    private ACLMessage createMessage(AgentController receiver) throws Codec.CodecException, OntologyException, StaleProxyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiver.getName(), AID.ISGUID));

        return msg;
    }
}
