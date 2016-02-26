<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    
    <h1>${context}</h1>
    
    <form action="${contexturl}?cmd=search" method="get">
      Search all of ${context}: <input type="text" name="query" size="40"/> <input type="submit" value="Query"/> <input type="hidden" name="cmd" value="search" />
    </form>
    
    #if($versioncontrolled)
      Click <a href="${contexturl}/?cmd=revisions">here</a> for the latest changes, or click on a meta link below to zoom in on a document.<br/>
    #end
    
    Click <a href="${contexturl}/?cmd=validationreport">here</a> for the validation report of this entire context<br/>
    Click <a href="${contexturl}/?cmd=browse">here</a> to browse the library
    
    <h2>Books by Category</h2>
    <table>
      <tr><th>Category</th><th>Books</th></tr>
    
      #foreach($category in $classification_category)
        <tr>
            <td>${category.name}</td>
            <td>
                #foreach($book in $category.books)
                    <a href="${context}${book.path}">${book.title}</a> (
                    #foreach($outputFormat in $outputFormats)                   
                      <a href="${context}${book.path}?output=$outputFormat">$outputFormat</a>
                    #end
                    <a href="${context}${book.path}?cmd=meta">meta</a> )<br/>
                #end
            </td>
          </tr>
      #end  
    </table>
    
    <h2>Books by folder</h2>
    <table>
      <tr><th>Folder</th><th>Books</th></tr>
      #foreach($folder in $$classification_folder)
        <tr>
          <td>${folder.name}</td>
          <td>
            #foreach($book in $folder.books)
               <a href="${context}${book.path}">${book.title}</a> 
              (
               #foreach($outputFormat in $outputFormats)                   
                 <a href="${context}${book.path}?output=$outputFormat">$outputFormat</a>
               #end
               <a href="${context}${book.path}?cmd=meta">meta</a> )<br/>
            #end
          </td>
        </tr>
      #end  
    </table>
    
    <br/><br/>
    Latest successfull Pull request was at ${refresh}<br/> 
    Currently using skin: ${skin}<br>
  </body>
</html>