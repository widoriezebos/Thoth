#set($page_title = "Browse " + ${path})
#parse ("header.tpl")
  #if(${atRoot})
    <h1>Browse ${context}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  #else
    <h1>Browse ${path}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    <a href="..?cmd=browse">Step back</a><br/>
  #end
  <a href="${contexturl}">Back to Index</a>
  
  <table>
    <tr><th>Timestamp</th><th>Name</th><th>Size</th><th>Contents</th></tr>
    #foreach($contentNode in $contentNodes)
      #if(${contentNode.folder})
        <tr><td>${thothutil.formatDate($contentNode.dateModified)}</td>
            <td><a href="${contexturl}${contentNode.path}/?cmd=browse">${contentNode.path}</a></td>
            <td></td><td></td>
        </tr>
      #else
        <tr><td>${thothutil.formatDate($contentNode.dateModified)}</td><td>${contentNode.path}</td><td>${contentNode.size}</td>
            <td><a href="${contexturl}${contentNode.path}">html</a> 
                <a href="${contexturl}${contentNode.path}?output=raw">raw</a>
                #if(${permissions.contains("META")})<a href="${contexturl}${contentNode.path}?cmd=meta">meta</a>#end
            </td>
        </tr>
      #end
    #end
  </table>
#parse ("footer.tpl")
    