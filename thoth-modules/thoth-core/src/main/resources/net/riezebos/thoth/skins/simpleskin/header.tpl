<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>$page_title</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css">

#if($markdown_mode)##
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/markdown.css">
#elseif($use_tabs)##
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/tabs.css"/>
#else
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/menu.css"/>
#end
    <script type="text/javascript" src="${skinbase}/Webresources/details-shim.min.js"></script>
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/details-shim.min.css">
  </head>
  <body>
