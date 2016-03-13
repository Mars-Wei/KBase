package uestc.edu.cn.knowledgegraph.newcon;

import java.util.ArrayList;
import java.util.HashMap;

//import scala.collection.mutable.ArrayLike;

public class HashMaps {
	private HashMap<String, String> hashMapString;
	private HashMap<String, ArrayList<String>> hashMapArray;
	private HashMap<String,String>hashMapsrelation;

	public HashMap<String, String> getHashMapsrelation() {
		return hashMapsrelation;
	}

	public void setHashMapsrelation(HashMap<String, String> hashMapsrelation) {
		this.hashMapsrelation = hashMapsrelation;
	}

	public HashMap<String, String> getHashMapString() {
		return hashMapString;
	}

	public void setHashMapString(HashMap<String, String> hashMapString) {
		this.hashMapString = hashMapString;
	}

	public HashMap<String, ArrayList<String>> getHashMapArray() {
		return hashMapArray;
	}

	public void setHashMapArray(HashMap<String, ArrayList<String>> hashMapArray) {
		this.hashMapArray = hashMapArray;
	}

}
