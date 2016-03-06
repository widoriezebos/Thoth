<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>Login to Thoth</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    <h3>Please enter your credentials to login</h3>

    <form action="./?cmd=login" method="post">
      User name: <input type="text" name="username" value="" size="40"/><br/>
      Passord: <input type="password" name="password" value=""/><br/>
      <input type="submit" value="Login"/> 
    </form>
    #if($message)
      <br/>
      $message
    #end

  </body>
</html>