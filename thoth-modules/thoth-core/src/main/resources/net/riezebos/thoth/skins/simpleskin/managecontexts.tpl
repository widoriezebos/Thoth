<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>Context Management</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css"/>
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/tabs.css"/>
  </head>
  <body>
    <h1>User Management<img class="logo" src="${skinbase}/Webresources/logo.png"/></h1>
    <a href="${contexturl}">Back to Index</a>
    
    #if($message)
      <pre>$message</pre>
    #end
    
    #set($tab = $selectedtab)
    #if(!$tab)#set($tab = "tab1")#end
    
    <ul class="tabs">
      <li>
        #################### TAB 1 REPOSITORIES ####################
        <input type="radio" name="tabs" id="tab1" #if($tab=="tab1")checked="true"#end/>
        <label for="tab1">Repositories</label>
        <div id="tab-content1" class="tab-content">
          #foreach($repository in $repositories)
            <details>
              <summary>
                ${repository.name}
                #if($repository.immutable)(Immutable)#end
              </summary>
              <div class="tabdetails">
              #if($repository.immutable)
                Cannot change
              #else  
                <form action="./?cmd=managecontexts&operation=updaterepository&name=${repository.name}&selectedtab=tab1" method="post">                                 
                  <div>Name: <input type="text" name="newname" value="${repository.name}" size="30"/></div>         
                  <div>Type: <select name="type">
                    #foreach($type in ${thothutil.getAllRepositoryTypes()})
                      <option value="$type"#if($type == $repository.type)selected="true"#end>$type</option>
                    #end  
                  </select></div>
                  <div>Location: <input type="text" name="location" #if(${repository.location})value="${repository.location}"#end size="60"/></div>                                                           
                  <div>Username: <input type="text" name="username" #if(${repository.username})value="${repository.username}"#end size="15"/></div>                                                           
                  <div>Password: <input type="password" name="password" size="15"/></div>                                                           
                  <input type="submit" value="Update"/>
                </form>
                <a class="tabaction" href="./?cmd=managecontexts&operation=deleterepository&name=${repository.name}&selectedtab=tab1">Delete ${repository.name}</a>
              #end
              </div>
            </details>  
          #end  
        </div>
      </li>
      
      <li>
        #################### TAB 2 CONTEXTS ####################
        <input type="radio" name="tabs" id="tab2" #if($tab=="tab2")checked="true"#end/>
        <label for="tab2">Contexts</label>
        <div id="tab-content2" class="tab-content">
          #foreach($context in $contexts)
            <details>
              <summary>
                ${context.name}
                #if($context.immutable)(Immutable)#end
              </summary>
              <div class="tabdetails">
                #if($context.immutable)
                  Cannot change
                #else
                  <form action="./?cmd=managecontexts&operation=updatecontext&name=${context.name}&selectedtab=tab2" method="post">                                 
                    <div>Name: <input type="text" name="newname" value="${context.name}" size="30"/></div>      
                    <div>Repository: <select name="repositoryname">
                      #foreach($repository in $repositories)
                        #if(!$repository.immutable)
                          <option value="$repository.name"#if($repository.equals($context.repositoryDefinition))selected="true"#end>$repository.name</option>
                        #end  
                      #end  
                    </select></div>
                    <div>Branch: <input type="text" name="branch" #if(${context.branch})value="${context.branch}"#end size="30"/></div>                                                           
                    <div>Library root: <input type="text" name="libraryroot" #if(${context.libraryRoot})value="${context.libraryRoot}"#end size="50"/></div>                                                           
                    <div>Refresh interval: <input type="text" name="refreshinterval" #if(${context.refreshInterval}!=0)value="${context.refreshInterval}"#end size="10"/> (seconds)</div>                                                           
                    <input type="submit" value="Update"/>
                  </form>
                  <a class="tabaction" href="./?cmd=managecontexts&operation=deletecontext&name=${context.name}&selectedtab=tab2">Delete ${context.name}</a>
                #end
              </div>
            </details>    
          #end  
        </div>
      </li>
    
      <li>
        #################### TAB 3 CREATE REPOSITORY ####################
        <input type="radio" name="tabs" id="tab3" #if($tab=="tab3")checked="true"#end/>
        <label for="tab3">Create repository</label>
        <div id="tab-content3" class="tab-content">
          <form action="./?cmd=managecontexts&operation=createrepository&selectedtab=tab3" method="post">
            <div>Name: <input type="text" name="name" size="30"/></div>         
            <div>Type: <select name="type">
              #foreach($type in ${thothutil.getAllRepositoryTypes()})
                <option value="$type">$type</option>
              #end  
            </select></div>
            <div>Location: <input type="text" name="location" size="60"/></div>                                                           
            <div>Username: <input type="text" name="username" size="15"/></div>                                                           
            <div>Password: <input type="password" name="password" size="15"/></div>                                                           
            <input type="submit" value="Create"/>
          </form>
      
        </div>
      </li>
      
      <li>
        #################### TAB 4 CREATE CONTEXT ####################
        <input type="radio" name="tabs" id="tab4" #if($tab=="tab4")checked="true"#end/>
        <label for="tab4">Create context</label>
        <div id="tab-content4" class="tab-content">
          <form action="./?cmd=managecontexts&operation=createcontext&selectedtab=tab4" method="post">
            <div>Name: <input type="text" name="name" size="30"/></div>      
            <div>Repository: <select name="repositoryname">
              #foreach($repository in $repositories)
                #if(!$repository.immutable)
                  <option value="$repository.name">$repository.name</option>
                #end  
              #end  
            </select></div>
            <div>Branch: <input type="text" name="branch" size="30"/></div>                                                           
            <div>Library root: <input type="text" name="libraryroot" size="50"/></div>                                                           
            <div>Refresh interval: <input type="text" name="refreshinterval" size="10"/></div>                                                           
            <input type="submit" value="Create"/>
          </form>
        </div>
      </li>
    </ul>
    
    <br style="clear: both;" />
    <br/>
  </body>
</html>