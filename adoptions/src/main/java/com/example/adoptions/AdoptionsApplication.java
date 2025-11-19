package com.example.adoptions;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
public class AdoptionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdoptionsApplication.class, args);
    }

}

// look mom no Lombok!!
record Dog(@Id int id, String name, String owner, String description) {
}

interface DogRepository extends ListCrudRepository<@NonNull Dog, @NonNull Integer> {
}

@Controller
@ResponseBody
class AdoptionsController {

    private final DogRepository dogRepository;

    AdoptionsController(DogRepository dogRepository) {
        this.dogRepository = dogRepository;
    }

    @PostMapping("/animals/{animalId}/adoptions")
    void adopt(@PathVariable int animalId, @RequestParam String owner) {
        this.dogRepository.findById(animalId).ifPresent(dog -> {
            var updated = this.dogRepository.save(
                    new Dog(dog.id(), dog.name(), owner, dog.description()));
            IO.println("adopted " + updated);
        });
    }
}
