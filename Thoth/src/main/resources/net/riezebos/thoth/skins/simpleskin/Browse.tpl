<!DOCTYPE html>
<html lang="en-US">
<head>
<title>Browse ${path}</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
	
<h1>Browse ${path}</h1>

#foreach($contentNode in $contentNodes)
#if(${contentNode.folder})
<a href="${branchurl}${contentNode.path}?cmd=browse">${contentNode.dateModified} ${contentNode.size} ${contentNode.path}</a><br/>
#else
${contentNode.dateModified} ${contentNode.size} ${contentNode.path} (<a href="${branchurl}${contentNode.path}">html</a> <a href="${branchurl}${contentNode.path}?output=raw">raw</a>)<br/>
#end
#end
</body>
</html>