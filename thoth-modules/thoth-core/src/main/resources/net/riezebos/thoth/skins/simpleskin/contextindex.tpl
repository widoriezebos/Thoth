<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css"/>
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
    
    #if($versioncontrolled && ${permissions.contains("REVISION")})
      Click <a href="${contexturl}${libraryroot}?cmd=revisions">here</a> for the latest changes, or click on a meta link below to zoom in on a document.<br/>
    #end
    
    #if(${permissions.contains("VALIDATE")})
    Click <a href="${contexturl}${libraryroot}?cmd=validationreport">here</a> for the validation report of this entire context<br/>
    #end
    #if(${permissions.contains("BROWSE")})
    Click <a href="${contexturl}${libraryroot}?cmd=browse">here</a> to browse the library
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
    #if(${permissions.contains("MANAGE_USERS")})
      <a href=".?cmd=manageusers">Manage users</a><br/>
    #end   
    
    #if($loggedin)
      <a href=".?cmd=userprofile">Edit profile</a>
      <br/>
      <a href=".?cmd=logout">Log out</a><br/>
    #else
      <a href=".?cmd=login">Login</a> <br/>
    #end

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