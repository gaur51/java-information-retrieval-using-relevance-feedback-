import java.io.*;
import java.lang.String;
import java.util.*;

/**
* This class is responsible for loading the stopwords file from stopwords.txt
* and removing stopwords from any given list of tokens.
*/
public class Stopwords {
    private static Set<String> stop = null;

    /**
    * Getter that ensures that the stopwords have been loaded from the file.
    */
    public static Set<String> getStopwordSet() {
        if (stop == null) {
            loadStopwords();
        }

        return stop;
    }


    /**
    * If they haven't been loaded already, load the stopwords from
    * stopwords.txt into a token set.
    */
    public static void loadStopwords() {
        if (stop != null) {
            return;
        }

        String line = null;
        
        BufferedReader in = null;
        String stopwords = "";
        try {
            in = new BufferedReader(new FileReader("stopwords.txt"));
            line = in.readLine();

            while (line != null) {
                //System.out.println(line);
                stopwords += " " + line;
                line = in.readLine();
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        // System.out.println(line);

        stop = new TreeSet<String>(Arrays.asList(stopwords.split(" +")));

        // System.out.println(">Stopword set: " + stop);
    }


    /**
    * Returns a copy of the input list with all stopwords removed.
    */
    public static List<String> cleanStopwords(List<String> dirtyTokens) {
        if (stop == null) {
            loadStopwords();
        }

        List<String> cleanTokens = new ArrayList<String>();
        for (String token : dirtyTokens) {
            if (!stop.contains(token)) {
                cleanTokens.add(token);
            }
        }

        return cleanTokens;
    }


    /**
    * Test code.
    */
    public static void main(String[] args) {
        // Test Stopword code here.
    }
}