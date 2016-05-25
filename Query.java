import java.util.*;
import java.net.URLEncoder;

/**
* We represent a query as a list of tokens.
*/
class Query extends ArrayList<String> {
    public Query(String queryString) {
        super(Utils.tokenizeString(queryString));
    }


    public Query(Query query) {
        super(query);
    }


    public String toString() {
        return Utils.joinStringList(this, " ");
    }


    /**
    * Turns the query into a format that URLs can understand.
    */
    public String serialize() {
        try {
            return URLEncoder.encode(this.toString(), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }

    /**
    * A test case.
    */
    public static void main(String[] args) {
        System.out.println(new Query("π").serialize());
    }
}
