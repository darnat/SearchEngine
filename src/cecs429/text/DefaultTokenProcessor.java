package cecs429.text;

import java.lang.*;
import java.util.*;

/**
 * A DefaultTokenProcessor creates terms from tokens by:
 *   - Removing all non-alphanumeric characters from the beginning and end of the token
 *   - Removing all apostropes or quotation marks
 *   - Removing hyphens / split on hyphens (multiple tokens produced)
 *   - Converting to lowercase
 *   - Stemming token using Porter2 stemmer
 */
public class DefaultTokenProcessor implements TokenProcessor {
	// private SnowballStemmer mStemmer;

	public DefaultTokenProcessor() {
		// try {
		// 	Class stemClass = Class.forName("libs.snowball.englishStemmer");
		// 	mStemmer = (SnowballStemmer) stemClass.newInstance();
		// } catch(Exception ex) {
		// 	System.out.println(ex);
		// }
	}

	@Override
	public List<String> processToken(String token) {
		List<String> terms = new ArrayList<>();
		Sanitizer sanitizer = Sanitizer.getInstance();
		String strToken = normalizeToken(token);

		if (!strToken.isEmpty()) {
			if (strToken.contains("-")) {
				// Remove hyphens and add to terms list
				String unhyphenToken = strToken.replaceAll("-", "");
				terms.add(unhyphenToken);
				// Add stemmed as well
				terms.add(sanitizer.stemToken(unhyphenToken));

				// Split on hyphens and add to terms list
				String[] splitTokens = strToken.split("-");
				for (String splitToken : splitTokens) {
					// terms.add(splitToken);
					terms.add(sanitizer.stemToken(splitToken));
				}
			} else {
				// terms.add(strToken);
				terms.add(sanitizer.stemToken(strToken));
			}
		}

		return terms;
	}

	public String normalizeToken(String token) {
		// Remove single/double quotes and lowercase token before adding
		StringBuilder tokenSB = new StringBuilder(token.replaceAll("[\'\"]", "").toLowerCase());
		int len = tokenSB.length();

		// Remove non-alphanumeric character from start
		if (len > 0 && tokenSB.substring(0, 1).matches("\\W")) {
			tokenSB.deleteCharAt(0);
			len = tokenSB.length();
		}

		// Remove non-alphanumeric character from end
		if (len > 0 && tokenSB.substring(len - 1, len).matches("\\W")) {
			tokenSB.deleteCharAt(len - 1);
		}

		return tokenSB.toString();
	}

	// public String stemToken(String token) {
	// 	mStemmer.setCurrent(token);
	// 	mStemmer.stem();
	// 	return mStemmer.getCurrent();
	// }
}
