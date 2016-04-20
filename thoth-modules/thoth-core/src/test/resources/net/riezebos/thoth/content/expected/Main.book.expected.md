Main Book


[//]: # "Include begin: /main/Main.md"

#The main file of the TestRepos library

Welcome to Main.md


##This is a simple document

that is used to test various aspects of the ContentManagers


[//]: # "Include begin: /main/subs/SubOne.md"

#This is a Sub

And it is number one.

And let's have an error in here:


##and why not another one

Yes? ![](../main/subs/imagenotthere/either.png) nope.

[//]: # "Include end: /main/subs/SubOne.md"

[//]: # "Include end: /main/Main.md"

[//]: # "Include begin: /main/Second.md"

#The second big chapter

With some addition text

[//]: # "Include end: /main/Second.md"

[//]: # "Include begin: /main/Third.md"

#Third topic

And here's the third topic. Although counting in sequential order does not make sense right?
It's all about the *includes*!

[//]: # "Include end: /main/Third.md"

**The following problems occurred during generation of this document:**

    /main/subs/SubOne.md(5): Include not found: yup/thisoneis/problematic.md
    /main/subs/SubOne.md(8): Link invalid: imagenotthere/either.png
