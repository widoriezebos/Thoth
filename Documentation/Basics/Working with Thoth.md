# Working with Thoth
Let’s get familiar with the toolset. An important aspect of working with Thoth is version control, currently based on Git. Version control can be a bit daunting if you are new to the concept, but if you stick to the basics and use [a good Git client](https://www.sourcetreeapp.com) you will quickly get the hang of it. (Note: For very simple setups you can skip the use of Git altogether and use the File System based content manager)

## Git concepts
It is important to note that Git uses a local version control repository to store all it’s information. The local repository is usually created by ‘Cloning’ the central repository. After cloning the trick is that this local repository can be synchronized with the central repository. This synchronization is done by you using ‘Pull’ and ‘Push’. ‘Pull’ fetches information from the central repository and ‘Push’ writes your local changes to the central repository. Before you can ‘Push’ your changes however there are a few steps you need to do first:

1. You must commit your local changes to your local repository
2. You must then Pull any changes that happened since your last Pull from the central repository
3. If there are conflicting changes (i.e. both you and somebody else changed something on the same line in the same file) you have to resolve them. You do this by editing the file containing the conflict until you are satisfied, and then mark the file ‘resolved’ in Git. All your changes have to be committed before you continue.
4. You can now Push your local changes to the central repository. In rare cases (on a very busy central repository) somebody might have pushed changes while you were resolving conflicts; in that case you have to retry from step 2.

## Setting up your local Git repository
In the examples below we will use SourceTree as a Git client; but there is no pressing reason why you could not use any other Git client. The point is that you create a local Git repository where you will work on your documentation, and that you do this by cloning the central repository.

### Cloning the central repository

