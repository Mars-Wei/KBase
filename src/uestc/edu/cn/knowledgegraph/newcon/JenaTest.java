package uestc.edu.cn.knowledgegraph.newcon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
public class JenaTest {
	private static final String DB_PATH = "E:/graduate/ontology112.db";
	private static final String ONTOLOGY_URL = "E:/graduate/owl/ontology.owl";
	public static int sum=0;
	public static void main(String arg[]){
	File file = new File(ONTOLOGY_URL);
	OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	try {
		ontModel.read(new FileInputStream(file), "");
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}
      //System.out.println(ontModel);
    GraphDatabaseService db = new GraphDatabaseFactory()
    .newEmbeddedDatabase(DB_PATH);
     // begin transaction
    Transaction tx = db.beginTx();
	try{
	Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", db);//得到owl中的root节点
	 //迭代本体库中的类
     for (Iterator i = ontModel.listClasses(); i.hasNext();) {
			OntClass c=(OntClass)i.next(); // 返回类型强制转换,本体库里面的class类
			
			//统计本体层的数目
			if(!c.equals(null)){sum++;}
			if(c.getSuperClass().getLocalName().equals("Thing")){
		    	String str=c.getURI();
				String strNow=str.substring(str.indexOf('#')+1);
				Node supclassNode=getOrCreateNodeWithUniqueFactory(strNow, db);//得到所有分类的节点
				//System.out.println(strNow);
			if(strNow.equals("人物")){
					System.out.println(c.getLocalName());}
			
				supclassNode.createRelationshipTo(thingNode, 
						 DynamicRelationshipType.withName("isA")); //添加isA关系到知识库中
			}
			
				for (Iterator it=c.listSubClasses();it.hasNext();) {
					//System.out.print("Class ");
					OntClass sb = (OntClass) it.next();
					//String str1=c.getModel().getGraph().getPrefixMapping().shortForm(c.getURI());
					String strSB=sb.getURI();
					String strSub=strSB.substring(strSB.indexOf('#') + 1);
					//System.out.println(strSub);
//					Node subclassNode=getOrCreateNodeWithUniqueFactory(strSub, db);//得到所有分类的子节点
//					subclassNode.createRelationshipTo(supclassNode,
//			                  DynamicRelationshipType.withName("isA"));  
//					System.out.println(strNow+ "'s suberClass is "
//							+ strSub);
				}// sub class ends

    }	
	 tx.success();
    } finally {
        tx.finish();
        }
  System.out.println("本体层数目:"+sum);
  System.out.println("加载完成");
  
	
	}
	

  private static Node getOrCreateNodeWithUniqueFactory(String nodeName, GraphDatabaseService graphDb) {
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
