package AverageMAS;

import jade.content.lang.Codec;
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

import static AverageMAS.Ontology.Message.START;

/**
 * Created by User on 10/16/14.
 */
public class GeneratorAgent extends Agent {
    private static String filePath = "input_1.txt";//"input_0.txt";
    private static String separator = " ";

    private ArrayList<AgentController> mAgents = new ArrayList<AgentController>();
    private AgentContainer allAgents;
    private AgentContainer allListeners;

    protected void setup(){
        Object[] args = getArguments();
        String fileName = (args != null && args.length > 0) ? (String) args[0] : null;

        int[][] matrix = readMatrixFromFile(fileName);
        if (matrix == null)
            return;

        matrix = analyseMatrix(matrix);
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

    private int[][] analyseMatrix(int[][] matrix){
        matrix = removeCycles(matrix);
        matrix = addNeedEdges(matrix);
        return matrix;
    }

    private int[][] removeCycles(int[][] matrix){
        return matrix;
    }

    private int[][] addNeedEdges(int[][] matrix){
        final int size = matrix[0].length;
        ArrayList<Integer> ints = new ArrayList<Integer>();

        for (int i = 0; i < size; i++){
            int count = 0;
            for (int j = 0; j < size; j++){
                if (i == j){
                    continue;
                }
                if (matrix[i][j] > 0){
                    count++;
                }
            }

            if (count == 0){
                ints.add(i);
            }
        }

        if (ints.size() > 1){
            int leader = ints.get(0);
            for (int id = 1; id < ints.size(); id++){
                matrix[id][leader] = 1;
            }
        }

        return matrix;
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
