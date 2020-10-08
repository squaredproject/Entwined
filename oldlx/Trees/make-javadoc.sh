#!/bin/sh
ln -s Trees.pde ._Trees.java
javadoc -classpath code/P3LX.jar -d javadoc ._Trees.java
rm ._Trees.java

