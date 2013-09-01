Translating SpotMachine
=======================

The preferred way to do translations is through git, the revision control system
used by SpotMachine. This involves downloading the complete set of source files
via the git program, updating your translation file (one per language) to match
the most current strings in the program, doing your actual translation, and then
uploading your updated language file through git. This page will explain how to
do so under Linux. 

If you're on Windows or Mac, the procedure is probably quite different. In that
case -- or if you're not very fond of using the command line -- it's also possible
to download the translation file from the website, do your translations, and
submit your work through the issue tracker. This will also be explained below.

Setting Up git
--------------

First thing you need to do is create a (free) [GitHub account](http://github.com/),
if you don't already have one. Then navigate to the
[SpotMachine GitHub page](https://github.com/pryds/spotmachine) and click the
"Fork" button in the top, right corner. This will create a complete copy (a fork)
of the SpotMachine git repository on your GitHub account. This will enable you to
make changes to your fork, and then apply to have those changes merged into the
SpotMachine repository. This is called a pull request.

In theory you can work on your fork directly from the GitHub website, but you
will have much better control if you pull your fork to your local computer, make
changes there, and push your changes to your fork. Here's how to do that:

- Make sure your have git and gettext installed on your computer. On
  Ubuntu/Debian that would be:

  ```
  sudo apt-get install git gettext
  ```

- Then, clone (download) your fork to your computer this way. First, cd to where
  you'd like to have your spotmachine directory. Then:

  ```
  git clone https://github.com/YOURGITHUBUSERNAME/spotmachine.git
  ```

- Now, cd into the spotmachine directory, and add a reference 'upstream' to the
  main SpotMachine repository:

  ```
  cd spotmachine
  git remote add upstream https://github.com/pryds/spotmachine.git
  ```

The following times
-------------------

- The following times, you can start from here. Make sure you have all the most
  recent files from 'upstream', and merge those changes into your working
  directory. Then let xgettext trawl through the source files and put all
  strings in the keys.pot file, and cd into the po directory which keeps all
  the translations:

  ```
  git fetch upstream
  git merge upstream/master
  xgettext -ktrc:1c,2 -ktnrc:1c,2,3 -ktr -kmarktr -ktrn:1,2 --from-code UTF-8 -o src/po/keys.pot $(find . -name "*.java")
  cd src/po
  ```

- Either create or update your language file:
  - If your language is not there, and you're starting a new translation, simply
    copy the keys.pot file to a new language file xx.po, where xx is the
    two-letter language code as specified by the
    [ISO 639-1](http://www.loc.gov/standards/iso639-2/php/code_list.php) naming
    convention. Also, add the language code to a new line in the languages file:

    ```
    cp keys.pot xx.po
    echo "xx" >> languages
    ```

  - If you're doing an update of your language, use the following command to
    merge the new strings in keys.pot into your language file, where xx is your
    language code:

    ```
    msgmerge -U xx.po keys.pot
    ```

- Now, you're ready to do your translation work. In principle, you can edit your
  language file with any text editor, but it is highly recommended to use an
  editor with .po capabilities, such as [lokalize](http://userbase.kde.org/Lokalize)
  or [poedit](http://sourceforge.net/projects/poedit/).

- In order to upload your changes to your fork, run the following. First, tell
  git which file(s) to add to the commit (in this case only xx.po, but you might
  also add the languages file if you're submitting a new language), then commit
  the changes into your repository on your local machine, and last push the
  commit to your fork on GitHub:

  ```
  git add xx.po
  git commit -m 'Updated Danish translation'   # Remember to change the comment!
  git push origin master
  ```

- Go to the GitHub page for your fork of SpotMachine, choose your recent commit
  and select to make a pull request. This will make your updates available to
  the SpotMachine project for inclusion.

If you want to know more about the details of git, there's a great online book
about it, called [Pro Git](http://git-scm.com/book), available for free.

Windows or Mac?
---------------

If you're not too excited to use the command line, or you're on Windows or Mac,
you can instead make a request for a language file with the updated strings
through the [issue tracker](http://github.com/pryds/spotmachine/issues),
translate the file using e.g. lokalize or poedit, and then submit your updated
file back through the same issue on the issue tracker.

