package uestc.edu.cn.knowledgegraph.newcon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


	/**
	 * 百度百科网页信息抽取
	 * @author tianshengzhao
	 *
	 */
	public class BaiKeFunction {
		HashMaps hashMaps=new HashMaps();
		HashMap<String, String> attrTable=new HashMap<String,String>();//存储一个key一个value的
		HashMap<String,ArrayList<String>> hashmul=new HashMap<String,ArrayList<String>>();//存储一个key多个value的
		
		
		/*
		 * 此方法用于抽取Html网页中的信息
		 * 输入：文件路径
		 * 输出：一个HashMap,存储的是属性键值对（attr,value）
		 * @param filePath
		 */
		
		
		public 	HashMaps  extraHTMLText(String filePath) {
			/*
			 * 读取文件，将其以String形式存入
			 */
			File file = new File(filePath);
			
			try {
				BufferedReader br=new BufferedReader(new FileReader(filePath));
				String line="";
				StringBuilder sb = new StringBuilder();
				
				while((line=br.readLine())!=null){
					sb.append(line);
				}
				 
				if (sb.length() > 0) {																							//如果string buffer 中有内容，进行处理
					Document doc = Jsoup.parse(sb.toString().replaceAll("&nbsp;", ""));	//将文本内容存为doc
					
					infoboxExtra(doc);																						//infobox信息抽取
					abstraExtra(doc);																							//abstract抽取
					openTagExtra(doc);																						//openTag抽取
					sameEntityExtra(doc);																					//同名实体抽取
					titleEXtra(doc);
				} else {
					System.out.println("No  Content");
				}//end if else
				
				
			}//end try
			catch (Exception e) {
				e.printStackTrace();
			} //end catch
			hashMaps.setHashMapArray(hashmul);
			hashMaps.setHashMapString(attrTable);
			return hashMaps;
		}

		/*
		 * infobox信息抽取函数：
		 * 输入： doc(Document)
		 * 输出： void
		 */
		public void	infoboxExtra(Document doc){
			Element infobox = doc.getElementById("baseInfoWrapDom");		//获取包含infobox的DIV

			if(infobox != null){																									//如果存在Infobox
				
				Element   left = infobox.getElementsByClass("baseInfoLeft").get(0);		//分别获取左右两个infobox
				Element   right = infobox.getElementsByClass("baseInfoRight").get(0);
				ArrayList<Element>  left_right = new ArrayList<Element>();		//将左右infobox存在一个list中
				left_right.add(left);
				left_right.add(right);
				
				for(Element info : left_right){
					Elements  iterm = info.getElementsByClass("biItem");//获取每一条信息基本单位，一个class为"biIterm"的div
					
					for(int i = 0;i<iterm.size();i++){
						Elements  inner = iterm.get(i).getElementsByClass("biItemInner");//获取class名为"biItemInner"的div
						String attr  = inner.get(0).getElementsByClass("biTitle").text();
						String value = inner.get(0).getElementsByClass("biContent").text();
						if(attr.length()!=0){//抽取的过程中可能没有抽到key值
							attrTable.put(attr, value);
						}
						
						//System.out.println("$$$$$$"+attr+attr.length());
					}//end for			
				}//end for		
			
			}//end if
			return ;
		}
		
		/*
		 * abstract抽取函数：
		 * 输入： doc(Document)
		 * 输出： void
		 */
		public void abstraExtra(Document doc){
			Elements divs = doc.getElementsByClass("card-summary-content");
			if( !divs.toString().equals("")){
				String value = divs.text();
				String key = "ABSTRACT";
				attrTable.put(key, value);
			}//end if
			return ;
		}
		
		/*
		 * openTag抽取函数：
		 * 输入： doc(Document)
		 * 输出： void
		 */
		public void openTagExtra(Document doc){
			ArrayList<String>  taglist = new ArrayList<String>();		//用于存储开放分类标签
			String key = "所属类型";
			
			Element tag = doc.getElementById("open-tag-item") ; 
			if(tag != null){
				//由于网站版本更新原因和数据非同时获取，对两种情况进行判断
				Elements  sapn_links = tag.getElementsByTag("sapn");	//新版取消标签超链接抽取分类
				Elements  a_links = tag.getElementsByTag("a");			//旧版使用超链接分类
				//分别进行抽取，如果没有，则不执行for循环抽取
				for(int i =0;i<a_links.size();i++){
					String value =  a_links.get(i).text();
					taglist.add(value);
				}//end for
				for(int i =0;i<sapn_links.size();i++){
					String value =  sapn_links.get(i).text();
					taglist.add(value);
				}//end for
					
			}//end if
			if(taglist.size() != 0){
				hashmul.put(key, taglist);
			}else {//这样方便后面的操作
				taglist.add("没有分类标签");
				hashmul.put(key, taglist);
			}
			return ; 
		}
		
		/*
		 *同名实体抽取函数：
		 * 输入： doc(Document)
		 * 输出： void
		 */
		
		public void titleEXtra(Document doc) {//用于抽取网页的描述主体的某某
			Elements title1 = doc.getElementsByTag("title");
			String tistr1 = title1.text();
			String tistr2;
			if(!tistr1.equals("")){
				try{
				 tistr2=tistr1.substring(0, tistr1.indexOf("_"));
//				 if(tistr2.indexOf("（")>0){
//					 tistr2=tistr1.substring(0, tistr1.indexOf("（"));
//				 }
				}catch(Exception e){
					return;
				}
				attrTable.put("实体",tistr2);
			}else{
				attrTable.put("实体",tistr1);
			}
			
		}
		
		public void sameEntityExtra(Document doc){
			ArrayList<String> entityList = new ArrayList<String>();		//用于存储同名实体 
			String key = "同名实体";
			
			Element sameEntity = doc.getElementById("polysemeBody");
//			System.out.println(sameEntity);
			if(sameEntity != null){
				Elements sE_li= sameEntity.getElementsByTag("li");
				for(int i=0;i<sE_li.size();i++){
					String value = sE_li.get(i).text().replace("▪", "");
					entityList.add(value);
				}//end for	
			}//end if
			
			if(entityList.size() != 0){
				hashmul.put(key, entityList);
			}
			return ;
		}
		
		
		public static void main(String[] args) {
			BaiKeFunction b =new BaiKeFunction();
			
			HashMaps  al = b.extraHTMLText("E:/Data/alldata/测试/2014-12-05 14-29-14.txt");
			//HashMaps  al = b.extraHTMLText("D:/lydata/alldata/科学家/2014-11-26 22-00-25.txt");
			HashMap<String, String> attrTable1=new HashMap<String,String>();
			HashMap<String,ArrayList<String>> hashmul1=new HashMap<String,ArrayList<String>>();
			attrTable1=al.getHashMapString();
			hashmul1=al.getHashMapArray();
			//抽取的结果显示
			Iterator it =attrTable1.keySet().iterator(); 
	        while(it.hasNext()) 
	        { 
	        	String key = (String)it.next();							 // key
	        	System.out.println(key + " -- "+attrTable1.get(key)); 
	        }
	        
	        Iterator it2 =hashmul1.keySet().iterator(); 
	        while(it2.hasNext()) 
	        { 
	        	String key = (String)it2.next();							 // key
	        	System.out.println(key + " -- "+hashmul1.get(key)); 
	        }
	        //System.out.println(al.size());
			
		}


	}