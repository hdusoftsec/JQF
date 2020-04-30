package edu.berkeley.cs.jqf.fuzz.ei;

import java.io.*;
import java.lang.Exception;
import java.text.DecimalFormat;
import java.util.*;

public class DictInput {
    public static void main(String[] args) throws Exception{
        fileReader("D://Zest//jqf//examples//src//test//resources//dictionaries//maven-model.dict");
    }

    /** 读取本地文件，并将每行数据存入数组 */
    public static ArrayList<String> fileReader(String fileName) throws Exception {
        try{
            System.out.println("File Path: " + fileName);
            String temp = null;
            File f = new File(fileName);
            //指定读取编码用于读取中文
            InputStreamReader read = new InputStreamReader(new FileInputStream(f),"GBK");
            ArrayList<String> readList = new ArrayList<String>();
            BufferedReader reader=new BufferedReader(read);
            LinkedHashMap<String, String> stringRate = new LinkedHashMap<String, String>();
            while((temp=reader.readLine())!=null &&!"".equals(temp)){
                readList.add(temp);
                stringRate.put(temp,doubleToString(Math.random()));
            }
            read.close();
            int size = readList.size();
            System.out.println("Total Number: " + size);
            System.out.println("String: " + readList);
            System.out.println("Rate: " + stringRate);
            return readList;
        }catch (Exception e) {
            // logger.info("读取文件--->失败！- 原因：文件路径错误或者文件不存在");
            e.printStackTrace();
            return null;
        }
    }

    public static String doubleToString (double value){
        DecimalFormat df = new DecimalFormat( "0.00" );
        String str=df.format( value );
        return str;
    }
}
