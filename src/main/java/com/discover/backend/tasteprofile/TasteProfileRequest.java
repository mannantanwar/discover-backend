package com.discover.backend.tasteprofile;

import lombok.Data;

import java.util.List;

@Data
public class TasteProfileRequest {

    private List<String> tags;
}
