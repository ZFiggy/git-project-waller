Makegitrepository(): guess what it does based on the name. 
 - makes git, objects, index, and HEAD and then prints if they were made successfully or if the directory already exists

HashFile(): (reused method we made together as a class)
 - takes path as input
 - Reads the file with Readallbytes()
 - turns bytes into hash in byte form with MessageDigest.digest()
 - turns these bytes into hexadecimal and returns