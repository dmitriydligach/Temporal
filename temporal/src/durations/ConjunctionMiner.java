package durations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Set;

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
 * TODO: it seems like only the first occurence in a document of a query is returned.
 * TODO: probably need to get all of them.
 */
public class ConjunctionMiner {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

		final int maxHits = 1000000;
		final String searchField = "content";
		final String indexLocation = "/home/dima/data/mimic/index/";
		final String signAndSymptomFile = "/home/dima/thyme/duration/data/unique-sign-symptoms.txt";
		final String outputFile = "/home/dima/out/conjunction/counts.txt";
		final int contextWindowInCharacters = 50;
		
		BufferedWriter writer = Utils.getWriter(outputFile, false);
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

    Set<String> signsAndSymptoms = Utils.readSetValuesFromFile(signAndSymptomFile);
    
		for(String ss1 : signsAndSymptoms) {
		  for(String ss2 : signsAndSymptoms) {
		    
		    String queryText = ss1 + " and " + ss2;
		    PhraseQuery phraseQuery = Utils.makePhraseQuery(queryText, searchField, 0);
        
		    TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;     

        for(ScoreDoc scoreDoc : scoreDocs) {
          Document document = indexSearcher.doc(scoreDoc.doc);
          String text = document.get(searchField).toLowerCase().replace("[\n\r]", " ");    
          String context = Utils.getContext(queryText, text, contextWindowInCharacters);
          String contextWithNonPrintableCharactersRemoved = context.replaceAll("[^\\x20-\\x7E]", "");
          writer.write(queryText + ": " + contextWithNonPrintableCharactersRemoved + "\n");
        }
		  }
		}
		
		writer.close();
    indexSearcher.close();
    System.out.println("done!");
	}
}

