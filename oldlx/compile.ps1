
#cd "$( dirname "$0" )"
md Trees\build-tmp -ea 0
javac -cp "Trees\code\*" -d Trees\build-tmp Trees\*.java
