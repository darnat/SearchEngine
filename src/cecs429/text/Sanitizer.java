package cecs429.text;

import libs.snowball.SnowballStemmer;

public class Sanitizer {
    private SnowballStemmer mStemmer;
    private static Sanitizer mInstance = null;

    private Sanitizer() {
        try {
			Class stemClass = Class.forName("libs.snowball.englishStemmer");
            mStemmer = (SnowballStemmer) stemClass.newInstance();
		} catch(Exception ex) {
			System.out.println(ex);
        }
    }

    public String sanitize(String value) {
        return value.trim().replaceAll("\t", " ").replaceAll(" +", " ");
    }

    public String stemToken(String token) {
        mStemmer.setCurrent(token);
        mStemmer.stem();
		return mStemmer.getCurrent();
    }
    
    public static Sanitizer getInstance() {
        if (mInstance == null) {
            mInstance = new Sanitizer();
        }
        return mInstance;
    }

}