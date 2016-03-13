package ICTCLAS.I3S.AC;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;







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
public class SegmentProcess {
	
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
			if (ictclas.ICTCLAS_Init(argu.getBytes("UTF-8"))==false){
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
	
	
	
	
	public String segmentmain(String str) throws IOException{
		SegmentProcess sp = new SegmentProcess();
		sp.init();
//		String datapath="D:\\informationextractiondata\\data2.txt";
//		BufferedReader br=new BufferedReader(new FileReader(datapath));
//		String context = "";
//		String line = null;
//		while((line=br.readLine())!=null) {
//			context = context.concat(line);
//		}
//		br.close();
//		String segmentresult=sp.paragraphProcess(context,1);
		String segmentresult=sp.paragraphProcess(str, 1);
		return segmentresult;	
		
		
	}
}