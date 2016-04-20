#set($page_title = "Login to Thoth")
#parse ("header.tpl")
  <h1>Login<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <h3>Please enter your credentials to login</h3>

  <form action="./?cmd=login" method="post">
    User name: <input type="text" name="username" value="" size="40" autofocus="true"/><br/>
    Password: <input type="password" name="password" value=""/><br/>
    <input type="submit" value="Login"/> 
  </form>
  #if($message)
    <br/>
    $message
  #end
#parse ("footer.tpl")
