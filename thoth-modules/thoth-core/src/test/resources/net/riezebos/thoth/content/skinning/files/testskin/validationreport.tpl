<!DOCTYPE html>
<html lang="en-US">
  <head>
    <title>${document.name} Meta Info</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  <body>
    <h1>Validation report</h1>
    
    #if(${errors.isEmpty()})
      Very good, there are currently no validation errors in ${context}
    #else
      <h3>Validation Errors</h3>
      <errors>
        #foreach($error in $errors)
          #if(${error.fileRelated})
            ${error.file}(<a href="${contexturl}${error.file}">${error.line}</a>): ##
          #end
          <strong>${error.errorMessage}</strong><br>
        #end
      </errors>
    #end
    
  </body>
</html>