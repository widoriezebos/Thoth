<!DOCTYPE html>
<html lang="en-US">
<head>
<title>Eyefreight Documentation Index</title>
<link rel="icon" href="${skinbase}/Webresources/eyefreight-favicon.png" type="image/png" />
<link rel="shortcut icon" href="${skinbase}/Webresources/eyefreight-favicon.png" type="image/png" />
<link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">
</head>
<body>
<h3>Please select one of the branches below</h3>
#foreach($branch in $branches)
<a href="${branch}">${branch}</a><br/>
#end	
<br/><br>Latest successfull refresh was at ${refresh}
<br/>To update the site to the latest version right now; you can <a href=".?cmd=pull">Pull manually</a> or click <a href=".?cmd=reindex">here</a> to force a reindex<br/>
</body>
</html>