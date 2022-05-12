# How to find swagger paths

To find **swagger** specifications go to:

*http://ip:port/swagger-ui.html*

- **ip** => it is the ip on which the microservice is running. If you run locally it is localhost
- **ms_port** => check the microservice port on its resource file


**To start swagger plugin** spring application must be up and running

## What other microservices need to use this gateway?

- Plugin swagger in pom in order to generate openapi.json
- Openapi documentation throughout the microservice  
- Json must have the name of the microservice <ms_name>.json
- You need to import this json into the resource folder of the gateway
- Run maven clean install
- Run the gateway
- To specify role in controller methods use the annotation @Operation
