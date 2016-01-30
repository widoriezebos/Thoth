<!DOCTYPE html>
<html lang="en-US">
<head>
<title>${title}</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />

<!-- Prince PDF related CSS
     The style below must remain inside this document because of keyword replacement.
     Also: the body tag must appear here because otherwise the PDF fails to render the header and footer
     for some reason -->
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