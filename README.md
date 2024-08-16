# Microservices Web Application Tool for Teaching API vulnerabilities Exploits and Defences

## Introduction

## Project Description
This project is a front-end and back-end separation project, this is the back-end of the project, The front-end is based on `Springboot` and `Microservice` implementation. The front-end project is another file.

The program has a total of 7 services:
> Challenge service: challenge progress check, challenge status update\
>User services: login, registration, account check, user information update, address add, address update\
>Commodity service: all commodities, commodity details, commodity picture details\
>Shopping cart service: shopping cart add, shopping cart modify, shopping cart display, shopping cart delete.\
>Order service: order generation, order display, order details, order details modification.\
>Websocket service: connect, close, transfer information.\
>Gateway service: request routing

The back-end has adopted the MVC model, and the corresponding interface, control layer, and data persistence layer have been designed according to the data required by the front-end in modules. The back-end has been deployed to AliCloud, the online back-end address is:

## Instruction
Online Visit: The project has been deployed in the AliCloud, Google is the most compatible browser for this project, you are welcome to use Google to access this project, visit the link for:

Local Visit: 
1. To run the project, you need to load the maven dependencies and compile the maven project.
2. Modify the database connection information
3. modify the nacos registry address
4. Start the project services in turn, i.e. open the Application file under Services to run.
5. Run the front-end file
6. open localhost:8080
