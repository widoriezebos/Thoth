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
    <h3>Please select one of the contexts below</h3>

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
    Click <a href=".?cmd=reindex">here</a> to force a reindex<br/>
    #end
    <br/>
    <sub>Powered by Thoth core version ${thothutil.getVersion()}</sub>
  </body>
</html>