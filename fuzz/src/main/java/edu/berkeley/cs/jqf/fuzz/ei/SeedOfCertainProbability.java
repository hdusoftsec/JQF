package edu.berkeley.cs.jqf.fuzz.ei;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Probability {

    /** 种子对应的字节值 */
    public static ArrayList<Integer> seedValue = new ArrayList<>();

    /** 种子出现的概率 */
    public static ArrayList<Double> probability = new ArrayList<>();

    public Probability(){
        super();
        this.seedValue = new ArrayList<>();
        this.probability = new ArrayList<>();
    }

    /** Map存储种子的字节值和概率 */
    public static Integer seedOfCertainProbabilityMap(LinkedHashMap<Integer, Double> l){
        Integer item = 0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        double totalProbability = 0.0;
        for (int i=0;i<l.size();i++){
            totalProbability += l.get(i);
        }

        return item;
    }

    /** ArrayList分别存储*/
    public static Integer seedOfCertainProbability (ArrayList<Integer> v, ArrayList<Double> p){
        Integer seed = 0;
        double temp = Math.random();
        temp = (double) Math.round(temp * 100) / 100;
        double totalProbability = 0.0;
        int i=0;
        // int k=p.size();
        while (temp > totalProbability){
            seed = v.get(i);
            totalProbability += p.get(i);
            i++;
        }
        return seed;
    }

    public static void main(String[] args){
        seedValue.add(0);
        seedValue.add(1);
        seedValue.add(2);
        seedValue.add(3);
        seedValue.add(4);
        probability.add(0.15);
        probability.add(0.1);
        probability.add(0.25);
        probability.add(0.36);
        probability.add(0.14);
        System.out.println(seedValue);
        System.out.println(probability);
        int zero=0;
        int one=0;
        int two=0;
        int three=0;
        int four=0;
        for (int i=0;i<10000;i++){
            Integer result = seedOfCertainProbability(seedValue,probability);
            if (result == 0){
                zero ++;
            }else if (result == 1){
                one ++;
            }else if (result == 2){
                two ++;
            }else if (result == 3){
                three ++;
            }else if (result == 4){
                four ++;
            }
        }
        System.out.println("0出现次数：" + zero);
        System.out.println("1出现次数：" + one);
        System.out.println("2出现次数：" + two);
        System.out.println("3出现次数：" + three);
        System.out.println("4出现次数：" + four);
    }
}
