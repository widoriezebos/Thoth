#set($page_title = ${context} + " Revisions")
#parse ("header.tpl")
  <h1>Latest changes<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <a href="${contexturl}">Back to Index</a>
  <span class="floatright">Showing page ${page}</span><br/>
  #set($prevpage=${page}+-1)
  #if($prevpage > 0)
    <a href="${contexturl}?cmd=revisions&amp;page=${prevpage}">Previous page</a>
  #else
    (First page)
  #end
  <span class="floatright">
  #if($hasmore)
    #set($nextpage=${page}+1)
    <a href="${contexturl}?cmd=revisions&amp;page=${nextpage}">Next page</a>
  #else
    (last page)
  #end
  </span>
  &nbsp;
  
  <table>
    <tr><th>Timestamp</th><th>Author</th><th>Diff</th><th>Comment</th></tr>
    #foreach($commit in $commitList)
      <tr>
        <td>${thothutil.formatTimestamp($commit.timestamp)}</td>
        <td>${commit.author}</td>
        <td>
          <details class="revisiondetail">
            <summary>Actions</summary>
            #foreach($revision in $commit.revisions)
              ${revision.action} ${thothutil.addHtmlBreaks(${revision.path})} (<a href="${contexturl}${revision.path}?cmd=diff&commitId=${thothutil.encodeUrl($revision.commitId)}">Diff</a>)<br/>
            #end
          </details>
        </td>
        <td>${commit.message}</td>
      </tr>    
    #end
  </table>
  
#parse ("footer.tpl")
    