package org.example;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String id;
    private String name;
    private String email;
    private String gender;
    private String status;

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("id:" + id);

        return sb.toString();
    }
}
