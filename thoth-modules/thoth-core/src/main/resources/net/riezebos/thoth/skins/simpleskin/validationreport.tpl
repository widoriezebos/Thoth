<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Validation Report</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    <h1>${context} Validation report<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    <a href="${contexturl}">Back to Index</a>
    <br/>

    <h3>Orphaned comments</h3>
    #if(${orphanedComments.isEmpty()})
      There are currently no orphaned comments in ${context}
    #else
      <errors>
        #foreach($comment in $orphanedComments)
          <div>
            <pre>${comment.documentPath}</pre>
            ${thothutil.formatTimestamp($comment.timeCreated)} ${comment.userName}@${comment.contextName}: ${comment.title}<br/>
            #if($identity == $comment.userName || ${permissions.contains("DELETE_ANY_COMMENT")})
              <a class="commentaction" href=".?cmd=validationreport&operation=deletecomment&commentid=$comment.getId()&$pointer">Delete</a>
            #end
          </div>  
        #end
      </errors>
    #end

    <h3>Validation Errors</h3>
    #if(${errors.isEmpty()})
      There are currently no validation errors in ${context}
    #else
      <errors>
        #foreach($error in $errors)
          #if(${error.fileRelated})
            ${error.file}(<a href="${contexturl}${error.file}">${error.line}</a>): ##
          #end
          <strong>${error.errorMessage}</strong><br>
        #end
      </errors>
    #end

  </body>
</html>