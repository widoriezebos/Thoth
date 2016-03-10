# User Management
Thoth comes equipped with built-in user management. Users can be member of (one or more) groups and groups are granted permissions. Permissions determine what functionality is available to a user. By default there is a user ‘administrator’ with password ‘Welcome2Thoth!’ who is a member of group ‘administrators’. Note that there is a special case for the ‘administrator’ user: regardless the group administrator is in; all permissions are granted. For any other user the granted permissions depend on the group(s) that the user is a member of.

## Default groups
By default there will be three groups (corresponding permissions in brackets): 
**thoth\_administrators**(ACCESS, READ\_BOOKS, READ\_FRAGMENTS, READ\_RESOURCE, BROWSE, DIFF, META, PULL, REINDEX, REVISION, SEARCH, VALIDATE, MANAGE\_USERS, MANAGE\_CONTEXTS)
**thoth\_writers**(ACCESS, READ\_BOOKS, READ\_FRAGMENTS, READ\_RESOURCE, BROWSE, DIFF, META, REVISION, SEARCH, VALIDATE)
**thoth\_readers**(ACCESS, READ\_BOOKS, READ\_RESOURCE, SEARCH)
**thoth\_anonymous**()

## Permissions
BASIC\_ACCESS
: Access to the Index and ContextIndex page. Without this Permission users first need to log in before they can see anything (not even the available contexts). The only exception is that when there are specific access rules that grant access to a certain path to everybody (i.e. /some/path RequireNone)

READ\_BOOKS
: Access the contents of Books. What a book actually is depends on the configuration, by default it would be any document with the ‘.book’ extension.

READ\_FRAGMENTS
: Access the contents of fragments (that make up the books)

READ\_RESOURCE
: Access anything that is neither book nor fragment. This is basically anything in the repository that is not a piece of text (i.e. images, property files, CSS etc)

BROWSE
: Browse the entire contents of a context (clicking through folders, clicking on files to access them)

DIFF
: Compare revisions of files

META
: Access meta information (revisions, structure, usage) of a file

PULL
: Manually Pull the contents of a (remote) repository.

REINDEX
: Force a reindex of contexts to occur (manually)

REVISION
: Access the revision history of a file

SEARCH
: Search the entire context using the search engine

VALIDATE
: Access the validation information page

MANAGE\_USERS
: Being able to administer (create, update, delete) groups, users, memberships and permissions

MANAGE\_CONTEXTS
: Being able to administer (create, update, delete) repositories and contexts

## Access control
Apart from the permissions that drive access to certain content (READ\_BOOKS, READ\_FRAGMENTS) you can also implement a finer grained access control for the content itself. If you want that you will have to create a file called `access.rules` in the root of your library. In this file you can then place ‘require group’ statements for path patterns. You can restrict access to files to certain groups based on matching the path of the files. The syntax of a rule in `access.rules` is

	<path spec> RequireNone
	<path spec> Require <groups>
	<path spec> RequireAll <groups>
	<path spec> RequireAny <groups>

where \<path spec\> is a path to a (set of) file(s) using ‘\*’ as a wild card.
and \<groups\> is a comma separated list of group names that a user has to be member of (has to be member of all of them) to be able to access the file matching the \<path spec\>. Require/RequireAll specifies that the users needs to be member of *all* groups, whereas RequireAny relaxes this to match just one (*any*) of the groups specified. With RequireNone you can grant *public access* to a certain path. You might want to use this if you use a Skin that is defined in a Context as the Main index skin. (Check configuration property skin.mainindexcontext)

Rules are matched from top to bottom; if no rule matches access will be denied.

Example of `access.rules`:

	/Library/Datamodel/* require modelers
	* requireAny thoth_readers, thoth_writers

In the example above part anything in the (sub folders) of /Library/Datamodel will be restricted to users that are member of the ‘modelers’ group.
Everything else requires users to be member of either the group ‘thoth\_readers’ or ‘thoth\_writers’. **Note**: without the last line nobody would be able to access anything except the modelers who could access books and fragments under /Library/Datamodel/

The Context Index page make use of these access rules as well to determine whether a user has access to certain books. The main index page determines whether a user has access to a context by checking whether the ‘/‘ path is accessible to the user (by applying the rules)