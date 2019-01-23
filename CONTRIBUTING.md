CONTRIBUTING
============

### Localizations:

Mastalab works only with [Crowdin](https://crowdin.com/project/mastalab), which offers nice tools for helping in translations.
New translations will be automatically merged in a branch.
If your language is not listed, please ask me to add it. If you prefer to work on an XML file, you should be able [to upload it with Crowdin](https://support.crowdin.com/xml-configuration/).
Crowdin will not pick up changes in develop branch, that's why all translations should be done with this tool.

If you're submiting a merge request and your work adds new strings to the app, make sure they only exist in the default strings.xml file (res/values/strings.xml).
If you add or modify strings of other languages, it will interfere with crowdin's translations.