#set($page_title = "User management")
#set($use_tabs = true)
#parse ("header.tpl")
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
        <div>
          #foreach($identity in $users)
            #set($pointer = "selectedtab=tab1&selectedline=$identity")
            #set($here = "user_${identity}")
            <details #if(${selectedline}=="$identity")open="true"#set($scrollto=$here)#end>
              <summary>
                ${identity.identifier}
              </summary>
              <div class="tabdetails" id="$here">
                <form action="./?cmd=manageusers&operation=updateuser&identifier=${identity.identifier}&${pointer}" method="post" autocomplete="off">
                  First name: <input type="text" name="firstname" #if(${identity.firstname})value="${identity.firstname}"#end size="10"/>         
                  Last name: <input type="text" name="lastname"  #if(${identity.lastname})value="${identity.lastname}"#end size="10"/>            
                  Password: <input type="password" name="password" autocomplete="off" value="" size="10"/>                                                           
                  Blocked: <input type="checkbox" name="blocked" #if(${identity.blockedUntil})checked="true"#end size="10"/>                           
                  <input type="submit" value="Update"/>
                </form>
               
                <h4>Group memberships</h4>
                #foreach($group in ${identity.memberships})
                  <div>
                    $group.identifier
                    <a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier&${pointer}">Remove</a>
                  </div>  
                #end
                <div>
                  <form action="./?cmd=manageusers&operation=addmember&member=${identity.identifier}&${pointer}" method="post">
                    <select name="group">
                      #foreach($member in ${groups})
                        #if(!${identity.memberships.contains($member)})
                          <option value="$member.identifier">$member.identifier</option>
                        #end  
                      #end  
                    </select>
                    <input type="submit" value="Add membership"/>
                  </form>
                </div>
                <a class="tabaction" href="./?cmd=manageusers&operation=deleteidentity&identifier=${identity.identifier}&${pointer}">Delete ${identity.identifier}</a>
              <div>
            </details>
          #end  
        </div>
      </div>
    </li>
    
    <li>
      #################### TAB 2 GROUPS ####################
      <input type="radio" name="tabs" id="tab2" #if($tab=="tab2")checked="true"#end/>
      <label for="tab2">Groups</label>
      <div id="tab-content2" class="tab-content">
        #foreach($group in $groups)
          #set($pointer = "selectedtab=tab2&selectedline=$group")
          #set($here = "group_${group}")
          <details #if(${selectedline}=="$group")open="true"#set($scrollto=$here)#end>
            <summary>
              ${group.identifier}
            </summary>  
            <div class="tabdetails" id="$here">
          <h3>Permissions</h3>
          #foreach($permission in ${thothutil.sortPermissions($group.permissions)})
            <div>$permission <a href="./?cmd=manageusers&operation=revokepermission&group=${group.identifier}&permission=$permission&${pointer}">Revoke</a></div><br/>
          #end
          <form action="./?cmd=manageusers&operation=grantpermission&group=${group.identifier}&${pointer}" method="post">
            <select name="permission">
              #foreach($perm in ${thothutil.getAllPermissions()})
                #if(!${group.permissions.contains($perm)})
                  <option value="$perm">$perm</option>
                #end  
              #end  
            </select>
            <input type="submit" value="Add permission"/>
          </form>

          <h3>Members</h3>
          #foreach($identity in ${group.members})
            <div>$identity.identifier <a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier&${pointer}">Remove</a></div><br/>
          #end
          <form action="./?cmd=manageusers&operation=addmember&group=${group.identifier}&${pointer}" method="post">
            <select name="member">
              #foreach($member in ${identities})
                #if(!${group.members.contains($member)} && $member!=$group)
                  <option value="$member.identifier">$member.identifier</option>
                #end  
              #end  
            </select>
            <input type="submit" value="Add member"/>
          </form>
          <a class="tabaction" href="./?cmd=manageusers&operation=deleteidentity&identifier=${group.identifier}&${pointer}">Delete group ${group.identifier}</a>
          </div>
          </details>
        #end  
      </div>
    
    </li>
    <li>
      #################### TAB 3 CREATE USER ####################
      <input type="radio" name="tabs" id="tab3" #if($tab=="tab3")checked="true"#end/>
      <label for="tab3">Create user</label>
      <div id="tab-content3" class="tab-content">
        <form action="./?cmd=manageusers&operation=createuser&selectedtab=tab3" method="post" autocomplete="off">
          Identifier: <input type="text" name="identifier" value="" size="15" autofocus="true"/>                                       
          First name: <input type="text" name="firstname" value="" size="10"/>         
          Last name: <input type="text" name="lastname"  value="" size="10"/>            
          Password: <input type="password" name="password" autocomplete="off" value="" size="10"/>                                                           
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

  #if($scrollto)
    <script>
      window.onload = function(){
          document.getElementById('$scrollto').scrollIntoView(true);
      };
    </script>
  #end
#parse ("footer.tpl")
    