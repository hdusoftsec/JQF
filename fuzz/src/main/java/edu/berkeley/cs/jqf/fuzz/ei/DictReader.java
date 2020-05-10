package edu.berkeley.cs.jqf.fuzz.ei;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.lang.Exception;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class DictReader {

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

    public static void main(String[] args) {
        String path = "D://Zest//jqf//examples//src//test//resources//dictionaries//seed.dict";
        ArrayList<Integer> seedValues = DictReader.seedsOfCertainProbability(path);
        System.out.println("---------------------------------------------------------");
        System.out.println("seedValues： "  + seedValues);
        System.out.println("seedValues的大小： " + seedValues.size());
        System.out.println("---------------------------------------------------------");
    }
}
