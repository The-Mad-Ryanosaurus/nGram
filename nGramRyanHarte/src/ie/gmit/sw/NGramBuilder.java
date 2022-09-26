package ie.gmit.sw;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NGramBuilder
{
    private File filesDirectory;
    private File outputFile;
    private int ngramSize;
    private Boolean whitespaceFiltering;
    private Boolean calculatePercentFrequency;
    private Boolean useSlidingNGramCalculation;

    private Map<String, Long> frequency;
    int totalNGrams = 0;

    public NGramBuilder(File filesDirectory, File outputFile, int ngramSize, Boolean whitespaceFiltering, Boolean calculatePercentFrequency, Boolean useSlidingNGramCalculation)
    {
        this.filesDirectory = filesDirectory;
        this.outputFile = outputFile;
        this.ngramSize = ngramSize;
        this.whitespaceFiltering = whitespaceFiltering;
        this.calculatePercentFrequency = calculatePercentFrequency;
        this.useSlidingNGramCalculation = useSlidingNGramCalculation;
        frequency = new HashMap<>();
    }

    // O(n^2*1)
    // n number of files
    // n number of character per file
    // 1 the ngram size for loop is always the same regardless of the input size
    // ignore constants so
    // O(n^2)
    public void build() throws InterruptedException, IOException
    {
        for (File file : filesDirectory.listFiles())// O(n) number of files
        {
            if (!file.isFile())
            {
                continue;
            }

            System.out.println(ConsoleColour.YELLOW);
            System.out.println(file.getName());

            String content = Files.readString(file.toPath());
            content = content.toLowerCase();

            if (whitespaceFiltering)
            {
                content = content.replaceAll("[^a-z]", "");
            }

            char[] ngram = new char[ngramSize];

            int size = content.length();

            for (int i = 0; i < size; i ++)// O(n) number of chars in each file
            {
                // Only render the progress bar every so often otherwise slows it down to much
                if (i % Math.max(size / 100, 1) == 0)
                {
                    Progressbar.printProgress(i, size);
                }

                for (int j = 0; j < ngramSize; j++)// O(1) ngram size is always the same regardless of files contents
                {
                    if ((i + j) < content.length())
                    {
                        ngram[j] = content.charAt(i + j);
                    }
                    else
                    {
                        ngram[j] = '_';
                    }
                    // System.out.print(ngram[j]);
                }
                // System.out.println();

                String ngramString = new String(ngram);
                frequency.put(ngramString, frequency.getOrDefault(ngramString, 0L) + 1L);
                totalNGrams++;
            }
            // print the progress bar at 100% due only running
            // it every so often it never reaches 100
            Progressbar.printProgress(size, size);
            System.out.println();
        }
    }

    // O(2n log(n))
    // quick sort = n * log(n)
    // n for through the sorted array
    // ignore constants in O notation so
    // O(n log(n))
    public void output() throws IOException
    {
        // Sort the hashmap
        List<Entry<String, Long>> list = new LinkedList<>(frequency.entrySet());

        // more than likely quicksort so O(n log (n))
        list.sort((a, b) -> -(int) (a.getValue() - b.getValue()));

        // Write the sorted map to csv file
        FileWriter myWriter = new FileWriter(outputFile);
        for (var pair : list) // O(n)
        {
            String ngram = pair.getKey();
            long freq = pair.getValue();
            double percent = ((double) pair.getValue() / (double) totalNGrams) * 100;
            myWriter.write(ngram + ",");
            myWriter.write(freq + "");
            if (calculatePercentFrequency)
            {
                myWriter.write("," + percent);
            }
            myWriter.write('\n');
        }
        myWriter.close();

        System.out.println();
        System.out.println("Done! Outputing to " + outputFile.toString());
    }
}
