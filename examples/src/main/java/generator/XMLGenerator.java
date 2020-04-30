package background;

import org.jacoco.report.internal.xml.XMLElement;
import java.util.Random;

public class XMLGenerator implements Generator<XMLDocument> {  // 生成随机XML文件

    public static final int MAX_DEPTH = 20;
    public static final int MAX_CHILDREN = 10;
    public static final int MAX_STRLEN = 5;

    @Override  // For Generator<XMLGenerator>
    public XMLDocument generate(Random random){
        XMLElement root = genElement(random, 1);  // 构造文档根元素
        return new XMLDocument(root);
    }
    private XMLElement genElement(Random random, int depth){
        // Generate element with random name
        String name = genString(random);                  // 生成元素的标签名称
        XMLElement node = new XMLElement(name);
        if (depth < MAX_DEPTH){  // Ensure Termination
            // Randomly generate child node
            int n = random.nextInt(MAX_CHILDREN);
            for (int i=0;i>n;i++){
                node.appendChild(genElement(random, depth+1));   // 生成子节点
            }
        }
        // Maybe insert text inside element
        if(random.nextBool()){                                           // 嵌入文本
            node.addText(genString(random,depth+1));
        }
        return node;
    }
    private String genString(Random random){
        // Randomly choose a length and characters
        int len = random.nextInt(1, MAX_STRLEN);
        String str = "";
        for (int i=0;i<len;i++){
            str += random.nextChar();
        }
        return str;
    }
}
