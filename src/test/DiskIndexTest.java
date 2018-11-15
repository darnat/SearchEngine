package test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.RankedRetrieval;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import org.junit.Test;

public class DiskIndexTest {
	static DocumentCorpus corpus;
	static Index index;
	
	public void initializeTest() {
		   Path corpusPath = Paths.get("small-corpus").toAbsolutePath().normalize();
		   corpus = DirectoryCorpus.loadJsonDirectory(corpusPath, ".json");
		   TokenProcessor processor = new DefaultTokenProcessor();
		   indexCorpus(corpus, processor, corpusPath.resolve("index"));
	}
	
	@Test
	public void testSimpleQuery() {
		BooleanQueryParser queryParser = new BooleanQueryParser();
		RankedRetrieval rr = new RankedRetrieval();
	}
	
	private static void indexCorpus(DocumentCorpus corpus, TokenProcessor processor, Path absolutePath) {
		Iterable<Document> documents = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		DiskIndexWriter diw = new DiskIndexWriter();
		Map<Integer, Map<String, Integer>> docWeightList = new HashMap<>();

		for (Document doc : documents) {
			int position = 0;
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					index.addTerm(term, doc.getId(), position);
					
					// For document weight calculations
					if (docWeightList.containsKey(doc.getId())) {
						Map<String, Integer> docWeights = docWeightList.get(doc.getId());

						if (docWeights.containsKey(term)) {
							docWeights.put(term, docWeights.get(term) + 1); // increment term frequency
						} else {
							docWeights.put(term, 1);
						}
					} else {
						Map<String, Integer> weight = new HashMap<String, Integer>();
						weight.put(term, 1);
						docWeightList.put(doc.getId(), weight);
					}
				}
				position++;
			}
		}

		try {
			// Save disk-based index
			diw.writeIndex(absolutePath, index);

			// Save document weights
			diw.createDocumentWeights(absolutePath, docWeightList);

		} catch (Exception ex) {
			System.out.println("Error creating disk-based index: " + ex.getMessage());
		}
	}
}