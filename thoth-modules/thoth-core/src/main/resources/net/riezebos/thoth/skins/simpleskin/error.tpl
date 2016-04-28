#set($page_title = "Oooops")
#parse ("header.tpl")
  <h1>Thoth is very sorry about this<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  Something happened that Thoth did not expect (and was not prepared for).<br/>
  If this happens again, do let his creators know.
  
  <h3>Scary technical stuff that might help a developer below</h3>
  <pre>
    ${stack}
  </pre>
#parse ("footer.tpl")
  