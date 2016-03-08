<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css"/>
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/menu.css"/>
  </head>
  <body>
    
    <h1>${context}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    
    #if(${permissions.contains("SEARCH")})
    <form action="${contexturl}?cmd=search" method="get">
      Search all of ${context}: 
      <input type="text" name="query" size="40" autofocus="true"/> 
      <input type="submit" value="Search"/> 
      <input type="hidden" name="cmd" value="search" />
    </form>
    #end
    
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
                    #if(${permissions.contains("META")})<a href="${context}${book.path}?cmd=meta">meta</a>#end 
                    )<br/>
                #end
            </td>
          </tr>
      #end  
    </table>
        
    <br/><br/>

    <ul>
      #if($versioncontrolled && ${permissions.contains("REVISION")})
        <li><a href="${contexturl}${libraryroot}?cmd=revisions">Revisions</a></li>
      #end
      #if(${permissions.contains("VALIDATE")})
        <li><a href="${contexturl}${libraryroot}?cmd=validationreport">Validation report</a></li>
      #end
      #if(${permissions.contains("BROWSE")})
        <li><a href="${contexturl}${libraryroot}?cmd=browse">Browse</a></li>
      #end
      #if(${permissions.contains("MANAGE_USERS")})
        <li><a href=".?cmd=manageusers">Manage users</a></li>
      #end
      #if(${permissions.contains("PULL")})
        <li><a href=".?cmd=pull">Pull</a></li>
      #end   
      #if(${permissions.contains("REINDEX")})
        <li><a href=".?cmd=reindex">Reindex</a></li>
      #end
      #if($loggedin)
        <li><a href=".?cmd=userprofile">Edit profile</a></li>
        <li class="floatright"><a href=".?cmd=logout">Log out</a></li>
      #else
        <li class="floatright"><a href=".?cmd=login">Login</a></li> 
      #end
    </ul>

    <footer>
      #if($loggedin)
        Logged in as ${identity}.
      #end  
      Currently using skin: ${skin}<br>
      Latest successfull Pull request was at ${refresh}.
      Powered by Thoth core version ${thothutil.getVersion()}
    </footer>
    
  </body>
</html>