#set($page_title = ${context} + " Revisions")
#parse ("header.tpl")
  <h1>Latest changes<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <a href="${contexturl}">Back to Index</a>
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
            ${revision.action} ${thothutil.addHtmlBreaks(${revision.path})} (<a href="${contexturl}${revision.path}?cmd=diff&commitId=${thothutil.encodeUrl($revision.commitId)}">Diff</a>)<br/>
          #end
        </td>
        <td>${commit.message}</td>
      </tr>    
    #end
  </table>
  
  <br/><br/>
  Latest successfull Pull request was at ${refresh} 
#parse ("footer.tpl")
    