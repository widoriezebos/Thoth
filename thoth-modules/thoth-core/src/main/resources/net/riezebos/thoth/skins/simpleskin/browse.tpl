<!DOCTYPE html>
<html lang="en-US">
<head>
  <meta charset="utf-8">
  <title>Browse ${path}</title>
  <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
  <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
  <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
    <a href="${contexturl}">Back to Index</a>
  
  #if(${atRoot})
    <h1>Contents of context ${context}</h1>
  #else
    <h1>Browse ${path}</h1>
    <a href="..?cmd=browse">Step back</a>
  #end
  
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
                <a href="${contexturl}${contentNode.path}?cmd=meta">meta</a>
            </td>
        </tr>
      #end
    #end
  </table>
</body>
</html>