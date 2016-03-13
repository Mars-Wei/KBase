package uestc.edu.cn.knowledgegraph.newcon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class NetSpider {
	static int startNum=1;//起始网站编号
	static int endNum=10;//终止网站编号
	static int threadCount = 4;//设置线程数为4
	static ArrayList<Integer> crawlerUrlList=new ArrayList();//待抓取的网页编号
	
	//同步方法，获得当前待抓取网页的编号
	public synchronized static int getUrl() {
		int tmpUrl;
		if(crawlerUrlList.isEmpty())return -1;
		tmpUrl = crawlerUrlList.get(0);
		crawlerUrlList.remove(0);
		return tmpUrl;
	}
	
	public void writeContent(String urlPath,int tmp
			) throws UnsupportedEncodingException, IOException {
		
		URL url=new URL(urlPath);
		
		//设置延时的处理readtime out问题
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(5000);
		//获取网页源码
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				url.openStream(),"UTF-8"));
		
		String line="";
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null){
			sb.append(line);
		}
		//过滤错误网页
		String tistr2=null;
		Document doc = Jsoup.parse(sb.toString().replaceAll("&nbsp;", ""));	//将文本内容存为doc
		Elements title1 = doc.getElementsByTag("title");
		String tistr1 = title1.text();
		if(tistr1.contains("_"))
			tistr2=tistr1.substring(0, tistr1.indexOf("_"));
		
		String filelocation="F:/baike/"+tistr2+"_"+tmp+".txt";
		if(!tistr1.equals("百度百科——全球最大中文百科全书")){
			BufferedWriter bw=new BufferedWriter(new FileWriter(filelocation,true));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		}
		else{
			System.out.println("错误网页:"+urlPath);
		}

	}
	
	public void spide() {
		
		for(int i=0;i<threadCount;i++){
			new Thread(new Runnable(){
				public void run() {
					
					while (!crawlerUrlList.isEmpty()) {
						int tmp = getUrl();//获取url
						if(tmp!=-1){
							//获取url所对应的标签
							String urlPath="http://baike.baidu.com/view/"+tmp+".html?fromTaglist";
							
							System.out.println(Thread.currentThread().getName()+"开始写:"+urlPath);
							try {
								writeContent(urlPath,tmp);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			},"thread-"+i).start();
		}
		
		}
	
	public static void main(String arg[]) throws IOException{
		
		NetSpider nsp=new NetSpider();
		
		for(int i=startNum;i<=endNum;i++)
			crawlerUrlList.add(i);
		nsp.spide();
//		long startTime=System.currentTimeMillis();   //获取开始时间
//		long endTime=System.currentTimeMillis(); //获取结束时间
//		double minute=(endTime-startTime)/1000.0;
//		System.out.println("程序运行时间： "+minute+"s");

	}
}
