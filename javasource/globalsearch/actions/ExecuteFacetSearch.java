// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package globalsearch.actions;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import globalsearch.supportfunctions.DirectoryCreator;
import globalsearch.supportfunctions.QueryHelper;
import globalsearch.supportfunctions.SearchHelper;
import com.mendix.systemwideinterfaces.core.IMendixObject;

/**
 * Use this action to execute a faceted search, so a search where additional filter options are returned in the results.
 */
public class ExecuteFacetSearch extends CustomJavaAction<java.util.List<IMendixObject>>
{
	private IMendixObject __SearchObject;
	private globalsearch.proxies.Searcher SearchObject;

	public ExecuteFacetSearch(IContext context, IMendixObject SearchObject)
	{
		super(context);
		this.__SearchObject = SearchObject;
	}

	@java.lang.Override
	public java.util.List<IMendixObject> executeAction() throws Exception
	{
		this.SearchObject = __SearchObject == null ? null : globalsearch.proxies.Searcher.initialize(getContext(), __SearchObject);

		// BEGIN USER CODE
		String searchString = SearchObject.getSearchString();
		
		// Create directory and instantiate reader and seacher.
		FSDirectory indexDir = DirectoryCreator.create();		
		IndexReader reader = DirectoryReader.open(indexDir);
		
		IndexSearcher searcher = new IndexSearcher(reader);
		Query parsedquery = QueryHelper.getQuery(searchString, SearchObject.getSearchType());
		
		return SearchHelper.performFacetSearch(SearchObject, searcher, parsedquery, this.getContext());
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "ExecuteFacetSearch";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
