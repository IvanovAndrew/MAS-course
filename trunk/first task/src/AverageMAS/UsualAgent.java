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
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

/**
 * Created by User on 10/16/14.
 */
public class UsualAgent extends Agent {
    public static final String PREFIX_NAME = "agent_";

    private boolean isRemovingCycles = false;
    private boolean isAverageCalculating = false;

    private ContentManager manager = getContentManager();
    private int myNumber = Common.Random.nextInt(Common.NUMBER_UPPER_BOUND) - Common.NUMBER_RANGE;

    private String mMyName;

    private String neighbor;
    private ArrayList<String> myNeighbors;

    private ArrayList<String> knowsAboutMe = new ArrayList<String>();
    private int finallyAccepted = 0;
    private int rejectedMe = 0;

    private int sum = 0;
    private int agentCount = 0;
    private int stopCount = 0;
    private int initiallyKnowsAboutMe = 0;

    private ACLMessage generatorMsg;
    private ArrayList<ACLMessage> queue = new ArrayList<ACLMessage>();

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
                    //System.out.println(getLocalName() + " received " + content + " from " + msg.getSender().getLocalName());

                    if (content.equals(Message.START_CALCULATION)){
                        startAverageAction();
                    }
                    else if (content.equals(Message.REMOVE_CYCLES)){
                        System.out.println(getLocalName() + " " + Message.REMOVE_CYCLES);
                        generatorMsg = msg;
                        startRemovingCycles();
                    }else{
                        if (isRemovingCycles){
                            handleRemovingCycles(msg);
                        }else if (isAverageCalculating){
                            handleAverageMessage(msg);
                        }else
                        {
                            queue.add(msg);
                            System.out.println(getLocalName() + " has queue");
                        }
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
            return new ArrayList<String>();
        }

        ArrayList<String> result = new ArrayList<String>();
        for (int id = 0; id < size; id++){
            result.add(PREFIX_NAME + ids.get(id));
        }

        return result;
    }

    private void startRemovingCycles() {
        isRemovingCycles = true;
//        System.out.println(getLocalName() + " " + Message.REMOVE_CYCLES + " initially knows about me: " + initiallyKnowsAboutMe);

        if (initiallyKnowsAboutMe == 0){
            jade.util.leap.ArrayList path = new jade.util.leap.ArrayList();
            path.add(mMyName);

            try {
                for(String neigh : myNeighbors){
                    sendAccept(neigh, path, true);
                }
                sendIOkayMsg();
            }catch (StaleProxyException e) {
                e.printStackTrace();
            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }

        int length = queue.size();
        for (int i = 0; i < length; i++){
            handleRemovingCycles(queue.get(i));
        }
    }

    //region Removing cycles message sending
    private void sendFinallyAccept() throws ControllerException, Codec.CodecException, OntologyException {
        jade.util.leap.ArrayList path = createPath(knowsAboutMe);
        path.add(mMyName);

        if (myNeighbors.size() > 0){
            for (String name : myNeighbors){
                sendAccept(name, path, true);
            }
            sendIOkayMsg();
        }else{
            sendIAloneMsg();
        }

        /*System.out.println(String.format("%1$s: about me knows %2$d agents. I know about %3$d agents",
                           getLocalName(),
                           finallyAccepted,
                           myNeighbors.size()));*/
    }

    private void sendAccept(String receiver, jade.util.leap.ArrayList path, boolean isFinally) throws ControllerException, Codec.CodecException, OntologyException {
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

    private void sendRejectMessage(String receiver) throws ControllerException, Codec.CodecException, OntologyException {
        ACLMessage msg = createMessage(receiver);

        msg.setLanguage(Common.Codec.getName());
        msg.setOntology(CyclesOntology.getInstance().getName());

        CyclesMessage info = new CyclesMessage();
        info.setMessage(Message.REJECT);

        manager.fillContent(msg, info);
        send(msg);
    }

    private void sendIAloneMsg() throws ControllerException, Codec.CodecException, OntologyException {
        ACLMessage msg = generatorMsg.createReply();

        msg.setLanguage(Common.Codec.getName());

        msg.setContent(Message.I_AM_ALONE);
        //System.out.println(getLocalName() + " sends " + Message.I_AM_ALONE);
        send(msg);
    }

    private void sendIOkayMsg() throws ControllerException, Codec.CodecException, OntologyException {
        ACLMessage msg = generatorMsg.createReply();

        msg.setLanguage(Common.Codec.getName());

        msg.setContent(Message.I_AM_OK);
        //System.out.println(getLocalName() + " sends " + Message.I_AM_ALONE);
        send(msg);
    }

    private void sendAcceptOrRejectMag(jade.util.leap.ArrayList path) throws ControllerException, Codec.CodecException, OntologyException{
        AgentContainer ac = getContainerController();

        ArrayList<String> rejected = new ArrayList<String>();
        for (String name : myNeighbors){

            if (knowsAboutMe.contains(name)){
                sendRejectMessage(name);
                rejected.add(name);
            }
            else{
                jade.util.leap.ArrayList newPath = createPath(knowsAboutMe);
                newPath.add(mMyName);
                sendAccept(name, path, false);
            }
        }

        for (String name : rejected){
            myNeighbors.remove(name);
        }
    }
    //endregion

    //region Removing cycles message handling
    private jade.util.leap.ArrayList createPath(ArrayList<String> list){
        jade.util.leap.ArrayList result = new jade.util.leap.ArrayList();

        for (String name : list){
            result.add(name);
        }
        return result;
    }

    private void handleRejectMsg(String name) throws OntologyException, Codec.CodecException, ControllerException
    {
        rejectedMe += 1;

        knowsAboutMe.remove(name);

        if (finallyAccepted + rejectedMe >= initiallyKnowsAboutMe)
        {
            sendFinallyAccept();
        }else{
            jade.util.leap.ArrayList path = createPath(knowsAboutMe);
            path.add(mMyName);
            sendAcceptOrRejectMag(path);
        }
    }

    private void handleFinallyAcceptMsg(CyclesMessage msg) throws OntologyException, Codec.CodecException, ControllerException {
        finallyAccepted += 1;

        jade.util.leap.ArrayList path = msg.getPath();
        updateIncomingEdges(path);

        if (finallyAccepted + rejectedMe >= initiallyKnowsAboutMe){
            sendFinallyAccept();
        }else{
            sendAcceptOrRejectMag(path);
        }
        //System.out.println(getLocalName() + " fAccepted " + finallyAccepted + " rejected " + rejectedMe + " total: " + initiallyKnowsAboutMe);
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
        finallyAccepted += 1;
        /*System.out.println(String.format("%1$s: about me knows %2$d agents. I know about %3$d agents",
                getLocalName(),
                finallyAccepted,
                myNeighbors.size()));*/
    }

    private void handleAddEdgeMsg(CyclesMessage msg){
        String name = msg.getName();
//        System.out.println(getLocalName() + " I know about _" + name + "_ agent");
        myNeighbors.add(name);

        /*System.out.println(String.format("%1$s: about me knows %2$d agents. I know about %3$d agents",
                getLocalName(),
                finallyAccepted,
                myNeighbors.size()));*/
    }

    private void handleAcceptMsg(CyclesMessage msg) throws ControllerException, Codec.CodecException, OntologyException
    {
        jade.util.leap.ArrayList path = msg.getPath();

        updateIncomingEdges(path);
        path.add(mMyName);

        sendAcceptOrRejectMag(path);
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
    //endregion

    //region Average message handling
    private void startAverageAction(){
        isAverageCalculating = true;
        isRemovingCycles = false;

        final int size = myNeighbors.size();
        neighbor = size > 0 ? myNeighbors.get(Common.Random.nextInt(size)) : "";

        System.out.println(
                String.format("%1$s: %2$s ", mMyName, "START_CALCULATION was received.") +
                        String.format("%1$s: %2$d ", "Inner edges", finallyAccepted) +
                        String.format("%1$s: %2$d ", "Out edges", myNeighbors.size()) +
                        String.format("MY NUMBER: %1$d", myNumber));
        if (neighbor.isEmpty()){
            return;
        }

        if (finallyAccepted == 0){
            sendNumber(myNumber, neighbor);
            sendStopToAllNeigbors();
        }
    }

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
                System.out.println(String.format("%UNEXPECTED 1$s: %2$s %3$s", mMyName, content, "was received"));
            }
        }catch (UngroundedException e) {
            e.printStackTrace();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }

    private void handleNumberMsg(MessageContent msg){
        sum += msg.getNumber();
        agentCount += msg.getCount();
    }

    private void handleStopMsg(MessageContent msg){
        stopCount++;
        /*System.out.println(String.format("%1$s set %2$d stop msg. Need %3$d msgs.",
                            getLocalName(),
                            stopCount,
                            finallyAccepted));
        System.out.println(String.format("%1$s neighbor -- %2$s", getLocalName(), neighbor));*/

        if (stopCount >= finallyAccepted && neighbor.isEmpty()){
            sum += myNumber;
            agentCount++;

            float average = sum / (float) agentCount;

            System.out.println(String.format("%1$s SUM: %2$d AGENTS: %3$d AVERAGE: %4$f", mMyName, sum, agentCount, average));
            sendNumber(sum, agentCount, CenterAgent.CENTER_NAME);
            System.out.println(String.format("TOTAL MESSAGES: %1$s", Common.messagesTotal));
        }
        else if (stopCount >= finallyAccepted){
            sendNumber(sum + myNumber, agentCount + 1, neighbor);
            sendStopToAllNeigbors();
        }
    }
    //endregion

    //region Average message sending
    private void sendNumber(int number, String neighbor){
        sendNumber(number, 1, neighbor);
    }

    private void sendNumber(int number, int count, String neighbor) {
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
        }catch (ControllerException e)
        {
            e.printStackTrace();
        }
    }

    private void sendStopToAllNeigbors(){
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setLanguage(Common.Codec.getName());
            msg.setOntology(AverageOntology.getInstance().getName());

            manager.fillContent(msg, new MessageContent(Message.STOP));

            AgentContainer ac = getContainerController();
            for (String name : myNeighbors){
                msg.addReceiver(new AID(ac.getAgent(name).getName(), AID.ISGUID));
                Common.messagesTotal++;
            }
//            System.out.println(getLocalName() + " sends all " + Message.STOP);
            send(msg);

        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
    //endregion

    private ACLMessage createMessage(String receiver) throws Codec.CodecException, OntologyException, ControllerException
    {
        AgentContainer ac = getContainerController();

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(ac.getAgent(receiver).getName(), AID.ISGUID));

        return msg;
    }
}