package ICTCLAS.I3S.AC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;









import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;

import ICTCLAS.I3S.AC.ICTCLAS50;

/**
 * 对ICTCLAS50进行wrap
 * 主要功能有：
 * 初始化，默认为UTF-8，默认使用自带的字典，默认进行词性标注
 * TODO：fileProcess对文件进行分词(暂时没有)
 * pragraphProcess对字符串进行分词
 * 退出
 * @author sd
 *
 */


public class SegmentProcessOrigin {
	
	private ICTCLAS50 ictclas = new ICTCLAS50();
	/**
	 * 初始化SegmentProcess（ICTCLAS）
	 * 主要包括：
	 * 默认按照UTF-8的编码方式初始化
	 * 默认使用用户字典，按照UTF-8的编码方式导入
	 * 默认采用词性标注，采用的标注集为SetPosmap参数为0的情况，应该是计算所二级标注集。
	 * @return
	 */
	public boolean init(){
		String argu=".";
		try{
			if (  ictclas.ICTCLAS_Init(argu.getBytes("utf-8")  )==false){
				System.out.println("Init Fail!");
				System.out.println("----------");
				return false;
			}else{
//				System.out.println("ICTCLAS init success!");
				int nCount=0;
				String usrdir="userdict.txt";
				byte[] usrdirb = usrdir.getBytes();
				nCount = ictclas.ICTCLAS_ImportUserDictFile(usrdirb, 3);
//				System.out.println("Import userdict.txt");
				if (ictclas.ICTCLAS_SetPOSmap(0)==1){
//					System.out.println("默认采用词性标注，计算所二级标注集。如果ICTCLAS中SetPOSmap的参与为0代表的是计算所而级标注集的话。");
					return true;
				}else{
					System.out.println("设置为词性标注失败！");
					return false;
				}
			}
		}catch(Exception ex){
			return false;
		}
	}
	
	/**
	 * 对文本文件进行分词并返回分词后的文本为String类型
	 * 输入参数String src是文本文件的路径，istag为1代表词性标注，0代表不词性标注，ecodeName是读入文本的编码方式
	 * @return
	 */
	public String fileProcess(String src, int istag, String ecodeName){
		return "";
	}
	
	/**
	 * 对一段文本分词，文本是UTF-8编码的
	 * 输入参数String src是待分词的文本，istag为1时代表词性标注，0时代表不词性标注
	 * @return String
	 * @param String
	 * @param int
	 * @see 对一段文本进行分词，文本是UTF-8编码的
	 * @see	输入参数String src是待分词的文本，istag为1时代表词性标注，0时代表不词性标注
	 */
	public String paragraphProcess(String src, int istag){
		byte[] rt = null;
		String rtStr = null;
		try{
			rt=ictclas.ICTCLAS_ParagraphProcess(src.getBytes("UTF-8"), 3, istag);//使用指定的字符集将此 String 编码为 byte 序列，并将结果存储到一个新的 byte 数组中。
			rtStr = new String(rt,0,rt.length,"UTF-8");
		}catch(UnsupportedEncodingException e1){
			System.out.println("分词的时候请使用UTF-8编码！");
			e1.printStackTrace();
		}
		return rtStr;
	}
	
	/**
	 * 退出
	 * @return boolean
	 */
	public boolean exit(){
		return ictclas.ICTCLAS_Exit();
	}
	
	
	
	
	public static void main(String[] args) throws UnsupportedEncodingException{
		
//		String line=null;
//
//		HashSet<String> StopWord=new HashSet();
//		try {
//			InputStream in = new FileInputStream("Data/StopWord.txt");//读取文件上的数据
//			//指定编码方式，将字节流向字符流的转换
//			InputStreamReader isr = new InputStreamReader(in,"UTF-16");
//			//创建字符流缓冲区
//			BufferedReader bufr = new BufferedReader(isr);
//			while((line = bufr.readLine())!=null){
////				 System.out.println(line);
//				StopWord.add(line);
//			}
//	        isr.close(); 
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
		
		String line=null;

		HashSet<String> StopWord=new HashSet();
		try {
			InputStream in = new FileInputStream("Data/PunctuationCleaning.txt");//读取文件上的数据
			//指定编码方式，将字节流向字符流的转换
			InputStreamReader isr = new InputStreamReader(in,"UTF-16");
			//创建字符流缓冲区
			BufferedReader bufr = new BufferedReader(isr);
			while((line = bufr.readLine())!=null){
//				 System.out.println(line);
				StopWord.add(line);
			}
	        isr.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	

		SegmentProcessOrigin sp = new SegmentProcessOrigin();
		sp.init();
		String str =sp.paragraphProcess("很多人犹记得三年前    的热潮”。",1);
		String str1 = sp.paragraphProcess("很多人犹记得三年前“那些年”的热潮，柯震东几乎是本色出演了倔强又调皮的大男孩柯景腾。",1);
//		String str2 = sp.paragraphProcess("可以高效处理大集合的同时保留基本的统计关系用于如分类，异常检测，总结，",1);
//		String str3 = sp.paragraphProcess("相似性和相关的基本任务判断。信息检索领域的研究人员在这个问题上已经取得了重大进步。",1);
//		String str4 = sp.paragraphProcess("通过IR的研究人员提出的文本语料库的基本方法——",1);
//		String str5 = sp.paragraphProcess("已成功地部署在现代互联网搜索引擎中——减少每个文档在语料库中的实数的向量，",1);
//		String str6 = sp.paragraphProcess("其中每一个向量代表比率数量。在流行的计划中，在语料库中的每个文档中从“词”或“条件”选择一个基本的词汇，",1);
//		String str7 = sp.paragraphProcess("计数形成的每个单词出现的次数。经过适当的正常化，这个词的频率计数与文档频计数相比，",1);
//		String str8 = sp.paragraphProcess("该计数测量在整个语料库中出现的次数。",1);
//		String str9 = sp.paragraphProcess("最终的结果是一个文档术语 X矩阵，矩阵的每列包含语料库每个文件的值。",1);
//		String str10 = sp.paragraphProcess("因此，计划减少随机长度文档的固定长度列表数量。", 1);

//		for(int i=0;i<nn.length();i++){
//		if(StopWord.contains(nn.charAt(i))){
//			word[i].replaceAll(nn.charAt(i), "");
//		}
//	}
		
//		String word[] =str.split("\\s+");
//		String TermVector[]=new String[word.length+1];
//		int n=0;
//		for(int i=0;i<word.length;i++){
//			if(StopWord.contains(word[i])){
//				System.out.println("删除停用词:"+word[i]);
//				word[i].replaceAll(word[i], "");
//			}
//			else{
//				TermVector[n]=word[i];
//				n++;
//			}
//		}
//		for(int i=0;TermVector[i]!=null;i++){
//			System.out.println(TermVector[i]);
//		}
		
		
		
		System.out.println(str);
//		System.out.println(str1);
//		System.out.println(str2);
//		System.out.println(str3);
//		System.out.println(str4);
//		System.out.println(str5);
//		System.out.println(str6);
//		System.out.println(str7);
//		System.out.println(str8);
//		System.out.println(str9);
//		System.out.println(str10);
		
//		String argu="dsf";
//		ICTCLAS50 ictclas = new ICTCLAS50();
//		if (ictclas.ICTCLAS_Init(argu.getBytes("UTF-8"))==false){
//			System.out.println("Init Fail!");
//			System.out.println("----------");
//		
//		}
//		else{
//			System.out.println("hello");
//		}
		
		
	
	}
}