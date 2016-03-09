<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>User management</title>
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
        #################### TAB 1 USERS ####################
        <input type="radio" name="tabs" id="tab1" #if($tab=="tab1")checked="true"#end/>
        <label for="tab1">Users</label>
        <div id="tab-content1" class="tab-content">
          <table>
            #foreach($identity in $users)
              <tr>
                <th><h3>${identity.identifier}</h3></th>
                <th><a href="./?cmd=manageusers&operation=deleteidentity&identifier=${identity.identifier}&selectedtab=tab1">Delete</a></th>
              </tr>
              <tr>
                  <td>
                      <form action="./?cmd=manageusers&operation=updateuser&identifier=${identity.identifier}&selectedtab=tab1" method="post">                                 
                        First name: <input type="text" name="firstname" #if(${identity.firstname})value="${identity.firstname}"#end size="10"/>         
                        Last name: <input type="text" name="lastname"  #if(${identity.lastname})value="${identity.lastname}"#end size="10"/>            
                        Password: <input type="password" name="password" value="" size="10"/>                                                           
                        Blocked: <input type="checkbox" name="blocked" #if(${identity.blockedUntil})checked="true"#end size="10"/>                           
                        <input type="submit" value="Update"/>
                      </form>
                  </td>
                  <td></td>
              </tr>
              <tr><th colspan="2">Group memberships</th></tr>
              #foreach($group in ${identity.memberships})
              <tr><td>$group.identifier</td><td><a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier&selectedtab=tab1">Remove</a></td></tr>
              #end
              <tr>
                  <td>
                      <form action="./?cmd=manageusers&operation=addmember&member=${identity.identifier}&selectedtab=tab1" method="post">
                        <select name="group">
                          #foreach($member in ${groups})
                            #if(!${identity.memberships.contains($member)})
                              <option value="$member.identifier">$member.identifier</option>
                            #end  
                          #end  
                        </select>
                        <input type="submit" value="Add membership"/>
                      </form>
                  </td>
                  <td></td>
              </tr>
            #end  
          </table>
        </div>
      </li>
      
      <li>
        #################### TAB 2 GROUPS ####################
        <input type="radio" name="tabs" id="tab2" #if($tab=="tab2")checked="true"#end/>
        <label for="tab2">Groups</label>
        <div id="tab-content2" class="tab-content">
          <table>
            #foreach($group in $groups)
              <tr>
                <th><h3>${group.identifier}</h3></th>
                <th><a href="./?cmd=manageusers&operation=deleteidentity&identifier=${group.identifier}&selectedtab=tab2">Delete</a></th>
              </tr>
              <tr><th>Permissions</th><th>Action</th></tr>
              #foreach($permission in ${thothutil.sortPermissions($group.permissions)})
              <tr><td>$permission</td><td><a href="./?cmd=manageusers&operation=revokepermission&group=${group.identifier}&permission=$permission&selectedtab=tab2">Revoke</a></td></tr>
              #end
              <tr>
                  <td>
                      <form action="./?cmd=manageusers&operation=grantpermission&group=${group.identifier}&selectedtab=tab2" method="post">
                        <select name="permission">
                          #foreach($perm in ${thothutil.getAllPermissions()})
                            #if(!${group.permissions.contains($perm)})
                              <option value="$perm">$perm</option>
                            #end  
                          #end  
                        </select>
                        <input type="submit" value="Add permission"/>
                      </form>
                  </td>
                  <td></td>
              </tr>
              <tr><th colspan="2">Members</th></tr>
              #foreach($identity in ${group.members})
              <tr><td>$identity.identifier</td><td><a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier&selectedtab=tab2">Remove</a></td></tr>
              #end
              <tr>
                  <td>
                      <form action="./?cmd=manageusers&operation=addmember&group=${group.identifier}&selectedtab=tab2" method="post">
                        <select name="member">
                          #foreach($member in ${identities})
                            #if(!${group.members.contains($member)} && $member!=$group)
                              <option value="$member.identifier">$member.identifier</option>
                            #end  
                          #end  
                        </select>
                        <input type="submit" value="Add member"/>
                      </form>
                  </td>
                  <td></td>
              </tr>
            #end  
          </table>
        </div>
      
      </li>
      <li>
        #################### TAB 3 CREATE USER ####################
        <input type="radio" name="tabs" id="tab3" #if($tab=="tab3")checked="true"#end/>
        <label for="tab3">Create user</label>
        <div id="tab-content3" class="tab-content">
          <form action="./?cmd=manageusers&operation=createuser&selectedtab=tab3" method="post">
            Identifier: <input type="text" name="identifier" value="" size="15" autofocus="true"/>                                       
            First name: <input type="text" name="firstname" value="" size="10"/>         
            Last name: <input type="text" name="lastname"  value="" size="10"/>            
            Password: <input type="password" name="password" value="" size="10"/>                                                           
            <input type="submit" value="Create"/>
          </form>
      
        </div>
      </li>
      
      <li>
        #################### TAB 4 CREATE GROUP ####################
        <input type="radio" name="tabs" id="tab4" #if($tab=="tab4")checked="true"#end/>
        <label for="tab4">Create group</label>
        <div id="tab-content4" class="tab-content">
          <form action="./?cmd=manageusers&operation=creategroup&selectedtab=tab4" method="post">
            Identifier: <input type="text" name="identifier" value="" size="40"/><input type="submit" value="Create"/>
          </form>
        </div>
      </li>
    </ul>
    
    <br style="clear: both;" />
    <br/>
  </body>
</html>