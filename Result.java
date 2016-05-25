import java.util.*;

/**
* We represent each result as a title, url, and description. We also store a
* list-of-tokens representation for the title and description.
*/
class Result {
	// Raw Strings.
	public String title;
	public String url;
	public String description;
	// Processed String lists.
	public List<String> titleTokens;
	public List<String> descriptionTokens;
	public List<String> titleTokensWithoutStopwords;
	public List<String> descriptionTokensWithoutStopwords;


	public Result(String title, String url, String description) {
		// Raw Strings.
		this.title = title;
		this.url = url;
		this.description = description;
		// Processed String lists.
		this.titleTokens = Utils.tokenizeString(title);
		this.descriptionTokens = Utils.tokenizeString(description);
		this.titleTokensWithoutStopwords
		    = Stopwords.cleanStopwords(this.titleTokens);
		this.descriptionTokensWithoutStopwords
		    = Stopwords.cleanStopwords(this.descriptionTokens);
	}


	public String toString() {
		return "[\n"
		       + " URL: " + this.url + "\n"
		       + " Title: " + this.title + "\n"
		       + " Summary: " + this.description + "\n"
		       + "]";
	}
}