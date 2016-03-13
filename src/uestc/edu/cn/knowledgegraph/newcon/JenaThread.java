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
class JenaThread {
	String DB_PATH = "E:/graduate/ontology11.db";
    String ONTOLOGY_URL = "E:/graduate/owl/ontology.owl";
    File file = new File(ONTOLOGY_URL);
    OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
      //System.out.println(ontModel);
    { try {
		ontModel.read(new FileInputStream(file), "");
	} catch (FileNotFoundException e1) {
		e1.printStackTrace();
	}
    }
    GraphDatabaseService db = new GraphDatabaseFactory()
    .newEmbeddedDatabase(DB_PATH);
     // begin transaction
    Transaction tx = db.beginTx();
  //处理子类的进程
    class thread1 implements Runnable{
    	@Override
    	public void run() {
    		
    		// TODO Auto-generated method stub
    		
    		
    		try{
    		Node thingNode = getOrCreateNodeWithUniqueFactory("owl:Thing", db);//得到owl中的root节点
    		 //迭代本体库中的类
    	     for (Iterator i = ontModel.listClasses(); i.hasNext();) {
    				OntClass c = (OntClass) i.next(); // 返回类型强制转换,本体库里面的class类
    				String strNow=c.getLocalName();
    				Node supclassNode=getOrCreateNodeWithUniqueFactory(strNow, db);//得到所有分类的节点
    			 //若当前类是Thing的子类，则链接到thingNode节点下
    				 if(c.getSuperClass().getLocalName().equals("Thing")){
    					supclassNode.createRelationshipTo(thingNode, 
    							 DynamicRelationshipType.withName("isA"));
    				}
    		    // 得到当前类的子类
    				for (Iterator it = c.listSubClasses(); it.hasNext();) {
    					//System.out.print("Class ");
    					OntClass sb = (OntClass) it.next();
    				    String strSub=sb.getLocalName();
    					Node subclassNode=getOrCreateNodeWithUniqueFactory(strSub, db);//得到所有分类的子节点
    					subclassNode.createRelationshipTo(supclassNode,
    			                  DynamicRelationshipType.withName("isA"));  
    				}// sub class ends
    		
    	     }	
    		 tx.success();
    	    } finally {
    	        tx.finish();
    	        }
    	}
    }
  //处理关系和属性的进程 
    class thread2 implements Runnable{
    	@Override
    	public void run() {
 
    		try{
    		 //迭代本体库中的类
    	     for (Iterator i = ontModel.listClasses(); i.hasNext();) {
    				OntClass c = (OntClass) i.next(); // 返回类型强制转换,本体库里面的class类
    				String strNow=c.getLocalName();
    				Node supclassNode=getOrCreateNodeWithUniqueFactory(strNow, db);//得到所有分类的节点
    				 for(Iterator it=c.listInstances();it.hasNext();)
    					{
    						Individual in=(Individual)it.next();
    						if(!in.isAnon()){
    							String name=in.getLocalName();
    							//System.out.println(name);
    						 Node entityNode=getOrCreateNodeWithUniqueFactory(name,db);//得到实体节点
    						 entityNode.createRelationshipTo(supclassNode, 
    								 DynamicRelationshipType.withName("isA"));
    					 for (Iterator ipp = c.listDeclaredProperties(); ipp.hasNext();) {
    					     OntProperty p = (OntProperty) ipp.next();	
    					     //得到实体的关系
    			        if(p.isObjectProperty()){
    					    	 String relation=p.getLocalName();
    					    	 if(in.getPropertyValue(p)!=null)
    					    	 {
    					         String re=in.getPropertyValue(p).toString();
    					    	 // System.out.println(re);
    					    	 String relentity=re.substring(re.indexOf('#') + 1);
    					    	// System.out.println(relation+":"+relentity);
    					    	 Node relNode=getOrCreateNodeWithUniqueFactory(relentity,db);//得到相关实体节点
    					    	 entityNode.createRelationshipTo(relNode, 
    									 DynamicRelationshipType.withName(relation));
    					    	 }
    					     }//end relation
    					     
    					     //得到实体的基本属性
    			  if(p.isDatatypeProperty()){
    					String proper=p.getLocalName();
    					if(in.getPropertyValue(p)!=null)
    					    {
    					     String pro=in.getPropertyValue(p).toString();
    					     String relproper=pro.substring(pro.indexOf('#')+1);
    					    	  // System.out.println(proper+":"+relproper);
    					    	   entityNode.setProperty(proper, relproper);  
    					       } 
    					     }//end Property
    					  }//end for	 		 
    			     	}//end in
    				  }//end for
    	     }	
    		 tx.success();
    	    } finally {
    	        tx.finish();
    	     }
    	}	
    }
   public Node getOrCreateNodeWithUniqueFactory(String nodeName, GraphDatabaseService graphDb) {
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

 class test{
		public static void main(String arg[]){
			 Thread thread1 = new Thread(new JenaThread().new thread1());
			  Thread thread2 = new Thread(new JenaThread().new thread2());
			  long startTime=System.currentTimeMillis();   //获取开始时间
			  thread1.start();
			  thread2.start();
			  long endTime=System.currentTimeMillis(); //获取结束时间
			 double minute=(endTime-startTime)/1000.0;
			 System.out.println("程序运行时间： "+minute+"s");}
		
}

 