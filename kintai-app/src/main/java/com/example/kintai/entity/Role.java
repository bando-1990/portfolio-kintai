package com.example.kintai.entity;

import jakarta.persistence.*;
import lombok.*;

/** ロールマスタ */
@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", unique = true, nullable = false, length = 32)
    private String code;
}
