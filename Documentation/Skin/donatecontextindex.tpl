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
                    #if(${permissions.contains("COMMENT")})<a href="${context}${book.path}?cmd=comment">comment</a>#end
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
      #if(${permissions.contains("MANAGE_CONTEXTS")})
        <li><a href=".?cmd=managecontexts">Manage contexts</a></li>
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

    <br/>
    <div style="font-size: 10px; float: left">
      Like Thoth? Using it? Then please help me improve Thoth and make a donation.
      <form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top"/>
        <input type="hidden" name="cmd" value="_s-xclick"/>
        <input type="hidden" name="hosted_button_id" value="RV98AZNQZF3SQ"/>
        <input type="image" src="https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!"/>
        <img alt="" border="0" src="https://www.paypalobjects.com/nl_NL/i/scr/pixel.gif" width="1" height="1"/>
      </form>
    </div>
    
    <script type="text/javascript">
      var sc_project=10912825; 
      var sc_invisible=1; 
      var sc_security="dc27d06e"; 
      var scJsHost = (("https:" == document.location.protocol) ?
                       "https://secure." : "http://www.");
      document.write("<sc"+"ript type='text/javascript' src='" +
                     scJsHost+
                     "statcounter.com/counter/counter.js'></"+"script>");
    </script>
		     
    <noscript><div class="statcounter"><a title="shopify stats"
    href="http://statcounter.com/shopify/" target="_blank"><img
    class="statcounter"
    src="http://c.statcounter.com/10912825/0/dc27d06e/1/"
    alt="stats"></a></div></noscript>
  </body>
</html>