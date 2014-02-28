package durations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
		final String indexLocation = "/Users/Dima/Boston/Data/Mimic/Index/";
		final String signAndSymptomFile = "/Users/Dima/Boston/Thyme/Duration/Data/List/unique-disease-disorder.txt";
		final String outputFile = "/Users/Dima/Boston/Output/counts.txt";
		final int minCoocurence = 1; // discard conjunctions below this frequency threshold
		
		BufferedWriter writer = Utils.getWriter(outputFile, false);
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

    Set<String> signsAndSymptoms = Utils.readSetValuesFromFile(signAndSymptomFile);

    // set of co-occuring symptom pairs, e.g. {{pain, anxiety}, {abuse, depression}, ...}
    Set<Set<String>> adjacency = new HashSet<Set<String>>();
    
    for(String ss1 : signsAndSymptoms) {
		  for(String ss2 : signsAndSymptoms) {
		    if(ss1.equals(ss2)) {
		      continue;
		    }
		    
		    String queryText = ss1 + " and " + ss2;
		    PhraseQuery phraseQuery = Utils.makePhraseQuery(queryText, searchField, 0);
		    TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        
        if(scoreDocs.length > minCoocurence) {
          writer.write(ss1 + "|" + ss2 + "|" + scoreDocs.length + "\n");
          adjacency.add(new HashSet<String>(Arrays.asList(ss1, ss2)));
        }
		  }
		}
		
		writer.close();
    indexSearcher.close();
	}

	/**
	 * Convert set of co-occuring symptoms into graphviz dot format.
	 */
	public static void toDot(Set<Set<String>> adjacency, String file) throws IOException {

	  BufferedWriter writer = Utils.getWriter(file, false);
	  writer.write("graph g {\n");

	  for(Set<String> pair : adjacency) {
	    String output = String.format("%s--%s;\n", pair.toArray()[0], pair.toArray()[1]);
	    writer.write(output);
	  }

	  writer.write("}\n");
	  writer.close();
	}
}
