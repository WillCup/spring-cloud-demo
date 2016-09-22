package com.will;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableZuulProxy		// 启用zuul来做最前端的网关工作
@EnableDiscoveryClient	// 启动后会向eureka server注册当前service
@SpringBootApplication
public class ReservationClientApplication {

	/**
	 * 如果没有@LoadBalanced的话,restTemplate的调用会报错
	 * @return
     */
	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {

		SpringApplication.run(ReservationClientApplication.class, args);
	}
}


@RestController
@RequestMapping("/reservations")
@EnableCircuitBreaker
class ReservationApiGatewayRestController {

	@Autowired
	private RestTemplate restTemplate;

	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> getNames() {

		ParameterizedTypeReference<Resources<Reservation>> ptr =
				new ParameterizedTypeReference<Resources<Reservation>>() {};

		ResponseEntity<Resources<Reservation>> exchange = this.restTemplate.exchange(
				"http://reservation-service/reservations",
				HttpMethod.GET,
				null,
				ptr
		);
		System.out.println(exchange
				.getBody().getContent());
		return exchange
				.getBody()
				.getContent()
				.stream()
				.map(Reservation::getName)
				.collect(Collectors.toList());
	}

	@HystrixCommand(fallbackMethod="nameFallBack")
	@RequestMapping(method = RequestMethod.GET, value = "/name")
	public String getName() {
		return this.restTemplate.getForObject("http://reservation-service/reservations/search/byname?name=will", String.class);
	}

	/**
	 * 断熔后执行的fallback方法
	 * @return
     */
	private String nameFallBack() {
		return "will's fallback";
	}

}

class Reservation {
	private Long id;
	private String name;


	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}