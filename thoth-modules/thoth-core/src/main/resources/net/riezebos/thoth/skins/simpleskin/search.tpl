<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>Search results</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    <h1>Search results<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    <a href="${contexturl}">Back to Index</a>
    <form action="${contexturl}" method="get">
      Search all of ${context}: 
      <input type="text" name="query" value="#if($query)${thothutil.escapeHtml($query)}#end" size="40" autofocus="true"/>
      <input type="submit" value="Search"/> <input type="hidden" name="cmd" value="search" />
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
    #if($query)
    <h1>Search results for '$query'</h1>
    #end
    
    #if($errorMessage)
      <fragment>
        <pre>
          Could not understand your query<br/>
          $errorMessage
        </pre>
      </fragment>
    #end
    
    #if($searchResults.isEmpty())
      Sorry, no documents found for your query.
    #end
    
    #foreach($searchResult in $searchResults)
      <searchresult>
        ${searchResult.indexNumber}. Found in <a href="$contexturl${searchResult.document}">${searchResult.document}</a>
        #if(${permissions.contains("META")})(<a href="$contexturl${searchResult.document}?cmd=meta">meta</a>)#end
        
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