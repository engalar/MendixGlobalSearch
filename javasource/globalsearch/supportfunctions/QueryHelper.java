package globalsearch.supportfunctions;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;

public class QueryHelper {
	
	public static Query getQuery(String raw, globalsearch.proxies.Enum_SearchType type) throws ParseException {
		if(type.equals(globalsearch.proxies.Enum_SearchType.Advanced)) {
			QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
			return parser.parse(raw);
		}else {
			return buildBooleanQuery(raw);
		}
	}
	
	private static Query buildBooleanQuery(String searchString) throws ParseException {
		
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		
		if(searchString.isEmpty() == false) {
			
			// Set up the parser.
			QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
			parser.setAllowLeadingWildcard(true);  
			parser.setDefaultOperator(QueryParser.Operator.AND); 
			
			// Generate query.
			String[] queryWords = searchString.split(" ");
			
			// Wildcard queries.
			int fuzzyCount = 0;
			List<String> fuzzyWords = new ArrayList<String>();
			if(queryWords.length > 1) {
				SpanNearQuery.Builder wildcardBuilder = new SpanNearQuery.Builder("contents", false).setSlop(globalsearch.proxies.constants.Constants.getSlop().intValue());
				SpanNearQuery.Builder fuzzyBuilder = new SpanNearQuery.Builder("contents", false).setSlop(globalsearch.proxies.constants.Constants.getSlop().intValue());
			    for (int i = 0; i < queryWords.length; i++) {
			        WildcardQuery wildQuery = new WildcardQuery(new Term("contents","*" + queryWords[i].trim().replaceAll("[ ]+", "*") + "*"));
			        if(queryWords[i].length() >= 3) {
			        	 FuzzyQuery fuzzy = new FuzzyQuery(new Term("contents",queryWords[i].trim()), 1, 0, 20, true);
			        	 fuzzyBuilder.addClause(new SpanMultiTermQueryWrapper<FuzzyQuery>(fuzzy));
			        	 ++fuzzyCount;
			        	 fuzzyWords.add(queryWords[i]);
			        }
			        wildcardBuilder.addClause(new SpanMultiTermQueryWrapper<WildcardQuery>(wildQuery));
			        
			    }
			    queryBuilder.add(wildcardBuilder.build(), BooleanClause.Occur.SHOULD);
			    if(fuzzyCount > 1) queryBuilder.add(fuzzyBuilder.build(), BooleanClause.Occur.SHOULD);
			    else if(fuzzyCount == 1){
			    	FuzzyQuery fuzzy = new FuzzyQuery(new Term("contents", fuzzyWords.get(0)));
					queryBuilder.add(fuzzy, BooleanClause.Occur.SHOULD);
			    }
			} else {
				String wildCardString = "*" + searchString.trim().replaceAll("[ ]+", "*") + "*";
				Query wildCardQuery = new WildcardQuery(new Term("contents", wildCardString));
				if(searchString.length() >= 3) {
					FuzzyQuery fuzzy = new FuzzyQuery(new Term("contents", searchString.trim()), 1, 0, 20, true);
					queryBuilder.add(fuzzy, BooleanClause.Occur.SHOULD);
				}
				queryBuilder.add(wildCardQuery, BooleanClause.Occur.SHOULD);
				
			}
		}
		
		return queryBuilder.build();
		
	}
	
	public static Query rewriteQuery(Query q, String field) {
	    if(q instanceof BooleanQuery) {
	    	BooleanQuery.Builder booleanBuilder = new BooleanQuery.Builder();
	        
	        for (BooleanClause clause : (BooleanQuery)q) {
	            booleanBuilder.add(rewriteQuery(clause.getQuery(), field), clause.getOccur());
	        }
	        return booleanBuilder.build();
	    }else if(q instanceof TermQuery) {
	        return new TermQuery(new Term(field, ((TermQuery)q).getTerm().text()));
	    }else if(q instanceof PhraseQuery) {
	    	PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
	        Term[] terms = ((PhraseQuery)q).getTerms();
	        for (int i = 0; i < terms.length; i++) {
	        	phraseQueryBuilder.add(new Term(field, terms[i].text()), ((PhraseQuery)q).getPositions()[i]);
	        }
	        return phraseQueryBuilder.build();
	    }else if(q instanceof WildcardQuery) {
	        return new WildcardQuery(new Term(field, ((WildcardQuery)q).getTerm().text()));
	    } else {
	        throw new UnsupportedOperationException("Query type not known: " + q.getClass());
	    }
	}

}
