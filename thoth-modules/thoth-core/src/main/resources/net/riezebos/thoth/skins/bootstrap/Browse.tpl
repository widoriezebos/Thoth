<!DOCTYPE html>
<html lang="en-US">
<head>
<title>Browse ${path}</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
	
#if(${atRoot})
<h1>Contents of context ${context}</h1>
<a href="${contexturl}">Step back</a>
#else
<h1>Browse ${path}</h1>
<a href="..?cmd=browse">Step back</a>
#end

<table>
  <tr><th>Timestamp</th><th>Size</th><th>Name</th><th>Link</th></tr>
#foreach($contentNode in $contentNodes)
#if(${contentNode.folder})
  <tr><td>${thothutil.formatDate($contentNode.dateModified)}</td><td>${contentNode.size}</td>
      <td><a href="${contexturl}${contentNode.path}/?cmd=browse">${contentNode.path}</a></td>
      <td></td>
  </tr>
#else
  <tr><td>${thothutil.formatDate($contentNode.dateModified)}</td><td>${contentNode.size}</td><td>${contentNode.path}</td>
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