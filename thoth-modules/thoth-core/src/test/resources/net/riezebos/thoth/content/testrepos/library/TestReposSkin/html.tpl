<!DOCTYPE html>
<html lang="en-US">
  <head>
    <title>${title}</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    
    <style>
      body {
        display: block;
        page: main;
      }
      @page main {
        @bottom-left { 
          content: "${title}"; 
        }
      }
    </style>
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/markdown.css">
  </head>
  <body>
    ${body}  
  </body>
</html>