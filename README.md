# 677 Lab 3

The program is done in java in eclipse enviroment. To create the jar file, you need eclipse to export the project to become a jar file. (I already generated one in the src folder)

To run the program clone the entire project, go the "src" folder, make sure "time_logs" file exit and run following command.

The client's third arg is the number of client you want (better <6)

#########
DO NOT set up on localhost!!!
#########

For edlab setup:
On edlab 1
java -jar lab3.jar CatalogServer 0 128.119.243.164
java -jar lab3.jar OrderServer 0 128.119.243.147 128.119.243.168
On edlab 2
java -jar lab3.jar CatalogServer 1 128.119.243.147
java -jar lab3.jar OrderServer 1 128.119.243.164 128.119.243.168
On edlab 3
java -jar lab3.jar FrontendServer 128.119.243.147 128.119.243.147 128.119.243.164 128.119.243.164
On edlab 7
java -jar lab3.jar Client 128.119.243.168 1

elnux1.cs.umass.edu 128.119.243.147 cluster0
elnux2.cs.umass.edu 128.119.243.164 cluster1
elnux3.cs.umass.edu 128.119.243.168 FrontendServer
elnux7.cs.umass.edu 128.119.243.175 client

The source code is in src/src/main/java/lab3
