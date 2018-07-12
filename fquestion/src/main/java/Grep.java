//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Grep {
    private static Charset charset = Charset.forName("utf-8");
    private static CharsetDecoder decoder = charset.newDecoder();

    private static Pattern linePattern = Pattern.compile(".*\r?\n");
    private static Pattern pattern;
    private static Pattern cutP = Pattern.compile("-d\".*?\"");
    private static Pattern fileName = Pattern.compile(" [^ ]*\\.txt");
    private static Pattern parts = Pattern.compile("-f [0-9]*");

    private static Pattern cats = Pattern.compile("cat");
    private static Pattern grep = Pattern.compile("grep");
    private static Pattern wc = Pattern.compile("wc");
    private static Pattern sort = Pattern.compile("sort");
    private static Pattern cut = Pattern.compile("cut");

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("输入：");
        String input = sc.nextLine();

        String[] commands = input.split("\\|");
        String fileName = "temp";

        if (commands.length == 1){
            System.out.println(trans(commands[0]));
        }else{
            //File file = new File(fileName);
            try{
                FileWriter fw = new FileWriter(fileName+String.valueOf(0), false);
                BufferedWriter bw = new BufferedWriter(fw);
                for (String s:trans(commands[0]).split("\n")){
                    bw.write(s+"\r\n");
                }
                bw.close();
                fw.close();
            }catch (Exception e){
                e.printStackTrace();
            }

            for (int i = 1; i < commands.length; i++){
                try{
                    String result = trans(commands[i] + " temp"+String.valueOf(i-1));
                    if (i == commands.length-1){
                        System.out.println(result);
                    }
                    //System.out.println(commands[i] + " temp");
                    //System.out.println(result);
                    FileWriter fw = new FileWriter(fileName+String.valueOf(i), false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    for (String s: result.split("\n")){
                        bw.write(s+"\r\n");
                    }
                    bw.close();
                    fw.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            for (int i = 0; i< commands.length-1;i++){
                File dele = new File(fileName+String.valueOf(i));
                if (dele.exists()){
                    //System.out.println(fileName+String.valueOf(i));
                    //System.out.println("gaha");
                    dele.delete();
                }
            }
            File change = new File(fileName+String.valueOf(commands.length-1));
            File newFile = new File(commands[0].split(" ")[commands[0].split(" ").length-1]+".result");

            if (newFile.exists()){
                newFile.delete();
                change.renameTo(newFile);
            }else{
                change.renameTo(newFile);
            }
        }
    }

    private static String grep(File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);
        FileChannel fc = fis.getChannel();

        int size = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size);

        CharBuffer cb = decoder.decode(bb);
        String result = grep(f, cb);
        fc.close();
        return result;
    }

    private static String grep(File f, CharBuffer cb) throws IOException{
        StringBuilder b = new StringBuilder();
        //逐行读取文件
        FileInputStream fp = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fp));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (pattern.matcher(line).find()){
                b.append(line);
                b.append("\n");
            }
        }
        br.close();
        return b.toString().length()==0?"":b.toString().substring(0, b.toString().length()-1);
    }

    private static void compile(String pat){
        try {
            pattern = Pattern.compile(pat);
        } catch (PatternSyntaxException e){
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static String cat(String file) throws IOException {
        StringBuilder b = new StringBuilder();
        File f = new File(file);
        //逐行读取文件
        FileInputStream fp = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fp));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            b.append(line);
            b.append("\n");
            //System.out.println(line);
        }
        br.close();
        return b.toString().substring(0, b.toString().length()-1);
    }

    public static String wc(String fileName) throws IOException {
        StringBuilder b = new StringBuilder();

        int lineCount = 0;
        int collumCount = 0;
        int wordCount = 0;
        File f = new File(fileName);

        FileInputStream fp = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fp));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            String dest = "";
            lineCount++;
            if (line.length() > collumCount){
                collumCount = line.length();
            }
            Pattern p = Pattern.compile("\\s+|\t|\r|\n");
            Matcher m = p.matcher(line);
            dest = m.replaceAll(" ");
            String[] spli = dest.split(" ");
            //System.out.println(spli.length);
            wordCount += spli.length;
        }
        //System.out.println(wordCount);
        b.append(lineCount);
        b.append("\t");
        b.append(collumCount);
        b.append("\t");
        b.append(wordCount);
        return b.toString();
    }

    public static String sort(String str) throws IOException{
        File f = new File(str);
        ArrayList lis = new ArrayList();
        StringBuilder b = new StringBuilder();

        FileInputStream fp = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fp));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            lis.add(line);
        }
        Collections.sort(lis);
        for (Object s:lis){
            b.append(s.toString());
            b.append("\n");
            //System.out.println(s.toString());
        }
        return b.toString().substring(0, b.toString().length()-1);
    }

    public static String cut(String input) throws IOException{
        Matcher cutOpD = cutP.matcher(input);
        Matcher foundFileName = fileName.matcher(input);
        Matcher whichParts = parts.matcher(input);
        StringBuilder b = new StringBuilder();

        String spliter = "";
        if (cutOpD.find()){
            spliter = cutOpD.group(0);
            spliter = spliter.substring(3,spliter.length()-1);
        }else{
            spliter = "\t";
        }

        int part = 0;
        if(whichParts.find()){
            part = Integer.parseInt(whichParts.group(0).substring(3,whichParts.group(0).length())) - 1;
        }

        String file = input.split("\\s")[input.split("\\s").length-1];
        File f = new File(file);
        FileInputStream fp = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(fp));
        for (String line = br.readLine(); line != null; line = br.readLine()){
            String[] ss = line.split(spliter);
            System.out.println(ss[part]);
            b.append(ss[part]);
            b.append("\n");
        }
        return b.toString().substring(0, b.toString().length()-1);
    }

    private static String trans(String str){
        String result = "";
        if (cats.matcher(str).find()){
            try{
                result = cat(str.substring(4,str.length()));
                //stringbuild 的时候末尾多了个换行符，在这里去掉
            }catch (IOException e){
                System.err.println("error");
            }
        }else if(grep.matcher(str).find()){
            String[] pat = str.substring(5,str.length()).split("\\s");
            compile(pat[0]);
            if (pat.length <= 2){
                try {
                    File file = new File(pat[1]);
                    result = grep(file);
                }catch (IOException e){
                    System.err.println(e);
                }
            }
        }else if(sort.matcher(str).find()){
            try {
                result = sort(str.substring(5, str.length()));
            }catch (IOException e){
                System.err.println(e);
            }
        }else if(cut.matcher(str).find()){
            try{
                result = cut(str);
            }catch (IOException e){
                System.err.println(e);
            }
        }else if(wc.matcher(str).find()){
            try{
                //System.out.println(str.substring(3, str.length()));
                result = wc(str.substring(3, str.length()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            System.err.println("only support cat, grep, wc, sort, cut");
        }
        return result;
    }
}
