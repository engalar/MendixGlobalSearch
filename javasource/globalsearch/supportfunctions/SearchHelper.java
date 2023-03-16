package globalsearch.supportfunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import globalsearch.proxies.Searcher;

public class SearchHelper {
	
	
	public static List<IMendixObject> performSearch(Searcher SearchObject, IndexSearcher searcher, Query parsedquery, IContext context) throws IOException, CoreException{
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(SearchObject.getNumberOfResults(), null, Integer.MAX_VALUE);
		searcher.search(parsedquery, collector);
		TopDocs returnedDocs = collector.topDocs();
		
		List<IMendixIdentifier> ids = new ArrayList<IMendixIdentifier>();

		// Processing results.
		for (ScoreDoc doc : returnedDocs.scoreDocs) {

			// Get the document.
			Document searchDoc = searcher.doc(doc.doc);
			
			// Get the id of the document.
			String docId = searchDoc.get("id");
			// Create mendix identifier with the id.
			IMendixIdentifier currentDoc = Core.createMendixIdentifier(docId);
			if(currentDoc == null) continue;
			ids.add(currentDoc);
			
		}
		return Core.retrieveIdList(context, ids);
	}
	
	public static List<IMendixObject> performFacetSearch(Searcher SearchObject, IndexSearcher searcher, Query parsedquery, IContext context) throws IOException, CoreException{
		
		// Performing search.
		FacetsCollector facetsCollector = new FacetsCollector(true);
		TopScoreDocCollector collector = TopScoreDocCollector.create(SearchObject.getNumberOfResults(), null, Integer.MAX_VALUE);
		searcher.search(parsedquery, MultiCollector.wrap(collector,facetsCollector));
		Facets facet = new SortedSetDocValuesFacetCounts(new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader()), facetsCollector);
		List<FacetResult> facets = facet.getAllDims(10);
		TopDocs returnedDocs = collector.topDocs();
		
		List<IMendixIdentifier> ids = new ArrayList<IMendixIdentifier>();
		// Processing results.
		for (ScoreDoc doc : returnedDocs.scoreDocs) {

			// Get the document.
			Document searchDoc = searcher.doc(doc.doc);
			
			// Get the id of the document.
			String docId = searchDoc.get("id");
			// Create mendix identifier with the id.
			IMendixIdentifier currentDoc = Core.createMendixIdentifier(docId);
			if(currentDoc == null) continue;
			ids.add(currentDoc);
			
		}
		
		//Add facets.
		for (FacetResult i : facets) {
			IMendixObject facetGroup = Core.instantiate(context, globalsearch.proxies.FilterResult.entityName);
			facetGroup.setValue(context, globalsearch.proxies.FilterResult.MemberNames.Name.toString(), i.dim);
			facetGroup.setValue(context, globalsearch.proxies.FilterResult.MemberNames.Count.toString(),
					i.value.intValue());
			facetGroup.setValue(context, globalsearch.proxies.FilterResult.MemberNames.FilterResult_Searcher.toString(),
					SearchObject.getMendixObject().getId());
			for (LabelAndValue j : Arrays.asList(i.labelValues)) {
				IMendixObject facetMendix = Core.instantiate(context, globalsearch.proxies.FacetResult.entityName);
				facetMendix.setValue(context, globalsearch.proxies.FacetResult.MemberNames.Name.toString(), j.label);
				facetMendix.setValue(context, globalsearch.proxies.FacetResult.MemberNames.Count.toString(),
						j.value.intValue());
				facetMendix.setValue(context,
						globalsearch.proxies.FacetResult.MemberNames.FacetResult_FilterResult.toString(),
						facetGroup.getId());
			}
		}
		return Core.retrieveIdList(context, ids);
	}
}
