package br.com.naildesigner.agendamento_service_AgendeNail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients; // Se você usa FeignClient

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients // Se você usa Feign Client neste projeto
public class AgendamentoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendamentoServiceApplication.class, args);
    }
}
