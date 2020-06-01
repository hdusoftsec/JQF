package edu.berkeley.cs.jqf.fuzz.ei;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class SeedsGenerator {

    /** 标签及出现次数 */
    private static Map<String,Integer> stringWithNumber = new HashMap<>();

    /** 标签及出现概率 */
    private static Map<String,Double> stringWithProbability = new HashMap<>();

    /** 标签列表 */
    private static ArrayList<String> stringList = new ArrayList<>();

    /** 概率列表 */
    private static ArrayList<Double> probabilityList = new ArrayList<>();

    /** 种子列表 */
    private static ArrayList<String> seedList = new ArrayList<>();

    // D:\Zest\jqf\examples\src\test\resources\dictionaries\maven-model.dict
    private static String path = "examples\\src\\test\\resources\\dictionaries\\maven-model.dict";
    private static ArrayList<String> dictList = new ArrayList<>();

    /** 获取字典库 */
    public static ArrayList<String> getDictionaryList(String filePath){
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                dictList.add(tempString);
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
        return dictList;
    }

    /** 统计标签及概率 */
    public static Map<String, Double> getStringWithProbability(ArrayList<String> dictionaryList){

        // 计算种子的出现次数
        for(String str:dictionaryList){
            int i = 1;
            if (str != "null"){
                if(stringWithNumber.get(str) != null ){
                    i=stringWithNumber.get(str)+1;
                }
                stringWithNumber.put(str,i);
            }
        }

        // 计算种子的出现概率，double保留位小数
        Set set = stringWithNumber.keySet();
        int totalNum = dictionaryList.size();
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
            String key = (String)iter.next();
            Integer value = (Integer) stringWithNumber.get(key);
            double proba = new BigDecimal((float)value/totalNum)
                    .setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            stringWithProbability.put(key, proba);
        }

        return stringWithProbability;
    }

    /** 获取标签列表 */
    public static ArrayList<String> getStringList(Map<String, Double> stringProbabilityMap){
        for (Map.Entry<String,Double> entry:stringProbabilityMap.entrySet()){
            stringList.add(entry.getKey());
        }
        return stringList;
    }

    /** 获取概率列表 */
    public static ArrayList<Double> getProbabilityList(Map<String, Double> stringProbabilityMap){
        for (Map.Entry<String,Double> entry:stringProbabilityMap.entrySet()){
            probabilityList.add(entry.getValue());
        }
        return probabilityList;
    }

    /** 根据概率生成种子 */
    public static String getSeed(ArrayList<String> sList, ArrayList<Double> pList){
        String seed=null;
        int i=0;
        double totalProbability = 0.0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        while ((temp > totalProbability) & (i < sList.size())){
            seed = sList.get(i);
            totalProbability += pList.get(i);
            i++;
        }
        return seed;
    }

    /** 生成种子列表 */
    public static ArrayList<String> getSeedList(ArrayList<String> sList, ArrayList<Double> pList){
        String seed = null;
        int size = sList.size();
        for (int i=0;i<size;i++){
            seed = getSeed(sList, pList);
            if (seed != null){
                seedList.add(seed);
            }
        }
        return seedList;
    }

    public static void main(String[] args){
        dictList = getDictionaryList(path);
        System.out.println("dictionary: ");
        System.out.println(dictList);
        System.out.println("大小: " + dictList.size() + "\n");

        stringWithProbability = getStringWithProbability(dictList);
        System.out.println("tagWithProbability: ");
        System.out.println(stringWithProbability);
        System.out.println("大小: " + stringWithProbability.size() + "\n");

        stringList = getStringList(stringWithProbability);
        System.out.println("tagList: ");
        System.out.println(stringList);
        System.out.println("大小: " + stringList.size() + "\n");

        probabilityList = getProbabilityList(stringWithProbability);
        System.out.println("probabilityList: ");
        System.out.println(probabilityList);
        System.out.println("大小: " + probabilityList.size() + "\n");

        seedList = getSeedList(stringList,probabilityList);
        System.out.println("seedList: ");
        System.out.println(seedList);
        System.out.println("大小: " + seedList.size());

    }
}
