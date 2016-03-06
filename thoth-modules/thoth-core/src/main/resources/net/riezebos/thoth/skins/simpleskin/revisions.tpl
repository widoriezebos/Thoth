<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Documentation Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    <h1>Latest changes<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    Showing page ${page}<br/>
    #set($prevpage=${page}+-1)
    #if($prevpage > 0)
      <a href="${contexturl}?cmd=revisions&amp;page=${prevpage}">Previous page</a>
    #else
      (First page)
    #end
    #if($hasmore)
      #set($nextpage=${page}+1)
      <a href="${contexturl}?cmd=revisions&amp;page=${nextpage}">Next page</a>
    #else
      (last page)
    #end
    &nbsp;
    
    <table>
      <tr><th>Timestamp</th><th>Author</th><th>Diff</th><th>Comment</th></tr>
      #foreach($commit in $commitList)
        <tr>
          <td>${thothutil.formatTimestamp($commit.timestamp)}</td>
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
    
    <br/><br/>
    Latest successfull Pull request was at ${refresh} 
    
  </body>
</html>