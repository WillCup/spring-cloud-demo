package com.will;

import org.apache.coyote.http2.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;

@EnableEurekaClient
@SpringBootApplication
public class ReservationServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Component
class DummyData implements CommandLineRunner {

	@Override
	public void run(String... strings) throws Exception {
		java.util.stream.Stream.of("will", "wangjin","chenxin", "tet")
				.forEach(name -> reservationRepository.save(new Reservation(name)));

		reservationRepository.findAll().forEach(System.out::println);
		reservationRepository.findByName("wangjin").forEach(System.out::println);
	}


	@Autowired
	private ReservationRepository reservationRepository;
}

@RefreshScope
@RestController
class MessageRestController {
	@Value("${message}")
	private String message;

	@RequestMapping("/message")
	String msg() {
		return this.message;
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {
	@RestResource(path = "byname")
	Collection<Reservation> findByName(@Param("name") String name);
}

@Entity
class Reservation {
	@Id
	@GeneratedValue
	private long id;
	private String name;

	public Reservation(String name) {
		this.name = name;
	}

	public Reservation() {
	}

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