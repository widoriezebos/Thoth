<!DOCTYPE html>
<html lang="en-US">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>${title}</title>
<link rel="icon" href="${skinbase}/Webresources/eyefreight-favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/eyefreight-favicon.png" type="image/png" />

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
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet" integrity="sha256-7s5uDGW3AHqw6xtJmNNtr+OBRJUlgkNJEo78P4b0yRw= sha512-nNo+yCHEyn0smMxSswnf/OnX6/KwJuZTlNZBjauKhTK0c+zT+q5JOCx0UFhXQ6rJR9jg6Es8gPuD2uZcYDLqSw==" crossorigin="anonymous">
<script src="https://code.jquery.com/jquery-2.2.0.min.js" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha256-KXn5puMvxCw+dAYznun+drMdG1IFl3agK0p/pqT9KAo= sha512-2e8qq0ETcfWRI4HJBzQiA3UoyFk6tbNyG+qSaIBZLyW9Xf3sWZHN/lxe9fTh1U45DpPf07yj94KsUHHWe4Yk1A==" crossorigin="anonymous"></script>
<script type="text/javascript">
	$(function() {
		$( "table" ).addClass( "table table-striped table-hover" );
		$( "img" ).addClass( "img-responsive" );
	});
</script>
</head>
<body>
<div class="container">
${body}	
</div>
</body>
</html>