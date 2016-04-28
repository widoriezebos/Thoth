  <footer>
    #if($loggedin)
      Logged in as ${identity}.
    #end  
    Currently using skin: ${skin}<br>
    Latest successfull Pull request was at ${refresh}.
    Powered by Thoth core version ${thothutil.getVersion()}
  </footer>
  </body>
</html>