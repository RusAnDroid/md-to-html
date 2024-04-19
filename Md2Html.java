package md2html;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.FileNotFoundException;

import java.nio.charset.StandardCharsets;

import java.util.Scanner;
import java.io.File;

import java.util.ArrayList;
import java.util.List;


public class Md2Html {
	public static void main(String[] args) {
        try {

            Scanner inputScanner = new Scanner(
                new File(args[0]),
                StandardCharsets.UTF_8
            );

            List<ParagraphParser> paragraphs = new ArrayList<>();

            try {
                StringBuilder currentParagraphBuilder = new StringBuilder();
                while (inputScanner.hasNextLine()) {
                    String line = inputScanner.nextLine();
                    if (line.isEmpty()) {
                        if (!currentParagraphBuilder.isEmpty()) {
                            currentParagraphBuilder.delete(currentParagraphBuilder.length() - System.lineSeparator().length(), currentParagraphBuilder.length());
                            paragraphs.add(new ParagraphParser(currentParagraphBuilder.toString()));
                        }
                        currentParagraphBuilder.setLength(0);
                        continue;
                    }
                    currentParagraphBuilder.append(line);
                    if (!line.isEmpty()) {
                        currentParagraphBuilder.append(System.lineSeparator());
                    }
                }
                if (!currentParagraphBuilder.isEmpty()) {
                    currentParagraphBuilder.delete(currentParagraphBuilder.length() - System.lineSeparator().length(), currentParagraphBuilder.length());
                    paragraphs.add(new ParagraphParser(currentParagraphBuilder.toString()));
                }
            } finally {
                inputScanner.close();
            }

            
            try {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(args[1]),
                        StandardCharsets.UTF_8
                ))) {
                    for (ParagraphParser paragraph : paragraphs) {
                        writer.write(paragraph.parse());
                        writer.newLine();
                    }
                }

            } catch (IOException e) {
                System.out.println("Writing error:  " + e.getMessage());
            }
            
        } catch (FileNotFoundException e) {
           System.out.println("Input file wasn't found:" + e.getMessage());
        } catch (IOException e) {
           System.out.println("Reading error:  " + e.getMessage());
        }
    }
}