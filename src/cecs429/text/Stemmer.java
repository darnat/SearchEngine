package cecs429.text;

import libs.snowball.SnowballStemmer;

public class Stemmer {
    private static SnowballStemmer mStemmer;
    private static Stemmer mInstance;

    private Stemmer() {
        try {
            Class stemClass = Class.forName("libs.snowball.englishStemmer");
            mStemmer = (SnowballStemmer) stemClass.newInstance();
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    public static Stemmer getInstance() {
        if (mInstance == null) {
            mInstance = new Stemmer();
        }
        return mInstance;
    }

    public String stemToken(String token) {
        mStemmer.setCurrent(token);
        mStemmer.stem();
		return mStemmer.getCurrent();
    }
}