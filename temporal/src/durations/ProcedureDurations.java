package durations;

import java.io.File;
import java.io.IOException;
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
import com.google.common.collect.Sets;

public class ProcedureDurations {
	
	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

	  final String eventFile = "/home/dima/thyme/event-durations/data/unique-procedures.txt";

		Set<String> events = Utils.readSetValuesFromFile(eventFile);
    Set<String> lessThanDayVerbs = Sets.newHashSet("done", "performed");
		Set<String> lessThanDayTimes = Sets.newHashSet("yesterday", "today");
		Set<String> moreThanDayVerbs = Sets.newHashSet("stopped", "started", "began", "finished", "ended");
		Set<String> moreThanDayTimes = Sets.newHashSet("yesterday", "today", "last week", "last month");

		Multiset<String> lessThanDay = count(events, lessThanDayVerbs, lessThanDayTimes);
		Multiset<String> moreThanDay = count(events, moreThanDayVerbs, moreThanDayTimes);
		for(String event : Sets.union(lessThanDay.elementSet(), moreThanDay.elementSet())) {
		  System.out.format("%20s %4d %4d\n", event, lessThanDay.count(event), moreThanDay.count(event));
		}
	}

	 public static Multiset<String> count(Set<String> events, Set<String> verbs, Set<String> times) 
	     throws CorruptIndexException, IOException, ParseException, InvalidTokenOffsetsException {

	    final int maxHits = 1000000;
	    final String searchField = "content";
	    final String indexLocation = "/home/dima/data/mimic/index/";

	    IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(indexLocation)));
	    IndexSearcher indexSearcher = new IndexSearcher(indexReader);

	    Multiset<String> eventHitCounts = HashMultiset.create();
	    for(String event : events) {
	      for(String verb : verbs) {
	        for(String time : times) {
	          PhraseQuery phraseQuery = new PhraseQuery();
	          phraseQuery.add(new Term(searchField, event));
	          phraseQuery.add(new Term(searchField, verb));
	          phraseQuery.add(new Term(searchField, time));
	          phraseQuery.setSlop(0);
	          
	          TopDocs topDocs = indexSearcher.search(phraseQuery, maxHits);
	          ScoreDoc[] scoreDocs = topDocs.scoreDocs;     
	          eventHitCounts.add(event, scoreDocs.length);
	          
	          if(scoreDocs.length > 0) {
	            // System.out.format("%s (%d)\n", phraseQuery, scoreDocs.length);
	          }
	        }
	      }
	    }
	    
	    indexSearcher.close();
	    return eventHitCounts;
	  }

}

