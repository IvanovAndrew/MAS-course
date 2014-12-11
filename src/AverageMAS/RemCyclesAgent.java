package AverageMAS;

import AverageMAS.CyclesOntology.CyclesMessage;
import AverageMAS.CyclesOntology.CyclesOntology;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

import static AverageMAS.Message.REMOVE_CYCLES;

/**
 * Created by Andrew on 10.12.2014.
 */
public class RemCyclesAgent extends Agent{
    public static final String NAME = "Cycles";
    private ArrayList<AID> coveredAgents = new ArrayList<AID>();
    private ArrayList<AID> aloneAgents= new ArrayList<AID>();

    private int totalAgents;
    private ACLMessage generatorMsg;

    private ContentManager manager = getContentManager();

    protected void setup(){
        Common.registerCyclesOntology(manager);

        Object[] args = getArguments();

        if (args != null && args.length > 0){
            totalAgents = (Integer) args[0];
        }

        addBehaviour(new CyclicBehaviour(this){

            public void action (){

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null){
                    try
                    {
                        String content = msg.getContent();
                        AID sender = msg.getSender();

                        if (content.equals(Message.REMOVE_CYCLES)){
                            generatorMsg = msg;
                            startRemovingCycles();
                        }

                        if (content.equals(Message.I_AM_ALONE)){
                            aloneAgents.add(sender);
                            handleImAloneMsg();
                        }
                        else if (content.equals(Message.I_AM_OK)){
                            coveredAgents.add(sender);
                        }
                    }
                    catch (Codec.CodecException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    catch (OntologyException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    catch (StaleProxyException e)
                    {
                        e.printStackTrace();
                    }
                    catch (ControllerException e)
                    {
                        e.printStackTrace();
                    }
                }
                else{
                    block();
                }
            }
        });
    }

    private ACLMessage createNewIncomingEdgeMsg(String receiver) throws Codec.CodecException, OntologyException, ControllerException
    {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setLanguage(Common.Codec.getName());
        message.setOntology(CyclesOntology.getInstance().getName());

        AgentContainer ac = getContainerController();

        message.addReceiver(new AID(ac.getAgent(receiver).getName(), AID.ISGUID));
        CyclesMessage content = new CyclesMessage();
        content.setMessage(Message.INCOMING_EDGE);
        manager.fillContent(message, content);

        return message;
    }

    private void handleImAloneMsg() throws ControllerException, Codec.CodecException, OntologyException
    {
        if (coveredAgents.size() + aloneAgents.size() == totalAgents){
            int alones = aloneAgents.size();

            if (alones > 1){
                String stock = aloneAgents.get(0).getLocalName();

                for (int i = 1; i < alones; i++){
                    String receiver = aloneAgents.get(i).getLocalName();
                    ACLMessage message1 = createAddNewEdgeMsg(receiver, stock);
                    send(message1);

                    ACLMessage message2 = createNewIncomingEdgeMsg(stock);
                    send(message2);
                }
                sendCyclesIsRemoved();
            }
        }
    }

    private ACLMessage createAddNewEdgeMsg(String receiver, String to) throws Codec.CodecException, OntologyException, ControllerException
    {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

        AgentContainer ac = getContainerController();

        message.setLanguage(Common.Codec.getName());
        message.setOntology(CyclesOntology.getInstance().getName());
        message.addReceiver(new AID(ac.getAgent(receiver).getName(), AID.ISGUID));

        CyclesMessage content = new CyclesMessage();
        content.setMessage(Message.ADD_EDGE);
        content.setName(to);
        manager.fillContent(message, content);

        return message;
    }

    private void startRemovingCycles() throws ControllerException, Codec.CodecException, OntologyException {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent(REMOVE_CYCLES);
        AgentContainer ac = getContainerController();

        for (int i = 0; i < totalAgents; i++){
            String agentName = UsualAgent.PREFIX_NAME + i;
            message.addReceiver(new AID(ac.getAgent(agentName).getName(), AID.ISGUID));
        }
        send(message);
    }

    private void sendCyclesIsRemoved(){
        ACLMessage msg = generatorMsg.createReply();
        msg.setContent(Message.CYCLES_IS_REMOVED);
        send(msg);
    }
}
