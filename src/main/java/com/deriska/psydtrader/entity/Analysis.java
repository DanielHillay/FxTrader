package com.deriska.psydtrader.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@RequiredArgsConstructor
@Data
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String category;
    private String analysisName;

    @ManyToMany(mappedBy = "listOfAnalysis")
    private List<Trades> tradesList;

}
