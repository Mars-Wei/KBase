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
public class JenaOWL {
	private static final String DB_PATH = "E:/graduate/ontology05.db";
	private static final String ONTOLOGY_URL = "E:/graduate/owl/sample_instances.owl";
	//public int sum=0;//统计本体层的数目
  public void jenaToOwl(){
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
				OntClass c = (OntClass) i.next(); // 返回类型强制转换,本体库里面的class类
//				String str=c.getURI();
//				String strNow=str.substring(str.indexOf('#')+1);
				//System.out.println(strNow);
//				//统计本体层的数目
//				if(!c.equals(null)){sum++;}
				String strNow=c.getLocalName();
				Node supclassNode=getOrCreateNodeWithUniqueFactory(strNow, db);//得到所有分类的节点
			 //若当前类是Thing的子类，则链接到thingNode节点下
				 if(c.getSuperClass().getLocalName().equals("Thing")){
					supclassNode.createRelationshipTo(thingNode, 
							 DynamicRelationshipType.withName("isA"));
				}
		// 获取当前类的父类
//				for (Iterator it = c.listSuperClasses(); it.hasNext();){
//					OntClass sp = (OntClass) it.next();
////					String str = c.getModel().getGraph().getPrefixMapping().shortForm(c.getURI()) // 获取URI
////							+ "'s superClass is "; //得到当前类
//					String strSP = sp.getURI();
//					String strSup=strSP.substring(strSP.indexOf('#') + 1);
////					System.out.println(strNow+" supclass is"+strSup);
////					System.out.println(strSP.substring(strSP.indexOf('#') + 1));//父类
//					try { // 另一种简化处理URI的方法
//						str = str + ":"	+ strSP.substring(strSP.indexOf('#') + 1);
//						//System.out.println("Class " + str);
//					} catch (Exception e) {
//					}
//	             } // super class ends
//		  
		    // 得到当前类的子类
				for (Iterator it = c.listSubClasses(); it.hasNext();) {
					//System.out.print("Class ");
					OntClass sb = (OntClass) it.next();
					//String str1=c.getModel().getGraph().getPrefixMapping().shortForm(c.getURI());
//					String strSB=sb.getURI();
//					String strSub=strSB.substring(strSB.indexOf('#') + 1);
				  String strSub=sb.getLocalName();
					Node subclassNode=getOrCreateNodeWithUniqueFactory(strSub, db);//得到所有分类的子节点
					subclassNode.createRelationshipTo(supclassNode,
			                  DynamicRelationshipType.withName("isA"));  
//					System.out.println(strNow+ "'s suberClass is "
//							+ strSub);
				}// sub class ends
				
	     //得到当前类的对象属性和关系属性
//		      for (Iterator ipp = c.listDeclaredProperties(); ipp.hasNext();) {
//			     OntProperty p = (OntProperty) ipp.next();
//				 //对象属性
//				  if(p.isObjectProperty()){
//					System.out.println("     ObjectProperty:" + p.getLocalName());
//						}
//				  //关系属性
//				if(p.isDatatypeProperty()){
//				    System.out.println("    DatatypeProperty:" + p.getLocalName());
//					   }
//					}// property ends 
				
		 //得到类下面的实体，将实体和上层概念链接起来
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
	//  System.out.println("本体层数目:"+sum);
	 System.out.println("加载完成");
		
	}
	public static void main(String arg[]){
		long startTime=System.currentTimeMillis();   //获取开始时间
		JenaOWL jj=new JenaOWL();
		jj.jenaToOwl();
		long endTime=System.currentTimeMillis(); //获取结束时间
		double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	
	}
	
  private Node getOrCreateNodeWithUniqueFactory(String nodeName, GraphDatabaseService graphDb) {
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
