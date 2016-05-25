import java.util.*;

/**
* Some static utilities used by other classes.
*/
public class Utils {
	private static final boolean PRINT_DEBUG_INFO = false;

	public static List<String> tokenizeString(String string) {
		// Only keep spaces, apostrophies, and letter characters (including letter characters in languages other than English).Then turn into lowercase. Then discard any "'s" at the end of a word. Then split into a list of tokens based on the spaces.

		String step0_5 = string; // string.replaceAll("[-–—]", " ");
		String step1 = step0_5.replaceAll("[^\\p{L} '’]", "");
		String step2 = step1.toLowerCase();
		String step3 = step2.replaceAll("['’]s", "");
		List<String> step4 = Arrays.asList(step3.split("\\s+"));

		if (PRINT_DEBUG_INFO) {
			System.out.println("step0: " + string);
			System.out.println("step0_5: " + step0_5);
			System.out.println("step1: " + step1);
			System.out.println("step2: " + step2);
			System.out.println("step3: " + step3);
			System.out.println("step4: " + step4);
		}
		return step4;
	}


	public static String joinStringList(List<String> stringList, String separator) {
		if (stringList.isEmpty()) {
			return "";
		}
		String string = null;
		for (String s : stringList) {
			if (string == null) {
				string = s;
			} else {
				string += separator + s;
			}
		}
		return string;
	}


	public static Map<String, Integer> tokenToIndexMap(Set<String> tokens) {
		Map<String, Integer> tokenToIndex = new HashMap<String, Integer>();
		int i = 0;
		for (String token : tokens) {
			tokenToIndex.put(token, i);
			i++;
		}
		return tokenToIndex;
	}


	public static ArrayList<String> indexToTokenMap(Set<String> tokens) {
		return new ArrayList<String>(tokens);
	}
}