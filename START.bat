@echo off
@title Revision
COLOR B
set CLASSPATH=.;dist\MRev.jar
java -Xmx100m mrev.InputHandler
PAUSE