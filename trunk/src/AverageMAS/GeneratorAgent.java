package AverageMAS;

import AverageMAS.Ontology.Message;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by User on 10/16/14.
 */
public class GeneratorAgent extends Agent {
    private static String filePath = "input.txt";
    private static String separator = " ";

    private ContentManager mManager = (ContentManager) getContentManager();
    private Codec mCodec = new SLCodec();

    private ArrayList<AgentController> mAgents = new ArrayList<AgentController>();
    private AgentContainer allAgents;
    private AgentContainer allListeners;

    protected void setup(){
        Object[] args = getArguments();
        String fileName = (args != null && args.length > 0) ? (String) args[0] : null;

        int[][] matrix = readMatrixFromFile(fileName);
        if (matrix == null)
            return;

        jade.core.Runtime rt = Runtime.instance();
        ProfileImpl p = new ProfileImpl(false);
        allListeners = rt.createAgentContainer(p);

        try {
            for (int agent = 0; agent < matrix.length; agent++){
                ArrayList<String> neighborhoods = new ArrayList<String>();
                for (int other = 0; other < matrix.length; other++){
                    if (other == agent)
                        continue;

                    if (matrix[agent][other] > 0){
                        String name = UsualAgent.PREFIX_NAME + other;
                        neighborhoods.add(name);
                    }
                }
                int agentNumber = matrix[agent][agent];
                AgentController newAgent = createUsualAgent(agent, neighborhoods, agentNumber);

                mAgents.add(newAgent);
                newAgent.start();
            }

            AgentController centerAgent = createCenterAgent();
            centerAgent.start();

            for (AgentController agent : mAgents){
                jade.lang.acl.ACLMessage message = createMessage(agent.getName());
                send(message);
            }

        }catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }

    private ACLMessage createMessage(String receiver) throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        //msg.setLanguage(mCodec.getName());
        msg.setContent(Message.START);
        msg.addReceiver(new AID(receiver, AID.ISGUID));

        /*MessageContent content = new MessageContent();
        content.setMessage(Message.START);
        mManager.fillContent(msg, content);*/
        return msg;
    }

    private AgentController createUsualAgent(int id, ArrayList<String> neighborhoods, int agentNumber) throws StaleProxyException {
        String agentName = UsualAgent.PREFIX_NAME + id;
        Object[] array = {agentName, neighborhoods, agentNumber};
        return allListeners.createNewAgent(agentName, "AverageMAS.UsualAgent", array);
    }

    private AgentController createCenterAgent() throws StaleProxyException {
        return allListeners.createNewAgent(CenterAgent.CENTER_NAME, "AverageMAS.CenterAgent", null);
    }

    private static int[][] readMatrixFromFile (String fileName){
        if (fileName == null){
            fileName = filePath;
        }
        try {
            FileReader fileReader = new FileReader(fileName);
            return readMatrixFromFile(new BufferedReader(fileReader));
        }catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int[][] readMatrixFromFile (BufferedReader input){
        String line;
        try {
            line = input.readLine();
            int agentsCount = Integer.parseInt(line);
            int matrix[][] = new int[agentsCount][agentsCount];

            int lineNumber = 0;
            while ((line = input.readLine()) != null){
                StringTokenizer tokenizer = new StringTokenizer(line, separator);

                int i = 0;

                while (tokenizer.hasMoreTokens()){
                    String token = tokenizer.nextToken();
                    if (token.isEmpty()) continue;

                    matrix[lineNumber][i++] = Integer.parseInt(token);
                }
                lineNumber++;
            }
            return matrix;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
