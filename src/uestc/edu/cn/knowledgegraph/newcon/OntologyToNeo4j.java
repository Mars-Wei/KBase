package uestc.edu.cn.knowledgegraph.newcon;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.*;



import java.io.File;
import java.util.Map;

//将本体存入图数据库

//http://neo4j.com/blog/and-now-for-something-completely-different-using-owl-with-neo4j/?_ga=1.142115894.526256345.1411285427
public class OntologyToNeo4j {

	/**
	 * @param args
	 */
	private static final String DB_PATH = "E:/Data/ontology3.db";
	private static final String ONTOLOGY_URL = "E:/Data/people1.owl";

	public static void main(String[] args) throws Exception {
		// Get hold of an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File(ONTOLOGY_URL);

		// Load the local copy
		OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file);
		OntologyToNeo4j  ontology=new OntologyToNeo4j();
		
		ontology.importOntology(localPizza);
//		System.out.println(localPizza);
		
	}

	
	//下面的是具体的方法
	public  void importOntology(OWLOntology ontology) throws Exception {
		
		OWLReasoner reasoner = new Reasoner(ontology);
		

		if (!reasoner.isConsistent()) {
			// Throw your exception of choice here
			throw new Exception("Ontology is inconsistent");
		}

		// database configuration
		GraphDatabaseService db = new GraphDatabaseFactory()
				.newEmbeddedDatabase(DB_PATH);

		// begin transaction
        Transaction tx = db.beginTx();
        try {
        	org.neo4j.graphdb.Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", db);//得到owl中的root节点
        	//org.neo4j.graphdb.Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing");
        	//Get all the classes defined in the ontology and add them to the graph.这里是拿到所有的分类
        	for (OWLClass c :ontology.getClassesInSignature(true)) {
                String classString = c.toString();//函数设计的时候是带#的
//                System.out.println(classString);
                if (classString.contains("#")) {
                    classString = classString.substring(
                     classString.indexOf("#")+1,classString.lastIndexOf(">"));
                }
                org.neo4j.graphdb.Node classNode = getOrCreateNodeWithUniqueFactory(classString, db);//把owl中的string拿出来存到neo4j中的点中
             
                /*下面的
              *Find out if they have any super classes. If they do, link them. 
              If they don’t, link back to owl:Thing. Make sure only to link to 
              the direct super classes! The relationship type used to express the 
              rdf:type property is a custom one named “isA”
              * */
                
                NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(c, true);//判断有没有上级的class

                if (superclasses.isEmpty()) {
                    classNode.createRelationshipTo(thingNode,
                     DynamicRelationshipType.withName("ISA"));    
                } else {
                    for (org.semanticweb.owlapi.reasoner.Node<OWLClass>
                     parentOWLNode: superclasses) {
                        
                        OWLClassExpression parent =
                         parentOWLNode.getRepresentativeElement();
                        String parentString = parent.toString();
                        
                        if (parentString.contains("#")) {
                            parentString = parentString.substring(
                             parentString.indexOf("#")+1,
                             parentString.lastIndexOf(">"));
                        }
                        org.neo4j.graphdb.Node parentNode =
                         getOrCreateNodeWithUniqueFactory(parentString, db);
                        classNode.createRelationshipTo(parentNode,
                         DynamicRelationshipType.withName("ISA"));
                    }
                }
                
                /*Now for each class, get all the individuals. Create nodes and 
                 * link them back to their parent class，也就是把实例和父概念联系起来,下面
                 * 的和上面的一样也是下那是得到owl中存储的实体，然后和父概念联系起来
                 */                
                for (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual> in
                 : reasoner.getInstances(c, true)) {
                    OWLNamedIndividual i = in.getRepresentativeElement();
                    String indString = i.toString();
                    System.out.println(indString);
                    if (indString.contains("#")) {
                        indString = indString.substring(
                         indString.indexOf("#")+1,indString.lastIndexOf(">"));
                    }
                    org.neo4j.graphdb.Node individualNode = 
                     getOrCreateNodeWithUniqueFactory(indString, db);
                                             
                    individualNode.createRelationshipTo(classNode,
                    DynamicRelationshipType.withName("isA"));//建立动态的关系
                  
                    
                    
                    /*For each individual, get all object properties and all data
                     *properties. Add them to the graph as node properties or
                     * relationships. Make sure to get all axioms, not just the 
                     * asserted ones.下面是把所有实体的属性存储在neo4j里面，这里也是把属性作为关系存储在图数据库中
                     * */
                    
                    for (OWLObjectPropertyExpression objectProperty:
                     ontology.getObjectPropertiesInSignature()) {
                       for  
                       (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual> 
                        object: reasoner.getObjectPropertyValues(i,
                        objectProperty)) {
                            String reltype = objectProperty.toString();
                            reltype = reltype.substring(reltype.indexOf("#")+1,
                             reltype.lastIndexOf(">"));
                            
                            String s =
                             object.getRepresentativeElement().toString();
                            s = s.substring(s.indexOf("#")+1,
                             s.lastIndexOf(">"));
                            org.neo4j.graphdb.Node objectNode =
                             getOrCreateNodeWithUniqueFactory(s, db);
                            individualNode.createRelationshipTo(objectNode,
                             DynamicRelationshipType.withName(reltype));
                        }
                    }

                    //下面是给每个节点设置图数据库中的属性
                    for (OWLDataPropertyExpression dataProperty:
                     ontology.getDataPropertiesInSignature()) {

                        for (OWLLiteral object: reasoner.getDataPropertyValues(
                         i, dataProperty.asOWLDataProperty())) {
                            String reltype =
                             dataProperty.asOWLDataProperty().toString();
                            reltype = reltype.substring(reltype.indexOf("#")+1, 
                             reltype.lastIndexOf(">"));
                            
                            String s = object.toString();
                            individualNode.setProperty(reltype, s);
               
                        }
                    }
                }
            }
            tx.success();
        } finally {
            tx.finish();
        }
        
        System.out.print("done");
		
		db.shutdown();
	}
	
	
	private static org.neo4j.graphdb.Node getOrCreateNodeWithUniqueFactory(String nodeName, GraphDatabaseService graphDb) {
        UniqueFactory<org.neo4j.graphdb.Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "index") {
			@Override
			protected void initialize(org.neo4j.graphdb.Node created,
					Map<String, Object> properties) {
				created.setProperty("name", properties.get("name"));
				created.setProperty("URI","http://uestc/edu/cn/knowledgegraph/"+properties.get("name"));
			}
        };

        return factory.getOrCreate("name", nodeName);
    }

}
