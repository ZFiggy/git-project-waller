makeGitRepository(): guess what it does based on the name. 
 - makes git, objects, index, and HEAD and then prints if they were made successfully or if the directory already exists

hashFile(String filePath): (reused method we made together as a class)
 - takes path as input
 - Reads the file with readAllBytes()
 - turns bytes into hash in byte form with MessageDigest.digest()
 - turns these bytes into hexadecimal and returns
 
createBlob(String filePath):
 - takes path as input
 - copies file into objects with same contents and hash as its name

updateIndex(String filePath):
- takes path as input
- updates index file in git folder
- adds file's hash and path in a line of the index file

updateIndexAndBlob(String filePath):
- takes path as input
- combines createBlob() and updateIndex() into one method call

createWorkingList():
- reads the index and adds blob to the front of each entry
- puts the new entries in an ArrayList<String>
- sorts the entries based on their paths alphabetically

createTree(String dirPath):
- takes path as input
- runs updateIndexAndBlob() on every file in the directory except those inside the git folder and .git and .DS_Store
- returns the root tree hash
- functions as a singular method to be called that runs the functionality of all the code

createTree(ArrayList<String> workingList, String dirPath):
- takes the working list and path as input
- writes the tree for one folder based on its direct children
- returns the hash of that tree

createRootTree():
- builds every tree from the root
- returns the hash of the root tree
- functions as a way to build all the trees with a simple method call

buildTree(ArrayList<String> workingList, String dirPath):
- takes the working list and path as input
- creates the trees for all subfolders within a given directory recursively
- creates the deepest entries first
- returns the hash of the tree built

replaceWithTreeLine(ArrayList<String> workingList, String dirPath, String hash):
- takes the working list and path and the tree hash as input
- replaces the entries in the working list that are inside of the tree with just the tree line