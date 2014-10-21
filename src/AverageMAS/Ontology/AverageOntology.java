package AverageMAS.Ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

/**
 * Created by User on 10/21/2014.
 */
public class AverageOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Average-ontology";

    private static Ontology sTheInstance = new AverageOntology(BasicOntology.getInstance());

    public static Ontology getInstance(){
        return sTheInstance;
    }

    private AverageOntology(Ontology base) {
        super(ONTOLOGY_NAME, base, new ReflectiveIntrospector());
        try {
            PrimitiveSchema integerSchema = (PrimitiveSchema) getSchema(BasicOntology.INTEGER);
            PrimitiveSchema stringSchema = (PrimitiveSchema) getSchema(BasicOntology.STRING);

            PredicateSchema schema = new PredicateSchema(MessageContent.MESSSAGE_CONTENT);
            schema.add(MessageContent.NUMBER, integerSchema, ObjectSchema.MANDATORY);
            schema.add(MessageContent.COUNT, integerSchema, ObjectSchema.MANDATORY);
            schema.add(MessageContent.MESSAGE, stringSchema, ObjectSchema.MANDATORY);

            add(schema, MessageContent.class);
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}
