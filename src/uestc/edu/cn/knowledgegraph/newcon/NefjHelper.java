package uestc.edu.cn.knowledgegraph.newcon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ICTCLAS.I3S.AC.*;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;
import org.neo4j.rest.graphdb.RestGraphDatabase;




import org.semanticweb.HermiT.debugger.commands.ExitCommand;


//import TraverseAndQuery.RelTypes;

public class NefjHelper {

	private static final String DB_PATH = "E:/Data/KnowledgeBase.db";
	OntologyNodeWordBag ontologyWordBag=new OntologyNodeWordBag();
	// private static final String DB_PATH="http://127.0.0.1/db/data";

	public String greeting;

	// START SNIPPET: vars
	static GraphDatabaseService graphDb;
	Relationship relationship;
	Judge judge=new Judge();
	// END SNIPPET: vars

	// START SNIPPET: createReltype
	// String
	private static enum RelTypes implements RelationshipType {
		KNOWS

	}

	// END SNIPPET: createReltype
	
	/**
	 * 判断数据库中是否存在该点，如果存在则返回该点，不存在则返回null
	 * 判断方法：先比较实体名是否相同，再比较所属类型是否完全一致
	 */
//	public Node getAlreadyAddedNodes(String str,ArrayList<String> leiXingList) {
//		//ArrayList<String> leiXingList=new ArrayList<String>();
//		String leiXing="";//中间点的类型
//		String allLeiXing="";//要处理点的类型
//		ArrayList<Node> candidate=new ArrayList<Node>();//候选实体
//		
//		for(String temp:leiXingList){
//			allLeiXing=allLeiXing+temp+" ";
//		}
// 
//		Transaction tx = graphDb.beginTx();
//		try{
//			Iterator<Node> it = graphDb.getAllNodes().iterator();
//			while (it.hasNext()) {
//				Node nodemiddle = it.next();
//				if(nodemiddle.hasProperty("实体")){
//					
//					String nodemiddleString=nodemiddle.getProperty("实体").toString();
//					leiXing=nodemiddle.getProperty("所属类型").toString();
//					//如果数据库中存在该实体，则返回false
//					if(str.equals(nodemiddleString)){
//						if(allLeiXing.trim().equals(leiXing.trim())){//需修改%%%%%%%%%%%%%%%%%%%%%%%%
//						return nodemiddle;
//						}					
//					}
//				}
//			}	
//			tx.success();
//		} finally {
//			tx.finish();
//		} 
//	
//		return null;//如果是新加节点或者是在数据库中没有重复的则返回true；表明里面没有相同的节点
//	}
	
	public Node getAlreadyAddedNodes(String individual,HashMap<String,ArrayList<String>> hashmul,
			HashMap<String,String> hashtwice,String zhaiYao) {

		String line=null;
		int num=0;
		ArrayList<Node> candidate=new ArrayList<Node>();//候选实体
		ArrayList<String> WordList =new ArrayList();//用于存储特征词
		ArrayList<String> WordList2=new ArrayList();//用于存储数据库中实例的特征词
		//去掉同名实体后的括号
		 if(individual.indexOf("（")>0){
			 individual=individual.substring(0, individual.indexOf("（"));
	    }
		individual = individual.replaceAll("[\\pP‘’“”]", "");//删除标点字符
		individual = individual.replaceAll("[\\pZ‘’“”]", "");//删除分隔符

		Transaction tx = graphDb.beginTx();
		
		try{
			//遍历数据库查找同名实体
			Iterator<Node> it = graphDb.getAllNodes().iterator();
			while (it.hasNext()) {
				Node NodeMiddle = it.next();
				//选取同名实体作为候选实体
				if(NodeMiddle.hasProperty("实体")){
					String nodemiddleString=NodeMiddle.getProperty("实体").toString();
					 if(nodemiddleString.indexOf("（")>0){//去掉同名实体后的括号
						 nodemiddleString=nodemiddleString.substring(0, nodemiddleString.indexOf("（"));
				     }
					nodemiddleString = nodemiddleString.replaceAll("[\\pP‘’“”]", "");
					nodemiddleString = nodemiddleString.replaceAll("[\\pZ‘’“”]", "");
					
					if(individual.equals(nodemiddleString)){
						candidate.add(NodeMiddle);
						num++;
					}
				}				
			}
			if(candidate.isEmpty()){//数据库中没有同名实体，返回null
				System.out.println("没有找到同名实体");
				return null;
			}
				
//			else{//列出同名实体
//				System.out.println("\n"+"以下为"+individual+"的同名实体：");
//				ListIterator<Node> listIt=candidate.listIterator();
//				while(listIt.hasNext()){
//					Node node = listIt.next();
//				    System.out.println(node.getProperty("实体"));
////				    System.out.println(node.getProperty("abstract"));
//				}
//			}
			
			//构造该实体的特征词
			TextProcessing similarity = new TextProcessing();
			if(zhaiYao!=null){
			WordList = similarity.SegmentProcess(zhaiYao);
			}
			WordList.addAll(hashmul.get("所属类型"));
			
			System.out.println("\n"+"以下为"+individual+"的同名实体：");
			
			//为每个候选实体计算相似度
			for(int i=0;i<num;i++){
				
				WordList2.clear();
				
				Node candidateNode = candidate.get(i);
				if(candidateNode.hasProperty("abstract")){
				WordList2 = similarity.SegmentProcess(candidateNode.getProperty("abstract").toString());
				}
				if(candidateNode.hasProperty("所属类型")){
				String NodeType[] = candidate.get(i).getProperty("所属类型").toString().split("\\s+");
				for(int j=0;j<NodeType.length;j++){
					if(NodeType[j].equals(""))continue;
					WordList2.add(NodeType[j]);
				}
				}
				

				String[] array1 = (String[])WordList.toArray(new String[0]);
				String[] array2 = (String[])WordList2.toArray(new String[0]);
				WordList.addAll(WordList2);
//				WordList.retainAll(WordList2);//只保留在WordList2里存在的数据，等于交集
//				WordList2.retainAll(WordList);//只保留在WordList里存在的数据，等于交集
				//删除WordList中相同元素
				WordList=(ArrayList<String>) similarity.removeDuplicateWithOrder(WordList);
				int[] wordNum1 = new int[WordList.size()];
				int[] wordNum2 = new int[WordList.size()];

				
				ListIterator<String> It=WordList.listIterator();
				int k=0;
				while(It.hasNext()){
		
					String word = It.next();
					for(int m=0;m<array1.length;m++){
						if(word.equals(array1[m]))
							wordNum1[k]++;

					}
					for(int m=0;m<array2.length;m++){
						if(word.equals(array2[m]))
							wordNum2[k]++;
					}
					k++;
				}
				int numerator = 0;
				int denominator1 = 0;
				int denominator2 = 0;
				for(int m=0;m<WordList.size();m++){
					
					numerator+=wordNum1[m]*wordNum2[m];
					denominator1+=Math.pow(wordNum1[m],2);
					denominator2+=Math.pow(wordNum2[m],2);
					}
				
				Node node = candidate.get(i);
				System.out.print(node.getProperty("实体")+"···");
				
				double sim = numerator/(Math.sqrt(denominator1)*Math.sqrt(denominator2));
				System.out.println(sim);	
			}
			
			
			System.out.println(candidate.get(0).getPropertyKeys());
			Iterator<String> it1 = candidate.get(0).getPropertyKeys().iterator();
			while(it1.hasNext()){
				String name = it1.next();
				System.out.print(name+":");
				System.out.println(candidate.get(0).getProperty(name));
			}
			
			
		
//			ListIterator<String> it2=WordList.listIterator();
//			while(it2.hasNext()){
//				String node = it2.next();
//			    System.out.println(node);
//			}
			
			
			tx.success();
		} finally {
			tx.finish();
			
			
		} 	
		return null;//如果是新加节点或者是在数据库中没有重复的则返回true；表明里面没有相同的节点
	}

	/**
	 * 确定创建的新的节点将要链接到的本体的叶子节点
	 * 通过下面的几个看看能不能找到本体中的点
	 * 1分类标签2职业3abstract4词袋
	 * fenlei是具体实体的分类，name才是本体的
	 * 
	 */
	public HashMap<Node,Boolean> getEndNodeByFenlei(ArrayList<String> fenleiList,HashMap<String,String> hashtwice,String zhaiYao) {
		String vocation="";//通过获取其职业来联系到本体
		String fenLei="";
		boolean isFindOntology=false;
		boolean isMillitary=false;
		//如果一个实体，没有abstract，下面需要用到，所以必须加上一些字，不然会报错
		if(zhaiYao==null){
			zhaiYao=zhaiYao+"摘要是空的";
		}
		
		HashMap<Node,Boolean> returnhashmap=new HashMap<Node,Boolean>();
		
		for(String fen:fenleiList){
				fenLei=fenLei+fen;
		}
	
		//使用下面的原因是如果下面的是空的话，程序中的contains就会出现问题
				
		if(hashtwice.containsKey("职业")){
			vocation=hashtwice.get("职业");
		}
		//如果一个实体，没有abstract，下面需要用到，所以必须加上一些字，不然会报错
		if(vocation==null){
			vocation=vocation+"职位是空的";
		}
		
		Transaction tx = graphDb.beginTx();
		Node node =null;
		try {
			Iterator<Node> it = graphDb.getAllNodes().iterator();

			while (it.hasNext()) {
				Node nodemiddle = it.next();
				try {
					if(nodemiddle.hasProperty("name")){
						String name = nodemiddle.getProperty("name").toString();
						//下面的三种情况最好不要用else if因为我们先用分类遍历所有的点之后，再用职位遍历，最后用摘要遍历
						if (name.length() > 0) {//中间可能生成点了，但是没有name   &&(indiviual!=nodemiddleString),else不能加在这里，因为是处理数据库里面所有的点
							
//							isMillitary=judge.isContainInfo(ontologyWordBag.military,name);
							//找到了，但是要找到那个叶子节点,叶子节点没有向外的isA关系
							if (((fenLei.contains(name))&&(!nodemiddle.hasRelationship(Direction.INCOMING, DynamicRelationshipType.withName("ISA"))))||isMillitary) {
							//if ((fenLei.contains(name))) {//这个是通过开放分类确定本体的叶子节点
								node = nodemiddle;
								System.out.println("通过分类加进来的---"+fenLei);
								
								isFindOntology=true;
								break;//找到就结束
							}
							if ((vocation.contains(name)&&(!nodemiddle.hasRelationship(Direction.INCOMING, DynamicRelationshipType.withName("ISA"))))||isMillitary) {
								//这个是通过职位确定本体的叶子节点
								node = nodemiddle;
								System.out.println("通过职位加进来的---"+vocation);
								isFindOntology=true;
								break;//找到就结束
							}
							if ((zhaiYao.contains(name)&&(!nodemiddle.hasRelationship(Direction.INCOMING, DynamicRelationshipType.withName("ISA"))))||isMillitary) {
								//这个是通过摘要确定本体的叶子节点
								node = nodemiddle;
								System.out.println("通过摘要加进来的---"+zhaiYao);
								isFindOntology=true;
								break;//找到就结束
							}
//							else{//找不到的就结束，else不能加在这里，因为是处理数据库里面所有的点
//								isCanSettle=false;
//								System.out.println("暂时没有找到合适的本体，没有联系到数据库");
//								break;
//							}
							
						}
						
					}
						
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				}
				
			}
			tx.success();
		} finally {
			tx.finish();
		}
		returnhashmap.put(node,isFindOntology);
		return returnhashmap;
		
	}

	public void createDb() {
		// deleteFileOrDirectory(new File(DB_PATH));
		// START SNIPPET: startDb
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);// 如果连上次的
		// GraphDatabaseService graphDb=new
		// RestGraphDatabase("http://localhost:7474/db/data");
		registerShutdownHook(graphDb);
		
	}
	
	/**
	 * 该实体存在于数据库，则丰富该实体属性
	 * 
	 */
	public void AddNodeProp(Node node, HashMap<String, String> props,HashMap<String,ArrayList<String>> muiltyValues){
		
		Transaction tx = graphDb.beginTx();
		ArrayList<String> keysettwice = new ArrayList<String>();
		ArrayList<String> valuesettwice = new ArrayList<String>();
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next(); // key
			keysettwice.add(key);// 属性作为relation
			valuesettwice.add(props.get(key));// 属性值作为node

		}
		  ArrayList<String> str = new ArrayList<String>();
		  Iterator it2 =muiltyValues.keySet().iterator(); 

		   int k=0;
	        while(it2.hasNext()) 
	        {  
	        	String key = (String)it2.next();
	            str.add(k,key);//作为第几个元素存储
	            k++;	            
	        }
	        
			try {
				for (int i = 0; i < valuesettwice.size(); i++) {//一个key一个value的创建节点
					//如果实体不存在该属性，则添加
					if(node.hasProperty(keysettwice.get(i)))
						continue;
					else{
					node.setProperty(keysettwice.get(i), valuesettwice.get(i));
					}

				}

				for(int i=0;i<k;i++){//存储一对多的属性
					String ss="";
					if(node.hasProperty(str.get(i)))
						continue;
					else{
						for(int n=0;n<muiltyValues.get(str.get(i)).size();n++){
							ss=ss+" "+muiltyValues.get(str.get(i)).get(n);
						}
						
						node.setProperty(str.get(i),ss);
					}
					
				}
					
			
			 
				tx.success();

			} finally {
				tx.finish();
			}
	}
	/**
	 * 创建新节点，增加节点属性
	 */
	public Node CreateNodeAndRelation(String name, HashMap<String, String> props,HashMap<String,ArrayList<String>> muiltyValues) 
	{
		Transaction tx = graphDb.beginTx();
		Node node = null;
		ArrayList<String> keysettwice = new ArrayList<String>();
		ArrayList<String> valuesettwice = new ArrayList<String>();
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next(); // key
			keysettwice.add(key);// 属性作为relation
			valuesettwice.add(props.get(key));// 属性值作为node

		}
		  ArrayList<String> str = new ArrayList<String>();
		  Iterator it2 =muiltyValues.keySet().iterator(); 

		   int j=0;
		   int k=0;
	        while(it2.hasNext()) 
	        {  
	        	String key = (String)it2.next();
	            str.add(j,key);//做为第几个元素存储
	            j++;
	            k++;
	        // System.out.println(k);
	        // System.out.println(key + " -- "+muiltyValues.get(key)); 
	            
	        }
	        
		try {

			node = graphDb.createNode();
			node.setProperty("实体", name);
			node.setProperty("URI","http://uestc/edu/cn/knowledgegraph/"+name);
			for (int i = 0; i < valuesettwice.size(); i++) {//一个key一个value的创建节点
				
				node.setProperty(keysettwice.get(i), valuesettwice.get(i));

			}

//			for (int i = 0; i < nodes.size(); i++) {
//				if(nodes.get(i).getProperty("name")!=""){//可能上面抽取的信息没有，所以就就没有内容
//					relationship = node.createRelationshipTo(nodes.get(i),
//					DynamicRelationshipType.withName(keysettwice.get(i)));
//				}
//				
//				// System.out.println(keysettwice.get(i)+"--"+valuesettwice.get(i));
//				 
//			}

			for(int i=0;i<k;i++){
			    //将数组里面的值当做一个节点存储
				String ss="";
				for(int n=0;n<muiltyValues.get(str.get(i)).size();n++){
					ss=ss+" "+muiltyValues.get(str.get(i)).get(n);
				}
				
				node.setProperty(str.get(i),ss);
			}
		 
			tx.success();

		} finally {
			tx.finish();
		}
		return node;
	}
	
	/**
	 * 将新节点与本体节点连接，关系为"实例"
	 */
	public void CreateRelation(Node ontologynode, Node fenleinode) {
		Transaction tx = graphDb.beginTx();

		try {
			//System.out.println(ontologynode.getProperty("name"));
			relationship = ontologynode.createRelationshipTo(fenleinode,
					DynamicRelationshipType.withName("ISA"));

			tx.success();
		} finally {
			tx.finish();
		}
	}
	

	/**
	 * 删除无效的节点,也就是那些没有任何信息的点，没有name属性，没有relation
	 */
	public void deleteInvalidNodes() {
		Transaction tx = graphDb.beginTx();
		Node node = null;
		try {
			Iterator<Node> it = graphDb.getAllNodes().iterator();
			while (it.hasNext()) {
				Node nodemiddle = it.next();
				//中间可能生成点了，但是没有name   &&(indiviual!=nodemiddleString)
				if (!(nodemiddle.hasProperty("name"))&&(!(nodemiddle.hasRelationship()))) {
						nodemiddle.delete();
				}
			}
			tx.success();
		}finally {
			tx.finish();
		}
		//System.out.println("^^^^^^^^^^^^^^");
		//System.out.println(graphDb);
	}

//	public Traverser getFriends(final Node person) {
//
//		TraversalDescription td = Traversal.description().breadthFirst()
//		// 这里是广度优先，也可以定义为深度优先遍历
//
//				// 这里定义边类型必须为KNOWS，且必须都为出边
//
//				.relationships(RelTypes.KNOWS, Direction.OUTGOING)
//
//				// 排除开始顶点
//
//				.evaluator(Evaluators.excludeStartPosition());
//
//		return td.traverse(person);
//
//	}
//
//	public void printNodeFriends(Node node) {
//
//		int friendsNumbers = 0;
//
//		System.out.println(node.getProperty("name") + "'s friends:");
//
//		for (Path friendPath : getFriends(node)) {
//
//			System.out.println("At depth " + friendPath.length() + " => "
//
//			+ friendPath.endNode().getProperty("name"));
//
//			friendsNumbers++;
//
//		}
//
//		System.out.println("Number of friends found: " + friendsNumbers);
//
//	}

	public void shutDown() {
		System.out.println("Shutting down database ...");
		// START SNIPPET: shutdownServer
		graphDb.shutdown();
		// END SNIPPET: shutdownServer
	}

	// START SNIPPET: shutdownHook
	static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	// END SNIPPET: shutdownHook
	public Node geiNode() {
		Node node=null;
		return node;
	}
	
	private static void deleteFileOrDirectory(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					deleteFileOrDirectory(child);
				}
			}
			file.delete();
		}
	}
}