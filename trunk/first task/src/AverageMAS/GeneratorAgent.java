package AverageMAS;

import jade.content.ContentManager;
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

/**
 * Created by User on 10/16/14.
 */
public class GeneratorAgent extends Agent {
    private static final String filePath = "input_cycles.txt";//"input_2.txt";//"input_1.txt";//"input_0.txt";
    private static final String separator = " ";

    private ArrayList<AgentController> mAgents = new ArrayList<AgentController>();

    private AgentContainer container;
    private ContentManager manager = getContentManager();

    protected void setup(){
        Common.registerAverageOntology(manager);
        Common.registerCyclesOntology(manager);

        Object[] args = getArguments();
        String fileName = (args != null && args.length > 0) ? (String) args[0] : null;

        final int[][] matrix = readMatrixFromFile(fileName);
        if (matrix == null)
            return;
        checkMatrix(matrix);

        jade.core.Runtime rt = Runtime.instance();
        ProfileImpl p = new ProfileImpl(false);
        container = rt.createAgentContainer(p);

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

            AgentController cyclesAgent = createCyclesAgent(mAgents.size());
            cyclesAgent.start();

            removeCycles(cyclesAgent);

            addBehaviour(new CyclicBehaviour(this){
                public void action (){
                    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                    if (msg != null){
                        String content = msg.getContent();

                        if (content.equals(Message.CYCLES_IS_REMOVED)){
                            try {
                                startAverageCalculation();
                            } catch (StaleProxyException e) {
                                e.printStackTrace();
                            } catch (Codec.CodecException e) {
                                e.printStackTrace();
                            } catch (OntologyException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

        }catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private void removeCycles(AgentController agent) throws StaleProxyException {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setContent(Message.REMOVE_CYCLES);
        msg.addReceiver(new AID(agent.getName(), AID.ISGUID));
        send(msg);
    }

    private void startAverageCalculation() throws StaleProxyException, Codec.CodecException, OntologyException {
//        System.out.println("I start average calculation");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setContent(Message.START_CALCULATION);

        for (AgentController agent : mAgents){
            message.addReceiver(new AID(agent.getName(), AID.ISGUID));
        }
        send(message);
    }

    //region agents creating
    private AgentController createUsualAgent(int id, ArrayList<Integer> neighborsIds, int knowsAbout) throws StaleProxyException {
        String agentName = UsualAgent.PREFIX_NAME + id;
        Object[] array = {neighborsIds, knowsAbout};
        return container.createNewAgent(agentName, "AverageMAS.UsualAgent", array);
    }

    private AgentController createCenterAgent() throws StaleProxyException {
        return container.createNewAgent(CenterAgent.CENTER_NAME, "AverageMAS.CenterAgent", null);
    }

    private AgentController createCyclesAgent(int agentsCount) throws StaleProxyException {
        Object[] array = {agentsCount};
        return container.createNewAgent(RemCyclesAgent.NAME, "AverageMAS.RemCyclesAgent", array);
    }
    //endregion

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

    //region matrix manipulations
    private void printMatrix(int[][] matrix){
        int vertexCount = matrix[0].length;

        for (int i = 0; i < vertexCount; i++){
            for (int j = 0; j < vertexCount; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private void checkMatrix(int[][] matrix){
        int size = (matrix[0]).length;

        int minSum = size;
        int minIndex = -1;
        // need agent with 0 in arcs
        for (int i = 0; i < size; i++){
            int sum = 0;
            for (int j = 0; j < size; j++){
                if (i == j)
                    continue;
                if (matrix[j][i] > 0)
                    sum += 1;
            }
            if (sum == 0)
                return;

            if (sum < minSum){
                minSum = sum;
                minIndex = i;
            }
        }

        for (int j = 0; j < size; j++){
            if (j == minIndex)
                continue;
            matrix[j][minIndex] = 0;
        }
    }
    //endregion
}