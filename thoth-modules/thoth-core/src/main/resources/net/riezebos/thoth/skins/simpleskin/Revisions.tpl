<!DOCTYPE html>
<html lang="en-US">
<head>
<title>${context} Documentation Index</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
<h1>Latest changes</h1>
<table>
  <tr><th>Timestamp</th><th>Author</th><th>Diff</th><th>Comment</th></tr>
#foreach($commit in $commitList)
  <tr>
      <td>${commit.formattedTimestamp}</td>
      <td>${commit.author}</td>
      <td>     
#foreach($revision in $commit.revisions)
      ${revision.action} ${revision.path} (<a href="${contexturl}${revision.path}?cmd=diff&commitId=${thothutil.encodeUrl($revision.commitId)}">Diff</a>)<br/>
#end
      </td>
      <td>${commit.message}</td>
  </tr>    
#end
</table>

<br/><br/>Latest successfull Pull request was at ${refresh} 

</body>
</html>