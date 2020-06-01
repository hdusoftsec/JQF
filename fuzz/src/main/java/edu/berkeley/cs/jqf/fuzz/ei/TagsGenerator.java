package edu.berkeley.cs.jqf.fuzz.ei;


import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 读 POM 文件 */
public class TagsGenerator {

    /** 标签及出现次数 */
    private static Map<String,Integer> tagWithNumber = new HashMap<>();

    /** 标签及出现概率 */
    private static Map<String,Double> tagWithProbability = new HashMap<>();

    /** 标签列表 */
    private static ArrayList<String> tagList = new ArrayList<>();

    /** 概率列表 */
    private static ArrayList<Double> probabilityList = new ArrayList<>();

    /** 种子列表 */
    private static ArrayList<String> seedList = new ArrayList<>();

    /** 获取字典库 */
    private static String path = "D:\\Zest\\jqf\\examples\\src\\test\\seeds\\xml\\pom.xml";
    private static ArrayList<String> dictList = new ArrayList<>();

    /** 获取文件中的标签 */
    public static ArrayList<String> getDictionaryList (String dictPath) throws IOException {
        try {
            File file = new File(dictPath);
            if (file.exists() && file.isFile()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(read);
                String tag = null;
                String str = null;
                while ((tag = br.readLine()) != null) {
                    String regex = "\\<\\w+\\>";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(tag);
                    while (matcher.find()) {
                        tag = matcher.group();
                        str = tag.replaceAll("[<>]+","");
                        dictList.add(str);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dictList;
    }

    /** 统计标签及概率 */
    public static Map<String, Double> getStringWithProbability(ArrayList<String> dictionaryList){

        // 计算种子的出现次数
        for(String str:dictionaryList){
            int i = 1;
            if (str != "null"){
                if(tagWithNumber.get(str) != null ){
                    i=tagWithNumber.get(str)+1;
                }
                tagWithNumber.put(str,i);
            }
        }

        // 计算种子的出现概率，double保留位小数
        Set set = tagWithNumber.keySet();
        int totalNum = dictionaryList.size();
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
            String key = (String)iter.next();
            Integer value = (Integer) tagWithNumber.get(key);
            double proba = new BigDecimal((float)value/totalNum)
                    .setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            tagWithProbability.put(key, proba);
        }

        return tagWithProbability;
    }

    /** 获取标签列表 */
    public static ArrayList<String> getStringList(Map<String, Double> stringProbabilityMap){
        for (Map.Entry<String,Double> entry:stringProbabilityMap.entrySet()){
            tagList.add(entry.getKey());
        }
        return tagList;
    }

    /** 获取概率列表 */
    public static ArrayList<Double> getProbabilityList(Map<String, Double> stringProbabilityMap){
        for (Map.Entry<String,Double> entry:stringProbabilityMap.entrySet()){
            probabilityList.add(entry.getValue());
        }
        return probabilityList;
    }

    /** 根据概率生成种子 */
    public static String getSeed(ArrayList<String> tList, ArrayList<Double> pList){
        String seed=null;
        int i=0;
        double totalProbability = 0.0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        while ((temp > totalProbability) & (i < tList.size())){
            seed = tList.get(i);
            totalProbability += pList.get(i);
            i++;
        }
        return seed;
    }

    /** 生成种子列表 */
    public static ArrayList<String> getSeedList(ArrayList<String> tList, ArrayList<Double> pList){
        String seed = null;
        int size = tList.size();
        for (int i=0;i<size;i++){
            seed = getSeed(tList, pList);
            if (seed != null){
                seedList.add(seed);
            }
        }
        return seedList;
    }

    /** 输出到文件中 */
    private static void outPut(String outputPath, ArrayList<String> tagList, ArrayList<Double> probList) throws IOException {
        File file = new File(outputPath);
        if (!file.exists())
            file.createNewFile();
        FileOutputStream out = new FileOutputStream(file, true);
        PrintStream p = new PrintStream(out);
        for (int i=0;i<tagList.size()-1;i++){
            if (tagList.get(i) != "\t"){
                p.println(tagList.get(i) + "   " + probList.get(i));
            }
        }
        out.close();
    }

    public static void main(String[] args) throws IOException {
        dictList = getDictionaryList(path);
        System.out.println("dictionary: ");
        System.out.println(dictList);
        System.out.println("大小: " + dictList.size() + "\n");

        tagWithProbability = getStringWithProbability(dictList);
        System.out.println("tagWithProbability: ");
        System.out.println(tagWithProbability);
        System.out.println("大小: " + tagWithProbability.size() + "\n");

        tagList = getStringList(tagWithProbability);
        System.out.println("tagList: ");
        System.out.println(tagList);
        System.out.println("大小: " + tagList.size() + "\n");

        probabilityList = getProbabilityList(tagWithProbability);
        System.out.println("probabilityList: ");
        System.out.println(probabilityList);
        System.out.println("大小: " + probabilityList.size() + "\n");

        seedList = getSeedList(tagList,probabilityList);
        System.out.println("seedList: ");
        System.out.println(seedList);
        System.out.println("大小: " + seedList.size());
    }
}
