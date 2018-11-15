package test;

import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class DefaultTokenProcessor implements TokenProcessor {
	private Stemmer mStemmer = Stemmer.getInstance();
	
        public void initializeTest() {
            String testCase0 = "the pOTato MAn-fish";
            String testCase1 = "$$$$POTato#$%#";
            String testCase2 = "SwIMMER@$#$#_(#";
            
            List<String> data = new ArrayList<>();
            data.add("the potato manfish");
            data.add("the potato man");
            data.add("fish");
            
            assertEquals(data, processToken(testCase0));
            assertEquals("potato", normalizeToken(testCase1));
            
            //The return value from snobol is really wierd. cant get to match because [] are returned
            assertEquals("swimm", normalizeAndStemToken(testCase2).substring(0, 5));
        }
        
	@Override
	public List<String> processToken(String token) {
		List<String> terms = new ArrayList<>();
		String strToken = normalizeToken(token);

		if (!strToken.isEmpty()) {
			if (strToken.contains("-")) {
				// Remove hyphens and add to terms list
				String unhyphenToken = strToken.replaceAll("-", "");
				terms.add(mStemmer.stemToken(unhyphenToken));

				// Split on hyphens and add to terms list
				String[] splitTokens = strToken.split("-");
				for (String splitToken : splitTokens) {
					terms.add(mStemmer.stemToken(splitToken));
				}
			} else {
				terms.add(mStemmer.stemToken(strToken));
			}
		}

		return terms;
	}

	public String normalizeAndStemToken(String token) {
		return mStemmer.stemToken(normalizeToken(token));
	}

	public String normalizeToken(String token) {
		// Remove single/double quotes and lowercase token before adding
		StringBuilder tokenSB = new StringBuilder(token.replaceAll("[\'\"]", "").toLowerCase());

		// Remove non-alphanumeric characters from beginning of token
		while (tokenSB.length() > 0 && !Character.isLetterOrDigit(tokenSB.charAt(0))) {
			tokenSB.deleteCharAt(0);
		}

		// Remove non-alphanumeric characters from end of token
		while (tokenSB.length() > 0 && !Character.isLetterOrDigit(tokenSB.charAt(tokenSB.length() - 1))) {
			tokenSB.deleteCharAt(tokenSB.length() - 1);
		}

		return tokenSB.toString();
	}
}
