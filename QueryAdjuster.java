import java.util.*;

/*
* Class responsible for expanding queries based on user feedback.
*/
public class QueryAdjuster {
    private static final boolean PRINT_DEBUG_INFO = false;

    private int numAdditionalTerms;


    /*
    * Default constructor.
    */
    public QueryAdjuster() {
        // Use default number of additional terms.
        this(2);
    }


    /*
    * Constructor specifying the maximum number of additional terms that we
    * should add to the query at each iteration.
    */
    public QueryAdjuster(int numAdditionalTerms) {
        this.numAdditionalTerms = numAdditionalTerms;
    }


    /**
    * Takes in a query and the human-labeled results and returns an expanded
    * query with up to numAdditionalTerms more terms. The terms in the new
    * query are in what we consider a good approximation of the best possible
    * order.
    */
    Query adjustQuery(Query oldQuery, List<Result> relevantResults,
                      List<Result> irrelevantResults) {
        Stopwords stopwords = new Stopwords();

        // Get a set of all the tokens without stopwords. Each of these unique
        // tokens will be a dimension in our feature or weight vector.
        SortedSet<String> tokenSet = new TreeSet<String>();
        tokenSet.addAll(getAllCleanTokens(relevantResults));
        tokenSet.addAll(getAllCleanTokens(irrelevantResults));

        // Set up tokenToIndex and indexToToken lists so that it's easy to
        // find out which dimension of the feature vector corresponds to which
        // token and vice versa.
        Map<String, Integer> tokenToIndex = Utils.tokenToIndexMap(tokenSet);
        ArrayList<String> indexToToken = Utils.indexToTokenMap(tokenSet);

        // Train the weight vector based on our user-labeled data.
        double[] weights = getWeights(relevantResults, irrelevantResults,
                                      tokenToIndex, indexToToken);

        // Save the tokens and weights as a list of comparable TokenWeightPairs
        // and sort this list based on the reverse order to the one imposed
        // by the TokenWeightPair class.
        ArrayList<TokenWeightPair> tokenWeightPairs =
            new ArrayList<TokenWeightPair>();
        for (int i = 0; i < weights.length; i++) {
            tokenWeightPairs.add(
                new TokenWeightPair(indexToToken.get(i), weights[i]));
        }
        Collections.sort(tokenWeightPairs, Collections.reverseOrder());

        // Print for debugging.
        if (PRINT_DEBUG_INFO) {
            System.out.println("Token dictionary, in alphabetical order:\n"
                               + indexToToken + "\n");
            System.out.println("Weights: " + Arrays.toString(weights) + "\n");
            System.out.println("Terms sorted by weight: " + tokenWeightPairs);
        }

        Query newQuery = new Query(oldQuery);

        // Add the top numAdditionalTerms terms to the new query. Don't add
        // terms with negative weight.
        int numTermsAdded = 0;
        for (TokenWeightPair tokenWeightPair : tokenWeightPairs) {
            if (numTermsAdded == numAdditionalTerms
                    || tokenWeightPair.weight <= 0) {
                break;
            }
            if (!newQuery.contains(tokenWeightPair.token)
                    && !newQuery.contains(tokenWeightPair.token + "s")
                    && !newQuery.contains(
                        tokenWeightPair.token.substring(0,
                                tokenWeightPair.token.length() - 1))) {
                newQuery.add(tokenWeightPair.token);
                numTermsAdded++;
            }
        }

        // Put query into approximately best possible query order.
        newQuery = getBestQueryOrdering(newQuery, relevantResults,
                                        irrelevantResults);

        return newQuery;
    }


    private LinkedHashSet<String> getAllTokens(List<Result> resultList) {
        LinkedHashSet<String> allWords = new LinkedHashSet<String>();
        for (Result result : resultList) {
            allWords.addAll(result.titleTokens);
            allWords.addAll(result.descriptionTokens);
        }
        return allWords;
    }

    private LinkedHashSet<String> getAllCleanTokens(List<Result> resultList) {
        LinkedHashSet<String> allWords = new LinkedHashSet<String>();
        for (Result result : resultList) {
            allWords.addAll(result.titleTokensWithoutStopwords);
            allWords.addAll(result.descriptionTokensWithoutStopwords);
        }
        return allWords;
    }

    /**
    * Obtains the weights for all of the given tokens.
    * Note: tokenToIndex and indexToToken can be obtained by calling the
    * relevant methods in the Utils class.
    */
    private double[] getWeights(List<Result> relevantResults,
                                List<Result> irrelevantResults,
                                Map<String, Integer> tokenToIndex,
                                List<String> indexToToken) {
        double[] weights = new double[indexToToken.size()];

        final double RELEVANT_WEIGHT = 0.75;
        final double IRRELEVANT_WEIGHT = 0.15;

        final double TITLE_WEIGHT = 1.1;
        final double DESCRIPTION_WEIGHT = 1.0;

        // Contribution from tokens in relevant results.
        for (Result result : relevantResults) {
            // We weigh the increment so that irrelevant and relevant documents
            // carry the same weight.
            for (String token : result.titleTokensWithoutStopwords) {
                weights[tokenToIndex.get(token)]
                += RELEVANT_WEIGHT * TITLE_WEIGHT
                   / (TITLE_WEIGHT * result.titleTokensWithoutStopwords.size()
                      + DESCRIPTION_WEIGHT * result.descriptionTokensWithoutStopwords.size())
                   / relevantResults.size();
            }
            for (String token : result.descriptionTokensWithoutStopwords) {
                weights[tokenToIndex.get(token)]
                += RELEVANT_WEIGHT * DESCRIPTION_WEIGHT
                   / (TITLE_WEIGHT * result.titleTokensWithoutStopwords.size()
                      + DESCRIPTION_WEIGHT * result.descriptionTokensWithoutStopwords.size())
                   / relevantResults.size();
            }
        }
        // Contribution from tokens in irrelevant results.
        for (Result result : irrelevantResults) {
            // We weigh the increment so that irrelevant and relevant documents
            // carry the same weight.
            for (String token : result.titleTokensWithoutStopwords) {
                weights[tokenToIndex.get(token)]
                -= IRRELEVANT_WEIGHT * TITLE_WEIGHT
                   / (TITLE_WEIGHT * result.titleTokensWithoutStopwords.size()
                      + DESCRIPTION_WEIGHT * result.descriptionTokensWithoutStopwords.size())
                   / irrelevantResults.size();
            }
            for (String token : result.descriptionTokensWithoutStopwords) {
                weights[tokenToIndex.get(token)]
                -= IRRELEVANT_WEIGHT * DESCRIPTION_WEIGHT
                   / (TITLE_WEIGHT * result.titleTokensWithoutStopwords.size()
                      + DESCRIPTION_WEIGHT * result.descriptionTokensWithoutStopwords.size())
                   / irrelevantResults.size();
            }
        }
        return weights;
    }


    /**
    * This method takes in a query, and based on the relevant and irrelevant
    * documents, returns a new Query where the words are in a more appropriate
    * order. The ordering that we use is just the ordering in which we see the
    * first instance of each token in the relevant documents.
    */
    private Query getBestQueryOrdering(Query disorderedQuery,
                                       List<Result> relevantResults,
                                       List<Result> irrelevantResults) {
        // We use a LinkedHashSet to preserve the insertion ordering of the
        // tokens. This ordering will later be used when deciding the query
        // order of the terms.
        LinkedHashSet<String> relevantTokenSet = getAllTokens(relevantResults);
        // Add any missing query word just in case.
        relevantTokenSet.addAll(disorderedQuery);
        // Add irrelevant tokens last, just in case. We expect these not to be
        // used at all, but it's just to be safe.
        relevantTokenSet.addAll(getAllTokens(irrelevantResults));

        if (PRINT_DEBUG_INFO) {
            System.out.println(relevantTokenSet);
        }
        final Map<String, Integer> tokenToIndex
            = Utils.tokenToIndexMap(relevantTokenSet);

        // This comparator defines the order we will impose on the tokens. We
        // just use tokenToIndex to get the absolute positioning of each token
        // and then compare them.
        Comparator<String> tokenComparator = new Comparator<String>() {
            @Override
            public int compare(final String s1, final String s2) {
                Integer i1 = tokenToIndex.get(s1);
                Integer i2 = tokenToIndex.get(s2);

                if (i1 == null) {
                    i1 = 1000000;
                }
                if (i2 == null) {
                    i2 = 1000000;
                }

                return i1.compareTo(i2);
            }
        };

        if (PRINT_DEBUG_INFO) {
            System.out.println("Disordered query: " + disorderedQuery);
            for (String token : disorderedQuery) {
                System.out.println("Query token: " + token + " Token index:"
                                   + tokenToIndex.get(token));
            }
        }
        // Sort the query tokens based on the ordering imposed by the Comparator.
        Query orderedQuery = new Query(disorderedQuery);
        Collections.sort(orderedQuery, tokenComparator);
        if (PRINT_DEBUG_INFO) {
            System.out.println("Ordered query: " + disorderedQuery);
        }

        return orderedQuery;
    }


    /*
    * Test for this class. Ignore this function when running the main program.
    */
    public static void main(String[] args) {
        /*
        QueryAdjuster queryAdjuster = new QueryAdjuster();
        Query query = new Query("gates");
        List<Result> relevantResults = new ArrayList<Result>();
        List<Result> irrelevantResults = new ArrayList<Result>();
        relevantResults.add(new Result("Who We Are - Bill & Melinda Gates Foundation", "http://www.gatesfoundation.org/who-we-are", "melinda gates “As a dad, I work to protect my kids ... letter from bill and melinda gates Read the philosophy behind the philanthropy's"));
        irrelevantResults.add(new Result("Gates Corporation", "http://www.gates.com", "Gates Corporation is Powering Progress™ in the Oil & Gas, Energy, Mining, Marine, Agriculture, Transportation and Automotive Industries."));
        Query newQuery = queryAdjuster.adjustQuery(query, relevantResults, irrelevantResults);

        System.out.println("----\nNew Query: " + newQuery + "\n---");
        */

        QueryAdjuster queryAdjuster = new QueryAdjuster();
        Query query = new Query("musk");
        List<Result> relevantResults = new ArrayList<Result>();
        List<Result> irrelevantResults = new ArrayList<Result>();
        relevantResults.add(new Result("Elon Musk - Wikipedia, the free encyclopedia", "http://www.example.com", "Elon Reeve Musk (born June 28, 1971) is a South African-born Canadian-American business magnate, engineer, inventor and investor. [14] He is the CEO and CTO of SpaceX ..."));
        relevantResults.add(new Result("Elon Musk (@elonmusk) | Twitter", "http://www.example.com/", "Elon Musk-backed SolarCity claims to have built industry's most efficient solar panel http://www. theverge.com/2015/10/2/9439 173/elon-musk-backed-solarcity-claims-to ..."));
        relevantResults.add(new Result("Elon Musk: “I Definitely Don’t Want to Live Forever ...", "http://www.example.com/", "One futuristic interest SpaceX and Tesla C.E.O. Elon Musk doesn’t share with some of his fellow Silicon Valley titans? The idea that extending life is an obviously ..."));
        relevantResults.add(new Result("Elon Musk Tells Tesla Competitors to Bring It On", "http://www.example.com/", "When asked by Børsen how he feels about taking on auto and energy industry giants, Musk acknowledged that it’s challenging, yet pointed to how widely his car ..."));

        irrelevantResults.add(new Result("Musk - Wikipedia, the free encyclopedia", "http://www.example.com", "Musk is a class of aromatic substances commonly used as base notes in perfumery. They include glandular secretions from animals such as the musk deer, numerous plants ..."));
        irrelevantResults.add(new Result("Musk | Define Musk at Dictionary.com", "http://www.example.com", "noun 1. a substance secreted in a glandular sac under the skin of the abdomen of the male musk deer, having a strong odor, and used in perfumery. 2. an artificial ..."));
        irrelevantResults.add(new Result("MUSK", "http://www.example.com/", "MUSK are Lennart Döring and Gerald Zollner, who met in Berlin's nightlife back in 2009. Soon after they went through the history of dancemusic, ..."));
        irrelevantResults.add(new Result("Musk | Definition of musk by Merriam-Webster", "http://www.example.com/", "Definition of MUSK for Kids: a strong-smelling material that is used in perfumes and is obtained from a gland of an Asian deer (musk deer) or is prepared artificially"));
        irrelevantResults.add(new Result("Musk perfume ingredient, Musk fragrance and essential oils ...", "http://www.example.com/", "Musk is a whole class of fragrant substances used as base notes in perfumery. This wonderful animalistic note creates a groundwork on which the rest of the aromatic ..."));
        irrelevantResults.add(new Result("Amazon.com: perfume musk: Beauty", "http://www.example.com/", "ASDM Beverly Hills Egyptian Musk, A Rich, Egyptian Musk with Precious Wood Blends and Floral Top Note., 0.5 Ounce"));


        Query newQuery = queryAdjuster.adjustQuery(query, relevantResults,
                         irrelevantResults);

        System.out.println("----\nNew Query: " + newQuery + "\n---");
    }
}


/*
* We define a (token, weight) tuple just for convenience, so that it is easier
* to sort the tokens by weight. We break ties in weight by using the word
* length.
*/
class TokenWeightPair implements Comparable<TokenWeightPair> {
    String token;
    Double weight;


    public TokenWeightPair(String token, Double weight) {
        this.token = token;
        this.weight = weight;
    }


    public String toString() {
        return token + " " + weight;
    }


    public int compareTo(TokenWeightPair b) {
        // Sort by weight. If there's a tie, sort by word length.
        Integer tokenLength = token.length();
        Integer bTokenLength = b.token.length();
        return weight.compareTo(b.weight) != 0 ? weight.compareTo(b.weight)
               : tokenLength.compareTo(bTokenLength);
    }

}
