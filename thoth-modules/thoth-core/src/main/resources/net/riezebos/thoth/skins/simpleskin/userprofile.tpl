<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css"/>
  </head>
  <body>
    <a href="${contexturl}">Back to Index</a>
    
    #if($message)
      <pre>$message</pre>
    #end
    
    <h1>User ${user.identifier}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    
    <form action="./?cmd=userprofile" method="post">                                 
      Identifier: <input type="text" value="${user.identifier}" size="15" disabled="true"/><br/>                                       
      First name: <input type="text" name="firstname" #if(${user.firstname})value="${user.firstname}"#end size="15" autofocus="true"/><br/>         
      Last name: <input type="text" name="lastname"  #if(${user.lastname})value="${user.lastname}"#end size="15"/><br/>            
      Password: <input type="password" name="password" value="" size="15"/><br/>                                                           
      Repeat Password: <input type="password" name="password2" value="" size="15"/><br/>                                                           
      <input type="submit" value="Update"/>
    </form>

  </body>
</html>