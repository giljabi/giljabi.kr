package kr.giljabi;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
        String fileFullName = String.format("/Users/parknamjun/IdeaProjects/giljabi/forest100/data.txt");

        List<String> lines = Files.readAllLines(Paths.get(fileFullName));
        for (String line : lines) {
            String[] mmm = line.split("\t");
            String[] name = mmm[0].split("_");
            String[] eng = mmm[1].split("_");

            //System.out.format("%s,%s\n", name[0], eng[0]);
            //System.out.format("%s\n", name[0]);
            System.out.format("mv %s %s\n", mmm[0], mmm[1]);

        }
    }

}