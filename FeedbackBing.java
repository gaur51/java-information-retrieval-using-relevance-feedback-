import java.util.*;

public class FeedbackBing {
    /**
    * The main program.
    */
    public static void main(String[] args) {
        // Parse and validate program input.
        if (args.length != 5) {
            System.err.println("usage: java FeedbackBing <client-key>"
                               + " <precision> <'query'> <full feedback>"
                               + " <# additional terms>");
            return;
        }
        String bingAccountKey = args[0];
        Double targetMinPrecision = parsePrecision(args[1]);
        Query query = new Query(args[2]);
        String fullFeedback = args[3];
        Integer numAdditionalTerms = parseNumAdditionalTerms(args[4]);
        if (targetMinPrecision == null || numAdditionalTerms == null) {
            return;
        }

        // Set up class instances.
        Extractor extractor = new Extractor(bingAccountKey);
        QueryAdjuster queryAdjuster = new QueryAdjuster(numAdditionalTerms);
        Scanner prompt = new Scanner(System.in);

        // Main loop.
        // TODO: instead of resetting relevant and irrelevant results each
        //       loop, keep the variables outside of the loop and keep adding
        //       to them.
        double currentPrecision;
        while (true) {
            // Print the parameters to be used in current Bing query.
            System.out.println("Parameters:");
            System.out.println("Client key         = " + bingAccountKey);
            System.out.println("Query              = " + query);
            System.out.println("Required Precision = " + targetMinPrecision);

            // Query Bing to get top 10 results.
            System.out.println("URL: " + extractor.getQueryURL(query));
            List<Result> resultList = extractor.getTop10BingResults(query);
            assert(resultList.size() == 10);
            System.out.println("Total no of results : " + resultList.size());

            // Ask the user for which of the outputs were relevant.
            List<Result> relevantResults = new ArrayList<Result>();
            List<Result> irrelevantResults = new ArrayList<Result>();
            getUserFeedback(prompt, resultList, relevantResults,
                            irrelevantResults);

            // Measure precision.
            currentPrecision = 1.0 * relevantResults.size()
                               / (relevantResults.size()
                                  + irrelevantResults.size());

            System.out.println("======================");
            System.out.println("FEEDBACK SUMMARY");
            System.out.println("Query: " + query);
            System.out.println("Precision: " + currentPrecision);

            if (currentPrecision < targetMinPrecision
                    && relevantResults.size() > 0) {
                // Current precision isn't good enough but it's nonzero, so
                // adjust query and keep looping.
                System.out.println("Still below the desired precision of : "
                                   + targetMinPrecision);
                int oldQuerySize = query.size();
                query = queryAdjuster.adjustQuery(query, relevantResults,
                                                  irrelevantResults);
                assert(query.size() > oldQuerySize);
                assert(query.size() <= oldQuerySize + numAdditionalTerms);
                System.out.println("Augmented query: " + query);
            } else {
                // If we either achieve the desired precision or don't have any
                // relevant results, stop looping.
                break;
            }
        }

        // Print final message before the program finishes.
        if (currentPrecision == 0.0) {
            // None of the results were relevant.
            System.out.println("Below desired precision, but can no longer"
                               + " augment the query");
        } else {
            // Reached desired precision.
            System.out.println("Desired precision reached, done");
        }
    }


    /**
    * Parses the string representing the double precision floating point
    * expected minimum precision. Validates the precision value to make sure it
    * is in the range [0, 1].
    * Returns: The double amount if the parsing succeeded. null otherwise.
    */
    private static Double parsePrecision(String precisionString) {
        Double targetMinPrecision;
        try {
            targetMinPrecision = Double.parseDouble(precisionString);
        } catch (NumberFormatException e) {
            System.err.println("The precision you entered isn't a valid"
                               + " number.");
            return null;
        }
        if (targetMinPrecision < 0 || targetMinPrecision > 1) {
            System.err.println("The required minimum precision needs to be in"
                               + " the range [0,1].");
            return null;
        }
        return targetMinPrecision;
    }


    /**
    * Parses the string representing the integer # of additional terms wanted.
    * Makes sure that this value is positive.
    * Returns: The integer amount if the parsing succeeded. null otherwise.
    */
    private static Integer parseNumAdditionalTerms(
        String numAdditionalTermsString) {
        Integer numAdditionalTerms;
        try {
            numAdditionalTerms = Integer.parseInt(numAdditionalTermsString);
        } catch (NumberFormatException e) {
            System.err.println("The <# additional terms> you entered isn't a"
                               + " valid integer.");
            return null;
        }
        if (numAdditionalTerms < 0) {
            System.err.println("The <# additional terms> value needs to be"
                               + " positive.");
            return null;
        }
        return numAdditionalTerms;
    }


    /**
    * Presents the Bing results to the user and asks for which of the results
    * are relevant and which ones aren't. This method fills the relevantResults
    * and irrelevantResults lists accordingly.
    */
    private static void getUserFeedback(Scanner prompt,
                                        List<Result> resultList,
                                        List<Result> relevantResults,
                                        List<Result> irrelevantResults) {
        relevantResults.clear();
        irrelevantResults.clear();

        System.out.println("Bing Search Results:");
        System.out.println("======================");
        for (int i = 0; i < resultList.size(); i++) {
            // Print result.
            Result thisResult = resultList.get(i);
            System.out.println("Result " + (i + 1));
            System.out.println(thisResult);
            System.out.println();

            // Ask the user if this result is relevant.
            System.out.print("Relevant (Y/N)?");
            String yn;
            while (true) {
                yn = prompt.nextLine().toUpperCase();
                if (yn.equals("Y") || yn.equals("N")) {
                    break;
                } else {
                    System.out.println("Sorry, I didn't understand that."
                                       + " Please enter 'Y' or 'N', without"
                                       + " the quotes: ");
                }
            }
            if (yn.equals("Y")) {
                relevantResults.add(thisResult);
            } else {
                irrelevantResults.add(thisResult);
            }
            System.out.println("=======================");
        }
    }
}