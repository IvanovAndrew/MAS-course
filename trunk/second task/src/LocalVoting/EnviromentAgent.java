package LocalVoting;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;

/**
 * Created by User on 12/12/2014.
 */
public class EnviromentAgent extends Agent {

    private AgentContainer container;
    private ArrayList<String> agents = new ArrayList<String>();

    protected void setup(){

        jade.core.Runtime rt = jade.core.Runtime.instance();
        ProfileImpl p = new ProfileImpl(false);
        container = rt.createAgentContainer(p);

        for (int agentId = 0; agentId < Input.AGENTS_COUNT; agentId++){
            ArrayList<Integer> neighbors = new ArrayList<Integer>();

            for (int other = 0; other < Input.AGENTS_COUNT; other++){
                if (other == agentId)
                    continue;

                if (Input.MATRIX[agentId][other] > 0){
                    neighbors.add(other);
                }
            }

            AgentController newAgent = null;
            try {
                String agentName = UsualAgent.PREFIX_NAME + agentId;
                newAgent = container.createNewAgent(agentName, "LocalVoting.UsualAgent", new Object[]{neighbors});
                agents.add(agentName);
                newAgent.start();
            } catch (StaleProxyException e) {
                e.printStackTrace();
            }
        }

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(Message.GO);
        for (String name : agents){
            try {
                msg.addReceiver(new AID(container.getAgent(name).getName(), AID.ISGUID));
            } catch (ControllerException e) {
                e.printStackTrace();
            }
        }
        send(msg);
    }
}