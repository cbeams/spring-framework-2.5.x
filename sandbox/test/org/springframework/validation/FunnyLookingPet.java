package org.springframework.validation;


/**
 * @author  keith
 */
public class FunnyLookingPet extends Pet {
    private String hatColor;
    
    public FunnyLookingPet() {
        super();
    }

    /**
     * @@org.springframework.rcp.validator.rules.Required()
     */
    public String getNickName() {
        return null;
    }
    
    /**
     * @@org.springframework.rcp.validator.rules.Required()
     */
    public String getHatColor() {
        return hatColor;
    }

    public void setHatColor(String color) {
        this.hatColor = color;
    }
}
