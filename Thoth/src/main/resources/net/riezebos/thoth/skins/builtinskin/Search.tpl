<!DOCTYPE html>
<html lang="en-US">
<title>Search results</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
<body>
<form action="${branchurl}" method="get">
  Search all of ${branch}: <input type="text" name="query" value="$query"/> <input type="submit" value="Query"/> <input type="hidden" name="cmd" value="search" />
</form>
<h1>Search results for '$query'</h1>

#if($errorMessage)
<fragment>
<pre>
There was a problem parsing your query.<br/>
$errorMessage
</pre>
</fragment>
#end

#if($searchResults.isEmpty())
Sorry, no documents found for your query.
#end
#foreach($searchResult in $searchResults)
<searchresult>
Found in <a href="$branchurl/${searchResult.document}">${searchResult.document}</a> (<a href="$branchurl/${searchResult.document}.meta">meta</a>)
#if(!${searchResult.bookReferences.isEmpty()})
which is part of 
#foreach($book in $searchResult.bookReferences)
<a href="$branchurl/${book.path}">${book.fileName}</a>&nbsp;
#end
#end
<br/>
#foreach($fragment in ${searchResult.fragments})
<fragment>
$fragment
</fragment>
#end
</searchresult>
<br>
#end
</body>
</html>