package AverageMAS;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static AverageMAS.Message.REMOVE_CYCLES;
import static AverageMAS.Message.START;

/**
 * Created by User on 10/16/14.
 */
public class GeneratorAgent extends Agent {
    private static final String filePath = "input_2.txt";//"input_1.txt";//"input_0.txt";
    private static final String separator = " ";
    public static String Name;

    private ArrayList<AgentController> mAgents = new ArrayList<AgentController>();
    private AgentContainer allAgents;
    private AgentContainer allListeners;

    protected void setup(){
        Name = getLocalName();
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
                ArrayList<Integer> neighbors = new ArrayList<Integer>();
                int knowsAbout = 0;

                for (int other = 0; other < matrix.length; other++){
                    if (other == agent)
                        continue;

                    if (matrix[agent][other] > 0){
                        neighbors.add(other);
                    }

                    if (matrix[other][agent] > 0){
                        knowsAbout++;
                    }
                }

                AgentController newAgent = createUsualAgent(agent, neighbors, knowsAbout);

                mAgents.add(newAgent);
                newAgent.start();


            }

            AgentController centerAgent = createCenterAgent();
            centerAgent.start();
            startRemovingCycles();
        }catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour(this){
            public void action (){

                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null){

                    String content = msg.getContent();

                    if (content.equals(Message.START)){
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

    private void startRemovingCycles() throws StaleProxyException, Codec.CodecException, OntologyException {
        for (AgentController agent : mAgents){
            jade.lang.acl.ACLMessage message = createRemovingMessage(agent.getName());
            send(message);
        }
    }

    private ACLMessage createRemovingMessage(String receiver) throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(REMOVE_CYCLES);
        msg.addReceiver(new AID(receiver, AID.ISGUID));
        return msg;
    }

    private void startAverageCalculation() throws StaleProxyException, Codec.CodecException, OntologyException {
        for (AgentController agent : mAgents){
            jade.lang.acl.ACLMessage message = createStartMessage(agent.getName());
            send(message);
        }
    }

    private ACLMessage createStartMessage(String receiver) throws Codec.CodecException, OntologyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(START);
        msg.addReceiver(new AID(receiver, AID.ISGUID));
        return msg;
    }

    private AgentController createUsualAgent(int id, ArrayList<Integer> neighborsIds, int knowsAbout) throws StaleProxyException {
        String agentName = UsualAgent.PREFIX_NAME + id;
        Object[] array = {neighborsIds, knowsAbout};
        return allListeners.createNewAgent(agentName, "AverageMAS.UsualAgent", array);
    }

    private AgentController createCenterAgent() throws StaleProxyException {
        return allListeners.createNewAgent(CenterAgent.CENTER_NAME, "AverageMAS.CenterAgent", null);
    }

    private void printMatrix(int[][] matrix){
        int vertexCount = matrix[0].length;

        for (int i = 0; i < vertexCount; i++){
            for (int j = 0; j < vertexCount; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }

    }


    //region read matrix from file
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
    //endregion
}
