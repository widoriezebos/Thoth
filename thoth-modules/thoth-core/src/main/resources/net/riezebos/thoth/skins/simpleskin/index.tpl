<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>Documentation Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
        <h1>Welcome to Thoth<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
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

    <br/><br/>
    Latest successfull Pull request was at ${refresh}
    <br/>
    #if(${permissions.contains("PULL")})
      To update the site to the latest version right now; you can <a href=".?cmd=pull">Pull manually</a><br/>
    #end   
    #if(${permissions.contains("REINDEX")})
    Click <a href=".?cmd=reindex">here</a> to force a reindex of all contexts<br/>
    #end
    #if(${permissions.contains("MANAGE_USERS")})
      <a href=".?cmd=manageusers">Manage users</a><br/>
    #end   
    <br/>
    #if($loggedin)
      Logged in as ${identity}.
      <br/>
      <a href=".?cmd=userprofile">Edit profile</a>
      <br/>
      <a href=".?cmd=logout">Log out</a><br/>
    #else
      #if(!$$contexts.isEmpty()) ##If the contexts are empty; a login link is rendered already header (see above)
        <a href=".?cmd=login">Login</a> <br/>
      #end  
    #end
    <sub>Powered by Thoth core version ${thothutil.getVersion()}</sub>
  </body>
</html>