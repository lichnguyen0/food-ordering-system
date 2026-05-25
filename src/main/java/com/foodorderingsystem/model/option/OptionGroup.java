package com.foodorderingsystem.model.option;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foodorderingsystem.model.food.Food;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class OptionGroup { // nhóm chứa các tuỳ chọn ví dụ, size: M, L,  topping
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(nullable = false)
    private String groupName;

    private boolean required;
    private boolean multiple;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foodId")
    @JsonIgnore
    private Food food;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItems = new ArrayList<>();
}
