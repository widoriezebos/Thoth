#set($page_title = "Changes to " + ${title})
#parse ("header.tpl")
  <h1>Changes to ${title}<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
  <a href="${contexturl}">Back to Index</a>
  <br/>
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
#parse ("footer.tpl")
    