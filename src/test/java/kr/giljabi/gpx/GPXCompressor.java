package kr.giljabi.gpx;

import com.github.diogoduailibe.lzstring4j.LZString;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GPXCompressor {

    public static void main(String[] args) {
        List<String> directoryPath = new ArrayList<>();
        directoryPath.add("/tmp/201902");
        directoryPath.add("/tmp/201903");
        directoryPath.add("/tmp/201904");
        directoryPath.add("/tmp/201905");
        directoryPath.add("/tmp/201906");
        directoryPath.add("/tmp/201907");
        directoryPath.add("/tmp/201908");
        directoryPath.add("/tmp/201909");
        directoryPath.add("/tmp/201910");

        for (String path : directoryPath) {
            compressor(path);
        }
    }

    private static void compressor(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Directory does not exist: " + directoryPath);
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".tcx"));

        if (files == null || files.length == 0) {
            System.out.println("No .gpx files found in the directory.");
            return;
        }
        int index = 1;
        for (File file : files) {
            try {
                System.out.format("Processing file: %d/%d, %s",
                        index++, files.length, file.getName());
                String content = readFile(file);
                String compressedContent = LZString.compressToUTF16(content);
                String compressedFileName = file.getName() + ".lz";
                File compressedFile = new File(directory, compressedFileName);
                writeFile(compressedFile, compressedContent);
                System.out.println("Compressed content saved to file " + compressedFileName);
            } catch (IOException e) {
                System.out.println("Error processing file: " + file.getName());
                e.printStackTrace();
            }
        }
    }

    private static String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}

