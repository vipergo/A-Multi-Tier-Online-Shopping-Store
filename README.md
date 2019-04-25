# 677 Lab 3

The program is done in java in eclipse enviroment. To create the jar file, you need eclipse to export the project to become a jar file. (I already generated one in the root folder)
To run the program clone the entire project.

The client's third arg is the number of client you want.

#########
On local machine, to run the docker container on local machine. Containers can only be run on local machine because the container can not access ip address outside of the local machine since my local ip is not expose to the external internet. So I use my internal ip address set up each container. Please MANUALLY INPUT the correct local ip address (replace the "REPLACE_ME" in the dockerfile), otherwise the ip the program can't connect to each other through localhost's ports. Sorry for the inconvenience! After 2 hours trying to make the ARG to allow arguement for build for eaiser ip input, it still doesn't work...

#########
Docker Build:
	docker build --network="host" -t cat0 -f Dockerfile.catserver0 .
	docker build --network="host" -t order0 -f Dockerfile.orderserver0 .
	docker build --network="host" -t cat1 -f Dockerfile.catserver1 .
	docker build --network="host" -t order1 -f Dockerfile.orderserver1 .
	docker build --network="host" -t frontend -f Dockerfile.frontend .
	docker build --network="host" -t client -f Dockerfile.client .


Docker Run:
	docker run -it -p 3154:3154 cat0
	docker run -it -p 3900:3900 order0
	docker run -it -p 3155:3155 cat1
	docker run -it -p 3901:3901 order1
	docker run -it -p 3800:3800 frontend
	docker run -it client
#########

For edlab setup:
Pleasure make sure the servers are on the correct edlab, otherwise the ip here doesn't match
On edlab 1
java -jar lab3.jar CatalogServer 0 3154 128.119.243.164:3155
java -jar lab3.jar OrderServer 0 3900 128.119.243.147:3154 128.119.243.168:3800
On edlab 2
java -jar lab3.jar CatalogServer 1 3155 128.119.243.147:3154
java -jar lab3.jar OrderServer 1 3901 128.119.243.164:3155 128.119.243.168:3800
On edlab 3
java -jar lab3.jar FrontendServer 3800 128.119.243.147:3154 128.119.243.147:3900 128.119.243.164 128.119.243.164:3901
On edlab 7
java -jar lab3.jar Client 128.119.243.168:3800 1

elnux1.cs.umass.edu 128.119.243.147 cluster0
elnux2.cs.umass.edu 128.119.243.164 cluster1
elnux3.cs.umass.edu 128.119.243.168 FrontendServer
elnux7.cs.umass.edu 128.119.243.175 client

The source code is in src/src/main/java/lab3
