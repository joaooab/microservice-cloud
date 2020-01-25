# microservice-cloud
Implementação referente ao curso da alura: https://cursos.alura.com.br/course/microservices-spring-cloud-service-registry-config-server

## Overview

Comunicação entre dois serviços Loja e Fornecedor. Ambos são registrados no Eureka-server, que passa a conhecer seus respectivos IP's e portas. Suas configurações são centralizadas no projeto Config-server.

## Service Registry com Eureka

Os serviços se registram no Eureka-server com a configuração definida no arquivo application.yml.

    eureka:
      client:
        register-with-eureka: true
        fetch-registry: true
        service-url:
          defaultZone: http://localhost:8761/eureka

Obs: a partir do registro o Eureka tem as informações de IP's e portas em que as aplicações estão rodando. A requisição http://localhost:8761/eureka/apps mostra as aplicações que foram registradas no Eureka-server

## Spring Config Server

Tem como função prover as configurações dos projetos, como exemplo a conexão com banco de dados na aplicação Fornecedor, que são obtidas através do repositório https://github.com/joaooab/microservice-cloud-repo/tree/master/fornecedor. O repositório é definido no application.yml do projeto Config-server

    spring:
      cloud:
        config:
          server:
            git:
              uri: https://github.com/joaooab/microservice-cloud-repo/
              search-paths: fornecedor

Obs: search-paths é o path default. Importante lembrar que essas configurações devem ser realizadas antes de iniciar a aplicação para que o Hibernate faça as devidas conexões na inicialização da aplicação, por isso as configurações de integração com o Config-server foram definidas no bootstrap.yml

    spring:
      application:
        name: 'fornecedor'
      profiles:
        active: default
      cloud:
        config:
          uri: http://localhost:8888
          
Obs: a requisição http://localhost:8888/fornecedor/default mostra as configurações definidas no arquvio. Um arquivo sem extensão é considerado como default (ex: fornecedor.yml), podemos adicionar a extensão (ex: fornecedor-dev.yml e fornecedor-prod.yml) e assim, obter as configurações informando o path http://localhost:8888/fornecedor/dev

## Load Balancer e Spring Feing

https://cloud.spring.io/spring-cloud-openfeign/reference/html/

O Spring Feign é um web service client. Muito semelhante ao Retrofit2 do Android. 

    @FeignClient("fornecedor")
    public interface FornecedorClient {

     @RequestMapping("/info/{estado}")
     InfoFornecedorDTO getInfoPorEstado(@PathVariable String estado);

     @RequestMapping(method = RequestMethod.POST, value = "/pedido")
     InfoPedidoDTO realizaPedido(List<ItemDaCompraDTO> itens);
    }
    
Obs: com o registro no Eureka o path "fornecedor" em tempo de execução é alterado pelo IP e porta em que a aplicação Fornecedor estiver rodando. Isso é possível por conta do Ribbon (load balancing client).
    
    @SpringBootApplication
    @EnableFeignClients
    public class LojaApplication {

     @Bean
     @LoadBalanced
     public RestTemplate getRestTemplate() {
      return new RestTemplate();
     }

     public static void main(String[] args) {
      SpringApplication.run(LojaApplication.class, args);
     }
    }

## Distributed Tracing e Spring Sleuth 

A fim de unificar e rastrear os logs das aplicações em micro serviços. Foi utilizado o https://papertrailapp.com/ para monitoramento dos logs, e o Spring Sleuth para separação de logs entre requisições

     Jan 25 12:14:23 joao 2020-01-25 12:14:23.422  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] b.c.a.m.loja.service.CompraService       : buscando informações de fornecedor DF 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.571  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.netflix.config.ChainedDynamicProperty  : Flipping property: fornecedor.ribbon.ActiveConnectionsLimit to use NEXT property: niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit = 2147483647 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.608  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.n.u.concurrent.ShutdownEnabledTimer    : Shutdown hook installed for: NFLoadBalancer-PingTimer-fornecedor 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.608  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.netflix.loadbalancer.BaseLoadBalancer  : Client: fornecedor instantiated a LoadBalancer: DynamicServerListLoadBalancer:{NFLoadBalancer:name=fornecedor,current list of Servers=[],Load balancer stats=Zone stats: {},Server stats: []}ServerList:null 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.614  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.n.l.DynamicServerListLoadBalancer      : Using serverListUpdater PollingServerListUpdater 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.636  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.netflix.config.ChainedDynamicProperty  : Flipping property: fornecedor.ribbon.ActiveConnectionsLimit to use NEXT property: niws.loadbalancer.availabilityFilteringRule.activeConnectionsLimit = 2147483647 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.638  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] c.n.l.DynamicServerListLoadBalancer      : DynamicServerListLoadBalancer for client fornecedor initialized: DynamicServerListLoadBalancer:{NFLoadBalancer:name=fornecedor,current list of Servers=[192.168.0.38:8081],Load balancer stats=Zone stats: {defaultzone=[Zone:defaultzone;	Instance count:1;	Active connections count: 0;	Circuit breaker tripped count: 0;	Active connections per server: 0.0;] },Server stats: [[Server:192.168.0.38:8081;	Zone:defaultZone;	Total Requests:0;	Successive connection failure:0;	Total blackout seconds:0;	Last connection made:Wed Dec 31 21:00:00 BRT 1969;	First connection made: Wed Dec 31 21:00:00 BRT 1969;	Active Connections:0;	total failure count in last (1000) msecs:0;	average resp time:0.0;	90 percentile resp time:0.0;	95 percentile resp time:0.0;	min resp time:0.0;	max resp time:0.0;	stddev resp time:0.0] ]}ServerList:org.springframework.cloud.netflix.ribbon.eureka.DomainExtractingServerList@4e286e1e 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.817  INFO [fornecedor,5948d34f3d40017c,6104b32e58d074a0,false] 16460 --- [nio-8081-exec-1] b.c.a.m.f.controller.InfoController      : recebido informações do fornecdor DF 
     Jan 25 12:14:23 joao 2020-01-25 12:14:23.840  INFO [fornecedor,5948d34f3d40017c,6104b32e58d074a0,false] 16460 --- [nio-8081-exec-1] o.h.h.i.QueryTranslatorFactoryInitiator  : HHH000397: Using ASTQueryTranslatorFactory 
     Jan 25 12:14:24 joao 2020-01-25 12:14:23.980  INFO [-,5948d34f3d40017c,5948d34f3d40017c,false] 16551 --- [nio-8080-exec-2] b.c.a.m.loja.service.CompraService       : realizando um pedido 
     Jan 25 12:14:24 joao 2020-01-25 12:14:24.006  INFO [fornecedor,5948d34f3d40017c,f2267165d4f3dca8,false] 16460 --- [nio-8081-exec-2] b.c.a.m.f.controller.PedidoController    : pedido recebido 

