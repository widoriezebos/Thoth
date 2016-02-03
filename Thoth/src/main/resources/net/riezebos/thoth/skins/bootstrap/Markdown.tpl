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
<script src="${skinbase}/Webresources/dataTables.conditionalPaging.js"></script>
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet" integrity="sha256-7s5uDGW3AHqw6xtJmNNtr+OBRJUlgkNJEo78P4b0yRw= sha512-nNo+yCHEyn0smMxSswnf/OnX6/KwJuZTlNZBjauKhTK0c+zT+q5JOCx0UFhXQ6rJR9jg6Es8gPuD2uZcYDLqSw==" crossorigin="anonymous">
<script src="https://code.jquery.com/jquery-2.2.0.min.js" crossorigin="anonymous"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha256-KXn5puMvxCw+dAYznun+drMdG1IFl3agK0p/pqT9KAo= sha512-2e8qq0ETcfWRI4HJBzQiA3UoyFk6tbNyG+qSaIBZLyW9Xf3sWZHN/lxe9fTh1U45DpPf07yj94KsUHHWe4Yk1A==" crossorigin="anonymous"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/s/bs/jszip-2.5.0,pdfmake-0.1.18,dt-1.10.10,af-2.1.0,b-1.1.0,b-colvis-1.1.0,b-html5-1.1.0,b-print-1.1.0,cr-1.3.0,r-2.0.0/datatables.min.css"/>
<script type="text/javascript" src="https://cdn.datatables.net/s/bs/jszip-2.5.0,pdfmake-0.1.18,dt-1.10.10,af-2.1.0,b-1.1.0,b-colvis-1.1.0,b-html5-1.1.0,b-print-1.1.0,cr-1.3.0,r-2.0.0/datatables.min.js"></script>
<script type="text/javascript">	
	$(function() {
		/*
			$( "table" ).addClass( "table table-striped table-hover" );
			$( "img" ).addClass( "img-responsive" );
		*/
		$("table").DataTable({
			responsive: true,
			colReorder: true,
			dom: "<'row'<'col-sm-3'B><'col-sm-3'l><'col-sm-6'f>>" +
				 "<'row'<'col-sm-12'tr>>" +
				 "<'row'<'col-sm-5'i><'col-sm-7'p>>",
			buttons: [
            	'copyHtml5',
            	'excelHtml5',
            	'csvHtml5',
            	'pdfHtml5'
        	]
		});
	});
	
	document.addEventListener("DOMContentLoaded", function(event) { 
  		var tables = document.querySelectorAll('table');
  		Array.prototype.forEach.call(tables, function(el, i){
			if (el.classList) {
  				el.classList.add("table");
  				el.classList.add("table-striped");
  				el.classList.add("table-hover");
 			} else {
  				el.className += ' ' + "table table-striped table-hover";
  			}
		});
		
		var imgs = document.querySelectorAll('img');
  		Array.prototype.forEach.call(imgs, function(el, i){
			if (el.classList) {
  				el.classList.add("img-responsive");
 			} else {
  				el.className += ' ' + "img-responsive";
  			}
		});
	});
</script>
</head>
<body>
<div class="container">
${body}	
</div>
</body>
</html>