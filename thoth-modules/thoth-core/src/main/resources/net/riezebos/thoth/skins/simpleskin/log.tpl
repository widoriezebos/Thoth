#set($page_title = $title)
#parse ("header.tpl")
  <h1>$title<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <a href="${contexturl}">Back to Index</a>
  <pre>
${log}
  </pre>
#parse ("footer.tpl")
  