package edu.berkeley.cs.jqf.fuzz.ei;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.lang.Exception;
import java.text.DecimalFormat;
import java.util.ArrayList;

/** 读二进制字典库 */
public class DictReaderA {

    /** 存储字典库中的字符串 */
    protected static ArrayList<Integer> dictInput = new ArrayList<>();

    /** 字符串出现概率(随机生成) */
    protected static ArrayList<Double> seedProbabilityRandom = new ArrayList<>();

    /** 种子及出现次数 */
    protected static Map<Integer,Integer> seedCountNum = new HashMap<>();

    /** 种子及出现概率 */
    protected static Map<Integer,Double> seedProbability = new HashMap<>();

    /** 根据概率生成的种子输入 */
    protected static ArrayList<Integer> seedInput = new ArrayList<>();

    /** 字符 */
    protected static ArrayList<Integer> dictInputByChar = new ArrayList<>();

    /** 字符串 */
    protected static ArrayList<String> dictInputByString = new ArrayList<>();


    /** 获取字典库中的字符串 */
    public static ArrayList<Integer> getDictInput(String filePath){
        File file= new File(filePath);
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            int tempbyte;
            while ((tempbyte = in.read()) != -1) {
                dictInput.add(tempbyte);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictInput;
    }

    public static ArrayList<String> getDictInputByLine(String filePath){
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                dictInputByString.add(tempString);
                System.out.println("line " + line + ": " + tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return dictInputByString;
    }

    /** 按字符获取字典库中的字符串 */
    public static void getDictInputByChar(String filePath){
        File file = new File(filePath);
        Reader reader = null;
        try {
            System.out.println("以字符为单位读取文件内容，一次读一个字节：");
            reader = new InputStreamReader(new FileInputStream(file));
            int tempChar;
            while ((tempChar = reader.read()) != -1) {
                if (((char) tempChar) != '\r') {
                    System.out.print((char) tempChar);
//                    dictInputByChar.add(tempChar);
//                    System.out.println((char)tempChar + "对应Integer值：" + tempChar);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("以字符为单位读取文件内容，一次读多个字节：");
            char[] tempchars = new char[30];
            int charread = 0;
            reader = new InputStreamReader(new FileInputStream(filePath));
            // 读入多个字符到字符数组中，charread为一次读取字符数
            while ((charread = reader.read(tempchars)) != -1) {
                // 同样屏蔽掉\r不显示
                if ((charread == tempchars.length)
                        && (tempchars[tempchars.length - 1] != '\r')) {
                    System.out.print(tempchars);
                } else {
                    for (int i = 0; i < charread; i++) {
                        if (tempchars[i] == '\r') {
                            continue;
                        } else {
                            System.out.print(tempchars[i]);
                        }
                    }
                }
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

    }

    /** 获取字符串的概率（目前是随机生成，后面要优化成统计概率） */
    public static ArrayList<Double> getSeedProbabilityRandomly(ArrayList<Integer> v){
        for (int i=0;i<v.size();i++){
            double temp = Math.random();
            double probability = (double) Math.round(temp * 100) / 100;
            seedProbabilityRandom.add(probability);
        }
        return seedProbabilityRandom;
    }

    /** 计算种子的概率 */
    public static Map<Integer,Double> seedsProbabilityCalculator(ArrayList<Integer> seedVal){
        // 统计重复的种子及其重复次数
        for(Integer sV:seedVal){
            int i = 1; //定义一个计数器，用来记录重复数据的个数
            if(seedCountNum.get(sV) != null){
                i=seedCountNum.get(sV)+1;
            }
            seedCountNum.put(sV,i);
        }

        // 计算出现概率，double保留位小数
        Set set = seedCountNum.keySet();
        int totalNum = seedVal.size();
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
            Integer key = (Integer) iter.next();
            Integer value = (Integer) seedCountNum.get(key);
            double proba = new BigDecimal((float)value/totalNum).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            seedProbability.put(key, proba);
        }
        return seedProbability;
    }

    /** 根据概率生成种子 */
    public static Integer singleSeedGenerator(Map<Integer,Double> seedMap){
        ArrayList<Integer> seedV = new ArrayList<>();
        ArrayList<Double> seedP = new ArrayList<>();
        for (Map.Entry<Integer,Double> entry : seedMap.entrySet()){
            seedV.add(entry.getKey());
            seedP.add(entry.getValue());
        }
        int seed = 0;
        int i=0;
        double totalProbability = 0.0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        while ((temp > totalProbability) & (i < seedMap.size())){
            seed = seedV.get(i);
            totalProbability += seedP.get(i);
            i++;
        }

        return seed;
    }

    /** 生成种子输入 */
    public static ArrayList<Integer> seedsOfCertainProbability(String filePath){
        // Map<Integer,Double> sp = new HashMap<>();
        // ArrayList<Integer> sv = new ArrayList<>();
        // ArrayList<Integer> socp = new ArrayList<>();

        dictInput = getDictInput(filePath);
        seedProbability = seedsProbabilityCalculator(dictInput);

        for (int i=0;i<seedProbability.size();i++){
            seedInput.add(singleSeedGenerator(seedProbability));
        }

        return seedInput;
    }
}
