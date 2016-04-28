#set($page_title = ${context} + " Validation Report")
#parse ("header.tpl")
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
     #foreach($document in $documents)
       <h4><a href="${contexturl}$document">$document</a>
       #if(${permissions.contains("META")})(<a href="${contexturl}$document?cmd=meta">meta</a>)#end 
       </h4>
       #foreach($error in $errorsByDocument.get($document))
         #if(${error.fileRelated})
           Line ${error.line}: ##
         #end
       <strong>${error.errorMessage}</strong><br>
       #end  
     #end
   </errors>
 #end
#parse ("footer.tpl")
