<!DOCTYPE html>
<html lang="en-US">
<title>Search results</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
<body>
<form action="${contexturl}" method="get">
  Search all of ${context}: <input type="text" name="query" value="${thothutil.escapeHtml($query)}"/> <input type="submit" value="Query"/> <input type="hidden" name="cmd" value="search" />
</form>
Showing page ${page}<br/>
#set($prevpage=${page}+-1)
#if($prevpage > 0)
<a href="${contexturl}?cmd=search&amp;query=${thothutil.encodeUrl($query)}&amp;page=${prevpage}">Previous page</a>
#else
(First page)
#end
#if($hasmore)
#set($nextpage=${page}+1)
<a href="${contexturl}?cmd=search&amp;query=${thothutil.encodeUrl($query)}&amp;page=${nextpage}">Next page</a>
#else
 (last page)
#end
&nbsp;
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
${searchResult.indexNumber}. Found in <a href="$contexturl${searchResult.document}">${searchResult.document}</a> (<a href="$contexturl${searchResult.document}?cmd=meta">meta</a>)
#if(!${searchResult.bookReferences.isEmpty()})
which is part of 
#foreach($book in $searchResult.bookReferences)
<a href="$contexturl${book.path}">${book.fileName}</a>&nbsp;
#end
#end
<br/>
#if(${searchResult.image})
<img src="$contexturl${searchResult.document}" alt="$contexturl${searchResult.document}">&nbsp;
#else
#foreach($fragment in ${searchResult.fragments})
<fragment>
$fragment.text
</fragment>
#end
#end
</searchresult>
<br>
#end
</body>
</html>