package edu.berkeley.cs.jqf.fuzz.ei;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/** 读字符串字典库 */
public class DictReaderB {

    /** 存储字典库中的字符串 */
    protected static ArrayList<String> dictInputByString = new ArrayList<>();

    /** 字符串出现次数 */
    protected static Map<String,Integer> seedCountNum = new HashMap<>();

    /** 字符串及出现概率 */
    protected static Map<String,Double> seedProbability = new HashMap<>();

    /** 根据概率生成的种子输入 */
    protected static ArrayList<Integer> seedInput = new ArrayList<>();

    /** 字典库文件路径 */
    protected static String dictFilePath = " ";

    /** 字符串及出现概率输出文件路径 */
    protected static String outPutPath = " ";


    /** 获取字典库中的字符串 */
    public static ArrayList<String> getDictInputByLine(String filePath){
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                dictInputByString.add(tempString);
                // System.out.println("line " + line + ": " + tempString);
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

    /** 计算种子的概率 */
    public static Map<String,Double> seedsProbabilityCalculator(ArrayList<String> stringInput){
        // 统计重复的种子及其重复次数
        for(String str:stringInput){
            int i = 1; //定义一个计数器，用来记录重复数据的个数
            if(seedCountNum.get(str) != null){
                i=seedCountNum.get(str)+1;
            }
            seedCountNum.put(str,i);
        }

        // 计算出现概率，double保留位小数
        Set set = seedCountNum.keySet();
        int totalNum = stringInput.size();
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
            String key = (String)iter.next();
            Integer value = (Integer) seedCountNum.get(key);
            double proba = new BigDecimal((float)value/totalNum).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            seedProbability.put(key, proba);
        }
        return seedProbability;
    }

    /** 根据概率生成字符串 */
    public static String singleSeedGenerator(Map<String,Double> seedMap){
        ArrayList<String> seedV = new ArrayList<>();
        ArrayList<Double> seedP = new ArrayList<>();
        for (Map.Entry<String,Double> entry : seedMap.entrySet()){
            seedV.add(entry.getKey());
            seedP.add(entry.getValue());
        }
        String seed = null;
        int i=0;
        double totalProbability = 0.0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        while ((temp > totalProbability) && (i < seedMap.size())){
            seed = seedV.get(i);
            totalProbability += seedP.get(i);
            i++;
        }
        return seed;
    }

    /** String转成Byte[]再转成Integer */
    public static Integer stringToInt(String strSeed){
        int intSeed=0;
        byte byteSeed[];
        if(strSeed != null){
            byteSeed = strSeed.getBytes();
            for (int i=0;i<byteSeed.length;i++){
                intSeed += byteSeed[i] << i*8;
            }
            String s = new String(byteSeed);
            // System.out.println("[String]: "+ strSeed + "   [Byte->String]: " + s +"   [Byte]: " + byteSeed + "   [length]: " + byteSeed.length +"   [Integer]: " + intSeed);
        }else {
            return null;
        }
        return intSeed;
    }

    /** 生成种子输入 */
    public static ArrayList<Integer> seedsOfCertainProbability(String filePath){

        dictInputByString = getDictInputByLine(filePath);
        seedProbability = seedsProbabilityCalculator(dictInputByString);

        System.out.println("DictReaderB.seedsOfCertainProbability filePath is "+ new File(filePath).getAbsolutePath());
        for (int i=0;i<seedProbability.size();i++){
            String str = singleSeedGenerator(seedProbability);
            if (str!="" && str != null){
                int seedValue = stringToInt(str);
                seedInput.add(seedValue);
            }else{
                seedInput.add(0);//seedInput.add(null);
            }
        }
        return seedInput;
    }

    /** 输出到文件中 */
    private static void outPut(String outPutPath, Map<String,Double> seedProbability) throws IOException {
        File file = new File(outPutPath);
        if (!file.exists())
            file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, true);
        PrintStream p = new PrintStream(out);
        for(Map.Entry<String, Double> entry : seedProbability.entrySet()){
            String seed = entry.getKey();
            double probability = entry.getValue();
            p.println(seed + "    " + probability);
        }
        out.close();
    }

    public static void main(String[] args) throws IOException {

        dictInputByString = getDictInputByLine(dictFilePath);
        // System.out.println("字典库中的字符串：");
        // System.out.println(dictInputByString + "\n");

        seedProbability = seedsProbabilityCalculator(dictInputByString);
        // System.out.println("字符串及出现概率：");
        // System.out.println(seedProbability + "\n");

        // seedInput = seedsOfCertainProbability(dictFilePath);
        // System.out.println("最终生成的种子：");
        // System.out.println(seedInput);

        outPut(outPutPath, seedProbability);

    }
}
