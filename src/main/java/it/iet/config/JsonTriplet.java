package it.iet.config;

import lombok.Data;

import java.util.List;

@Data
public class JsonTriplet {
    private String path;
    private String httpMethod;
    private List<String> roleList;
}
