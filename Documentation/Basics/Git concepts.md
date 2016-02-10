# Git concepts
It is important to note that Git uses a local version control repository to store all it’s information. The local repository is usually created by ‘Cloning’ the central repository. After cloning the trick is that this local repository can be synchronized with the central repository. This synchronization is done by you using ‘Pull’ and ‘Push’. ‘Pull’ fetches information from the central repository and ‘Push’ writes your local changes to the central repository. Before you can ‘Push’ your changes however there are a few steps you need to do first:

1. You must commit your local changes to your local repository
2. You must then Pull any changes that happened since your last Pull from the central repository
3. If there are conflicting changes (i.e. both you and somebody else changed something on the same line in the same file) you have to resolve them. You do this by editing the file containing the conflict until you are satisfied, and then mark the file ‘resolved’ in Git. All your changes have to be committed before you continue.
4. You can now Push your local changes to the central repository. In rare cases (on a very busy central repository) somebody might have pushed changes while you were resolving conflicts; in that case you have to retry from step 2.

## Setting up your local Git repository
In the examples below we will use SourceTree as a Git client; but there is no pressing reason why you could not use any other Git client. The point is that you create a local Git repository where you will work on your documentation, and that you do this by cloning the central repository.

## Starting SourceTree for the first time
When you start SourceTree for the first time you are asked to add an account. If you are familiar with Github then this is where you enter your Github account details. If you are still wandering what Git actually is all about then you might need some help to either set up a central repository (Github would be good) or skip Git altogether and continue with a file based repository. If you go for File Based then you can read the text below just for reference, but you will be missing out on a lot of goodies.

![](images/01%20Add%20Account.png)

Enter your account type and details and click ‘continue’.

## Cloning the central repository
The next step is about creating a local repository by cloning the central (remote) repository. Determine where you want to place your local repository (I usually create a folder Repositories in my home folder and put all repositories in there)

![](images/02%20Clone.png)

## SourceTree main screen
After cloning the repository you should see the main screen of SourceTree. A few things to note here: 

![](images/03%20Branches.png)

Under branches you see only one branch called ‘master’, and if you open up ‘REMOTES’ you see more branches that are not yet available locally. You can have one branch at a time in your local repository, it is like a snapshot of a particular version of your files. The master branch is not the one you want to work on. Usually you work on develop or a branch based on develop. To switch to develop, just double click on the REMOTES\>Origin develop branch. 

![](images/04%20Checkout%20Branch.png)

After doing that you have switched to the develop branch. Now you should think about the branches that you want to have. 

Typically you would have one repository for a set of documents that logically belong to each other (part of the same product for instance). In this repository you would then have one branch per version of the product. This makes it very easy to promote changes to an older version of the documentation to a new (even after the new branch was created) or back port documentation of a new version to an older version. Git makes it really easy to do by merging one branch into another and selecting the changes you want to have merged. This might sound a bit easier than it is in some cases but you should be able to find enough material on the web to become a Git expert in no time.

In any case, you should create a new branch based on develop and give it the name of a particular version of your (product) documentation. When you start working on the next version of the product documentation, you will create a new branch based on this initial version branch that you are about to create.

![](images/05%20Create%20New%20Branch.png)

## Ready to start writing!
You have now your new branch active in your local repository and you can start creating and editing files (all located inside the repository folder, you remember where you put it right?). After creating files they will show up in SourceTree in the ‘Working copy’ section. Anything in the working copy is a local change that has not yet been committed to the local repository, so it is just a change in a file and nothing else. To add them to the (local) version control you will have to commit the changes. To do this check all the files listed in the Working Copy section, add a comment for your commit (good practice) and click ‘Commit’.

![](images/07%20Commit%20Changes.png)

After you have committed your changes they are stored in the local repository and their state is therefore set to unchanged (as compared to what is stored in the local repository). The changes have not left your local machine however, this will only happen when you Push. Your mantra here is ‘Commit-Pull-Push’. You should always Pull before you Push. Git needs you to do this to make sure that any changes a colleague has pushed to the repository does not conflict with anything you are about to push. 

![](images/08%20Changes%20to%20Push.png)

Sidestep: If you forget to Pull before you commit, you might get an ugly error message (if you read carefully you will find ‘Please, commit your changes or stash them before you can merge’. To recover from this just close the dialog, commit your changes and then Pull again. 

![](images/11%20Pull%20before%20Commit.png)

Now click Pull and then Push to push your changes to the central repository. (Note the read badge with the number ‘1’ which shows you the number of local commits that are ready to get pushed)

![](images/09%20Push.png)

After ‘pull/pushing’ your Working Copy will show empty again, all your changes are now available to any colleagues sharing the central repository with you (by Pulling in your changes).

![](images/10%20No%20changes.png)

Now there might be a complication when a colleague did indeed push a change since the latest Pull you did, and that this change conflicts with a change of you. A conflict means that there is a change on the same line in the same file, and Git cannot automatically merge both changes. This so called ‘Merge Conflict’ will have to be resolved by you then.

![](images/13%20Merge%20Conflicts.png)

If you have conflicts you head over to the Working Copy and find out what files have a conflict. There are several ways to resolve a conflict; the easiest one is overwriting either your own or your colleagues’ changes. That might be easy but is usually not the way forward because you might overwrite other changes in the file as well. You will either resolve the conflicts with your ‘External Merge Tool’ (on OSX that would be FileMerge), handpick the segments in SourceTree or you will simply head to your editor and find the conflicts in the source. After making sure everything is in order save the file and switch back to SourceTree. Now right click on the conflicted file and select ‘Mark Resolved’.

![](images/15%20Mark%20Resolved.png)

Once every conflict has been resolved you can finally commit all your changes. You will notice that SourceTree enters a comment automatically about the merge and the conflicts

![](images/16%20Commit%20Resolved.png)

Now Pull again to make sure nobody Pushed in the mean time and then Push yourself.




