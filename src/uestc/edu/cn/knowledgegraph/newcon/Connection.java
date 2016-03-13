package uestc.edu.cn.knowledgegraph.newcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



import org.neo4j.graphdb.Node;

public class Connection {

	//调用BaiKeFunction得到实体属性
	public HashMaps getFromText(String filePath) {
		
		BaiKeFunction baike=new BaiKeFunction();
		HashMaps hashmaps=baike.extraHTMLText(filePath);
		return hashmaps;
	}

	public static void main(String[] args) throws IOException {
		
		String dir = "E:/Data/alldata";
		String unSettlePath="E:/Data/alldata/未处理文件.txt";
		BufferedWriter bw=new BufferedWriter(new FileWriter(unSettlePath,true));
		Connection con = new Connection();
		NefjHelper dbhelper = new NefjHelper();
		Judge judgeClass=new Judge();
		int allfiles=0;//所有网页的个数
		int unsettle=0;//未处理的网页个数
		int unstandard=0;//不是标准网页的个数
//		int unpeople=0;//非人物百科网页的个数
		int alreadyadded=0;//数据库中已存在的节点个数
		int addNode=0;//新添加的实体个数
		HashMaps hashmaps;//保存实体以及属性
		
		ArrayList<String> alreadyAdded=new ArrayList<String>();
		HashMap<Node,Boolean> hashmapOntology=new HashMap<Node,Boolean>();
		dbhelper.createDb();
		
		String[] files ={"测试"};
//		String[] files = {"初唐四杰","汉赋四大家","建安七子","江南四大才子","将军","金陵十二钗","六朝四大家"
//				,"诺贝尔文学奖得主","唐宋八大家","桐城三祖","网络作家","扬州八怪","一百零八将","语言学家","元曲四大家","竹林七贤","著名作家"};
		String filePath="";
		
		//获得每个txt文件路径
		for (String file : files) {
			File f = new File(dir+File.separator+file);
			for (File fi : f.listFiles()) {
				filePath=fi.toString();
				System.out.println(filePath);
				allfiles++;
				
				//判断txt文件是否为空
				boolean isHasContent=judgeClass.judgeContentNull(filePath);
				if(!isHasContent){
					continue;
				}
				
				//调用BaiKeFunction抽取实体以及属性
				hashmaps = con.getFromText(filePath);

				HashMap<String, String> hashtwice=new HashMap<String,String>();
				HashMap<String,ArrayList<String>> hashmul=new HashMap<String,ArrayList<String>>();
				hashtwice=hashmaps.getHashMapString();//一个key，一个value
				hashmul=hashmaps.getHashMapArray();//一个key，多个value,key为：所属类型、同名实体

				//得到该网页的描述主体
				String individual=hashtwice.get("实体");
				
				//如果是异常的网页就不执行这个网页了
				if((individual==null)){
					System.out.println("该网页不是标准的百科网页,抽取有问题");
					unstandard++;
					continue;
				}
				else if(individual.equals("")){
					System.out.println("该网页不是标准的百科网页,抽取有问题");
					unstandard++;
					continue;
				}

				System.out.println(individual);
				
				//如果不是人的话，就直接过滤掉，这个不能放在上面，因为放在上面有可能上面不是标准的网页，会报错
//				if(!judgeClass.isPeople(hashmul,hashtwice)){
//					System.out.println("该网页不是人的，过滤掉了");
//					unpeople++;
//					continue;
//				}
				
				String zhaiYao=hashtwice.get("ABSTRACT");
				//判断数据库中是否存在该点，如果存在则返回该点，不存在则返回null
				Node flagNode=dbhelper.getAlreadyAddedNodes(individual,hashmul,hashtwice,zhaiYao);
				
				
//				//如果数据库中没有该节点则创建
//				if(flagNode==null){
//					//查找要连接的本体的叶子节点	
//					hashmapOntology=dbhelper.getEndNodeByFenlei(hashmul.get("所属类型"),hashtwice,zhaiYao);
//					Node ontologynode =null;
//					boolean b=false;
//					Iterator it = hashmapOntology.keySet().iterator();
//					
//					while (it.hasNext()) {
//						//要连接的本体的节点
//						ontologynode = (Node) it.next(); 
//						b=hashmapOntology.get(ontologynode);
//					}
//					if(b){
//						//创建新的节点
//						Node fenleinode = dbhelper.CreateNodeAndRelation(individual, hashtwice,hashmul);
//                        //将新节点连接到本体节点
//						dbhelper.CreateRelation(ontologynode, fenleinode);
//						addNode++;
//					}else {
//						bw.write(filePath+"\r\n");
//						System.out.println("该点没有找到合适的分类，没有加进去");
//						unsettle++;
//						continue;
//					}
//				}
//				else{ //数据库中存在该点，则丰富其属性
//					dbhelper.AddNodeProp(flagNode, hashtwice,hashmul);
//					System.out.println(individual+"在数据库中已存在，进行属性丰富");
//					alreadyadded++;
//				}		
			}
		}
		
		dbhelper.deleteInvalidNodes();//删除生成的无效的节点
		dbhelper.shutDown();
		bw.flush();
		bw.close();
//		System.out.println("allfiles-----"+allfiles);
//		System.out.println("unstandard-----"+unstandard);
//		System.out.println("unsettle-----"+unsettle);
//		System.out.println("alreadyadded-----"+alreadyadded);
//		System.out.println("addNode-----"+addNode);
	}	
}
