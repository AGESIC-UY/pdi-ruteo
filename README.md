# Ruteo de la Plataforma de Interoperabilidad

## Introducción
La plataforma de interoperabilidad de AGESIC es una solución que brinda capacidades de integración y middleware entre proveedores y consumidores de servicios. Entre otras, provee la funcionalidad de ruteo de invocaciones a servicios, esto permite ocultar los endpoints a los consumidores y aplicar maniobras sobre los mismos de forma transparente para los consumidores. Todas las invocaciones a servicios a través de la plataforma serán redireccionadas por medio del servicio de ruteo.

## Descripción de la solución
Esta solución plantea el uso de una arquitectura orientada a microservicios. De esta forma, los principales servicios de la misma estarán descompuestos en este tipo de servicios, donde cada uno de estos tendrá una responsabilidad acotada a nivel de negocio.

La arquitectura orientada a microservicios es un método de desarrollo de aplicaciones de software como un conjunto de servicios pequeños, independientes, desplegables, y modulares en el que cada uno se ejecuta en un proceso único y se comunica a través de un mecanismo bien definido, ligero para servir en general a un único objetivo de negocio.

Estos servicios están construidos sobre las capas de negocio y con independencia de despliegue. La forma en la cual los microservicios se comunican entre sí depende de los requisitos de cada aplicación, sin embargo se suele utilizar HTTP/REST con JSON. En la mayoría de los casos, REST (Representational State Transfer) el cual es un método de integración útil debido a su baja complejidad.

Hay un mínimo de gestión centralizada de estos servicios, que pueden estar escritos en lenguajes de programación diferentes y que pueden utilizar diferentes tecnologías de almacenamiento de datos. La arquitectura orientada a microservicios pone cada elemento de funcionalidad en un servicio separado y los escala mediante la distribución de estos servicios a través de servidores o procesos nuevos, según sea necesario replicar.

La solución contara con una serie de servicios básicos, los cuales proveen las funcionalidades iniciales del middleware, requeridas para que los demás microservicios
puedan operar en la plataforma. En el diagrama podemos apreciar:

![Microservicios utilizados en el Ruteo](https://github.com/AGESIC-UY/ruteo-pdi/blob/main/image.png?raw=true)

- **Configuración**: Este servicio brinda el soporte de configuración necesario para que los microservicios de la plataforma puedan obtenerla desde un lugar centralizado. El servicio almacena la configuración en un servidor centralizado (en este caso GIT), permitiendole a los diferentes microservicios registrados la posibilidad de obtener y refrescar sus parámetros de configuración dinámicamente.
- **Registro**: El registro provee un servicio para que los microservicios puedan comunicarse entre sí. Disponibiliza funcionalidades que le permiten a los demás servicios registrar un endpoint que pueda ser invocado por otros servicios y a su vez, otras funcionalidades para consultar el estado de los servicios así como refrescar los mismos.
- **Ruteador**: Implementa la funcionalidad de negocio de ruteo de la PDI. Este servicio expone una interfaz REST y SOAP para resolver el ruteo hacia los servicios registrados en la configuración de ruteo de la plataforma.
- **Timestamp**: Este servicio provee una interfaz SOAP para obtener una marca de tiempo de la plataforma.
- **Backoffice**: Es una aplicación que permite administrar la configuración de los servicios de configuración de la plataforma. Brinda una interfaz web que puede ser usada por usuarios autorizados por la plataforma para ajustar dicha configuración.
- **Dashboard**: Este servicio provee las funcionalidades de agregación de datos, relacionados con los diferentes _circuit breakers_ definidos en la plataforma.
- **Edge** _(No utilizado en PDI)_: Para aplicaciones externas al middleware o servicios que deseen consumir un servicio de la plataforma, el servicio de borde (Edge) permite rutear invocaciones externas en invocaciones internas.

Es importante aclarar que dentro de este proyecto se encuentran unicamente los microservicios _Ruteador_, _Timestamp_ y _Backoffice_, ya que los restantes (junto con otros artefactos) se encuentra en proyectos aparte, dado que son funcionalidades core para otros proyectos. Los proyectos que necesarios para ejecutar el Ruteo los puede encontrar ![aquí](https://github.com/AGESIC-UY/pdi-core).

Para finalizar, a nivel lógico la estructura de los microservicio es la de una aplicación web estándar en capas. En el siguiente diagrama se aprecia dicha estructura.

![Arquitectura de microservicio](https://github.com/AGESIC-UY/ruteo-pdi/blob/main/image2.png?raw=true)

- **API**: Expone las funcionalidades del microservicio para ser consumidas desde el exterior. Protocolos REST/SOAP.
- **Negocio**: Contiene la implementación de la funcionalidad del microservicio.
- **Persistencia**: En caso de que el microservicio necesite operar con datos persistentes, esta capa implementa las funcionalidades de acceso al medio persistente, no necesariamente relacional. Para el caso de esta solución, el ruteador utiliza como medio de persistencia el mismo repositorio Git.
- **Integración**: Si el microservicio necesita acceder a otros microservicios o consumir recursos que externos, esta capa se encarga de resolver el acceso a los mismos.
- **DTO / Utilidades**: Las clases utilitarias usadas por este microservicio, los objetos de transferencia y cualquier biblioteca necesaria para que el mismo pueda operar, se encuentra en esta capa.

## Ejecución
_En construcción_

![alt text](https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif)

## Contacto
Por cualquier duda o consulta, puede comunicarse a arquitectura@agesic.gub.uy

---

## Introduction
AGESIC's interoperability platform is a solution that provides integration and middleware capabilities between service providers and consumers. It provides the functionality of routing of invocations to services, among others, allowing to hide endpoints from consumers and operate transparently with them. All invocations to services through the platform will be redirected through the routing service.

## Solution description


## Execution


## Contact
If you require any further information, please contact arquitectura@agesic.gub.uy





