#macro(displaySection $section)
  #if($section.isFlatText())
    $section.getLocalText()
  #else
    <details open="true">
      #set($pointer = "selectedsection=$section.getPath()")
      #set($here = "$section.getPath()")
      <summary id="$here" class="documentsection">
        $section.getPath()    
      </summary>
      <details #if(${selectedsection}=="$section.getPath()")open="true"#set($scrollto=$here)#end>
        <summary class="commentsection">Comments (${section.getComments().size()})</summary>
        #foreach ($comment in $section.getComments())
          <div class="comment">
            ${thothutil.formatTimestamp($comment.getTimeCreated())} by ${comment.getUserName()}
            #if($identity == $comment.userName)
              <a class="commentaction" href="${documenturi}?cmd=comment&operation=copy&commentid=$comment.getId()&$pointer">Copy</a>
            #end
            #if($identity == $comment.userName || ${permissions.contains("DELETE_ANY_COMMENT")})
              <a class="commentaction" href="${documenturi}?cmd=comment&operation=delete&commentid=$comment.getId()&$pointer">Delete</a>
            #end
            <br/>
            $thothutil.markdown2html(${comment.getBody()})
          </div>
        #end
        <div>
          <form action="${documenturi}?cmd=comment&operation=create&docpath=$section.getPath()&$pointer" method="post">
            <textarea name="commenttext" rows="4" class="commentbox">#if(${selectedsection}=="$section.getPath()" && $editText)$editText#end</textarea>
            <br/>                                       
            <input type="submit" value="Add comment"/>
          </form>
        </div>
      </details>
      #if ($section.getSubSections().size() > 0)
        #foreach ($child in $section.getSubSections())
          #displaySection($child)
        #end
      #end
    </details>
  #end    
#end

<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${title}</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/markdown.css">
  </head>
  <body>
    <h1>Comment on ${title}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    <a href="${contexturl}">Back to Index</a>

    #displaySection($mainsection)
    
    #if($scrollto)
      <script>
        window.onload = function(){
            document.getElementById('$scrollto').scrollIntoView(true);
        };
      </script>
    #end
  </body>
</html>