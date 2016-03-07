# User Management
Thoth comes equipped with built-in user management. Users can be member of (one or more) groups and groups are granted permissions. Permissions determine what functionality is available to a user. By default there is a user ‘administrator’ with password ‘Welcome2Thoth!’ who is a member of group ‘administrators’. Note that there is a special case for the ‘administrator’ user: regardless the group administrator is in; all permissions are granted. For any other user the granted permissions depend on the group(s) that the user is a member of.

## Permissions

ACCESS
: Access to the Index and ContextIndex page. Without this Permission users first need to log in because they can see anything (not even the available contexts)

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


