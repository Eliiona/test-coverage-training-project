package lv.bootcamp.shelter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Adopter {
    private Long id;
    private String name;
    private String email;
    private int age;
    private int currentPetCount;
    private int previousAdoptions;
    private boolean largeProperty;
    private boolean exoticPermit;
}
