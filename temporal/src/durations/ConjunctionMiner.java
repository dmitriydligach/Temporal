package durations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
		final String indexLocation = "/home/dima/data/mimic/index/";
		final String signAndSymptomFile = "/home/dima/thyme/duration/data/unique-sign-symptoms.txt";
		final String outputFile = "/home/dima/out/conjunction/counts.txt";
		
		BufferedWriter writer = Utils.getWriter(outputFile, false);
    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

    Set<String> signsAndSymptoms = Utils.readSetValuesFromFile(signAndSymptomFile);
    Map<String, Set<String>> adjacency = new HashMap<String, Set<String>>(); // adjacency list
    
    for(String ss1 : signsAndSymptoms) {
      adjacency.put(ss1, new HashSet<String>());
      
		  for(String ss2 : signsAndSymptoms) {
		    
		    if(ss1.equals(ss2)) {
		      continue;
		    }

		    String queryText = ss1 + " and " + ss2;
		    PhraseQuery phraseQuery = Utils.makePhraseQuery(queryText, searchField, 0);
		    TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        
        if(scoreDocs.length > 5) {
          writer.write(queryText + ": " + scoreDocs.length + "\n");
          
          // about to add ss1 -> ss2 link
          // check first if ss12 -> ss1 already exists
          if(adjacency.get(ss2) != null) {
            if(adjacency.get(ss2).contains(ss1)) {
              continue;
            }
          }
          
          adjacency.get(ss1).add(ss2);  
        }
		  }
		}
		
    toDot(adjacency, "/home/dima/out/conjunction/graph.dot");
    
		writer.close();
    indexSearcher.close();
	}
	
	public static void toDot(Map<String, Set<String>> adjacency, String file) throws IOException {
	  
	  BufferedWriter writer = Utils.getWriter(file, false);
	  writer.write("graph g {\n");
	  
	  for(String from : adjacency.keySet()) {
	    for(String to : adjacency.get(from)) {
	      String output = String.format("%s--%s;\n", from, to);
	      writer.write(output);
	    }
	  }
	  
	  writer.write("}\n");
	  writer.close();
	}
}
