package uestc.edu.cn.knowledgegraph.newcon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class hudong {
	HashMaps hashMaps=new HashMaps();
//	ArrayList<HashMap> al = new ArrayList<HashMap>();
	HashMap<String, String> relationhash=new HashMap<String,String>();//存取关系对
	HashMap<String, String> attrTable=new HashMap<String,String>();//存储一个key一个value的
	HashMap<String,ArrayList<String>> hashmul=new HashMap<String,ArrayList<String>>();//存储一个key多个value的
	public HashMaps extraHTMLText(String filePath) {
		//用于存储一个网页的属性hash表
		//读取网页文件
		File file = new File(filePath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			int ch = 0;

			while ((ch = br.read()) != -1) {
				sb.append((char) ch);
			}			
			//如果读取的文件存在内容，进行抽取
			if (sb.toString().length() > 0) {
				//将文件存为Document形式
				Document doc = Jsoup.parse(sb.toString());
				/*
				 * 抽取infobox内容
				 */
				//通过jsoup API获取包含infobox的div
				Elements List = doc.select("div[class*=\"module zoom\"]");
				if(!List.toString().equals("")){
					Element div = List.get(0);
					//通过jsoup API进一步分离Html标签，获取infobox的承载形式---table
					Elements tables = div.getElementsByTag("table");
					Element  table = tables.get(0);	
					//通过jsoup API进一步分离Html标签，获取每一条td
					Elements tds = table.getElementsByTag("td");	
					for (int i=0;i<tds.size();i++) {		
						//对于每一条td，如果其text不为空，则可抽取
						if(!tds.get(i).text().equals("")){	
						Element td = tds.get(i);
					//HashMap hm = new HashMap(); //存储该条td的属性对
						String key=td.getElementsByTag("strong").text().replace("：", "");
						String value =td.select(" td>span").text().replace(" ", ",");
						attrTable.put(key, value);
					   // al.add(hm);
						}//end if
					}//end for
				}
				
				
				/*
				 * 抽取人物关系
				 */
				//获得包含人物关系的UL
				Element relation = doc.getElementById("fi_opposite");
				Element relation1 = doc.getElementById("holder1");
				
				Elements lis = new Elements();
				Elements lisecend = new Elements();
				//处理人物关系
				if(relation != null){
					lis = relation.getElementsByTag("li");
				}
				if(relation1 != null){
					lisecend = relation1.getElementsByTag("li");
				}
				//合并两个集合
				for(int i=0;i<lisecend.size();i++){
					Element li = lisecend.get(i);
					lis.add(li);
				}//end for
                 /*
                   * 抽取实体部分
                   */
//	           Elements title1 = doc.getElementsByTag("title");
//				String tistr1 = title1.text();
//				attrTable.put("实体", tistr1);
				
				//提取人物关系
				for(int i=0;i<lis.size();i++){
					String key = lis.get(i).getElementsByTag("a").text();
					String value = lis.get(i).ownText();
					if(value .equals("")){
						value ="待定";
					}//end if
					if(!key.equals("") && !value.equals("")){
						//HashMap hm = new HashMap(); //存储该条li的关系对
						relationhash.put(value, key); 						//！这里对调然后存入，key是关系，value是关系的值(这里是人名)
						//al.add(hm);
					}//end if
				}//end for
				
				
				/*
				 * 抽取开放分类
				 */
				ArrayList<String> al1=new ArrayList<>();
				//HashMap<String,ArrayList<String>>hm = new HashMap<>();
				String key = "所属类型";
				Element openclass = doc.getElementById("show_tag");		//获取包含完整开放分类的内容块
				if(openclass != null){
					Elements dd = openclass.getElementsByTag("dd");			//分离<dd>标签
					if(!dd.toString().equals("")){
						Elements links = dd.get(0).getElementsByTag("a");			//每一条<a>为一个开放分类
						for(int i=0;i<links.size();i++){
							//存储该条关系对
							String value = links.get(i).text();
							al1.add(value);
							
						}
						hashmul.put(key,al1); 						//将该关系对存为一个hashmap
						//al.add(hm);
						//end for
					}//end if
				}//end if
				//ArrayList alist=new ArrayList();
				//抽取的结果显示
//				for(HashMap h:al){
//					
//					//System.out.println(h);
//		
//     			for(Object key1:h.keySet())
//                  	{ 
////						
//         		   System.out.println(key1+"--"+h.get(key1));
//			      	   }
//				}
//					else{
//						alist.add(h.get(key1));
//						
//					}
//						
//					}
                  
//				 //System.out.println("所属类类"+":"+alist);
			}//end if
			else {
				System.out.println("No  Content");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			// logger.error	(e.getMessage());

		}
		
		hashMaps.setHashMapArray(hashmul);
		hashMaps.setHashMapString(attrTable);
		hashMaps.setHashMapsrelation(relationhash);
		return hashMaps;
		

	}
	


	public static void main(String[] args) {
		hudong hd=new hudong();
		HashMaps  al=hd.extraHTMLText("E:/graduate/hudong/chenlan.txt");
		HashMap<String, String> attrTable1=new HashMap<String,String>();
		HashMap<String,ArrayList<String>> hashmul1=new HashMap<String,ArrayList<String>>();
		attrTable1=al.getHashMapString();
		hashmul1=al.getHashMapArray();
		//System.out.println(attrTable1);
		//抽取的结果显示
		Iterator it =attrTable1.keySet().iterator(); 
        while(it.hasNext()) 
        { 
        	String key = (String)it.next();							 // key
        	System.out.println(key + "--"+attrTable1.get(key)); 
        }
        
        Iterator it2 =hashmul1.keySet().iterator(); 
        while(it2.hasNext()) 
        { 
        	String key = (String)it2.next();							 // key
        	System.out.println(key + "--"+hashmul1.get(key)); 
        }
        //System.out.println(al.size());
		
	}

}