package durations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;

import utils.Utils;

public class SignSymptomDurations {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

		final int maxHits = 1000000;
		final String searchField = "content";
		final String indexLocation = "/home/dima/data/mimic/index/";
		final String signAndSymptomFile = "/home/dima/thyme/duration/data/unique-sign-symptoms.txt";
		final String outputDirectory = "/home/dima/out/context/";
		final int contextWindowInCharacters = 50;
		final List<String> durationIndicators = Arrays.asList("for", "x");
		
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		for(String symptom : Utils.readSetValuesFromFile(signAndSymptomFile)) {
      String outputFile = outputDirectory + symptom + ".txt";
      BufferedWriter writer = Utils.getWriter(outputFile, false);
      
		  for(String durationIndicator : durationIndicators) {
		    String queryText = symptom + " " + durationIndicator;
		    PhraseQuery phraseQuery = Utils.makePhraseQuery(queryText, searchField, 0);

		    TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
		    ScoreDoc[] scoreDocs = topDocs.scoreDocs;  		

		    for(ScoreDoc scoreDoc : scoreDocs) {
		      Document document = indexSearcher.doc(scoreDoc.doc);
		      String text = document.get(searchField).toLowerCase().replace("[\n\r]", " ");      // TODO: lowercase or not?
		      String context = Utils.getContext(queryText, text, contextWindowInCharacters);
		      String contextWithNonPrintableCharactersRemoved = context.replaceAll("[^\\x20-\\x7E]", ""); // allowable range in hex
		      writer.write(contextWithNonPrintableCharactersRemoved + "\n");
		    }
		  }
		  
      writer.close();
		}
		
    indexSearcher.close();
    System.out.println("done!");
	}
}

