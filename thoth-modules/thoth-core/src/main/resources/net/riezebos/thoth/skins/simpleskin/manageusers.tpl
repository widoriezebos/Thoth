<!DOCTYPE html>
<html lang="en-US">
  <head>
    <meta charset="utf-8">
    <title>${context} Index</title>
    <link rel="icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="shortcut icon" href="${skinbase}/Webresources/favicon.png" type="image/png" />
    <link rel="stylesheet" type="text/css" href="${skinbase}/Webresources/style.css"/>
  </head>
  <body>
    <a href="${contexturl}">Back to Index</a>
    
    #if($message)
      $message
    #end
    
    <h1>User Management</h1>
    
    <h2>Create user</h2> 
    <form action="./?cmd=manageusers&operation=createuser" method="post">
      Identifier: <input type="text" value="" size="15" disabled="true"/>                                       
      First name: <input type="text" name="firstname" value="" size="10"/>         
      Last name: <input type="text" name="lastname"  value="" size="10"/>            
      Password: <input type="password" name="password" value="" size="10"/>                                                           
      <input type="submit" value="Create"/>
    </form>
    
    <h2>Create group</h2> 
    <form action="./?cmd=manageusers&operation=creategroup" method="post">
      Identifier: <input type="text" name="group" value="" size="40"/><input type="submit" value="Create"/>
    </form>

    <h2>Users</h2>

    <table>
      #foreach($identity in $users)
        <tr><th colspan="2"><h3>User ${identity.identifier}</h3></th></tr>
        <tr>
            <td>
                <form action="./?cmd=manageusers&operation=addmember&identity=${identity.identifier}" method="post">                                 
                  Identifier: <input type="text" value="${identity.identifier}" size="15" disabled="true"/>                                       
                  First name: <input type="text" name="firstname" #if(${identity.firstname})value="${identity.firstname}"#end size="10"/>         
                  Last name: <input type="text" name="lastname"  #if(${identity.lastname})value="${identity.lastname}"#end size="10"/>            
                  Password: <input type="password" name="password" value="" size="10"/>                                                           
                  Blocked: <input type="checkbox" name="blocked" #if(${identity.blocked})checked="true"#end size="10"/>                           
                  <input type="submit" value="Update"/>
                </form>
            </td>
            <td></td>
        </tr>
        <tr><th colspan="2">Group memberships of ${identity.identifier}</th></tr>
        #foreach($group in ${identity.memberships})
        <tr><td>$group.identifier</td><td><a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier">Remove</a></td></tr>
        #end
        <tr>
            <td>
                <form action="./?cmd=manageusers&operation=addmember&identity=${identity.identifier}" method="post">
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
        <tr><td colspan="2">&nbsp;</td></tr>
      #end  
    </table>
    
    <h2>Groups</h2>
    
    <table>
      #foreach($group in $groups)
        <tr><th colspan="2"><h3>Group ${group.identifier}</h3></th></tr>
        <tr><th>Permissions of group ${group.identifier}</th><th>Action</th></tr>
        #foreach($permission in ${thothutil.sortPermissions($group.permissions)})
        <tr><td>$permission</td><td><a href="./?cmd=manageusers&operation=revokepermission&group=${group.identifier}&permission=$permission">Revoke</a></td></tr>
        #end
        <tr>
            <td>
                <form action="./?cmd=manageusers&operation=addpermission&group=${group.id}" method="post">
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
        <tr><th colspan="2">Members of group ${group.identifier}</th></tr>
        #foreach($identity in ${group.members})
        <tr><td>$identity.identifier</td><td><a href="./?cmd=manageusers&operation=removemember&group=${group.identifier}&member=$identity.identifier">Remove</a></td></tr>
        #end
        <tr>
            <td>
                <form action="./?cmd=manageusers&operation=addmember&group=${group.identifier}" method="post">
                  <select name="identity">
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
        <tr><td colspan="2">&nbsp;</td></tr>
      #end  
    </table>
    
  </body>
</html>