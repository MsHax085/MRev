@echo off
@title Revision
COLOR B
set CLASSPATH=.;dist\MRev.jar;dist\lib\commons-io-2.4.jar
java -Xmx100m mrev.InputHandler
PAUSE