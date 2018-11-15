package test;

import org.junit.Test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PositionalIndexTest {
	static DocumentCorpus corpus;
	static Index index;
	
	   public static void initializeTest() {
		   Path corpusPath = Paths.get("small-corpus").toAbsolutePath().normalize();
		   corpus = DirectoryCorpus.loadJsonDirectory(corpusPath, ".json");
		   TokenProcessor processor = new DefaultTokenProcessor();
		   index = indexCorpusMemory(corpus, processor);
//		   assertEquals("I am done with Junit setup",str);
	   }
	   
	   @Test
	   public void testVocabSize() {
		   initializeTest();
		   List<String> vocabulary = index.getVocabulary();
//			for (int i = 0; i < vocabulary.size(); i++) {
//				System.out.println(i + ": " + vocabulary.get(i));
//			}
			assertEquals(vocabulary.size(), 46);
	   }
	   
	   @Test
	   public void testSimpleQuery() {
		   initializeTest();
		   TokenProcessor processor = new DefaultTokenProcessor();
		   BooleanQueryParser queryParser = new BooleanQueryParser();
		   QueryComponent qc = queryParser.parseQuery("corpus");
		   List<Posting> postings = qc.getPostings(index, processor);
		   assertEquals(1, postings.size());
		   
		   qc = queryParser.parseQuery("cake");
		   postings = qc.getPostings(index, processor);
		   assertEquals(2, postings.size());
		   
		   qc = queryParser.parseQuery("random");
		   postings = qc.getPostings(index, processor);
		   assertEquals(1, postings.size());
	   }
	   
	   @Test
	   public void testSimpleOrQuery() {
		   initializeTest();
		   TokenProcessor processor = new DefaultTokenProcessor();
		   BooleanQueryParser queryParser = new BooleanQueryParser();
		   QueryComponent qc = queryParser.parseQuery("wow + fork");
		   List<Posting> postings = qc.getPostings(index, processor);
		   assertEquals(2, postings.size());
	   }
	   
	   @Test
	   public void testSimpleAndQuery() {
		   initializeTest();
		   TokenProcessor processor = new DefaultTokenProcessor();
		   BooleanQueryParser queryParser = new BooleanQueryParser();
		   QueryComponent qc = queryParser.parseQuery("lie boy");
		   List<Posting> postings = qc.getPostings(index, processor);
		   assertEquals(1, postings.size());
		   
		   qc = queryParser.parseQuery("cake lie");
		   postings = qc.getPostings(index, processor);
		   assertEquals(2, postings.size());
	   }
	   
	   @Test
	   public void testComplexOrAndQuery() {
		   initializeTest();
		   TokenProcessor processor = new DefaultTokenProcessor();
		   BooleanQueryParser queryParser = new BooleanQueryParser();
		   QueryComponent qc = queryParser.parseQuery("lie + boy glados");
		   List<Posting> postings = qc.getPostings(index, processor);
		   assertEquals(1, postings.size());
	   }
	   
		private static Index indexCorpusMemory(DocumentCorpus corpus, TokenProcessor processor) {
			Iterable<Document> documents = corpus.getDocuments();
			PositionalInvertedIndex index = new PositionalInvertedIndex();

			for (Document doc : documents) {
				int position = 0;
				Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
				for (String token : tokens) {
					List<String> terms = processor.processToken(token);
					for (String term : terms) {
						index.addTerm(term, doc.getId(), position);
					}
					position++;
				}
			}

			return index;
		}
}
