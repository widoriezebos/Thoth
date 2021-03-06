#set($page_title = "Documentation Index")
#parse ("header.tpl")
  <h1>Welcome to $servername<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  #if(!$contexts.isEmpty())
    <h3>Please select one of the contexts below</h3>
  #else  
    #if(!$loggedin)
      Please <a href=".?cmd=login">Login</a> to gain access <br/>
    #end  
  #end
  
  #foreach($context in $contexts)
    <a href="${context}">${context}</a><br/>
  #end	

  #if(!$problems.isEmpty())
    <h3>Problems in the configuration</h3>
    <ul class="problems">
      #foreach($problem in $problems)
        <li>$problem</li>
      #end
    </ul>  
  #end
  <br/><br/>
  <ul>
    #if($versioncontrolled && ${permissions.contains("REVISION")})
      <li><a href="${contexturl}${libraryroot}?cmd=revisions">Revisions</a></li>
    #end
    #if(${permissions.contains("MANAGE_USERS")})
      <li><a href=".?cmd=manageusers">Manage users</a></li>
    #end
    #if(${permissions.contains("MANAGE_CONTEXTS")})
      <li><a href=".?cmd=managecontexts">Manage contexts</a></li>
    #end
    #if(${permissions.contains("PULL")})
      <li><a href=".?cmd=pull">Pull</a></li>
    #end   
    #if(${permissions.contains("REINDEX")})
      <li><a href=".?cmd=reindex">Reindex</a></li>
    #end
    #if($loggedin)
      <li><a href=".?cmd=userprofile">Edit profile</a></li>
      <li class="alignright"><a href=".?cmd=logout">Log out</a></li>
    #else
      <li class="alignright"><a href=".?cmd=login">Login</a></li> 
    #end
  </ul>
#parse ("footer.tpl")
    