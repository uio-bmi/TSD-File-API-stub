# TSD-File-API-stub
![](https://github.com/uio-bmi/TSD-File-API-stub/workflows/Java%20CI/badge.svg)
[![CodeFactor](https://www.codefactor.io/repository/github/uio-bmi/tsd-file-api-stub/badge)](https://www.codefactor.io/repository/github/uio-bmi/tsd-file-api-stub)


## Stub for TSD File API 
TSD file API documentation can be found at https://test.api.tsd.usit.no/v1/docs/tsd-api-integration.html#files_and_streaming

## Swagger API
https://localhost:8080/swagger-ui.html....

## configuration 
you need to define 
1. location for file upload by setting property `tsd.file.import` in `application.yml` file  or by setting environment variable `${DURABLE_FILE_IMPORT}` default `/tsd/%s/data/durable/file-import/` you need to put `%s` as a placeholder for project name
1. secret that will be used in creating and decoding JWT token by setting property `tsd.file.secretkey` in `application.yml` file  or by setting environment variable `${SECRET_KEY}`

## signup flow
```
	client                                         API
    ------                                         ---
    client_name, email            -------------->  /auth/basic/signup
                                  <--------------  client_id
                                  
    client_name, email,client_id  -------------->  /auth/basic/signupconfirm
                                  <--------------  confirmation_token 
                                  
    client_id, confirmation_token -------------->  /auth/basic/confirm
                                  <--------------  password
                                                   (details verified by TSD)
    client_id, password           -------------->  /auth/basic/api_key
                                  <--------------  api_key
```