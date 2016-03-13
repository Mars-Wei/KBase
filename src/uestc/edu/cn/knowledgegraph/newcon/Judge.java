package uestc.edu.cn.knowledgegraph.newcon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/*
下面的是判断我们文件来的一个新的节点是不是能在我们的本题库中找到相应的节点，能够连接上去
*/

public class Judge {
	private static final String DB_PATH = "E:/Data/ontology1.db";
	static GraphDatabaseService graphDb;
	static NefjHelper dbhelper = new NefjHelper();
	
	/**
	 * 判断该网页内容是否为人
	 *
	 */
	public boolean isPeople(HashMap<String,ArrayList<String>> mulhash,HashMap<String,String> hashtwice) {
		
		String vocation="";
		String abstractString="";
		if(hashtwice.containsKey("职业")){
			vocation=hashtwice.get("职业");
		}
		
		if(hashtwice.containsKey("abstract")){
			abstractString=hashtwice.get("abstract");
		}
		
		
		boolean b=false;
		if(mulhash.containsKey("所属类型")){//网页中如果有所属类型
			ArrayList<String> tagList=mulhash.get("所属类型");
			//System.out.println(tagList.size());
			for(String tag:tagList){
				if(tag.contains("人")){
					b=true;
					break;
				}
			}
		}else if(vocation.contains("师")||abstractString.contains("师")||vocation.contains("者")||abstractString.contains("者")){
			b=true;
		}
		return b;
	}
	
	/**
	 * 判断txt文件是否为空
	 *
	 */
	public boolean judgeContentNull(String filePath) throws IOException {
		BufferedReader br=new BufferedReader(new FileReader(filePath));
		boolean isHasConten=false;
		String line="";
		while((line=br.readLine())!=null){
			isHasConten=true;
			break;
		}
		return isHasConten;
	}
	
	/**
	 * 下面的是用来判断是不是含有同意本体的
	 *
	 */
	public boolean isContainInfo(String sameMeanOntology,String strinformation) {
		boolean b=false;
		String str1[]=sameMeanOntology.split(",");
		for(String str:str1){
			if(strinformation.contains(str)){
				b=true;
				break;
			}
		}
		return b;
	}
//	public boolean isMillitaryPeople() {
//		dbhelper.createDb();
//		ArrayList<String> allExistedList=new ArrayList<String>();
//		OntologyNodeWordBag wordBag=new OntologyNodeWordBag();
//		Transaction tx = graphDb.beginTx();
//		try{
//			Iterator<Node> it = graphDb.getAllNodes().iterator();
//			while (it.hasNext()) {
//				Node nodemiddle = it.next();
//				if(nodemiddle.hasProperty("name")){
//					String nodemiddleString=nodemiddle.getProperty("实体").toString();
//					if(nodemiddleString=="军事人物"){
//						nodemiddle.setProperty("同义本体",wordBag.military);
//					}
//					
//	
//				}
//			}	
//			tx.success();
//		} finally {
//			tx.finish();
//		} 
//		return
//	}
	
	
	
	public static void main(String[] args) {//下面的是把军事人物相同含义的做为本体的叶子节点的属性添加进来
		//dbhelper.createDb();
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);// 如果连上次的
		// GraphDatabaseService graphDb=new
		// RestGraphDatabase("http://localhost:7474/db/data");
		NefjHelper.registerShutdownHook(graphDb);
		ArrayList<String> allExistedList=new ArrayList<String>();
		OntologyNodeWordBag wordBag=new OntologyNodeWordBag();
		Transaction tx = graphDb.beginTx();	
		try {
			Iterator<Node> it = graphDb.getAllNodes().iterator();

			while (it.hasNext()) {
				Node nodemiddle = it.next();
				try {
					if(nodemiddle.hasProperty("name")){
						String name = nodemiddle.getProperty("name").toString();
						System.out.println(name);
						if (name.length() > 0) {//中间可能生成点了，但是没有name   &&(indiviual!=nodemiddleString),else不能加在这里，因为是处理数据库里面所有的点
							if (name.equals("军事人物")){ 
							    nodemiddle.setProperty("同义本体",wordBag.military);;
								break;//找到就结束
							}
							
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
		
		//dbhelper.shutDown();
	}
//	public boolean isConnection(String individual) {
//		Transaction tx = graphDb.beginTx();
//		try {
//			Iterator<Node> it = graphDb.getAllNodes().iterator();
//
//			while (it.hasNext()) {
//				Node nodemiddle = it.next();
//				try {
//					String name = nodemiddle.getProperty("name").toString();
//					if (name.length() > 0) {//中间可能生成点了，但是没有name   &&(indiviual!=nodemiddleString)
//						if ((fenLei.contains(name))&&(!nodemiddle.hasRelationship(Direction.INCOMING, DynamicRelationshipType.withName("isA")))) {//找到了，但是要找到那个叶子节点,叶子节点没有向外的isA关系
//						//if ((fenLei.contains(name))) {
//							node = nodemiddle;
//							break;//找到就结束
//						}
//					}
//				
//				} catch (Exception e) {
//					e.printStackTrace();
//					// TODO: handle exception
//				}
//
//			}
//			tx.success();
//				
//			
//		} finally {
//			// TODO: handle exception
//		}
//	}
	
}
