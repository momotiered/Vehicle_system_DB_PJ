package com.example.backend_dbpj.dto;

import java.util.List;

public class AssignPersonnelRequestDto {

    private List<Integer> personnelIds;

    public List<Integer> getPersonnelIds() {
        return personnelIds;
    }

    public void setPersonnelIds(List<Integer> personnelIds) {
        this.personnelIds = personnelIds;
    }
} 