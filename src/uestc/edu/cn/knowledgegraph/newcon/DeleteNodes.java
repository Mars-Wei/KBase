package uestc.edu.cn.knowledgegraph.newcon;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;


public class DeleteNodes {
	static GraphDatabaseService graphDb;
	public void deleteInvalidNodes() {//删除无效的节点
		Transaction tx = graphDb.beginTx();
		Node node = null;
		try {
			Iterator<Node> it = graphDb.getAllNodes().iterator();
			while (it.hasNext()) {
				Node nodemiddle = it.next();
					String name = nodemiddle.getProperty("name").toString();
					if (name.length() <0) {//中间可能生成点了，但是没有name   &&(indiviual!=nodemiddleString)
						nodemiddle.delete();
					}
			}
			tx.success();
		}finally {
			tx.finish();
		}
		System.out.println("^^^^^^^^^^^^^^");
		//System.out.println(graphDb);
	}
}
