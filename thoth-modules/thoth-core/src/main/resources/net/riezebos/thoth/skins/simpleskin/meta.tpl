#set($page_title = ${document.name} + " Meta Info")
#parse ("header.tpl")
  <h1>${document.name}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  Full path: $path<br/>
  Latest modification: ${thothutil.formatTimestamp($document.lastModified)}

  <h3>Meta tags</h3>
  <metatags>
    #foreach($key in $metatagKeys)
      ${key} = <strong>${metatags.get($key)}</strong><br/>
    #end
    #if($metatagKeys.isEmpty())
      None
    #end
  </metatags>

  <h3>Document Structure</h3>
  <documentstructure>
  #foreach($documentNode in $documentNodes)
    #foreach($idx in [0..$documentNode.level])
      &nbsp;&nbsp;&nbsp;##
    #end
    <a href="$contexturl/${documentNode.path}">${documentNode.fileName}</a> (<a href="$contexturl/${documentNode.path}?cmd=meta">meta</a>)<br/>
  #end
  </documentstructure>    
  
  #if(!${errors.isEmpty()})
    <h3>Validation Errors</h3>
    <errors>
      #foreach($error in $errors)
        #if(${error.fileRelated})
          ${error.file}(<a href="$contexturl/${error.file}">${error.line}</a>): ##
        #end
        <strong>${error.errorMessage}</strong><br>
      #end
    </errors>
  #end
  
  <h3>Included by</h3>
  #if($usedBy)
    #foreach($documentPath in $usedBy)
      <a href="$contexturl/${documentPath}">${documentPath}</a> (<a href="$contexturl/${documentPath}?cmd=meta">meta</a>)<br/>
    #end
  #else
    This document is not uncluded by any other document.
  #end
  
  #if($usedByIndirect)
    <h3>Included (indirectly) by</h3>
    #foreach($documentPath in $usedByIndirect)
      <a href="$contexturl/${documentPath}">${documentPath}</a> (<a href="$contexturl/${documentPath}?cmd=meta">meta</a>)<br/>
    #end
  #end

  #if($versioncontrolled)
    <h3>Version history (summary)</h3>
      
    <table>
      <tr><th>Timestamp</th><th>Author</th><th>Diff</th><th>Comment</th></tr>
      #foreach($commit in $commitList)
        <tr>
          <td>${thothutil.formatTimestamp($commit.timestamp)}</td>
          <td>${commit.author}</td>
          <td>     
            #foreach($revision in $commit.revisions)
              ${thothutil.addHtmlBreaks(${revision.path})} (<a href="$contexturl/${revision.fileName}?cmd=diff&commitId=${thothutil.encodeUrl($revision.commitId)}">Diff</a>)<br/>
            #end
          </td>
          <td>${commit.message}</td>
        </tr>    
      #end
    </table>
    
    <h3>Version history per fragment</h3>
    <table>
      #foreach($documentNode in $documentNodes)
        <tr><th colspan="3"><a href="$contexturl/${documentNode.path}">${documentNode.fileName}</a></th></tr>
        #set($commits = $commitMap.get(${documentNode.path}))
        <tr><th>Timestamp</th><th>Author</th><th>Comment</th></tr>
        #foreach($commit in $commits)
          <tr>
            <td>     
              #foreach($revision in $commit.revisions)
                <a href="$contexturl/${documentNode.path}?cmd=diff&commitId=${thothutil.encodeUrl($revision.commitId)}">${thothutil.formatTimestamp($commit.timestamp)}</a>
              #end
            </td>
            <td>${commit.author}</td>
            <td>${commit.message}</td>
          </tr>    
        #end  
          <tr><td colspan="3"></td></tr>
      #end
    </table>
  #end
#parse ("footer.tpl")
    