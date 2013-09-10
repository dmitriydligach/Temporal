package durations;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.FSDirectory;

import utils.Utils;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

public class DiseaseDisorderDurations {
	
	@SuppressWarnings("unchecked")
  public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

	  final String eventFile = "/home/dima/thyme/event-durations/data/unique-disease-disorders.txt";

		Set<String> events = Utils.readSetValuesFromFile(eventFile);
	  
		Set<String> lessThanDaySuffixes = Sets.newHashSet("yesterday", "today", "overnight", "noted");
    Set<String> lessThanDayPrefixes = Sets.newHashSet("episode of", "presents with"); // admitted with?
		Set<String> moreThanDayPrefixes = Sets.newHashSet("history of", "diagnosed with");

		Set<List<String>> lessThanDayPatterns1 = Sets.cartesianProduct(events, lessThanDaySuffixes);
		Set<List<String>> lessThanDayPatterns2 = Sets.cartesianProduct(lessThanDayPrefixes, events);
		Set<List<String>> moreThanDayPatterns = Sets.cartesianProduct(moreThanDayPrefixes, events);
		
		Multiset<String> lessThanDayEvidence1 = countEvidence(lessThanDayPatterns1, 0);
		Multiset<String> lessThanDayEvidence2 = countEvidence(lessThanDayPatterns2, 1);
		Multiset<String> lessThanDayEvidence = Multisets.sum(lessThanDayEvidence1, lessThanDayEvidence2);
		Multiset<String> moreThanDayEvidence = countEvidence(moreThanDayPatterns, 1);
		
		for(String event : Sets.union(lessThanDayEvidence.elementSet(), moreThanDayEvidence.elementSet())) {
		  System.out.format("%20s %4d %4d\n", event, lessThanDayEvidence.count(event), moreThanDayEvidence.count(event));
		}
	}

	/**
	 * Count the number of hits for each pattern. Save the results in a multiset
	 * indexing on the specified element in the pattern.
	 * 
	 * @param patterns Set of patterns in which each list is a single pattern
	 * @param eventPosition The index of the event in each pattern
	 */
	public static Multiset<String> countEvidence(Set<List<String>> patterns, int eventPosition) throws IOException {

	  final int maxHits = 1000000;
	  final String searchField = "content";
	  final String indexLocation = "/home/dima/data/mimic/index/";

	  IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
	  IndexSearcher indexSearcher = new IndexSearcher(indexReader);

	  Multiset<String> eventHitCounts = HashMultiset.create();
	  for(List<String> pattern : patterns) {
      
	    PhraseQuery phraseQuery = new PhraseQuery();
      phraseQuery.setSlop(0);
	    for(String string : pattern) {
	      String[] elements = string.split(" ");
	      for(String element : elements) {
	        phraseQuery.add(new Term(searchField, element));
	      }
	    }
	    
      TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
      ScoreDoc[] scoreDocs = topDocs.scoreDocs;     
      eventHitCounts.add(pattern.get(eventPosition), scoreDocs.length);
	  }
	  
	  indexSearcher.close();
	  return eventHitCounts;
	}
}

