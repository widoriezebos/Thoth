<!DOCTYPE html>
<html lang="en-US">
  <head>
    <title>Diff for ${title}</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
  </head>
  
  <body>
    <h2>Changes to ${title}</h2>
    <commitmessage>
    <div>${author}, ${timestamp}</div><br/>
    ${commitMessage}
    </commitmessage>
    <div>
      <br/><strong>Source Diff</strong>
    </div>
    <diffsection>
      ${body}
    </diffsection>
  </body>
</html>