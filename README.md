# Welcome to lvEditor!
lvEditor is a simple and modern looking Java text editor that focuses on functionality and simplicity.
![lvEditor demo](demo-lvEditor.gif)
## How to compile?
If you want to use lvEditor, you can go to [releases](https://github.com/fragis0/lvEditor/releases) to see the releases, otherwise you can compile using these commands:
**Get source files:**
```
git clone https://github.com/fragis0/lvEditor.git
cd lvEditor
```
**And compile Java code**
```
mkdir out
javac -cp lib/flatlaf-3.2.jar -d out src/lvEditor.java
cd out
jar xf ..\lib\flatlaf-3.2.jar
cd ..
cp res/icon.png out
jar cfm lvEditor.jar MANIFEST.MF -C out .
```
**Done!** You have compiled lvEditor into single .jar that includes all files required.
