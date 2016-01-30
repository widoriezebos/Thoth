<!DOCTYPE html>
<html lang="en-US">
<head>
<title>${branch} Documentation Index</title>
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
      ${revision.action} ${revision.path} (<a href="${branchurl}/${revision.path}.diff?commitId=${revision.commitId}">Diff</a>)<br/>
#end
      </td>
      <td>${commit.message}</td>
  </tr>    
#end
</table>

<br/><br/>Latest successfull refresh was at ${refresh} 

</body>
</html>