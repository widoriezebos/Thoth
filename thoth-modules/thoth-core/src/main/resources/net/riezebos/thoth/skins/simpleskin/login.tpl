#set($page_title = "Login to Thoth")
#parse ("header.tpl")

  <script>
    // We try to preserve any bookmark that was present in a URL that redirected
    // to the login page.
    
    function prepareSubmit(form) {
        // Extract the fragment from the browser's current location.
        var hash = decodeURIComponent(self.document.location.hash);
     
        // The fragment value may not contain a leading # symbol
        if (hash && hash.indexOf("#") === -1) {
            hash = "#" + hash;
        }
       
        // Append the fragment to the current action so that it persists to the redirected URL.
        form.action = form.action + hash;
        return true;
    }
  </script>

  <h1>Login<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <h3>Please enter your credentials to login</h3>

  <form action="./?cmd=login" method="post" onsubmit="return prepareSubmit(this);">
    User name: <input type="text" name="username" value="" size="40" autofocus="true"/><br/>
    Password: <input type="password" name="password" value=""/><br/>
    <input type="submit" value="Login"/> 
  </form>
  #if($message)
    <br/>
    $message
  #end
#parse ("footer.tpl")
