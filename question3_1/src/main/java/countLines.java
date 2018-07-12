import com.google.common.base.Optional;

import java.io.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class countLines {
    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static void main (String[] args) throws IOException{
        int count = 0;
        boolean multiLinesNote = false;
        boolean multiLinesHtmlNote = false;
        String fp = "F:\\Google Download\\StringUtils.java";
        //逐行读取文件
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fp)));
        for (String line = br.readLine(); line != null; line = br.readLine()){

            line = replaceBlank(line);
            if (line.length() > 0){
                System.out.println(line);
                if (line.length() > 1){
                    //System.out.println(line.substring(0, 2));
                    if(line.substring(0,2).equals("/*") && multiLinesNote == false && multiLinesHtmlNote == false) {
                        //System.out.println("start");
                        multiLinesNote = true;
                    }
                    if(line.length()>4 && line.substring(0,4).equals("<!--") && multiLinesHtmlNote == false && multiLinesNote == false){
                        multiLinesHtmlNote = true;
                    }
                    if (!multiLinesNote && !line.substring(0, 2).equals("//") && !multiLinesHtmlNote){
                        count++;
                    }
                    if (line.substring(line.length()-2, line.length()).equals("*/")){
                        //System.out.println("end");
                        multiLinesNote = false;
                    }
                    if (line.length()>4 && line.substring(line.length()-3, line.length()).equals("-->")){
                        //System.out.println("end");
                        multiLinesHtmlNote = false;
                    }
                }else{
                    count++;
                }
            }
        }
        System.out.println(count);
        br.close();
    }
}
