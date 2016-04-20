#set($page_title = "Edit profile")
#parse ("header.tpl")
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
#parse ("footer.tpl")
