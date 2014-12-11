package AverageMAS.CyclesOntology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.AggregateSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

/**
 * Created by User on 12/4/2014.
 */
public class CyclesOntology extends Ontology {
    public static final String ONTOLOGY_NAME = "Cycles-ontology";

    private static Ontology sTheInstance = new CyclesOntology(BasicOntology.getInstance());

    public static Ontology getInstance(){
        return sTheInstance;
    }

    private CyclesOntology(Ontology base) {
        super(ONTOLOGY_NAME, base, new ReflectiveIntrospector());
        try {
            PrimitiveSchema stringSchema = new PrimitiveSchema(BasicOntology.STRING);
            AggregateSchema list = new AggregateSchema(BasicOntology.SEQUENCE);

            PredicateSchema schema = new PredicateSchema(CyclesMessage.CYCLES_MESSAGE);
            schema.add(CyclesMessage.PATH, list, ObjectSchema.MANDATORY);
            schema.add(CyclesMessage.MESSAGE, stringSchema, ObjectSchema.MANDATORY);
            schema.add(CyclesMessage.NAME, stringSchema, ObjectSchema.MANDATORY);

            add(schema, CyclesMessage.class);
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}
