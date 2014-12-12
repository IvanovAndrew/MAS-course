package LocalVoting;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

/**
 * Created by User on 12/12/2014.
 */
public class MyOntology extends Ontology{
    public static final String ONTOLOGY_NAME = "My-ontology";

    private static Ontology sTheInstance = new MyOntology(BasicOntology.getInstance());

    public static Ontology getInstance(){
        return sTheInstance;
    }

    private MyOntology(Ontology base) {
        super(ONTOLOGY_NAME, base, new ReflectiveIntrospector());
        try {
            PrimitiveSchema floatSchema = (PrimitiveSchema) getSchema(BasicOntology.FLOAT);

            PredicateSchema schema = new PredicateSchema(MessageContent.MESSSAGE_CONTENT);
            schema.add(MessageContent.NUMBER, floatSchema, ObjectSchema.MANDATORY);

            add(schema, MessageContent.class);
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}
