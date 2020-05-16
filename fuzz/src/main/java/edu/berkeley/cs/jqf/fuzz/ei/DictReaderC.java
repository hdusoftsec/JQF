package edu.berkeley.cs.jqf.fuzz.ei;


import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 读标签字典库 */
public class DictReaderC {

    /** 标签 */
    protected static ArrayList<String> tags = new ArrayList<>();

    /** 字符串出现次数 */
    protected static Map<String,Integer> tagCountNum = new HashMap<>();

    /** 字符串及出现概率 */
    protected static Map<String,Double> tagProbability = new HashMap<>();

    /** 根据概率生成的种子输入 */
    protected static ArrayList<Integer> seedInput = new ArrayList<>();

    /** 字典库文件路径 */
    protected static String dictFilePath = " ";

    /** 字符串及出现概率输出文件路径 */
    protected static String outPutPath = " ";


    /** 获取文件中的标签 */
    private static ArrayList<String> getTagsInput (String filePath) throws IOException {
        int sumCount = 0;
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
                BufferedReader br = new BufferedReader(read);
                String strTxt = null;
                while ((strTxt = br.readLine()) != null) {
                    String regex = "\\<\\w+\\>";//正则匹配规则：<>
                    Pattern pattern = Pattern.compile(regex);  //正则匹配调用
                    Matcher matcher = pattern.matcher(strTxt);
                    while (matcher.find()) {
                        tags.add(matcher.group());
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tags;
    }

    /** 计算标签出现的概率 */
    public static Map<String,Double> tagProbabilityCalculator(ArrayList<String> tagList){
        // 统计重复的种子及其重复次数
        for(String str:tagList){
            int i = 1; //定义一个计数器，用来记录重复数据的个数
            if(tagCountNum.get(str) != null){
                i=tagCountNum.get(str)+1;
            }
            tagCountNum.put(str,i);
        }

        // 计算出现概率，double保留位小数
        Set set = tagCountNum.keySet();
        int totalNum = tagList.size();
        for(Iterator iter = set.iterator(); iter.hasNext();)
        {
            String key = (String)iter.next();
            Integer value = (Integer) tagCountNum.get(key);
            double proba = new BigDecimal((float)value/totalNum).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            tagProbability.put(key, proba);
        }
        return tagProbability;
    }

    /** 根据概率生成标签 */
    public static String singleSeedGenerator(Map<String,Double> tagMap){
        ArrayList<String> tagV = new ArrayList<>();
        ArrayList<Double> tagP = new ArrayList<>();
        for (Map.Entry<String,Double> entry : tagMap.entrySet()){
            tagV.add(entry.getKey());
            tagP.add(entry.getValue());
        }
        String tag = null;
        int i=0;
        double totalProbability = 0.0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        while ((temp > totalProbability) & (i < tagMap.size())){
            tag = tagV.get(i);
            totalProbability += tagP.get(i);
            i++;
        }
        return tag;
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
    public static ArrayList<Integer> seedsOfCertainProbability(String filePath) throws IOException {

        tags = getTagsInput(filePath);
        tagProbability = tagProbabilityCalculator(tags);

        for (int i=0;i<tagProbability.size();i++){
            String str = singleSeedGenerator(tagProbability);
            if (str!=null){
                int seedValue = stringToInt(str);
                seedInput.add(seedValue);
            }else{
                seedInput.add(null);
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

        tags = getTagsInput(dictFilePath);
//        System.out.println("字典库中的标签：");
//        System.out.println(tags + "\n");

        tagProbability = tagProbabilityCalculator(tags);
//        System.out.println("标签及出现概率：");
//        System.out.println(tagProbability + "\n");

        seedInput = seedsOfCertainProbability(dictFilePath);
//        System.out.println("最终生成的种子：");
//        System.out.println(seedInput);

        outPut(outPutPath, tagProbability);

    }

}
