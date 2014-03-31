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

/**
 * Extract durations of events using "<event> for" pattern.
 * 
 * TODO: what if a document contains multiple instances of a pattern? are all used?
 */

public class EventDurations {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

		final int maxHits = 1000000;
		final String searchField = "content";
		final String indexLocation = "/Users/Dima/Boston/Data/Mimic/Index/";
		final String eventFile = "/Users/Dima/Boston/Thyme/Duration/Data/List/unique-drug.txt";
		final String outputDirectory = "/Users/Dima/Boston/Thyme/Duration/Data/Drug/Context/";
		final int contextWindowInCharacters = 50;
		final List<String> durationIndicators = Arrays.asList("for", "x");
		
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		for(String event : Utils.readSetValuesFromFile(eventFile)) {
      String outputFile = outputDirectory + event + ".txt";
      BufferedWriter writer = Utils.getWriter(outputFile, false);
      
		  for(String durationIndicator : durationIndicators) {
		    String queryText = event + " " + durationIndicator;
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

