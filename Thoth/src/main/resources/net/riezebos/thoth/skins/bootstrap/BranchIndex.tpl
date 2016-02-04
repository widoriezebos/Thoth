<!DOCTYPE html>
<html lang="en-US">
<head>
<title>${branch} Documentation Index</title>
<link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
	
<h1>${branch} documentation</h1>

<form action="${branchurl}?cmd=search" method="get">
  Search all of ${branch}: <input type="text" name="query"/> <input type="submit" value="Query"/> <input type="hidden" name="cmd" value="search" />
</form>
Click <a href="${branchurl}/?cmd=revisions">here</a> for the latest changes, or click on a meta link below to zoom in on a document.<br/>
Click <a href="${branchurl}/?cmd=validationreport">here</a> for the validation report of this entire branch<br/>
Click <a href="${branchurl}/?cmd=browse">here</a> to browse the library

<h2>Books by Category</h2>
<table>
  <tr><th>Category</th><th>Books</th></tr>
#foreach($category in $categories)
  <tr>
      <td>${category.name}</td>
      <td>
#foreach($book in $category.books)
         <a href="${branch}${book.path}">${book.title}</a> (<a href="${branch}${book.path}?output=pdf">pdf</a>&nbsp;
                         <a href="${branch}${book.path}?output=raw">md</a>
			 <a href="${branch}${book.path}?cmd=meta">meta</a>)<br/>
#end
      </td>
  </tr>
#end	
</table>

<h2>Books by folder</h2>
<table>
  <tr><th>Folder</th><th>Books</th></tr>
#foreach($folder in $folders)
  <tr>
      <td>${folder.name}</td>
      <td>
#foreach($book in $folder.books)
         <a href="${branch}${book.path}">${book.title}</a> (<a href="${branch}${book.path}?output=pdf">pdf</a>&nbsp;
                         <a href="${branch}${book.path}?output=raw">md</a>)<br/>
#end
      </td>
  </tr>
#end	
</table>

<br/><br/>Latest successfull Pull request was at ${refresh} 

</body>
</html>