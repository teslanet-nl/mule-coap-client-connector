# Mule CoAP connector - CoapClient
![Mule-Coap logo](icons/coap-client-logo.svg)

Mule CoAP connector is an Anypoint Connector implementation of the [RFC7252 - Constrained Application Protocol](http://tools.ietf.org/html/rfc7252). 
With it Mule applications become CoAP capable and can communicate with other CoAP capable devices and services, realising Internet of Things solutions (IoT). 

The connector uses Californium, a Java CoAP implementation. More information about Californium and CoAP can be found at:

* [http://www.eclipse.org/californium/](http://www.eclipse.org/californium/)
* [http://coap.technology/](http://coap.technology/).

This component - the CoapClient Connector - is one of three parts of the Mule CoAP package.  
The other two being the CoapServer Connector and the Mule CoAP Commons component . 

The CoapClient Connector adds CoAP client capability to [Mule enterprise service bus](https://www.mulesoft.com/).
With it Mule applications can access iot-services using the CoAP protocol. 

# Mule supported versions
* Mule 3.8.x
* Mule 3.9.x

# CoAP supported versions
IETF rfc7252

#Dependencies
* Californium 1.0.6
* Mule Coap Commons 1.0.0

# Installation 
For beta connectors you can download the source code and build it with devkit to find it available on your local repository. Then you can add it to Studio


#Usage
For information about usage our documentation at https://github.com/rogierc/mule-coap-server

# Using in Maven Projects

Mule CoAP artefact releases will be published to [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cmule-coap-server).
The CloapClient connector can be used in your project by adding following dependency
to your `pom.xml` (without the dots):

```xml
  <dependencies>
    ...
    <dependency>
            <groupId>nl.teslanet.mule.transport.coap</groupId>
            <artifactId>mule-coap-client-connector</artifactId>
            <version>1.0.0</version>
    </dependency>
    ...
  </dependencies>
  ...
```

# Reporting Issues

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link https://github.com/rogierc/mule-coap-client-connector/issues.

# Contact

A bug, an idea, an issue? Create an issue on https://github.com/rogierc/mule-coap-client-connector/issues

# Contributing

Use issues or pull-requests on your fork.
